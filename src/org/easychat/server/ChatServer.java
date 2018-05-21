package org.easychat.server;

/**
 * Created by Slevy on 06.05.2018.
 */

import org.easychat.communication.ClientCommunication;
import org.easychat.communication.Mailbox;
import org.easychat.protocol.Message;
import org.easychat.protocol.MessageType;
import org.easychat.protocol.RetrieveMessage;

import java.io.DataInputStream;
import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

final public class ChatServer implements ServerOperations {

    /** Server Info **/
    private static final int LISTEN_PORT = 9090;
    private static final int SERVICE_PORT = 9091;
    private static final String serviceName = "serviceInfo";
    private static final boolean RUN = true;
    private final ServerInfo serverInfo;

    /** Singleton Pattern **/
    private ChatServer() {

        serverInfo = new ServerInfo(this);
    }

    public static ChatServer INSTANCE = null;

    public static ChatServer getInstance() {

        if (INSTANCE == null)
            INSTANCE = new ChatServer();
        return INSTANCE;
    }

    /** server synchronization **/
    private final ReadWriteLock serverLock = new ReentrantReadWriteLock();
    private final Lock readLock = serverLock.readLock();
    private final Lock writeLock = serverLock.writeLock();

    /** clients **/
    private final SortedMap<String, Mailbox> clients = new TreeMap<>(String::compareToIgnoreCase);

    /** server operations **/
    @Override
    public void addClient(String clientName, Socket clientSocket) {

        writeLock.lock();

        try {

            clients.put(clientName, new Mailbox());
            new Thread(new ClientCommunication(clientName, clientSocket)).start();

        } finally {
            writeLock.unlock();
        }

        this.broadcast(new RetrieveMessage());
    }

    @Override
    public void removeClient(String clientName) {

        writeLock.lock();

        try {
            clients.remove(clientName);
        } finally {
            writeLock.unlock();
        }

        this.broadcast(new RetrieveMessage());
    }

    @Override
    public boolean clientExists(String clientName) {

        readLock.lock();

        try {

            return clients.containsKey(clientName);

        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Mailbox getMailbox(String clientName) {

        readLock.lock();

        try {

            return clients.get(clientName);

        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void broadcast(Message message) {

        readLock.lock();

        try {

            if (message.getMessageType() == MessageType.PUBLIC ||
                    message.getMessageType() == MessageType.RETRIEVE) {

                clients.forEach((clientName, mailbox) -> {

                    try {

                        mailbox.writeMessage(message);

                    } catch (InterruptedException ex) {}
                });
            }

        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void sendTo(Message message) {

        readLock.lock();

        try {

            if (message.getMessageType() == MessageType.PRIVATE ||
                    message.getMessageType() == MessageType.DISCONNECT) {

                String sender = message.getSender();
                String receiver = message.getReceiver();
                Mailbox senderMailbox = clients.get(sender);
                Mailbox receiverMailbox = clients.get(receiver);

                if (senderMailbox != null && receiverMailbox != null) {

                    try {

                        senderMailbox.writeMessage(message);
                        receiverMailbox.writeMessage(message);

                    } catch (InterruptedException ex) {}
                }
            }

        } finally {
            readLock.unlock();
        }
    }

    @Override
    public List<String> getAllConnectedClients() {

        readLock.lock();

        try {

            Set<String> clientNames = clients.keySet();
            return new ArrayList<>(clientNames);

        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void loadServerInfo() {

        try {

            RemoteServerOperations remote = (RemoteServerOperations)
                    UnicastRemoteObject.exportObject(serverInfo, 0);
            Registry registry = LocateRegistry.createRegistry(SERVICE_PORT);
            registry.rebind(serviceName, remote);

        } catch (Exception ex) {

            ex.printStackTrace();
        }
    }

    /**
     *
     *  --- Server Operations ---
     *  removeClient
     *  addClient
     *  getMailbox
     *  broadcast
     *  sendTo
     *
     *  --- Server Remote Operations ---
     *  isNicknameAvailable
     *  getAllNicknames
     *  userExists -- ce se intampla daca trimit mesaj unuia care nu exista
     *
     */

    public static void main(String[] args) throws InterruptedException {

        ChatServer chatServer = ChatServer.getInstance();
        chatServer.loadServerInfo();

        try (final ServerSocket serverSocket = new ServerSocket(LISTEN_PORT)) {

            while (RUN) {

                final Socket clientSocket = serverSocket.accept();
                final DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());

                final String clientName = inputStream.readUTF();

                if (chatServer.clientExists(clientName)) {

                    inputStream.close();
                    clientSocket.close();
                    continue;
                }

                chatServer.addClient(clientName, clientSocket);
            }

        } catch (IOException ex) {

            ex.printStackTrace();
        }
    }
}
