package xyz.article.api.plugin;

import java.io.File;

public interface PluginManager {
    Plugin loadPlugin (File file);
    void disablePlugin (Plugin plugin);
    void disablePlugin (String name);
}
