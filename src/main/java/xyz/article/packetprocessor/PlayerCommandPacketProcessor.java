package xyz.article.packetprocessor;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.EntityMetadata;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.MetadataType;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.Pose;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.type.ByteEntityMetadata;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.type.ObjectEntityMetadata;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundSetEntityDataPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundPlayerCommandPacket;
import xyz.article.RunningData;
import xyz.article.api.Slider;
import xyz.article.api.entities.player.Player;
import xyz.article.api.packetprocessor.PacketProcessor;

public class PlayerCommandPacketProcessor implements PacketProcessor {
    @Override
    public void process(Session session, Packet packet) {
        if (packet instanceof ServerboundPlayerCommandPacket playerCommandPacket) {
            Player player = Slider.getPlayer(session);
            if (player != null) {
                switch (playerCommandPacket.getState()) {
                    case START_SNEAKING -> {
                        for (Player player1 : player.getWorld().getPlayers()) {
                            if (!player1.equals(player)) {
                                player1.sendPacket(new ClientboundSetEntityDataPacket(player.getEntityId(), new EntityMetadata[]{
                                        new ObjectEntityMetadata<>(6, MetadataType.POSE, Pose.SNEAKING)
                                }));
                            }
                        }
                    }

                    case STOP_SNEAKING -> {
                        for (Player player1 : player.getWorld().getPlayers()) {
                            if (!player1.equals(player)) {
                                player1.sendPacket(new ClientboundSetEntityDataPacket(player.getEntityId(), new EntityMetadata[]{
                                        new ObjectEntityMetadata<>(6, MetadataType.POSE, Pose.STANDING)
                                }));
                            }
                        }
                    }
                }
            }
        }
    }
}
