package org.easychat.chat;

import org.easychat.protocol.Message;
import org.easychat.protocol.PublicMessage;

/**
 * Created by Slevy on 06.05.2018.
 */
public final class PublicChat extends AbstractChat {

    private static final String RECEIVER = "EVERYONE";

    public PublicChat(String sender) {

        super(sender, RECEIVER);
    }

    @Override
    public Message sendMessage(String message) {

        return new PublicMessage(sender, message);
    }
}
