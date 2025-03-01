package xyz.article.packetprocessor;

import org.cloudburstmc.math.vector.Vector3d;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.data.game.entity.Effect;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundMoveEntityPosPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundUpdateMobEffectPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerPositionPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerPosPacket;
import xyz.article.MinecraftServer;
import xyz.article.api.packetprocessor.PacketProcessor;

import java.util.Random;

public class MovePlayerPosPacketProcessor implements PacketProcessor {
    @Override
    public void process(Session session, Packet packet) {
        if (packet instanceof ServerboundMovePlayerPosPacket playerPosPacket) {
            /*
            Player player = Slider.getPlayer(session);
            if (player != null) {
                double moveX = playerPosPacket.getX() - player.getLocation().pos().getX();
                double moveY = playerPosPacket.getY() - player.getLocation().pos().getY();
                double moveZ = playerPosPacket.getZ() - player.getLocation().pos().getZ();

                player.setLocation(new Location(player.getWorld(), Vector3d.from(playerPosPacket.getX(), playerPosPacket.getY(), playerPosPacket.getZ())));

                for (Session session1 : MinecraftServer.playerSessions) {
                    if (!session1.equals(session)) {
                        session1.send(new ClientboundMoveEntityPosPacket(player.getEntityID(), moveX, moveY, moveZ, playerPosPacket.isOnGround()));
                    }
                }

                if (playerPosPacket.getY() < -400) {
                    session.send(new ClientboundUpdateMobEffectPacket(player.getEntityID(), Effect.BLINDNESS, 255, 30, true, false, false, false));
                    session.send(new ClientboundPlayerPositionPacket(playerPosPacket.getX(), 1000d, playerPosPacket.getZ(), player.getAngle().getX(), player.getAngle().getY(), new Random().nextInt()));
                }
                if (playerPosPacket.getY() > 1000) {
                    session.send(new ClientboundUpdateMobEffectPacket(player.getEntityID(), Effect.BLINDNESS, 255, 30, true, false, false, false));
                    session.send(new ClientboundPlayerPositionPacket(playerPosPacket.getX(), -400d, playerPosPacket.getZ(), player.getAngle().getX(), player.getAngle().getY(), new Random().nextInt()));
                }
            }

             */
        }
    }
}