package xyz.article.packetprocessor;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.data.game.entity.Effect;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundMoveEntityPosRotPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundRotateHeadPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundUpdateMobEffectPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerPositionPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerPosRotPacket;
import xyz.article.RunningData;
import xyz.article.api.Slider;
import xyz.article.api.entities.player.Player;
import xyz.article.api.packetprocessor.PacketProcessor;

import java.util.Random;

public class MovePlayerPosRotPacketProcessor implements PacketProcessor {
    @Override
    public void process(Session session, Packet packet) {
        if (packet instanceof ServerboundMovePlayerPosRotPacket posRotPacket) {
            Player player = Slider.getPlayer(session);
            if (player != null) {
                double moveX = posRotPacket.getX() - player.getX();
                double moveY = posRotPacket.getY() - player.getY();
                double moveZ = posRotPacket.getZ() - player.getZ();

                float newYaw = posRotPacket.getYaw();
                float newPitch = posRotPacket.getPitch();

                player.setX(posRotPacket.getX());
                player.setY(posRotPacket.getY());
                player.setZ(posRotPacket.getZ());

                player.setYaw(newYaw);
                player.setPitch(newPitch);

                for (Session session1 : RunningData.globalSessions) {
                    if (!session1.equals(session)) {
                        session1.send(new ClientboundMoveEntityPosRotPacket(player.getEntityId(), moveX, moveY, moveZ, posRotPacket.getYaw(), posRotPacket.getPitch(), posRotPacket.isOnGround()));
                        session1.send(new ClientboundRotateHeadPacket(player.getEntityId(), newYaw));
                    }
                }

                if (posRotPacket.getY() < -400) {
                    session.send(new ClientboundUpdateMobEffectPacket(player.getEntityId(), Effect.BLINDNESS, 255, 30, true, false, false, false));
                    session.send(new ClientboundPlayerPositionPacket(posRotPacket.getX(), 1000d, posRotPacket.getZ(), player.getYaw(), player.getPitch(), new Random().nextInt()));
                }
                if (posRotPacket.getY() > 1000) {
                    session.send(new ClientboundUpdateMobEffectPacket(player.getEntityId(), Effect.BLINDNESS, 255, 30, true, false, false, false));
                    session.send(new ClientboundPlayerPositionPacket(posRotPacket.getX(), -400d, posRotPacket.getZ(), player.getYaw(), player.getPitch(), new Random().nextInt()));
                }
            }
        }
    }
}