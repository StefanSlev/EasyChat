package org.easychat.design;

/**
 * Created by Slevy on 17.05.2018.
 */
public interface Observer {

    void update();
    ObserverType getType();
}
