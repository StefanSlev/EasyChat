package org.easychat.chat;

import org.easychat.protocol.Message;

/**
 * Created by Slevy on 06.05.2018.
 */
public interface Chat {

    Message sendMessage(String message);
    void receiveMessage(String message);
    String[] getConversation();
}
