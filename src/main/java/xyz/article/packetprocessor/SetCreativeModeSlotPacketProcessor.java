package xyz.article.packetprocessor;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.data.game.entity.EquipmentSlot;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.Equipment;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundSetEquipmentPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundContainerSetContentPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.inventory.ServerboundSetCreativeModeSlotPacket;
import xyz.article.api.Slider;
import xyz.article.api.entities.player.Player;
import xyz.article.api.inventory.Inventory;
import xyz.article.api.packetprocessor.PacketProcessor;

public class SetCreativeModeSlotPacketProcessor implements PacketProcessor {
    @Override
    public void process(Session session, Packet packet) {
        if (packet instanceof ServerboundSetCreativeModeSlotPacket creativeModeSlotPacket) {
            ItemStack itemStack = creativeModeSlotPacket.getClickedItem();
            int slot = creativeModeSlotPacket.getSlot();
            Player player = Slider.getPlayer(session);
            if (itemStack == null) {
                if (player != null) {
                    ItemStack itemStack1 = player.getInventory().getItems()[slot];
                    if (itemStack1 != null) {
                        player.getInventory().setItem(slot, null);
                        player.getInventory().setDraggingItem(itemStack1);
                        session.send(new ClientboundContainerSetContentPacket(player.getInventory().getContainerId(), 0, player.getInventory().getItems(), itemStack1));
                    }
                    return;
                }
            }

            if (player != null) {
                Inventory inventory = player.getInventory();
                inventory.setItem(slot, itemStack);
                int slot1 = player.getMainHand().getCurrentSlot();
                player.getMainHand().setCurrentItem(inventory.getItems()[slot1 + 36]);
                session.send(new ClientboundContainerSetContentPacket(player.getInventory().getContainerId(), 0, player.getInventory().getItems(), null));

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
