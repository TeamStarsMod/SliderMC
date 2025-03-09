package xyz.article.packetprocessor;

import net.kyori.adventure.text.Component;
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
import xyz.article.api.world.block.BlockFace;
import xyz.article.api.world.block.blockstate.BlockStateManager;
import xyz.article.api.world.block.placement.BlockPlacementCalculator;
import xyz.article.api.world.chunk.ChunkData;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UseItemOnPacketProcessor implements PacketProcessor {
    private static final Logger log = LoggerFactory.getLogger(UseItemOnPacketProcessor.class);
    private final BlockStateManager stateManager;

    public UseItemOnPacketProcessor(BlockStateManager manager) {
        this.stateManager = manager;
    }

    @Override
    public void process(Session session, Packet packet) {
        if (packet instanceof ServerboundUseItemOnPacket useItemOnPacket) {
            Player player = Slider.getPlayer(session);
            Vector3i blockPos = useItemOnPacket.getPosition();
            int blockX = blockPos.getX();
            int blockY = blockPos.getY();
            int blockZ = blockPos.getZ();
            if (blockY < -64 || blockY > 320) {
                session.send(new ClientboundBlockChangedAckPacket(useItemOnPacket.getSequence()));
                session.send(new ClientboundSystemChatPacket(Component.text("超出世界y坐标限制"), false));
                return;
            }

            // 根据玩家点击的面对坐标进行修正
            BlockFace face = switch (useItemOnPacket.getFace()) {
                case UP -> {
                    blockY++;
                    yield BlockFace.UP;
                }
                case DOWN -> {
                    blockY--;
                    yield BlockFace.DOWN;
                }
                case NORTH -> {
                    blockZ--;
                    yield BlockFace.NORTH;
                }
                case SOUTH -> {
                    blockZ++;
                    yield BlockFace.SOUTH;
                }
                case WEST -> {
                    blockX--;
                    yield BlockFace.WEST;
                }
                case EAST -> {
                    blockX++;
                    yield BlockFace.EAST;
                }
            };

            if (player != null) {
                int playerBlockX = (int) Math.floor(player.getPosition().getX());
                int playerBlockY = (int) Math.floor(player.getPosition().getY());
                int playerBlockZ = (int) Math.floor(player.getPosition().getZ());

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

                    if (chunkSections[sectionIndex].getBlock(localX, localY, localZ) != 0) {
                        return;
                    }

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
                    // 获取物品对应的方块类型
                    int itemId = item != null ? item.getId() : 0;
                    String blockType = stateManager.getBlockIdFromItemId(itemId);
                    Map<String, String> stateProperties = BlockPlacementCalculator.calculateProperties(
                            player,
                            face,
                            Vector3i.from(blockX, blockY, blockZ),
                            blockType
                    );

                    int blockStateId = stateManager.getBlockStateId(itemId, stateProperties);

                    // 处理特殊方块
                    if (blockStateId != 0 && blockType.endsWith("_door")) {
                        System.out.println("door");
                        chunkSections[sectionIndex].setBlock(localX, localY, localZ, blockStateId);
                        Map<String, String> upperProps = new HashMap<>(stateProperties);
                        upperProps.put("half", "upper");
                        int upperStateId = stateManager.getBlockStateId(itemId, upperProps);
                        chunkSections[sectionIndex+1].setBlock(localX, localY+1, localZ, upperStateId);
                    }

                    chunkSections[sectionIndex].setBlock(localX, localY, localZ, blockStateId);

                    // 发送区块更新包
                    session.send(new ClientboundBlockChangedAckPacket(useItemOnPacket.getSequence()));
                    for (Player player1 : player.getWorld().getPlayers()) {
                        player1.sendPacket(new ClientboundBlockUpdatePacket(new BlockChangeEntry(Vector3i.from(blockX, blockY, blockZ), blockStateId)));
                        if (!(player1.getSession().equals(session))) {
                            player1.sendPacket(new ClientboundAnimatePacket(Objects.requireNonNull(Slider.getPlayer(session)).getEntityId(), Animation.SWING_ARM));
                        }
                    }

                    chunkData.updateHeightMap(blockX & 15, blockZ & 15);
                } else {
                    log.error("Invalid section index: {}", sectionIndex);
                }
            } else {
                log.error("Chunk Data (x{}, z{}) is null!", chunkX, chunkZ);
            }
        }
    }
}
