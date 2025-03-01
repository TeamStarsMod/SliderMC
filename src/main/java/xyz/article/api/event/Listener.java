package xyz.article.api.event;

import xyz.article.api.event.events.ClientPingEvent;

public interface Listener {
    void execute (ClientPingEvent event);
}
