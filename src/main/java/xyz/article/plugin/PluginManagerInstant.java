package xyz.article.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.article.api.plugin.Plugin;
import xyz.article.api.plugin.PluginManager;
import xyz.article.api.utils.ExceptionUtils;
import xyz.article.exceptions.IncompletePluginException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

public class PluginManagerInstant implements PluginManager {
    private static final Logger log = LoggerFactory.getLogger(PluginManagerInstant.class);

    @Override
    public Plugin loadPlugin(File file) {
        ClassLoader clazzLoader = PluginManagerInstant.class.getClassLoader().getParent();
        try (URLClassLoader classLoader = new URLClassLoader(new URL[]{file.toURI().toURL()})) {
            try (InputStream plugin_yml = classLoader.getResourceAsStream("slider.yml")) {
                if (plugin_yml == null) throw new IncompletePluginException("无法加载jar " + file.getName() + "，因为该jar没有slider.yml！");
            }
        } catch (IOException e) {
            ExceptionUtils.ExceptionHandler(log, e);
        }
        return null;
    }

    @Override
    public void disablePlugin(Plugin plugin) {

    }

    @Override
    public void disablePlugin(String name) {

    }
}
