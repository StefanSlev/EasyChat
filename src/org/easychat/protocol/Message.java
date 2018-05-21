package org.easychat.protocol;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by Slevy on 06.05.2018.
 */
public abstract class Message implements MessageProtocol, Serializable {

    /** static fields **/
    private static final long serialVersionUID = 1L;
    private static final String formatMessage = "%s %s: %s";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

    /** message components **/
    private final MessageType messageType;
    private final String sender;
    private final String receiver;
    private final String message;

    Message(MessageType messageType, String sender, String receiver, String message) {

        this.messageType = messageType;
        this.sender = sender;
        this.receiver = receiver;
        this.message = String.format(formatMessage,
                LocalTime.now().format(formatter), sender, message);
    }

    @Override
    public MessageType getMessageType() {
        return messageType;
    }

    @Override
    public String getSender() {
        return sender;
    }

    @Override
    public String getReceiver() {
        return receiver;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
