package xyz.article.api.event;

public interface EventManager {
    Event callEvent (Event event);
    void addListener (Listener listener);
    void removeListener (Listener listener);
}
