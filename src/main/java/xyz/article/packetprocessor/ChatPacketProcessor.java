package xyz.article.packetprocessor;

import net.kyori.adventure.text.Component;
import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundSystemChatPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.article.RunningData;
import xyz.article.api.Slider;
import xyz.article.api.packetprocessor.PacketProcessor;

public class ChatPacketProcessor implements PacketProcessor {
    private static final Logger log = LoggerFactory.getLogger(ChatPacketProcessor.class);

    @Override
    public void process(Session session, Packet packet) {
        if (packet instanceof ServerboundChatPacket chatPacket) {
            GameProfile profile = Slider.getPlayer(session).getProfile();
            for (Session session1 : RunningData.globalSessions) {
                session1.send(new ClientboundSystemChatPacket(Component.text("<" + profile.getName() + "> " + chatPacket.getMessage()), false));
            }
            log.info("{}: {}", profile.getName(), chatPacket.getMessage());
        }
    }
}
