package xyz.article.plugin;

import xyz.article.api.plugin.Plugin;
import xyz.article.api.plugin.PluginManager;

import java.io.File;

public class PluginManagerInstant implements PluginManager {
    @Override
    public Plugin loadPlugin(File file) {
        ClassLoader clazzLoader = PluginManagerInstant.class.getClassLoader().getParent();
        return null;
    }

    @Override
    public void disablePlugin(Plugin plugin) {

    }

    @Override
    public void disablePlugin(String name) {

    }
}
