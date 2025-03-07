package xyz.article;

import net.kyori.adventure.key.Key;
import org.geysermc.mcprotocollib.network.Session;
import xyz.article.api.entities.player.Player;
import xyz.article.api.world.World;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class RunningData {
    public static ConcurrentHashMap<Session, Player> globalSessionPlayerMap = new ConcurrentHashMap<>();
    public static CopyOnWriteArrayList<Session> globalSessions = new CopyOnWriteArrayList<>();
    public static CopyOnWriteArrayList<Player> globalPlayers = new CopyOnWriteArrayList<>();
    public static CopyOnWriteArrayList<Integer> globalEntities = new CopyOnWriteArrayList<>();
    public static ConcurrentHashMap<Key, World> worldMap = new ConcurrentHashMap<>();

    public static boolean stopping = false;
}
