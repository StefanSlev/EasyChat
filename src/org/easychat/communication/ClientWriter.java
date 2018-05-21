package org.easychat.communication;

import org.easychat.protocol.Message;
import org.easychat.protocol.MessageType;
import org.easychat.server.ChatServer;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by Slevy on 06.05.2018.
 */
final class ClientWriter implements Runnable {

    private final String clientName;
    private final Socket clientSocket;
    private final Mailbox mailbox;
    private boolean isActive = true;
    private final ChatServer chatServer = ChatServer.getInstance();

    ClientWriter(String clientName, Socket clientSocket) {

        this.clientName = clientName;
        this.clientSocket = clientSocket;
        this.mailbox = chatServer.getMailbox(clientName);
    }

    @Override
    public void run() {

        try (final ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream())) {

            while (isActive) {

                final Message message = mailbox.readMessage();

                if (message.getMessageType() == MessageType.DISCONNECT) {
                    isActive = false;
                } else {

                    outputStream.writeObject(message);
                }
            }

        } catch (IOException | InterruptedException ex) {}
    }
}
