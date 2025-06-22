package xyz.article.api.world.block;

import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockChangeEntry;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundBlockUpdatePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.article.api.Slider;
import xyz.article.api.entities.player.Player;
import xyz.article.api.world.World;
import xyz.article.api.world.chunk.ChunkData;
import xyz.article.api.world.chunk.ChunkPos;
import xyz.article.api.world.block.handlers.*;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 方块更新管理器
 * 负责处理方块状态变化时的连锁反应
 */
public class BlockUpdateManager {
    private static final Logger log = LoggerFactory.getLogger(BlockUpdateManager.class);
    
    // 更新队列，用于处理延迟更新
    private final Queue<BlockUpdateTask> updateQueue = new ConcurrentLinkedQueue<>();
    
    // 方块更新处理器映射
    private final Map<String, BlockUpdateHandler> updateHandlers = new HashMap<>();
    
    public BlockUpdateManager() {
        registerDefaultHandlers();
    }
    
    /**
     * 注册默认的方块更新处理器
     */
    private void registerDefaultHandlers() {
        // 红石相关
        registerHandler("minecraft:redstone_wire", new RedstoneWireHandler());
        registerHandler("minecraft:redstone_torch", new RedstoneTorchHandler());
        registerHandler("minecraft:redstone_block", new RedstoneBlockHandler());
        registerHandler("minecraft:lever", new LeverHandler());
        registerHandler("minecraft:stone_button", new ButtonHandler());
        registerHandler("minecraft:oak_button", new ButtonHandler());
        registerHandler("minecraft:birch_button", new ButtonHandler());
        registerHandler("minecraft:spruce_button", new ButtonHandler());
        registerHandler("minecraft:jungle_button", new ButtonHandler());
        registerHandler("minecraft:acacia_button", new ButtonHandler());
        registerHandler("minecraft:dark_oak_button", new ButtonHandler());
        registerHandler("minecraft:crimson_button", new ButtonHandler());
        registerHandler("minecraft:warped_button", new ButtonHandler());
        registerHandler("minecraft:polished_blackstone_button", new ButtonHandler());
        registerHandler("minecraft:stone_pressure_plate", new PressurePlateHandler());
        registerHandler("minecraft:oak_pressure_plate", new PressurePlateHandler());
        registerHandler("minecraft:birch_pressure_plate", new PressurePlateHandler());
        registerHandler("minecraft:spruce_pressure_plate", new PressurePlateHandler());
        registerHandler("minecraft:jungle_pressure_plate", new PressurePlateHandler());
        registerHandler("minecraft:acacia_pressure_plate", new PressurePlateHandler());
        registerHandler("minecraft:dark_oak_pressure_plate", new PressurePlateHandler());
        registerHandler("minecraft:crimson_pressure_plate", new PressurePlateHandler());
        registerHandler("minecraft:warped_pressure_plate", new PressurePlateHandler());
        registerHandler("minecraft:light_weighted_pressure_plate", new PressurePlateHandler());
        registerHandler("minecraft:heavy_weighted_pressure_plate", new PressurePlateHandler());
        registerHandler("minecraft:redstone_repeater", new RepeaterHandler());
        registerHandler("minecraft:redstone_comparator", new ComparatorHandler());
        
        // 活塞相关
        registerHandler("minecraft:piston", new PistonHandler());
        registerHandler("minecraft:sticky_piston", new StickyPistonHandler());
        registerHandler("minecraft:observer", new ObserverHandler());
        
        // 门相关
        registerHandler("minecraft:oak_door", new DoorHandler());
        registerHandler("minecraft:birch_door", new DoorHandler());
        registerHandler("minecraft:spruce_door", new DoorHandler());
        registerHandler("minecraft:jungle_door", new DoorHandler());
        registerHandler("minecraft:acacia_door", new DoorHandler());
        registerHandler("minecraft:dark_oak_door", new DoorHandler());
        registerHandler("minecraft:crimson_door", new DoorHandler());
        registerHandler("minecraft:warped_door", new DoorHandler());
        registerHandler("minecraft:iron_door", new DoorHandler());
        
        // 物理方块
        registerHandler("minecraft:sand", new GravityBlockHandler());
        registerHandler("minecraft:red_sand", new GravityBlockHandler());
        registerHandler("minecraft:gravel", new GravityBlockHandler());
        registerHandler("minecraft:anvil", new GravityBlockHandler());
        registerHandler("minecraft:white_concrete_powder", new GravityBlockHandler());
        registerHandler("minecraft:orange_concrete_powder", new GravityBlockHandler());
        registerHandler("minecraft:magenta_concrete_powder", new GravityBlockHandler());
        registerHandler("minecraft:light_blue_concrete_powder", new GravityBlockHandler());
        registerHandler("minecraft:yellow_concrete_powder", new GravityBlockHandler());
        registerHandler("minecraft:lime_concrete_powder", new GravityBlockHandler());
        registerHandler("minecraft:pink_concrete_powder", new GravityBlockHandler());
        registerHandler("minecraft:gray_concrete_powder", new GravityBlockHandler());
        registerHandler("minecraft:light_gray_concrete_powder", new GravityBlockHandler());
        registerHandler("minecraft:cyan_concrete_powder", new GravityBlockHandler());
        registerHandler("minecraft:purple_concrete_powder", new GravityBlockHandler());
        registerHandler("minecraft:blue_concrete_powder", new GravityBlockHandler());
        registerHandler("minecraft:brown_concrete_powder", new GravityBlockHandler());
        registerHandler("minecraft:green_concrete_powder", new GravityBlockHandler());
        registerHandler("minecraft:red_concrete_powder", new GravityBlockHandler());
        registerHandler("minecraft:black_concrete_powder", new GravityBlockHandler());
        
        // 连接方块
        registerHandler("minecraft:glass_pane", new ConnectableBlockHandler());
        registerHandler("minecraft:white_stained_glass_pane", new ConnectableBlockHandler());
        registerHandler("minecraft:orange_stained_glass_pane", new ConnectableBlockHandler());
        registerHandler("minecraft:magenta_stained_glass_pane", new ConnectableBlockHandler());
        registerHandler("minecraft:light_blue_stained_glass_pane", new ConnectableBlockHandler());
        registerHandler("minecraft:yellow_stained_glass_pane", new ConnectableBlockHandler());
        registerHandler("minecraft:lime_stained_glass_pane", new ConnectableBlockHandler());
        registerHandler("minecraft:pink_stained_glass_pane", new ConnectableBlockHandler());
        registerHandler("minecraft:gray_stained_glass_pane", new ConnectableBlockHandler());
        registerHandler("minecraft:light_gray_stained_glass_pane", new ConnectableBlockHandler());
        registerHandler("minecraft:cyan_stained_glass_pane", new ConnectableBlockHandler());
        registerHandler("minecraft:purple_stained_glass_pane", new ConnectableBlockHandler());
        registerHandler("minecraft:blue_stained_glass_pane", new ConnectableBlockHandler());
        registerHandler("minecraft:brown_stained_glass_pane", new ConnectableBlockHandler());
        registerHandler("minecraft:green_stained_glass_pane", new ConnectableBlockHandler());
        registerHandler("minecraft:red_stained_glass_pane", new ConnectableBlockHandler());
        registerHandler("minecraft:black_stained_glass_pane", new ConnectableBlockHandler());
        registerHandler("minecraft:iron_bars", new ConnectableBlockHandler());
        registerHandler("minecraft:oak_fence", new ConnectableBlockHandler());
        registerHandler("minecraft:birch_fence", new ConnectableBlockHandler());
        registerHandler("minecraft:spruce_fence", new ConnectableBlockHandler());
        registerHandler("minecraft:jungle_fence", new ConnectableBlockHandler());
        registerHandler("minecraft:acacia_fence", new ConnectableBlockHandler());
        registerHandler("minecraft:dark_oak_fence", new ConnectableBlockHandler());
        registerHandler("minecraft:crimson_fence", new ConnectableBlockHandler());
        registerHandler("minecraft:warped_fence", new ConnectableBlockHandler());
        registerHandler("minecraft:nether_brick_wall", new ConnectableBlockHandler());
        registerHandler("minecraft:cobblestone_wall", new ConnectableBlockHandler());
        registerHandler("minecraft:mossy_cobblestone_wall", new ConnectableBlockHandler());
        registerHandler("minecraft:granite_wall", new ConnectableBlockHandler());
        registerHandler("minecraft:diorite_wall", new ConnectableBlockHandler());
        registerHandler("minecraft:andesite_wall", new ConnectableBlockHandler());
        registerHandler("minecraft:deepslate_wall", new ConnectableBlockHandler());
        registerHandler("minecraft:polished_deepslate_wall", new ConnectableBlockHandler());
        registerHandler("minecraft:blackstone_wall", new ConnectableBlockHandler());
        registerHandler("minecraft:polished_blackstone_wall", new ConnectableBlockHandler());
        registerHandler("minecraft:polished_blackstone_brick_wall", new ConnectableBlockHandler());
        registerHandler("minecraft:end_stone_brick_wall", new ConnectableBlockHandler());
        registerHandler("minecraft:sandstone_wall", new ConnectableBlockHandler());
        registerHandler("minecraft:red_sandstone_wall", new ConnectableBlockHandler());
        registerHandler("minecraft:stone_brick_wall", new ConnectableBlockHandler());
        registerHandler("minecraft:mossy_stone_brick_wall", new ConnectableBlockHandler());
        registerHandler("minecraft:prismarine_wall", new ConnectableBlockHandler());
        registerHandler("minecraft:red_nether_brick_wall", new ConnectableBlockHandler());
        registerHandler("minecraft:quartz_wall", new ConnectableBlockHandler());
    }
    
    /**
     * 注册方块更新处理器
     */
    public void registerHandler(String blockType, BlockUpdateHandler handler) {
        updateHandlers.put(blockType, handler);
    }
    
    /**
     * 处理方块更新
     */
    public void handleBlockUpdate(World world, Vector3i position, int oldBlockState, int newBlockState) {
        // 获取旧方块和新方块的类型
        String oldBlockType = getBlockTypeFromState(oldBlockState);
        String newBlockType = getBlockTypeFromState(newBlockState);
        
        // 处理方块移除
        if (oldBlockType != null && updateHandlers.containsKey(oldBlockType)) {
            updateHandlers.get(oldBlockType).onBlockRemoved(world, position, oldBlockState);
        }
        
        // 处理方块放置
        if (newBlockType != null && updateHandlers.containsKey(newBlockType)) {
            updateHandlers.get(newBlockType).onBlockPlaced(world, position, newBlockState);
        }
        
        // 处理周围方块的更新
        handleNeighborUpdates(world, position);
    }
    
    /**
     * 处理周围方块的更新
     */
    private void handleNeighborUpdates(World world, Vector3i position) {
        // 检查六个方向的方块
        Vector3i[] neighbors = {
            position.add(1, 0, 0),  // 东
            position.add(-1, 0, 0), // 西
            position.add(0, 0, 1),  // 南
            position.add(0, 0, -1), // 北
            position.add(0, 1, 0),  // 上
            position.add(0, -1, 0)  // 下
        };
        
        for (Vector3i neighborPos : neighbors) {
            int blockState = getBlockState(world, neighborPos);
            if (blockState != 0) {
                String blockType = getBlockTypeFromState(blockState);
                if (blockType != null && updateHandlers.containsKey(blockType)) {
                    updateHandlers.get(blockType).onNeighborChanged(world, neighborPos, blockState, position);
                }
            }
        }
    }
    
    /**
     * 获取指定位置的方块状态
     */
    private int getBlockState(World world, Vector3i position) {
        ChunkPos chunkPos = Slider.getChunkPos(position.getX(), position.getZ(), world);
        ChunkData chunkData = world.getChunkDataMap().get(chunkPos.pos());
        if (chunkData == null) return 0;
        
        int localX = position.getX() & 15;
        int localZ = position.getZ() & 15;
        int sectionIndex = (position.getY() + 64) / 16;
        int localY = (position.getY() + 64) % 16;
        
        if (sectionIndex < 0 || sectionIndex >= 24) return 0;
        
        return chunkData.getChunkSections()[sectionIndex].getBlock(localX, localY, localZ);
    }
    
    /**
     * 从方块状态ID获取方块类型
     */
    private String getBlockTypeFromState(int blockState) {
        return BlockStateRegistry.getBlockType(blockState);
    }
    
    /**
     * 添加延迟更新任务
     */
    public void scheduleUpdate(World world, Vector3i position, int delay) {
        updateQueue.offer(new BlockUpdateTask(world, position, delay));
    }
    
    /**
     * 处理延迟更新队列
     */
    public void processScheduledUpdates() {
        Iterator<BlockUpdateTask> iterator = updateQueue.iterator();
        while (iterator.hasNext()) {
            BlockUpdateTask task = iterator.next();
            if (task.decrementDelay() <= 0) {
                // 执行更新
                int blockState = getBlockState(task.world, task.position);
                if (blockState != 0) {
                    String blockType = getBlockTypeFromState(blockState);
                    if (blockType != null && updateHandlers.containsKey(blockType)) {
                        updateHandlers.get(blockType).onScheduledUpdate(task.world, task.position, blockState);
                    }
                }
                iterator.remove();
            }
        }
    }
    
    /**
     * 方块更新任务
     */
    private static class BlockUpdateTask {
        final World world;
        final Vector3i position;
        int delay;
        
        BlockUpdateTask(World world, Vector3i position, int delay) {
            this.world = world;
            this.position = position;
            this.delay = delay;
        }
        
        int decrementDelay() {
            return --delay;
        }
    }
} 