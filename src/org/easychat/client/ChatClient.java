package org.easychat.client;

import org.easychat.chat.Chat;
import org.easychat.chat.PrivateChat;
import org.easychat.chat.PublicChat;
import org.easychat.communication.ServerListener;
import org.easychat.design.*;
import org.easychat.design.Observer;
import org.easychat.protocol.DisconnectMessage;
import org.easychat.protocol.Message;
import org.easychat.server.RemoteServerOperations;

import java.net.Socket;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.DataOutputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class ChatClient implements ClientOperations, Subject {

    /** server info **/
    private static final int SERVER_PORT = 9090;
    private static final int SERVICE_PORT = 9091;
    private static final String hostname = "localhost";
    private static final String serviceName = "serviceInfo";

    private final Socket serverSocket;
    private final ObjectOutputStream outputStream;

    /** client info **/
    private final String clientName;
    private final Chat publicChat;
    private final SortedMap<String, PrivateChat> privateChats = new TreeMap<>(String::compareToIgnoreCase);
    private Chat currentChat = null;
    private boolean isRunning = true;

    /** client synchronization for privateChats **/
    private final ReadWriteLock clientLock = new ReentrantReadWriteLock();
    private final Lock readLock = clientLock.readLock();
    private final Lock writeLock = clientLock.writeLock();

    /** observers **/
    private final List<Observer> observers = new ArrayList<>();
    private final Lock observersLock = new ReentrantLock();

    private volatile boolean usersConnectedFlag = false;
    private List<String> usersConnected = new ArrayList<>();
    private final Lock usersLock = new ReentrantLock();

    private volatile boolean chatModifiedFlag = false;
    private String chatModifiedNickname = "Public";
    private final Lock chatLock = new ReentrantLock();

    public ChatClient(String clientName) throws ClientException {

        /**
         *
         * check at server that the name is available
         */

        boolean nameAvailable = false;

        try {

            final Registry registry = LocateRegistry.getRegistry(hostname, SERVICE_PORT);
            final RemoteServerOperations remoteServer = (RemoteServerOperations) registry.lookup(serviceName);
            nameAvailable = remoteServer.isNicknameAvailable(clientName);

        } catch (Exception ex) {
            throw new ClientException(ClientExceptionType.CLIENT_CONNECT);
        }

        if (!nameAvailable) {
            throw new ClientException(ClientExceptionType.CLIENT_EXISTS);
        }

        this.clientName = clientName;
        publicChat = new PublicChat(clientName);

        currentChat = publicChat;

        try {

            serverSocket = new Socket(hostname, SERVER_PORT);

            final DataOutputStream initStream = new DataOutputStream(serverSocket.getOutputStream());
            initStream.writeUTF(clientName);
            initStream.flush();

            outputStream = new ObjectOutputStream(serverSocket.getOutputStream());
            new Thread(new ServerListener(this, serverSocket)).start();

        } catch (IOException ex) {

            stopConnection();
            throw new ClientException(ClientExceptionType.CLIENT_CONNECT);
        }
    }

    private void stopConnection() {

        isRunning = false;
        try {

            if (outputStream != null)
                outputStream.close();

            if (serverSocket != null)
                serverSocket.close();

        } catch (IOException ex) {}
    }

    public String getName() { return this.clientName;}

    @Override
    public void addPrivateChat(String nickname) {

        writeLock.lock();

        try {

            privateChats.put(nickname, new PrivateChat(clientName, nickname));

        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void removePrivateChat(String nickname) {

        writeLock.lock();

        try {
            privateChats.remove(nickname);
        } finally {
             writeLock.unlock();
        }
    }

    @Override
    public boolean privateChatExists(String nickname) {

        readLock.lock();

        try {

            return privateChats.containsKey(nickname);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public PrivateChat getPrivateChat(String nickname) {

        readLock.lock();

        try {

           return privateChats.get(nickname);

        } finally {
            readLock.unlock();
        }
    }

    @Override
    public String[] getConversation(String nickname) {

        readLock.lock();

        try {

            PrivateChat privateChat = privateChats.get(nickname);

            if (privateChat == null)
                return null;

            return privateChat.getConversation();

        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Chat getPublicChat() {

        return publicChat;
    }

    @Override
    public String[] getPublicConversation() {

        return publicChat.getConversation();
    }

    @Override
    public void openPublicChat() {

        currentChat = publicChat;
    }

    @Override
    public void openPrivateChat(String nickname) throws ClientException {

        /**
         *  check if this nickname is still available
         *  ServerInfo.userExists(nickname)
         */

        boolean nicknameExists = false;

        try {

            final Registry registry = LocateRegistry.getRegistry(hostname, SERVICE_PORT);
            final RemoteServerOperations remoteServer = (RemoteServerOperations) registry.lookup(serviceName);
            nicknameExists = remoteServer.userExists(nickname);

        } catch (Exception ex) {

            if (this.privateChatExists(nickname))
                this.removePrivateChat(nickname);

            throw new ClientException(ClientExceptionType.CLIENT_CONNECT);
        }

        if (!nicknameExists) {

            if (this.privateChatExists(nickname))
                this.removePrivateChat(nickname);

            throw new ClientException(ClientExceptionType.CLIENT_NOT_EXISTS);
        }

        /**
         *
         *  check if i was already talking to him
         */

        if (!this.privateChatExists(nickname))
            this.addPrivateChat(nickname);

        /**
         *  set the talkin'
         */
        currentChat = this.getPrivateChat(nickname);
    }

    @Override
    public void sendMessage(String message) throws ClientException {

        final Message toSend = currentChat.sendMessage(message);

        try {

            outputStream.writeObject(toSend);
            outputStream.flush();

        } catch (IOException ex) {

            stopConnection();
            throw new ClientException(ClientExceptionType.CLIENT_SEND);
        }
    }

    @Override
    public void sendDisconnect() {

        final Message toSend = new DisconnectMessage(clientName);

        try {

            outputStream.writeObject(toSend);
            outputStream.flush();

        } catch (IOException ex) {

        } finally {
            stopConnection();
        }
    }

    /** Users Connected Observation **/
    public void setUsersConnected(List<String> usersConnected) {

        usersLock.lock();

        try {

            this.usersConnected = usersConnected;
            final List<String> toRemove = new ArrayList<>();

            readLock.lock();

            try {

                privateChats.forEach((nickname, chat) -> {

                    if (!usersConnected.contains(nickname)) {
                        toRemove.add(nickname);
                    }
                });

            } finally {
                readLock.unlock();
            }

            toRemove.forEach(this::removePrivateChat);

            this.usersConnectedFlag = true;
            this.notifyObservers();

        } finally {
            usersLock.unlock();
        }
    }

    private List<String> getUsersConnected() {

        usersLock.lock();

        try {

            return this.usersConnected;

        } finally {

            usersLock.unlock();
        }
    }

    /** Chat modified observation **/
    public void setChatModified(String chatModifiedNickname) {

        chatLock.lock();

        try {
            this.chatModifiedNickname = chatModifiedNickname;
            this.chatModifiedFlag = true;
            this.notifyObservers();

        } finally {

            chatLock.unlock();
        }
    }

    public String getChatModified() {

        chatLock.lock();

        try {

            return this.chatModifiedNickname;

        } finally {
            chatLock.unlock();
        }
    }

    /** Observer Pattern **/
    @Override
    public void register(Observer observer) {

        observersLock.lock();

        try {

            if (!observers.contains(observer)) {

                observers.add(observer);

                if (usersConnectedFlag || chatModifiedFlag)
                    this.notifyObservers();
            }

        } finally {
            observersLock.unlock();
        }
    }

    @Override
    public void unregister(Observer observer) {

        observersLock.lock();

        try {

            observers.remove(observer);

        } finally {
            observersLock.unlock();
        }
    }

    @Override
    public void notifyObservers() {

        List<Observer> observersLocal = null;

        observersLock.lock();
        try {

            observersLocal = new ArrayList<>(this.observers);

            boolean usersHandled = false;
            boolean chatHandled = false;

            for (Observer observer : observersLocal) {

                switch (observer.getType()) {

                    case USERS_CONNECTED:
                        if (this.usersConnectedFlag) {
                            observer.update();
                            usersHandled = true;
                        }
                        break;
                    case CHAT_MODIFIED:
                        if (this.chatModifiedFlag) {
                            observer.update();
                            chatHandled = true;
                        }
                        break;
                }
            }

            if (this.usersConnectedFlag && usersHandled)
                this.usersConnectedFlag = false;
            if (this.chatModifiedFlag && chatHandled)
                this.chatModifiedFlag = false;

        } finally {

            observersLock.unlock();
        }
    }

    @Override
    public Object getUpdate(ObserverType type) {

        switch (type) {

            case USERS_CONNECTED:
                return this.getUsersConnected();
            case CHAT_MODIFIED:
                return this.getChatModified();
            default:
                return null;
        }
    }
}
