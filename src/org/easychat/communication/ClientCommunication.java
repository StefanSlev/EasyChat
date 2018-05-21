package org.easychat.communication;

import org.easychat.server.ChatServer;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by Slevy on 07.05.2018.
 */
public final class ClientCommunication implements Runnable {

    private final String clientName;
    private final Socket clientSocket;
    private final ChatServer chatServer = ChatServer.getInstance();

    public ClientCommunication(String clientName, Socket clientSocket) {

        this.clientName = clientName;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {

        final Thread listener = new Thread(new ClientListener(clientName, clientSocket));
        final Thread writer = new Thread(new ClientWriter(clientName, clientSocket));

        listener.start();
        writer.start();

        /** Waiting for the listener and writer to finish their work **/
        try {

            listener.join();
            writer.join();

        } catch (InterruptedException ex) {

            ex.printStackTrace();
        }

        /** close the socket **/
        try {

            clientSocket.close();

        } catch (IOException ex) {

            ex.printStackTrace();
        }

        /** remove the client **/
        chatServer.removeClient(clientName);
    }
}
