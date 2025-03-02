package xyz.article.api;

import org.geysermc.mcprotocollib.network.Session;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.article.MinecraftServer;
import xyz.article.RunningData;
import xyz.article.api.entities.player.Player;
import xyz.article.api.event.EventManager;

public class Slider {
    private final Server server = MinecraftServer.apiServer;
    /**
     * 获取玩家实例 (通过Session)
     * @param session 用于获取玩家的Session实例
     * @return 获取到的玩家实例
     */
    public static @Nullable Player getPlayer (Session session) {
        return RunningData.globalSessionPlayerMap.get(session);
    }

    /**
     * 获取玩家实例（通过玩家名或UUID）
     * @param name 玩家名或UUID
     * @return 获取到的玩家实例
     */
    public static @Nullable Player getPlayer (String name) {
        for (Player globalPlayer : RunningData.globalPlayers) {
            if (globalPlayer.getProfile().getName().equals(name)) return globalPlayer;
            else if (globalPlayer.getProfile().getId().toString().equals(name)) return globalPlayer;
        }
        return null;
    }

    public static @NotNull EventManager getEventManager () {
        return MinecraftServer.eventManager;
    }
}
