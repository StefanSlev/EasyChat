package org.easychat.protocol;

/**
 * Created by Slevy on 06.05.2018.
 */
final public class PublicMessage extends Message {

    private static final String RECEIVER = "EVERYONE";

    public PublicMessage(String sender, String message) {

        super(MessageType.PUBLIC, sender, RECEIVER, message);
    }
}
