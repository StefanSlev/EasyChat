package org.easychat.server;

import org.easychat.communication.Mailbox;
import org.easychat.protocol.Message;
import java.net.Socket;
import java.util.List;

/**
 * Created by Slevy on 07.05.2018.
 */
interface ServerOperations {

    /** internal operations **/
    void addClient(String clientName, Socket clientSocket);
    void removeClient(String clientName);
    boolean clientExists(String clientName);
    Mailbox getMailbox(String clientName);

    /** communication **/
    void broadcast(Message message);
    void sendTo(Message message);
    List<String> getAllConnectedClients();

    /** remote **/
    void loadServerInfo();
}
