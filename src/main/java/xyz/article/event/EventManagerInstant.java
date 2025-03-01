package xyz.article.event;

import xyz.article.api.event.Event;
import xyz.article.api.event.EventManager;
import xyz.article.api.event.Listener;
import xyz.article.api.event.events.ClientPingEvent;

import java.util.ArrayList;
import java.util.List;

public class EventManagerInstant implements EventManager {
    private final List<Listener> listeners;
    public EventManagerInstant () {
        listeners = new ArrayList<>();
    }

    @Override
    public Event callEvent (Event event) {
        for (Listener listener : listeners) {
            listener.execute((ClientPingEvent) event);
        }
        return event;
    }

    @Override
    public void addListener (Listener listener) {
        if (listeners.contains(listener)) throw new IllegalArgumentException("这个监听器已经被注册了！监听器实例 " + listener);
        else listeners.add(listener);
    }

    @Override
    public void removeListener (Listener listener) {
        if (listeners.contains(listener)) listeners.remove(listener);
        else throw new IllegalArgumentException("这个监听器还没有被注册，无法移除！监听器实例 " + listener);
    }
}
