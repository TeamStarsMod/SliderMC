package xyz.article;

import net.kyori.adventure.text.Component;
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
import org.geysermc.mcprotocollib.protocol.data.status.PlayerInfo;
import org.geysermc.mcprotocollib.protocol.data.status.ServerStatusInfo;
import org.geysermc.mcprotocollib.protocol.data.status.VersionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.article.api.event.EventManager;
import xyz.article.api.event.events.ClientPingEvent;
import xyz.article.api.packetprocessor.PacketProcessor;
import xyz.article.event.EventManagerInstant;
import xyz.article.handlers.LoginHandler;
import xyz.article.handlers.ServerInfoBuildHandler;

import java.util.ArrayList;

public class MinecraftServer {
    private static TcpServer server;
    public static EventManager eventManager;
    private static final Logger log = LoggerFactory.getLogger(MinecraftServer.class);

    public static void main(String[] args) {
        server = new TcpServer("0.0.0.0", 25565, MinecraftProtocol::new);
        eventManager = new EventManagerInstant();
        eventManager.addListener(new EventManagerInstant.ExampleListener());
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