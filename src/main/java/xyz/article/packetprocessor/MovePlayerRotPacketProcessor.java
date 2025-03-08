package xyz.article.packetprocessor;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundMoveEntityRotPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundRotateHeadPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerRotPacket;
import xyz.article.RunningData;
import xyz.article.api.Slider;
import xyz.article.api.entities.player.Player;
import xyz.article.api.packetprocessor.PacketProcessor;

public class MovePlayerRotPacketProcessor implements PacketProcessor {
    @Override
    public void process(Session session, Packet packet) {
        if (packet instanceof ServerboundMovePlayerRotPacket rotPacket) {
            Player player = Slider.getPlayer(session);
            if (player != null) {
                float newYaw = rotPacket.getYaw();
                float newPitch = rotPacket.getPitch();

                player.setYaw(newYaw);
                player.setPitch(newPitch);

                for (Session session1 : RunningData.globalSessions) {
                    if (!session1.equals(session)) {
                        session1.send(new ClientboundRotateHeadPacket(player.getEntityId(), newYaw));
                        session1.send(new ClientboundMoveEntityRotPacket(player.getEntityId(), newYaw, newPitch, rotPacket.isOnGround()));
                    }
                }
            }
        }
    }
}