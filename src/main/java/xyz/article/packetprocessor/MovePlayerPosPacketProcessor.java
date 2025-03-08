package xyz.article.packetprocessor;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundMoveEntityPosPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerPosPacket;
import xyz.article.RunningData;
import xyz.article.api.Slider;
import xyz.article.api.entities.player.Player;
import xyz.article.api.packetprocessor.PacketProcessor;

public class MovePlayerPosPacketProcessor implements PacketProcessor {
    @Override
    public void process(Session session, Packet packet) {
        if (packet instanceof ServerboundMovePlayerPosPacket playerPosPacket) {
            Player player = Slider.getPlayer(session);
            if (player != null) {
                double moveX = playerPosPacket.getX() - player.getPosition().getX();
                double moveY = playerPosPacket.getY() - player.getPosition().getY();
                double moveZ = playerPosPacket.getZ() - player.getPosition().getZ();

                player.updatePosition(playerPosPacket.getX(), playerPosPacket.getY(), playerPosPacket.getZ(), player.getYaw(), player.getPitch(), playerPosPacket.isOnGround());

                for (Session session1 : RunningData.globalSessions) {
                    if (!session1.equals(session)) {
                        session1.send(new ClientboundMoveEntityPosPacket(player.getEntityId(), moveX, moveY, moveZ, playerPosPacket.isOnGround()));
                    }
                }

                /*if (playerPosPacket.getY() < -400) {
                    session.send(new ClientboundUpdateMobEffectPacket(player.getEntityId(), Effect.BLINDNESS, 255, 30, true, false, false, false));
                    session.send(new ClientboundPlayerPositionPacket(playerPosPacket.getX(), 1000d, playerPosPacket.getZ(), player.getYaw(), player.getPitch(), new Random().nextInt()));
                    for (Player player1 : player.getWorld().getPlayers()) {
                        if (!player1.equals(player)) {
                            player1.sendPacket(new ClientboundTeleportEntityPacket(player.getEntityId(), playerPosPacket.getX(), 1000, playerPosPacket.getZ(), player.getYaw(), player.getPitch(), playerPosPacket.isOnGround()));
                        }
                    }
                }
                if (playerPosPacket.getY() > 1000) {
                    session.send(new ClientboundUpdateMobEffectPacket(player.getEntityId(), Effect.BLINDNESS, 255, 30, true, false, false, false));
                    session.send(new ClientboundPlayerPositionPacket(playerPosPacket.getX(), -400d, playerPosPacket.getZ(), player.getYaw(), player.getPitch(), new Random().nextInt()));
                    for (Player player1 : player.getWorld().getPlayers()) {
                        if (!player1.equals(player)) {
                            player1.sendPacket(new ClientboundTeleportEntityPacket(player.getEntityId(), playerPosPacket.getX(), -400, playerPosPacket.getZ(), player.getYaw(), player.getPitch(), playerPosPacket.isOnGround()));
                        }
                    }
                }*/
            }
        }
    }
}