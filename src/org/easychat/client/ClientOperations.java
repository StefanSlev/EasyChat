package org.easychat.client;

import org.easychat.chat.Chat;

/**
 * Created by Slevy on 08.05.2018.
 */
interface ClientOperations {

    /** internal operations **/
    void addPrivateChat(String nickname);
    void removePrivateChat(String nickname);
    boolean privateChatExists(String nickname);
    Chat getPrivateChat(String nickname);
    String[] getConversation(String nickname);
    Chat getPublicChat();
    String[] getPublicConversation();

    /** communication **/
    void openPublicChat();
    void openPrivateChat(String nickname) throws ClientException;
    void sendMessage(String message) throws ClientException;
    void sendDisconnect();
}
