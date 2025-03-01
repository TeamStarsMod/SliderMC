package xyz.article.api;

import org.geysermc.mcprotocollib.network.Session;
import xyz.article.RunningData;
import xyz.article.api.entities.player.Player;

public class Slider {
    public static Player getPlayer(Session session) {
        return RunningData.sessionPlayerMap.get(session);
    }
}
