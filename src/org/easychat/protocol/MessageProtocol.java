package org.easychat.protocol;

/**
 * Created by Slevy on 06.05.2018.
 */

interface MessageProtocol {

    MessageType getMessageType();
    String getSender();
    String getReceiver();
    String getMessage();
}
