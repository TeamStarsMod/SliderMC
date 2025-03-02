package xyz.article.api;

import xyz.article.api.event.EventManager;
import xyz.article.api.event.Listener;
import xyz.article.api.plugin.Plugin;
import xyz.article.api.plugin.PluginManager;

public interface Server {
    void registerEventListener (Listener listener, Plugin plugin);
    EventManager getEventManager ();
    PluginManager getPluginManager ();
}
