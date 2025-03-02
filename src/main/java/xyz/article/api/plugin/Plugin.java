package xyz.article.api.plugin;

import xyz.article.api.Server;
import xyz.article.api.event.EventManager;

import java.io.File;

public interface Plugin {
    File getDataFolder ();
    Description getDescription ();
    PluginManager getPluginManager ();
    Server getServer ();
    EventManager getEventManager ();
}
