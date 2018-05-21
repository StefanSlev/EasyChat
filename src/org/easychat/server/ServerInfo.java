package org.easychat.server;

import java.rmi.RemoteException;
import java.util.List;

/**
 * Created by Slevy on 08.05.2018.
 */
final class ServerInfo implements RemoteServerOperations {

    private final ChatServer chatServer;

    ServerInfo(ChatServer chatServer) {

        this.chatServer = chatServer;
    }

    @Override
    public boolean isNicknameAvailable(String nickname) throws RemoteException {
        return !chatServer.clientExists(nickname);
    }

    @Override
    public boolean userExists(String nickname) throws RemoteException {
        return chatServer.clientExists(nickname);
    }

    @Override
    public List<String> getAllConnectedClients() throws RemoteException {
        return chatServer.getAllConnectedClients();
    }
}
