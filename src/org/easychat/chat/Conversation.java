package org.easychat.chat;

import java.util.LinkedList;

/**
 * Created by Slevy on 06.05.2018.
 */
final public class Conversation {

    private static final int NR_LIMIT = 1000;
    private boolean isRead;
    private final LinkedList<String> messages = new LinkedList<>();

    public Conversation() {
        isRead = true;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead() {
        isRead = true;
    }

    public void addMessage(String message) {

        if (messages.size() == NR_LIMIT) {
            messages.removeFirst();
        }

        messages.addLast(message);
        isRead = false;
    }

    public String[] getConversation() {

        return messages.toArray(new String[messages.size()]);
    }
}
