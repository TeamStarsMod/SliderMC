package xyz.article.packetprocessor;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.cloudburstmc.math.vector.Vector2i;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.ChunkSection;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.Animation;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockChangeEntry;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundSystemChatPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundAnimatePacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundBlockChangedAckPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundBlockUpdatePacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundUseItemOnPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.article.api.Slider;
import xyz.article.api.entities.player.Player;
import xyz.article.api.packetprocessor.PacketProcessor;
import xyz.article.api.world.block.BlockItemMap;
import xyz.article.api.world.chunk.ChunkData;

import java.util.Objects;

public class UseItemOnPacketProcessor implements PacketProcessor {
    private static final Logger log = LoggerFactory.getLogger(UseItemOnPacketProcessor.class);

    @Override
    public void process(Session session, Packet packet) {
        if (packet instanceof ServerboundUseItemOnPacket useItemOnPacket) {
            Player player = Slider.getPlayer(session);
            Vector3i blockPos = useItemOnPacket.getPosition();
            int blockX = blockPos.getX();
            int blockY = blockPos.getY();
            int blockZ = blockPos.getZ();
            if (blockY < -63 || blockY > 319) {
                session.send(new ClientboundBlockChangedAckPacket(useItemOnPacket.getSequence()));
                session.send(new ClientboundSystemChatPacket(Component.text("超出世界y坐标限制"), false));
                return;
            }

            // 根据玩家点击的面对坐标进行修正
            switch (useItemOnPacket.getFace()) {
                case UP:
                    blockY++;
                    break;
                case DOWN:
                    blockY--;
                    break;
                case NORTH:
                    blockZ--;
                    break;
                case SOUTH:
                    blockZ++;
                    break;
                case WEST:
                    blockX--;
                    break;
                case EAST:
                    blockX++;
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected direction: " + useItemOnPacket.getFace());
            }

            if (player != null) {
                int playerBlockX = (int) Math.floor(player.getX());
                int playerBlockY = (int) Math.floor(player.getY());
                int playerBlockZ = (int) Math.floor(player.getZ());

                if ((blockX == playerBlockX && blockY == playerBlockY && blockZ == playerBlockZ) || (blockX == playerBlockX && blockY == playerBlockY + 1 && blockZ == playerBlockZ)) {
                    return;
                }
            }

            int chunkX = blockX >> 4; // >> 4 == / 16
            int chunkZ = blockZ >> 4;

            int sectionHeight = 16; // 每个section(子区块)的高度
            int worldBottom = -64; // 世界底部的Y坐标
            int sectionIndex = (blockY - worldBottom) / sectionHeight;

            ChunkData chunkData = null;
            if (player != null) {
                chunkData = player.getWorld().getChunkDataMap().get(Vector2i.from(chunkX, chunkZ));
            }
            if (chunkData != null) {
                ChunkSection[] chunkSections = chunkData.getChunkSections();

                if (sectionIndex < chunkSections.length) {
                    int localX = blockX & 15; // & 15 == % 16
                    int localY = blockY - (sectionIndex * sectionHeight + worldBottom);
                    int localZ = blockZ & 15;

                    // 设置新方块
                    int id = 0;
                    ItemStack item;
                    if (player.getMainHand().getCurrentItem() != null) {
                        item = player.getMainHand().getCurrentItem();
                    } else {
                        item = player.getLeftHand().getCurrentItem();
                    }
                    if (item != null) {
                        id = item.getId();
                    }
                    int blockID = BlockItemMap.getBlockID(id);
                    if (blockID == 0) {
                        session.send(new ClientboundBlockChangedAckPacket(useItemOnPacket.getSequence()));
                        session.send(new ClientboundSystemChatPacket(Component.text("没有获取到你手中方块物品对应的方块！").color(NamedTextColor.RED), false));
                        return;
                    }
                    chunkSections[sectionIndex].setBlock(localX, localY, localZ, blockID);

                    // 发送区块更新包
                    session.send(new ClientboundBlockChangedAckPacket(useItemOnPacket.getSequence()));
                    for (Player player1 : player.getWorld().getPlayers()) {
                        player1.sendPacket(new ClientboundBlockUpdatePacket(new BlockChangeEntry(Vector3i.from(blockX, blockY, blockZ), blockID)));
                        if (!(player1.getSession().equals(session))) {
                            player1.sendPacket(new ClientboundAnimatePacket(Objects.requireNonNull(Slider.getPlayer(session)).getEntityId(), Animation.SWING_ARM));
                        }
                    }
                } else {
                    log.error("Invalid section index: {}", sectionIndex);
                }
            } else {
                log.error("Chunk Data (x{}, z{}) is null!", chunkX, chunkZ);
            }
        }
    }
}
