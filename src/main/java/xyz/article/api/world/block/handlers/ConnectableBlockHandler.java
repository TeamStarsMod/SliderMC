package xyz.article.api.world.block.handlers;

import org.cloudburstmc.math.vector.Vector3i;
import xyz.article.api.Slider;
import xyz.article.api.world.World;
import xyz.article.api.world.block.BlockUpdateHandler;
import xyz.article.api.world.block.BlockStateRegistry;
import xyz.article.api.world.chunk.ChunkData;
import xyz.article.api.world.chunk.ChunkPos;

import java.util.HashMap;
import java.util.Map;

/**
 * 连接方块处理器
 * 处理玻璃板、栅栏、墙等需要连接逻辑的方块
 */
public class ConnectableBlockHandler implements BlockUpdateHandler {
    
    @Override
    public void onBlockPlaced(World world, Vector3i position, int blockState) {
        updateConnections(world, position, blockState);
    }
    
    @Override
    public void onNeighborChanged(World world, Vector3i position, int blockState, Vector3i neighborPosition) {
        // 当周围方块变化时，重新计算连接状态
        updateConnections(world, position, blockState);
    }
    
    /**
     * 更新方块的连接状态
     */
    private void updateConnections(World world, Vector3i position, int blockState) {
        // 检查四个方向的连接
        boolean north = shouldConnect(world, position.add(0, 0, -1), blockState);
        boolean south = shouldConnect(world, position.add(0, 0, 1), blockState);
        boolean east = shouldConnect(world, position.add(1, 0, 0), blockState);
        boolean west = shouldConnect(world, position.add(-1, 0, 0), blockState);
        
        // 创建新的属性映射
        Map<String, String> newProperties = new HashMap<>();
        newProperties.put("north", String.valueOf(north));
        newProperties.put("south", String.valueOf(south));
        newProperties.put("east", String.valueOf(east));
        newProperties.put("west", String.valueOf(west));
        
        // 对于玻璃板，还需要waterlogged属性
        if (isGlassPane(blockState)) {
            newProperties.put("waterlogged", "false"); // 简化处理
        }
        
        // TODO: 根据新属性计算新的方块状态ID并设置
        // int newBlockState = calculateNewBlockState(blockState, newProperties);
        // setBlock(world, position, newBlockState);
    }
    
    /**
     * 检查是否应该与指定位置的方块连接
     */
    private boolean shouldConnect(World world, Vector3i neighborPos, int currentBlockState) {
        int neighborBlockState = getBlockState(world, neighborPos);
        
        if (neighborBlockState == 0) {
            return false;
        }
        
        // 检查是否为同类型的连接方块
        if (isSameType(currentBlockState, neighborBlockState)) {
            return true;
        }
        
        // 检查是否为固体方块（对于栅栏和墙）
        if (isFenceOrWall(currentBlockState)) {
            return BlockStateRegistry.isSolidBlock(neighborBlockState);
        }
        
        return false;
    }
    
    /**
     * 检查是否为玻璃板
     */
    private boolean isGlassPane(int blockState) {
        String blockType = BlockStateRegistry.getBlockType(blockState);
        return blockType != null && blockType.contains("_pane");
    }
    
    /**
     * 检查是否为栅栏或墙
     */
    private boolean isFenceOrWall(int blockState) {
        String blockType = BlockStateRegistry.getBlockType(blockState);
        return blockType != null && (blockType.contains("_fence") || blockType.contains("_wall"));
    }
    
    /**
     * 检查是否为同类型的连接方块
     */
    private boolean isSameType(int blockState1, int blockState2) {
        String type1 = BlockStateRegistry.getBlockType(blockState1);
        String type2 = BlockStateRegistry.getBlockType(blockState2);
        return type1 != null && type1.equals(type2);
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
} 