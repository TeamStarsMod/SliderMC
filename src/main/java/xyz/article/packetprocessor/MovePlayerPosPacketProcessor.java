package xyz.article.packetprocessor;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.data.game.entity.Effect;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundMoveEntityPosPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundUpdateMobEffectPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerPositionPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerPosPacket;
import xyz.article.RunningData;
import xyz.article.api.Slider;
import xyz.article.api.entities.player.Player;
import xyz.article.api.packetprocessor.PacketProcessor;

import java.util.Random;

public class MovePlayerPosPacketProcessor implements PacketProcessor {
    @Override
    public void process(Session session, Packet packet) {
        if (packet instanceof ServerboundMovePlayerPosPacket playerPosPacket) {
            Player player = Slider.getPlayer(session);
            if (player != null) {
                double moveX = playerPosPacket.getX() - player.getX();
                double moveY = playerPosPacket.getY() - player.getY();
                double moveZ = playerPosPacket.getZ() - player.getZ();

                player.setX(playerPosPacket.getX());
                player.setY(playerPosPacket.getY());
                player.setZ(playerPosPacket.getZ());

                for (Session session1 : RunningData.sessions) {
                    if (!session1.equals(session)) {
                        session1.send(new ClientboundMoveEntityPosPacket(player.getEntityId(), moveX, moveY, moveZ, playerPosPacket.isOnGround()));
                    }
                }

                if (playerPosPacket.getY() < -400) {
                    session.send(new ClientboundUpdateMobEffectPacket(player.getEntityId(), Effect.BLINDNESS, 255, 30, true, false, false, false));
                    session.send(new ClientboundPlayerPositionPacket(playerPosPacket.getX(), 1000d, playerPosPacket.getZ(), player.getYaw(), player.getPitch(), new Random().nextInt()));
                }
                if (playerPosPacket.getY() > 1000) {
                    session.send(new ClientboundUpdateMobEffectPacket(player.getEntityId(), Effect.BLINDNESS, 255, 30, true, false, false, false));
                    session.send(new ClientboundPlayerPositionPacket(playerPosPacket.getX(), -400d, playerPosPacket.getZ(), player.getYaw(), player.getPitch(), new Random().nextInt()));
                }
            }
        }
    }
}