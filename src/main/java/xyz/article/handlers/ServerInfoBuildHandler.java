package xyz.article.handlers;

import net.kyori.adventure.text.Component;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodec;
import org.geysermc.mcprotocollib.protocol.data.status.PlayerInfo;
import org.geysermc.mcprotocollib.protocol.data.status.ServerStatusInfo;
import org.geysermc.mcprotocollib.protocol.data.status.VersionInfo;
import org.geysermc.mcprotocollib.protocol.data.status.handler.ServerInfoBuilder;
import xyz.article.api.event.events.ClientPingEvent;

import java.util.ArrayList;

import static xyz.article.MinecraftServer.eventManager;

public class ServerInfoBuildHandler implements ServerInfoBuilder {
    @Override
    public ServerStatusInfo buildInfo(Session session) {
        Component motd = Component.text("§cSlider§bMC §7- §aRebuild");
        PlayerInfo playerInfo = new PlayerInfo(100, 0, new ArrayList<>());
        VersionInfo versionInfo = new VersionInfo(MinecraftCodec.CODEC.getMinecraftVersion(), MinecraftCodec.CODEC.getProtocolVersion());
        byte[] icon = null;
        boolean enforcesSecureChat = false;
        ClientPingEvent event = new ClientPingEvent(motd, playerInfo, versionInfo, icon, enforcesSecureChat);
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
