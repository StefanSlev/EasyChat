package org.easychat.protocol;

/**
 * Created by Slevy on 17.05.2018.
 */
final public class RetrieveMessage extends Message {

    private static final String SENDER = "SERVER";
    private static final String RECEIVER = "EVERYONE";
    private static final String MESSAGE = "RETRIEVE";

    public RetrieveMessage() {

        super(MessageType.RETRIEVE, SENDER, RECEIVER, MESSAGE);
    }
}

