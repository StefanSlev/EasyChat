package org.easychat.protocol;

/**
 * Created by Slevy on 06.05.2018.
 */
final public class DisconnectMessage extends Message {

    private static final String MESSAGE = "DISCONNECT";

    public DisconnectMessage(String sender) {

        super(MessageType.DISCONNECT, sender, sender, MESSAGE);
    }
}
