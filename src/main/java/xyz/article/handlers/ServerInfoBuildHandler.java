package xyz.article.handlers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodec;
import org.geysermc.mcprotocollib.protocol.data.status.PlayerInfo;
import org.geysermc.mcprotocollib.protocol.data.status.ServerStatusInfo;
import org.geysermc.mcprotocollib.protocol.data.status.VersionInfo;
import org.geysermc.mcprotocollib.protocol.data.status.handler.ServerInfoBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.article.RunningData;
import xyz.article.Settings;
import xyz.article.api.entities.player.Player;
import xyz.article.api.event.events.ClientPingEvent;

import java.util.ArrayList;
import java.util.List;

import static xyz.article.MinecraftServer.eventManager;

/**
 * 玩家ping服务器逻辑类
 */
public class ServerInfoBuildHandler implements ServerInfoBuilder {
    private static final Logger log = LoggerFactory.getLogger(ServerInfoBuildHandler.class);

    @Override
    public ServerStatusInfo buildInfo(Session session) {
        // 原始格式的字符串
        String rawText = "SliderMC - Rebuild";
        // 定义渐变起止颜色（蓝 -> 绿）
        TextColor startColor = TextColor.fromHexString("#00bcff");
        TextColor endColor = TextColor.fromHexString("#ff6800");
        TextComponent.Builder builder = Component.text();
        char[] chars = rawText.toCharArray();
        int length = chars.length;

        for (int i = 0; i < length; i++) {
            // 计算颜色插值
            float ratio = (float) i / (length - 1);
            int red = 0;
            if (startColor != null) {
                if (endColor != null) {
                    red = (int) (startColor.red() * (1 - ratio) + endColor.red() * ratio);
                }
            }
            int green = 0;
            if (startColor != null) {
                if (endColor != null) {
                    green = (int) (startColor.green() * (1 - ratio) + endColor.green() * ratio);
                }
            }
            int blue = 0;
            if (startColor != null) {
                if (endColor != null) {
                    blue = (int) (startColor.blue() * (1 - ratio) + endColor.blue() * ratio);
                }
            }
            TextColor charColor = TextColor.color(red, green, blue);
            builder.append(Component.text(chars[i]).color(charColor));
        }

        Component gradientMOTD = builder.build();
        List<GameProfile> list = new ArrayList<>();
        for (Player player : RunningData.globalPlayers) {
            list.add(player.getProfile());
        }
        PlayerInfo playerInfo = new PlayerInfo(Settings.MAX_PLAYERS, RunningData.globalPlayers.size(), list);
        VersionInfo versionInfo = new VersionInfo(MinecraftCodec.CODEC.getMinecraftVersion(), MinecraftCodec.CODEC.getProtocolVersion());
        byte[] icon = null;
        boolean enforcesSecureChat = false;
        if (Settings.SHOULD_PING_SHOWN) {
            log.info("<-- {} has pinged -->", session.getRemoteAddress()); // 在玩家Ping服务器时，显示一条Ping消息
        }
        ClientPingEvent event = new ClientPingEvent(gradientMOTD, playerInfo, versionInfo, icon, enforcesSecureChat);
        eventManager.callEvent(event);
        return new ServerStatusInfo(
                event.motd, // Motd
                event.playerInfo, // Player Info
                event.versionInfo, // Version Info
                event.icon, // Icon (Base64)
                event.enforcesSecureChat // EnforcesSecureChat
        );
    }
}
