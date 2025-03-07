package xyz.article.packetprocessor;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.Animation;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundAnimatePacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundSwingPacket;
import xyz.article.api.Slider;
import xyz.article.api.entities.player.Player;
import xyz.article.api.packetprocessor.PacketProcessor;

public class SwingPacketProcessor implements PacketProcessor {
    @Override
    public void process(Session session, Packet packet) {
        if (packet instanceof ServerboundSwingPacket swingPacket) {
            Player player = Slider.getPlayer(session);
            if (player != null) {
                for (Player player1 : player.getWorld().getPlayers()) {
                    if (!player1.equals(player)) {
                        player1.sendPacket(new ClientboundAnimatePacket(player.getEntityId(), Animation.SWING_ARM));
                    }
                }
            }
        }
    }
}
