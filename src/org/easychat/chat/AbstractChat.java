package org.easychat.chat;

/**
 * Created by Slevy on 06.05.2018.
 */
abstract class AbstractChat implements Chat {

    private final Conversation conversation = new Conversation();
    final String sender;
    final String receiver;

    AbstractChat(String sender, String receiver) {

        this.sender = sender;
        this.receiver = receiver;
    }

    @Override
    public synchronized void receiveMessage(String message) {

        conversation.addMessage(message);
    }

    @Override
    public synchronized String[] getConversation() {

        return conversation.getConversation();
    }
}
