package org.easychat.chat;

import org.easychat.protocol.Message;
import org.easychat.protocol.PrivateMessage;

/**
 * Created by Slevy on 06.05.2018.
 */
public final class PrivateChat extends AbstractChat {

    public PrivateChat(String sender, String receiver) {

        super(sender, receiver);
    }

    @Override
    public Message sendMessage(String message) {

        return new PrivateMessage(sender, receiver, message);
    }
}
