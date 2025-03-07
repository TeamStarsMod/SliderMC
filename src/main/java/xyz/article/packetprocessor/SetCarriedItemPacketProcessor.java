package xyz.article.packetprocessor;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.data.game.entity.EquipmentSlot;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.Equipment;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundSetEquipmentPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundSetCarriedItemPacket;
import xyz.article.api.Slider;
import xyz.article.api.entities.player.Player;
import xyz.article.api.inventory.Inventory;
import xyz.article.api.packetprocessor.PacketProcessor;

public class SetCarriedItemPacketProcessor implements PacketProcessor {
    @Override
    public void process(Session session, Packet packet) {
        if (packet instanceof ServerboundSetCarriedItemPacket setCarriedItemPacket) {
            int slot = setCarriedItemPacket.getSlot();
            Player player = Slider.getPlayer(session);
            if (player != null) {
                Inventory inventory = player.getInventory();
                ItemStack item = inventory.getItems()[slot + 36];
                player.getMainHand().setCurrentItem(item);
                player.getMainHand().setCurrentSlot(slot);

                for (Player player1 : player.getWorld().getPlayers()) {
                    if (!(player1.getSession().equals(session))) {
                        player1.sendPacket(new ClientboundSetEquipmentPacket(player.getEntityId(), new Equipment[]{new Equipment(EquipmentSlot.MAIN_HAND, player.getMainHand().getCurrentItem())}));
                        player1.sendPacket(new ClientboundSetEquipmentPacket(player.getEntityId(), new Equipment[]{new Equipment(EquipmentSlot.OFF_HAND, player.getLeftHand().getCurrentItem())}));
                    }
                }
            }
        }
    }
}
