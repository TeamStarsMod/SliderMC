package xyz.article.packetprocessor;

import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.ChunkSection;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.Animation;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundAnimatePacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundBlockChangedAckPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundPlayerActionPacket;
import xyz.article.api.Slider;
import xyz.article.api.entities.player.Player;
import xyz.article.api.packetprocessor.PacketProcessor;
import xyz.article.api.world.World;
import xyz.article.api.world.block.BlockPos;
import xyz.article.api.world.chunk.ChunkData;
import xyz.article.api.world.chunk.ChunkPos;

public class PlayerActionPacketProcessor implements PacketProcessor {

    @Override
    public void process(Session session, Packet packet) {
        if (packet instanceof ServerboundPlayerActionPacket actionPacket) {
            switch (actionPacket.getAction()) {
                case START_DIGGING -> {
                    Player player = Slider.getPlayer(session);
                    if (player != null && player.getGameMode().equals(GameMode.CREATIVE)) {
                        // In Creative Mode
                        World world = player.getWorld();
                        BlockPos blockPos = new BlockPos(world, actionPacket.getPosition());
                        ChunkPos chunkPos = Slider.getChunkPos(blockPos);
                        int chunkSectionIndex = Slider.getChunkSectionIndex(blockPos.pos().getY());
                        ChunkData chunk = world.getChunkDataMap().get(chunkPos.pos());
                        ChunkSection[] chunkSections = chunk.getChunkSections();
                        ChunkSection targetSection = chunkSections[chunkSectionIndex];
                        Vector3i inChunkSectionLocation = Slider.getInChunkSectionLocation(blockPos.pos().getX(), blockPos.pos().getY(), blockPos.pos().getZ(), chunkSectionIndex);
                        targetSection.setBlock(inChunkSectionLocation.getX(), inChunkSectionLocation.getY(), inChunkSectionLocation.getZ(), 0);
                        session.send(new ClientboundBlockChangedAckPacket(actionPacket.getSequence()));
                        for (Player player1 : player.getWorld().getPlayers()) {
                            player1.sendPacket(chunk.getPacket());
                            if (!(player1.getSession().equals(session))) {
                                player1.sendPacket(new ClientboundAnimatePacket(player.getEntityId(), Animation.SWING_ARM));
                            }
                        }
                    }
                }

                case FINISH_DIGGING -> {
                    /*Player player = Slider.getPlayer(session);
                    if (player != null) {
                        World world = player.getWorld();
                        BlockPos blockPos = new BlockPos(world, actionPacket.getPosition());
                        ChunkPos chunkPos = Slider.getChunkPos(blockPos);
                        int chunkSectionIndex = Slider.getChunkSectionIndex(blockPos.pos().getY());
                        ChunkData chunk = world.getChunkDataMap().get(chunkPos.pos());
                        ChunkSection[] chunkSections = chunk.getChunkSections();
                        ChunkSection targetSection = chunkSections[chunkSectionIndex];
                        Vector3i inChunkSectionLocation = Slider.getInChunkSectionLocation(blockPos.pos().getX(), blockPos.pos().getY(), blockPos.pos().getZ(), chunkSectionIndex);
                        int id = new Random().nextInt();
                        int itemId = BlockItemMap.getItemID(targetSection.getBlock(inChunkSectionLocation.getX(), inChunkSectionLocation.getY(), inChunkSectionLocation.getZ()));
                        if (itemId == 0) {
                            player.sendMessage(Component.text("未能获取到此方块的掉落物！"));
                        }
                        ItemEntity entity = new ItemEntity(new Location(player.getWorld(), Vector3d.from(blockPos.pos().getX(), blockPos.pos().getY(), blockPos.pos().getZ())), new Random().nextInt(), new ItemStack(itemId), System.currentTimeMillis());
                        targetSection.setBlock(inChunkSectionLocation.getX(), inChunkSectionLocation.getY(), inChunkSectionLocation.getZ(), 0);
                        session.send(new ClientboundBlockChangedAckPacket(actionPacket.getSequence()));
                        for (Session session1 : MinecraftServer.playerSessions) {
                            session1.send(chunk.getChunkPacket());
                        }
                        for (Session session1 : world.getSessions()) {
                            session1.send(new ClientboundAddEntityPacket(id, UUID.randomUUID(), EntityType.ITEM, blockPos.pos().getX(), blockPos.pos().getY(), blockPos.pos().getZ(), 0, 0, 0));
                            session1.send(new ClientboundSetEntityDataPacket(id, new EntityMetadata[] {
                                    new ObjectEntityMetadata<>(8, MetadataType.ITEM, entity.getItemStack())
                            }));
                            world.getEntities().add(entity);
                        }
                    }*/
                }

                case DROP_ITEM -> {
                    /*Player player = Slider.getPlayer(session);
                    if (player != null) {
                        ItemStack itemStack = player.getMainHand().getCurrentItem();
                        if (itemStack != null) {
                            Location location = player.getLocation();
                            int id = new Random().nextInt();
                            UUID uuid = UUID.randomUUID();
                            double x = location.pos().getX();
                            double y = location.pos().getY();
                            double z = location.pos().getZ();

                            int slot = player.getMainHand().getCurrentSlot() + 36;
                            int amount = itemStack.getAmount();
                            if (amount > 1) {
                                ItemStack itemStack1 = new ItemStack(player.getInventory().getItem(slot).getId(), amount - 1, player.getInventory().getItem(slot).getDataComponents());
                                player.getInventory().setItem(slot, itemStack1);
                            } else {
                                player.getInventory().setItem(slot, null);
                            }
                            player.sendPacket(new ClientboundContainerSetContentPacket(player.getInventory().getContainerId(), 0, player.getInventory().getItems(), player.getInventory().getDragging()));
                            // 发送生成物品实体的数据包
                            ItemEntity entity = new ItemEntity(new Location(player.getWorld(), Vector3d.from(x, y, z)), id, itemStack, System.currentTimeMillis());
                            player.getWorld().getEntities().add(entity);
                            session.send(new ClientboundAddEntityPacket(id, uuid, EntityType.ITEM, x, y, z, 0, 0, 0));
                            session.send(new ClientboundSetEntityDataPacket(id, new EntityMetadata[] {
                                    new ObjectEntityMetadata<>(8, MetadataType.ITEM, itemStack)
                            }));
                        }
                    }*/
                }

            }
        }
    }
}
