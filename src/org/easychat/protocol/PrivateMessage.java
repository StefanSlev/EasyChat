package org.easychat.protocol;

/**
 * Created by Slevy on 06.05.2018.
 */
final public class PrivateMessage extends Message {

    public PrivateMessage(String sender, String receiver, String message) {

        super(MessageType.PRIVATE, sender, receiver, message);
    }
}
