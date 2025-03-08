package xyz.article.packetprocessor;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundMoveEntityPosRotPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundRotateHeadPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerPosRotPacket;
import xyz.article.RunningData;
import xyz.article.api.Slider;
import xyz.article.api.entities.player.Player;
import xyz.article.api.packetprocessor.PacketProcessor;

public class MovePlayerPosRotPacketProcessor implements PacketProcessor {
    @Override
    public void process(Session session, Packet packet) {
        if (packet instanceof ServerboundMovePlayerPosRotPacket posRotPacket) {
            Player player = Slider.getPlayer(session);
            if (player != null) {
                double moveX = posRotPacket.getX() - player.getPosition().getX();
                double moveY = posRotPacket.getY() - player.getPosition().getY();
                double moveZ = posRotPacket.getZ() - player.getPosition().getZ();

                float newYaw = posRotPacket.getYaw();
                float newPitch = posRotPacket.getPitch();

                player.updatePosition(posRotPacket.getX(), posRotPacket.getY(), posRotPacket.getZ(), newYaw, newPitch, posRotPacket.isOnGround());

                for (Session session1 : RunningData.globalSessions) {
                    if (!session1.equals(session)) {
                        session1.send(new ClientboundMoveEntityPosRotPacket(player.getEntityId(), moveX, moveY, moveZ, posRotPacket.getYaw(), posRotPacket.getPitch(), posRotPacket.isOnGround()));
                        session1.send(new ClientboundRotateHeadPacket(player.getEntityId(), newYaw));
                    }
                }

                /*if (posRotPacket.getY() < -400) {
                    session.send(new ClientboundUpdateMobEffectPacket(player.getEntityId(), Effect.BLINDNESS, 255, 30, true, false, false, false));
                    session.send(new ClientboundPlayerPositionPacket(posRotPacket.getX(), 1000d, posRotPacket.getZ(), player.getYaw(), player.getPitch(), new Random().nextInt()));
                    for (Player player1 : player.getWorld().getPlayers()) {
                        if (!player1.equals(player)) {
                            player1.sendPacket(new ClientboundTeleportEntityPacket(player.getEntityId(), posRotPacket.getX(), 1000, posRotPacket.getZ(), player.getYaw(), player.getPitch(), posRotPacket.isOnGround()));
                        }
                    }
                }
                if (posRotPacket.getY() > 1000) {
                    session.send(new ClientboundUpdateMobEffectPacket(player.getEntityId(), Effect.BLINDNESS, 255, 30, true, false, false, false));
                    session.send(new ClientboundPlayerPositionPacket(posRotPacket.getX(), -400d, posRotPacket.getZ(), player.getYaw(), player.getPitch(), new Random().nextInt()));
                    for (Player player1 : player.getWorld().getPlayers()) {
                        if (!player1.equals(player)) {
                            player1.sendPacket(new ClientboundTeleportEntityPacket(player.getEntityId(), posRotPacket.getX(), -400, posRotPacket.getZ(), player.getYaw(), player.getPitch(), posRotPacket.isOnGround()));
                        }
                    }
                }*/
            }
        }
    }
}