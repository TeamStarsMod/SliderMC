package xyz.article.api.plugin;

import java.io.File;
import java.util.List;

public interface PluginManager {
    Plugin loadPlugin (File file);
    void disablePlugin (Plugin plugin);
    void disablePlugin (String name);
    void loadAllJarInFolder (File file);
    List<Plugin> getPlugins ();
}
