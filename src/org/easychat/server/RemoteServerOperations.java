package org.easychat.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Created by Slevy on 07.05.2018.
 */
public interface RemoteServerOperations extends Remote {

    boolean isNicknameAvailable(String nickname) throws RemoteException;
    boolean userExists(String nickname) throws RemoteException;
    List<String> getAllConnectedClients() throws RemoteException;
}
