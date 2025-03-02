package xyz.article.api.plugin;

import xyz.article.api.Server;
import xyz.article.api.event.EventManager;

import java.io.File;
import java.net.URLClassLoader;

public abstract class SliderPlugin implements Plugin {
    private File dataFolder;
    private Description description;
    private PluginManager pluginManager;
    private Server server;
    private EventManager eventManager;
    private String name;
    private URLClassLoader classLoader;

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

    @Override
    public String getName () {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }

    @Override
    public URLClassLoader getClassLoader () {
        return classLoader;
    }

    public void setClassLoader (URLClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public abstract void onEnabled ();

    public abstract void onDisabled ();
}
