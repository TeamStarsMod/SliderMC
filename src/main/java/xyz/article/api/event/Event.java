package xyz.article.api.event;

public abstract class Event {
    public String getEventName () {
        return this.getClass().getSimpleName();
    }
}
