package org.easychat.design;

import javafx.application.Platform;
import org.easychat.ui.ClientUI;

import java.util.List;
/**
 * Created by Slevy on 17.05.2018.
 */
public final class UsersObserver implements Observer {

    private final Subject subject;
    private final ObserverType observerType;
    private final ClientUI clientUI;

    public UsersObserver(Subject subject, ClientUI clientUI) {

        this.observerType = ObserverType.USERS_CONNECTED;
        this.subject = subject;
        this.clientUI = clientUI;
        this.subject.register(this);
    }

    @Override
    public void update() {

        @SuppressWarnings("unchecked")
        List<String> users = (List<String>) subject.getUpdate(observerType);
        Platform.runLater(() -> clientUI.updateNicknames(users));
    }

    @Override
    public ObserverType getType() {
        return this.observerType;
    }
}
