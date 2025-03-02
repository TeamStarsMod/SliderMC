package xyz.article.api.plugin;

import xyz.article.api.Server;
import xyz.article.api.event.Event;
import xyz.article.api.event.EventManager;

import java.io.File;

public abstract class SliderPlugin implements Plugin {
    private File dataFolder;
    private Description description;
    private PluginManager pluginManager;
    private Server server;
    private EventManager eventManager;

    @Override
    public File getDataFolder () {
        return dataFolder;
    }

    public void setDataFolder (File dataFolder) {
        this.dataFolder = dataFolder;
    }

    @Override
    public Description getDescription () {
        return description;
    }

    public void setDescription (Description description) {
        this.description = description;
    }

    @Override
    public PluginManager getPluginManager () {
        return pluginManager;
    }

    public void setPluginManager (PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    @Override
    public Server getServer () {
        return server;
    }

    public void setServer (Server server) {
        this.server = server;
    }

    @Override
    public EventManager getEventManager () {
        return eventManager;
    }

    public void setEventManager (EventManager eventManager) {
        this.eventManager = eventManager;
    }

    public abstract void onEnabled ();

    public abstract void onDisabled ();
}
