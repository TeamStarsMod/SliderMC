package xyz.article.packetprocessor;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundPlayerAbilitiesPacket;
import xyz.article.api.Slider;
import xyz.article.api.entities.player.Player;
import xyz.article.api.packetprocessor.PacketProcessor;

public class PlayerAbilitiesPacketProcessor implements PacketProcessor {
    @Override
    public void process(Session session, Packet packet) {
        if (packet instanceof ServerboundPlayerAbilitiesPacket playerAbilitiesPacket) {
            Player player = Slider.getPlayer(session);
            if (player != null) {
                player.getPlayerAbilities().setFlying(playerAbilitiesPacket.isFlying());
            }
        }
    }
}
