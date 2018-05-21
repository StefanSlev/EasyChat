package org.easychat.design;

import javafx.application.Platform;
import org.easychat.ui.ClientUI;

/**
 * Created by Slevy on 18.05.2018.
 */
public final class ChatObserver implements Observer {

    private final Subject subject;
    private final ObserverType observerType;
    private final ClientUI clientUI;

    public ChatObserver(Subject subject, ClientUI clientUI) {

        this.observerType = ObserverType.CHAT_MODIFIED;
        this.subject = subject;
        this.clientUI = clientUI;
        this.subject.register(this);
    }

    @Override
    public void update() {

        @SuppressWarnings("unchecked")
        String chatModified = (String) subject.getUpdate(observerType);
        Platform.runLater(() -> clientUI.updateChat(chatModified));
    }

    @Override
    public ObserverType getType() {
        return this.observerType;
    }
}
