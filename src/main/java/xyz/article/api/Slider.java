package xyz.article.api;

import org.geysermc.mcprotocollib.network.Session;
import xyz.article.RunningData;
import xyz.article.api.entities.player.Player;

public class Slider {
    /**
     * 获取玩家实例 (通过Session)
     * @param session 用于获取玩家的Session实例
     * @return 获取到的玩家实例
     */
    public static Player getPlayer(Session session) {
        return RunningData.globalSessionPlayerMap.get(session);
    }
}
