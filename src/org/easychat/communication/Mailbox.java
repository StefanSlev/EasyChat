package org.easychat.communication;

/**
 * Created by Slevy on 06.05.2018.
 */

import org.easychat.protocol.Message;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import java.util.LinkedList;

final public class Mailbox {

    private static final int CAPACITY = 1000000;
    private final LinkedList<Message> messages = new LinkedList<>();

    /** synchronization utilities **/
    private final Lock mailboxLock = new ReentrantLock();
    private final Condition fullMailbox = mailboxLock.newCondition();
    private final Condition emptyMailbox = mailboxLock.newCondition();

    public void writeMessage(Message message) throws InterruptedException {

        mailboxLock.lock();

        try {

            boolean canWrite = (messages.size() < CAPACITY);

            while (!canWrite) {

                fullMailbox.await();
                canWrite = (messages.size() < CAPACITY);
            }

            messages.addLast(message);
            emptyMailbox.signal();

        } finally {
            mailboxLock.unlock();
        }
    }

    public Message readMessage() throws InterruptedException {

        mailboxLock.lock();

        try {

            boolean canRead = (messages.size() > 0);

            while (!canRead) {

                emptyMailbox.await();
                canRead = (messages.size() > 0);
            }

            final Message message = messages.pollFirst();
            fullMailbox.signal();

            return  message;

        } finally {
            mailboxLock.unlock();
        }
    }
}
