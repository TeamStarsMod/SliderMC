package xyz.article.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import xyz.article.MinecraftServer;
import xyz.article.api.plugin.Description;
import xyz.article.api.plugin.Plugin;
import xyz.article.api.plugin.PluginManager;
import xyz.article.api.plugin.SliderPlugin;
import xyz.article.api.utils.ExceptionUtils;
import xyz.article.exceptions.IncompletePluginException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PluginManagerInstant implements PluginManager {
    private static final Logger log = LoggerFactory.getLogger(PluginManagerInstant.class);
    private Map<String, SliderPlugin> plugins;
    private boolean isInited = false;

    public void init () {
        if (isInited) throw new RuntimeException("插件管理器已经初始化完成了！");
        else {
            isInited = true;
            plugins = new ConcurrentHashMap<>();
        }
    }

    @Override
    public Plugin loadPlugin(File file) {
        ClassLoader clazzLoader = PluginManagerInstant.class.getClassLoader().getParent();
        try (URLClassLoader classLoader = new URLClassLoader(new URL[]{file.toURI().toURL()}, clazzLoader)) {
            try (InputStream plugin_yml = classLoader.getResourceAsStream("slider.yml")) {
                if (plugin_yml == null) throw new IncompletePluginException("无法加载jar " + file.getName() + "，因为该jar没有slider.yml！");
                Yaml yaml = new Yaml();
                Map<String, Object> map = yaml.load(plugin_yml);
                String main_clazz = (String) map.get("main-class");
                String name = (String) map.get("name");
                String version = (String) map.get("version");
                Class<?> main_class = classLoader.loadClass(main_clazz);
                SliderPlugin sliderPlugin = (SliderPlugin) main_class.getDeclaredConstructor().newInstance();
                sliderPlugin.setPluginManager(MinecraftServer.pluginManager);
                sliderPlugin.setDataFolder(new File("./plugins", name));
                sliderPlugin.setName(name);
                sliderPlugin.setDescription(new Description(version));
                sliderPlugin.setEventManager(MinecraftServer.eventManager);
                sliderPlugin.setServer(MinecraftServer.apiServer);
                sliderPlugin.setClassLoader(classLoader);
                sliderPlugin.onEnabled();
                plugins.put(name, sliderPlugin);
                return sliderPlugin;
            } catch (Exception e) {
                ExceptionUtils.exceptionHandler(log, e);
            }
        } catch (IOException e) {
            ExceptionUtils.exceptionHandler(log, e);
        }
        return null;
    }

    @Override
    public void disablePlugin(Plugin plugin) {
        if (plugins.containsKey(plugin.getName())) {
            try {
                SliderPlugin sliderPlugin = plugins.get(plugin.getName());
                sliderPlugin.onDisabled();
                sliderPlugin.getClassLoader().close();
            } catch (IOException e) {
                ExceptionUtils.exceptionHandler(log, e);
            }
        }
    }

    @Override
    public void disablePlugin(String name) {
        if (plugins.containsKey(name)) {
            try {
                SliderPlugin sliderPlugin = plugins.get(name);
                sliderPlugin.onDisabled();
                sliderPlugin.getClassLoader().close();
            } catch (IOException e) {
                ExceptionUtils.exceptionHandler(log, e);
            }
        }
    }
}
