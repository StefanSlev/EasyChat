package org.easychat.design;

/**
 * Created by Slevy on 17.05.2018.
 */
public interface Subject {

    void register(Observer observer);
    void unregister(Observer observer);
    void notifyObservers();
    Object getUpdate(ObserverType type);
}
