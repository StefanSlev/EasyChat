package org.easychat.communication;

import org.easychat.protocol.DisconnectMessage;
import org.easychat.protocol.Message;
import org.easychat.server.ChatServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

/**
 * Created by Slevy on 06.05.2018.
 */
final class ClientListener implements Runnable {

    private final String clientName;
    private final Socket clientSocket;
    private boolean isActive = true;
    private final ChatServer chatServer = ChatServer.getInstance();

    /** disconnect info **/
    private final Message disconnectMessage;

    ClientListener(String clientName, Socket clientSocket) {

        this.clientName = clientName;
        this.clientSocket = clientSocket;
        this.disconnectMessage = new DisconnectMessage(clientName);
    }

    private void stopListener() {

        isActive = false;
        chatServer.sendTo(disconnectMessage);
    }

    @Override
    public void run() {

        try (final ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream())) {

            while (isActive) {

                final Message message = (Message) inputStream.readObject();

                if (message == null) {

                    stopListener();
                    break;
                }

                switch (message.getMessageType()) {

                    case DISCONNECT:
                        stopListener();
                        break;
                    case PUBLIC:
                        chatServer.broadcast(message);
                        break;
                    case PRIVATE:
                        chatServer.sendTo(message);
                        break;
                }
            }

        } catch (IOException | ClassNotFoundException ex) {

            stopListener();
        }
    }
}
