package xyz.article;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.auth.SessionService;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.server.ServerAdapter;
import org.geysermc.mcprotocollib.network.event.server.SessionAddedEvent;
import org.geysermc.mcprotocollib.network.event.server.SessionRemovedEvent;
import org.geysermc.mcprotocollib.network.event.session.SessionAdapter;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.network.tcp.TcpServer;
import org.geysermc.mcprotocollib.protocol.MinecraftConstants;
import org.geysermc.mcprotocollib.protocol.MinecraftProtocol;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodec;
import org.geysermc.mcprotocollib.protocol.data.status.PlayerInfo;
import org.geysermc.mcprotocollib.protocol.data.status.ServerStatusInfo;
import org.geysermc.mcprotocollib.protocol.data.status.VersionInfo;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundPlayerInfoRemovePacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundSystemChatPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundRemoveEntitiesPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.article.api.Slider;
import xyz.article.api.event.EventManager;
import xyz.article.api.event.events.ClientPingEvent;
import xyz.article.api.packetprocessor.PacketProcessor;
import xyz.article.event.EventManagerInstant;
import xyz.article.handlers.LoginHandler;
import xyz.article.handlers.ServerInfoBuildHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MinecraftServer {
    private static TcpServer server;
    public static EventManager eventManager;
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
        server.setGlobalFlag(MinecraftConstants.SERVER_INFO_BUILDER_KEY, new ServerInfoBuildHandler());

        server.setGlobalFlag(MinecraftConstants.SERVER_LOGIN_HANDLER_KEY, new LoginHandler());

        server.addListener(new ServerAdapter() {
            @Override
            public void sessionRemoved(SessionRemovedEvent event) {
                if (RunningData.sessions.contains(event.getSession())) {
                    GameProfile profile = event.getSession().getFlag(MinecraftConstants.PROFILE_KEY);
                    log.info("{} 离开了游戏", profile.getName());
                    RunningData.sessions.remove(event.getSession());
                    Component component = Component.text(profile.getName() + " 退出了游戏").color(NamedTextColor.YELLOW);
                    for (Session session1 : RunningData.sessions) {
                        session1.send(new ClientboundSystemChatPacket(component, false));
                        session1.send(new ClientboundRemoveEntitiesPacket(new int[]{Objects.requireNonNull(Slider.getPlayer(event.getSession())).getEntityId()}));
                        session1.send(new ClientboundPlayerInfoRemovePacket(List.of(profile.getId())));
                    }
                    RunningData.players.remove(Slider.getPlayer(event.getSession()));
                    RunningData.sessions.remove(event.getSession());
                    RunningData.sessionPlayerMap.remove(event.getSession());
                }
            }

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

        Register.register();
        server.bind();
    }

    public static void destroy() {
        server.close();
    }

    public static TcpServer getServer() {
        return server;
    }

    public static EventManager getEventManager () {
        return eventManager;
    }
}