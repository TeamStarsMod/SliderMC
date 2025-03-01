package xyz.article;

import org.geysermc.mcprotocollib.network.Session;
import xyz.article.api.entities.player.Player;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class RunningData {
    public static Map<Session, Player> sessionPlayerMap = new ConcurrentHashMap<>();
    public static List<Session> sessions = new CopyOnWriteArrayList<>();
    public static List<Player> players = new CopyOnWriteArrayList<>();
}
