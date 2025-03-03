package xyz.article;

import net.kyori.adventure.key.Key;
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
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundPlayerInfoRemovePacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundSystemChatPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundRemoveEntitiesPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.article.api.Server;
import xyz.article.api.Slider;
import xyz.article.api.entities.player.Player;
import xyz.article.api.event.EventManager;
import xyz.article.api.event.Listener;
import xyz.article.api.packetprocessor.PacketProcessor;
import xyz.article.api.plugin.Plugin;
import xyz.article.api.plugin.PluginManager;
import xyz.article.api.world.World;
import xyz.article.api.world.block.BlockItemMap;
import xyz.article.event.EventManagerInstant;
import xyz.article.handlers.LoginHandler;
import xyz.article.handlers.ServerInfoBuildHandler;
import xyz.article.plugin.PluginManagerInstant;
import xyz.article.world.OverWorldGenerator;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class MinecraftServer implements Server {
    private static MyTCPServer server;
    public static Server apiServer;
    public static EventManager eventManager;
    public static PluginManager pluginManager;
    private static final Logger log = LoggerFactory.getLogger(MinecraftServer.class);

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        server = new MyTCPServer("0.0.0.0", 25565, MinecraftProtocol::new);
        eventManager = new EventManagerInstant();
        pluginManager = new PluginManagerInstant();
        apiServer = new MinecraftServer();
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
                if (RunningData.globalSessions.contains(event.getSession())) {
                    GameProfile profile = event.getSession().getFlag(MinecraftConstants.PROFILE_KEY);
                    log.info("{} 离开了游戏", profile.getName());
                    RunningData.globalSessions.remove(event.getSession());
                    Component component = Component.text(profile.getName() + " 退出了游戏").color(NamedTextColor.YELLOW);
                    for (Session session1 : RunningData.globalSessions) {
                        session1.send(new ClientboundSystemChatPacket(component, false));
                        session1.send(new ClientboundRemoveEntitiesPacket(new int[]{Objects.requireNonNull(Slider.getPlayer(event.getSession())).getEntityId()}));
                        session1.send(new ClientboundPlayerInfoRemovePacket(List.of(profile.getId())));
                    }
                    Player player = Slider.getPlayer(event.getSession());
                    RunningData.globalPlayers.remove(player);
                    RunningData.globalSessions.remove(event.getSession());
                    RunningData.globalSessionPlayerMap.remove(event.getSession());
                    RunningData.globalEntities.remove(player.getEntityId());
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

        BlockItemMap.writeMap();
        RunningData.worldMap.put(Key.key("minecraft:overworld"), new World(Key.key("minecraft:overworld"), new OverWorldGenerator(114514L, 0.01, 0.1, 0.01)));
        Register.register();
        pluginManager.loadAllJarInFolder(new File("./plugins"));
        server.bind();
        log.info("启动完成，用时 {}ms，键入help来获取帮助！", System.currentTimeMillis() - start);
    }

    public static void destroy() {
        server.close();
    }

    public static TcpServer getServer() {
        return server;
    }

    @Override
    public void registerEventListener(Listener listener, Plugin plugin) {

    }

    @Override
    public EventManager getEventManager() {
        return eventManager;
    }

    @Override
    public PluginManager getPluginManager() {
        return null;
    }
}