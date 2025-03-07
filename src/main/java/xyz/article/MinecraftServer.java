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
import xyz.article.api.event.events.PlayerQuitEvent;
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
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class MinecraftServer implements Server {
    private static MyTCPServer server;
    public static Server apiServer;
    public static EventManager eventManager;
    public static PluginManager pluginManager;
    private static final Logger log = LoggerFactory.getLogger(MinecraftServer.class);
    private static final File saveDir = new File("./" + Settings.SAVE_FOLDER);

    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();
        new ShutdownHook();
        File propertiesFile = new File("./settings.yml");
        if (propertiesFile.createNewFile()) log.info("已创建配置文件");
        Settings.init(propertiesFile);
        server = new MyTCPServer(Settings.BIND_ADDRESS, Settings.SERVER_PORT, MinecraftProtocol::new);
        eventManager = new EventManagerInstant();
        pluginManager = new PluginManagerInstant();
        apiServer = new MinecraftServer();
        SessionService sessionService = new SessionService();
        sessionService.setProxy(null);
        server.setGlobalFlag(MinecraftConstants.SESSION_SERVICE_KEY, sessionService);
        server.setGlobalFlag(MinecraftConstants.ENCRYPT_CONNECTION, Settings.ONLINE_MODE);
        server.setGlobalFlag(MinecraftConstants.SHOULD_AUTHENTICATE, Settings.ONLINE_MODE);
        server.setGlobalFlag(MinecraftConstants.SERVER_COMPRESSION_THRESHOLD, 256);

        server.setGlobalFlag(MinecraftConstants.SERVER_INFO_BUILDER_KEY, new ServerInfoBuildHandler());
        server.setGlobalFlag(MinecraftConstants.SERVER_LOGIN_HANDLER_KEY, new LoginHandler());

        server.addListener(new ServerAdapter() {
            @Override
            public void sessionRemoved(SessionRemovedEvent event) {
                if (RunningData.globalSessions.contains(event.getSession())) {
                    GameProfile profile = event.getSession().getFlag(MinecraftConstants.PROFILE_KEY);
                    Player player = Slider.getPlayer(event.getSession());
                    log.info("{} 离开了游戏", profile.getName());
                    if (saveDir.mkdir()) log.info("正在创建存档文件夹");
                    File playerSaveDir = new File(saveDir, "playerdata");
                    if (playerSaveDir.mkdir()) log.info("正在创建玩家存档文件夹");
                    if (player != null) {
                        File playerFile = new File(playerSaveDir, player.getProfile().getId().toString() + ".slider");
                        try {
                            playerFile.createNewFile();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        player.saveToFile(playerFile);
                    }
                    RunningData.globalSessions.remove(event.getSession());
                    Component component = Component.text(profile.getName() + " 退出了游戏").color(NamedTextColor.YELLOW);
                    PlayerQuitEvent quitEvent = new PlayerQuitEvent(player, component);
                    Slider.getEventManager().callEvent(quitEvent);
                    for (Session session1 : RunningData.globalSessions) {
                        session1.send(new ClientboundSystemChatPacket(quitEvent.getQuitMessage(), false));
                        session1.send(new ClientboundRemoveEntitiesPacket(new int[]{Objects.requireNonNull(Slider.getPlayer(event.getSession())).getEntityId()}));
                        session1.send(new ClientboundPlayerInfoRemovePacket(List.of(profile.getId())));
                    }
                    RunningData.globalPlayers.remove(player);
                    RunningData.globalSessions.remove(event.getSession());
                    RunningData.globalSessionPlayerMap.remove(event.getSession());
                    if (player != null) {
                        RunningData.globalEntities.remove(player.getEntityId());
                    }
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

                        //System.out.println(packet);
                    }
                });
            }
        });

        BlockItemMap.writeMap();
        RunningData.worldMap.put(Key.key("minecraft:overworld"), new World(Key.key("minecraft:overworld"), new OverWorldGenerator(114514L, 0.02, 0.1, 0.01)));
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

    /**
     * 停止并保存服务器
     */
    public static void stop() {
        // 异步保存
        new Thread(() -> {
            try {
                RunningData.stopping = true;

                for (Player player : RunningData.globalPlayers) {
                    player.sendPacket(new ClientboundSystemChatPacket(Component.text("服务器正在关闭，请尽量不要进行操作！"), false));
                }

                Thread.currentThread().setName("Close Thread");
                if (saveDir.mkdir()) log.info("正在创建存档文件夹");
                RunningData.worldMap.forEach((key, world) -> {
                    File worldDir = new File(saveDir, "worlds");
                    if (worldDir.mkdir()) log.info("正在创建世界存档文件夹");
                    File worldFile = new File(worldDir, key.namespace() + "_" + key.value());
                    if (worldFile.mkdir()) log.info("正在为世界 {} 创建存档文件夹", world.getKey());
                    world.stop(worldFile);
                });

                File playerSaveDir = new File(saveDir, "playerdata");
                if (playerSaveDir.mkdir()) log.info("正在创建玩家存档文件夹");
                for (Player player : RunningData.globalPlayers) {
                    log.info("正在保存玩家 {}", player.getProfile().getName());
                    File playerFile = new File(playerSaveDir, player.getProfile().getId().toString() + ".slider");
                    playerFile.createNewFile();
                    player.saveToFile(playerFile);
                }

                log.info("正在关闭服务器...");
                server.close();
            }catch (IOException e) {
                log.error("在停止服务器时出现错误！ {}", e.toString());
            }
        }).start();
    }
}