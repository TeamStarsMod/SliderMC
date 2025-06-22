package xyz.article.packetprocessor;

import net.kyori.adventure.text.Component;
import org.cloudburstmc.math.vector.Vector2i;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.nbt.NbtMap;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.ChunkSection;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.Animation;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockChangeEntry;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockEntityInfo;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockEntityType;
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
import xyz.article.MinecraftServer;

import java.util.*;

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

                    List<ClientboundBlockUpdatePacket> blockUpdatePacketList = new ArrayList<>();
                    int blockStateId = stateManager.getBlockStateId(itemId, stateProperties);

                    // 处理特殊方块
                    if (blockStateId != 0) {
                        if (blockType.endsWith("_door")) {
                            // FIXME: 问题大的很
                            chunkSections[sectionIndex].setBlock(localX, localY, localZ, blockStateId);
                            Map<String, String> upperProps = new HashMap<>(stateProperties);
                            upperProps.put("half", "upper");
                            int upperStateId = stateManager.getBlockStateId(itemId, upperProps);
                            if (localY + 1 > 15) {
                                chunkSections[sectionIndex].setBlock(localX, localY + 1, localZ, upperStateId);
                            } else {
                                int sectionIndex1 = sectionIndex + 1;
                                if (sectionIndex1 > 24) {
                                    return;
                                }
                                chunkSections[sectionIndex1].setBlock(localX, 0, localZ, upperStateId);
                            }
                            blockUpdatePacketList.add(new ClientboundBlockUpdatePacket(new BlockChangeEntry(Vector3i.from(blockX, blockY + 1, blockZ), upperStateId)));
                        } else if (blockType.equalsIgnoreCase("chest")) {
                            BlockEntityInfo blockEntityInfo = new BlockEntityInfo(blockX, blockY, blockZ, BlockEntityType.CHEST, NbtMap.EMPTY);
                            // TODO: 完成逻辑
                        }
                    }

                    blockUpdatePacketList.add(new ClientboundBlockUpdatePacket(new BlockChangeEntry(Vector3i.from(blockX, blockY, blockZ), blockStateId)));

                    chunkSections[sectionIndex].setBlock(localX, localY, localZ, blockStateId);

                    // 触发方块更新系统
                    if (player != null && player.getWorld() != null) {
                        MinecraftServer.blockUpdateManager.handleBlockUpdate(
                            player.getWorld(), 
                            Vector3i.from(blockX, blockY, blockZ), 
                            0, // 旧方块状态（空气）
                            blockStateId // 新方块状态
                        );
                    }

                    // 发送区块更新包
                    session.send(new ClientboundBlockChangedAckPacket(useItemOnPacket.getSequence()));
                    for (Player player1 : player.getWorld().getPlayers()) {
                        for (ClientboundBlockUpdatePacket blockUpdatePacket : blockUpdatePacketList) {
                            if (player1.isWithinViewDistance(blockUpdatePacket.getEntry().getPosition().getX(), blockUpdatePacket.getEntry().getPosition().getY(), blockUpdatePacket.getEntry().getPosition().getZ())) {
                                player1.sendPacket(blockUpdatePacket);
                            }
                        }
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
