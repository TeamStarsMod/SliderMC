package xyz.article;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Settings {
    public static String BIND_ADDRESS;
    public static int SERVER_PORT;
    public static int MAX_PLAYERS;
    public static int VIEW_DISTANCE;
    public static boolean ONLINE_MODE;
    public static boolean SHOULD_PING_SHOWN;
    public static String SAVE_FOLDER = "save";
    public static int CHUNK_SAVE_TIME_MINUTE;

    private static File propertiesFile;

    public static void init(File propertiesFile1) throws IOException {
        propertiesFile = propertiesFile1;

        BIND_ADDRESS = (String) readAndCheck("bind-address", "0.0.0.0");
        SERVER_PORT = (int) readAndCheck("server-port", 25565);
        MAX_PLAYERS = (int) readAndCheck("max-players", 20);
        ONLINE_MODE = (boolean) readAndCheck("online-mode", true);
        VIEW_DISTANCE = (int) readAndCheck("view-distance", 10);
        SHOULD_PING_SHOWN = (boolean) readAndCheck("should-ping-shown", false);
        CHUNK_SAVE_TIME_MINUTE = (int) readAndCheck("chunk-save-time-minute", 30);
    }

    public static Object readAndCheck(String key, Object defaultValue) throws IOException {
        Yaml yamlLoader = new Yaml();
        Map<String, Object> map = yamlLoader.load(new FileInputStream(propertiesFile));
        if (map == null) map = new HashMap<>();
        map.putIfAbsent(key, defaultValue);
        yamlLoader.dump(map, new FileWriter(propertiesFile));
        return map.get(key);
    }
}
