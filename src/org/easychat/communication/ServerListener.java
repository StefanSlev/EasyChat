package org.easychat.communication;

import org.easychat.chat.Chat;
import org.easychat.client.ChatClient;
import org.easychat.protocol.Message;
import org.easychat.server.RemoteServerOperations;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

/**
 * Created by Slevy on 07.05.2018.
 */
public final class ServerListener implements Runnable {

    private static final int SERVICE_PORT = 9091;
    private static final String hostname = "localhost";
    private static final String serviceName = "serviceInfo";

    private final ChatClient chatClient;
    private final Socket serverSocket;
    private boolean isActive = true;

    public ServerListener(ChatClient chatClient, Socket serverSocket) {

        this.chatClient = chatClient;
        this.serverSocket = serverSocket;
    }

    private void stopListener() {

        isActive = false;
    }

    @Override
    public void run() {

        try (final ObjectInputStream inputStream = new ObjectInputStream(serverSocket.getInputStream())) {

            while (isActive) {

                final Message message = (Message) inputStream.readObject();

                if (message == null) {

                    stopListener();
                    break;
                }

                switch (message.getMessageType()) {

                    case PUBLIC:
                        final Chat publicChat = chatClient.getPublicChat();
                        publicChat.receiveMessage(message.getMessage());

                        new Thread(() -> chatClient.setChatModified("Public")).start();
                        break;
                    case PRIVATE:

                        final String nickname;

                        if (chatClient.getName().equals(message.getSender()))
                            nickname = message.getReceiver();
                        else
                            nickname = message.getSender();

                        if (!chatClient.privateChatExists(nickname))
                            chatClient.addPrivateChat(nickname);

                        final Chat privateChat = chatClient.getPrivateChat(nickname);
                        privateChat.receiveMessage(message.getMessage());

                        new Thread(() -> chatClient.setChatModified(nickname)).start();
                        break;
                    case RETRIEVE:
                        final Runnable getUsersConnected = () -> {

                            try {

                                final Registry registry = LocateRegistry.getRegistry(hostname, SERVICE_PORT);
                                final RemoteServerOperations remoteServer = (RemoteServerOperations) registry.lookup(serviceName);

                                List<String> usersConnected = remoteServer.getAllConnectedClients();
                                chatClient.setUsersConnected(usersConnected);

                            } catch (Exception ex) {}
                        };

                        new Thread(getUsersConnected).start();
                        break;
                }
            }

        } catch (IOException | ClassNotFoundException ex) {

            stopListener();
        }
    }
}
