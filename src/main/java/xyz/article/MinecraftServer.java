package xyz.article;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.auth.SessionService;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.server.ServerAdapter;
import org.geysermc.mcprotocollib.network.event.server.SessionAddedEvent;
import org.geysermc.mcprotocollib.network.event.session.SessionAdapter;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.network.tcp.TcpServer;
import org.geysermc.mcprotocollib.protocol.MinecraftConstants;
import org.geysermc.mcprotocollib.protocol.MinecraftProtocol;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodec;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.PlayerSpawnInfo;
import org.geysermc.mcprotocollib.protocol.data.status.PlayerInfo;
import org.geysermc.mcprotocollib.protocol.data.status.ServerStatusInfo;
import org.geysermc.mcprotocollib.protocol.data.status.VersionInfo;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.article.api.event.EventManager;
import xyz.article.api.event.events.ClientPingEvent;
import xyz.article.api.packetprocessor.PacketProcessor;
import xyz.article.event.EventManagerInstant;
import xyz.article.packets.ClientboundServerBrandPacket;

import java.util.ArrayList;

public class MinecraftServer {
    private static TcpServer server;
    private static EventManager eventManager;
    private static final Logger log = LoggerFactory.getLogger(MinecraftServer.class);

    public static void main(String[] args) {
        server = new TcpServer("0.0.0.0", 25565, MinecraftProtocol::new);
        eventManager = new EventManagerInstant();
        SessionService sessionService = new SessionService();
        sessionService.setProxy(null);
        server.setGlobalFlag(MinecraftConstants.SESSION_SERVICE_KEY, sessionService);
        server.setGlobalFlag(MinecraftConstants.ENCRYPT_CONNECTION, false);
        server.setGlobalFlag(MinecraftConstants.SHOULD_AUTHENTICATE, false);
        server.setGlobalFlag(MinecraftConstants.SERVER_COMPRESSION_THRESHOLD, 256);
        server.setGlobalFlag(MinecraftConstants.SERVER_INFO_BUILDER_KEY, session -> {
                    Component motd = Component.text("§cSlider§bMC §7- §aRebuild");
                    PlayerInfo playerInfo = new PlayerInfo(100, 0, new ArrayList<>());
                    VersionInfo versionInfo = new VersionInfo(MinecraftCodec.CODEC.getMinecraftVersion(), MinecraftCodec.CODEC.getProtocolVersion());
                    byte[] icon = null;
                    boolean enforcesSecureChat = false;
                    eventManager.callEvent(new ClientPingEvent(motd, playerInfo, versionInfo, icon, enforcesSecureChat));
                    return new ServerStatusInfo(
                            motd, // Motd
                            playerInfo, // Player Info
                            versionInfo, // Version Info
                            icon, // Icon (Base64)
                            enforcesSecureChat // EnforcesSecureChat
                    );
                }
        );

        server.setGlobalFlag(MinecraftConstants.SERVER_LOGIN_HANDLER_KEY, session -> {
            // Player Login Logic
            session.send(new ClientboundLoginPacket(0, false, new Key[]{ Key.key("minecraft:overworld") }, 100, 10, 16, false, false, false, new PlayerSpawnInfo(0, Key.key("minecraft:overworld"), 100, GameMode.CREATIVE, GameMode.CREATIVE, false, false, null, 100), true));
            session.send(new ClientboundServerBrandPacket("SliderMC - Rebuild").getPacket());

            GameProfile profile = session.getFlag(MinecraftConstants.PROFILE_KEY);
            log.info("玩家 {} 加入了游戏", profile.getName());
        });

        server.addListener(new ServerAdapter() {
            @Override
            public void sessionAdded(SessionAddedEvent event) {
                event.getSession().addListener(new SessionAdapter() {
                    @Override
                    public void packetReceived(Session session, Packet packet) {
                        for (PacketProcessor processor : Register.getPacketProcessors()) {
                            processor.process(session, packet);
                        }
                    }
                });
            }
        });

        server.bind();
    }

    public void destroy() {
        server.close();
    }

    public TcpServer getServer() {
        return server;
    }

    public EventManager getEventManager () {
        return eventManager;
    }
}