package xyz.article.api.world.block.handlers;

import org.cloudburstmc.math.vector.Vector3i;
import xyz.article.api.Slider;
import xyz.article.api.world.World;
import xyz.article.api.world.block.BlockUpdateHandler;
import xyz.article.api.world.chunk.ChunkData;
import xyz.article.api.world.chunk.ChunkPos;

/**
 * 重力方块处理器
 * 处理沙子、沙砾、混凝土粉末等重力方块的物理行为
 */
public class GravityBlockHandler implements BlockUpdateHandler {
    
    @Override
    public void onBlockPlaced(World world, Vector3i position, int blockState) {
        // 检查下方是否有支撑方块
        checkAndFall(world, position, blockState);
    }
    
    @Override
    public void onNeighborChanged(World world, Vector3i position, int blockState, Vector3i neighborPosition) {
        // 如果下方方块被移除，检查是否需要下落
        if (neighborPosition.getY() < position.getY()) {
            checkAndFall(world, position, blockState);
        }
    }
    
    @Override
    public void onScheduledUpdate(World world, Vector3i position, int blockState) {
        // 延迟检查下落
        checkAndFall(world, position, blockState);
    }
    
    /**
     * 检查方块是否需要下落
     */
    private void checkAndFall(World world, Vector3i position, int blockState) {
        Vector3i below = position.add(0, -1, 0);
        
        // 检查下方方块是否为空气
        if (isAir(world, below)) {
            // 计算下落距离
            int fallDistance = calculateFallDistance(world, position);
            
            if (fallDistance > 0) {
                // 移除原位置的方块
                setBlock(world, position, 0);
                
                // 在新位置放置方块
                Vector3i newPosition = position.add(0, -fallDistance, 0);
                setBlock(world, newPosition, blockState);
                
                // 如果下落距离超过4格，方块会变成掉落物（这里简化处理）
                if (fallDistance > 4) {
                    setBlock(world, newPosition, 0);
                    // TODO: 生成掉落物
                }
            }
        }
    }
    
    /**
     * 计算方块的下落距离
     */
    private int calculateFallDistance(World world, Vector3i position) {
        int distance = 0;
        Vector3i current = position.add(0, -1, 0);
        
        while (isAir(world, current) && current.getY() >= -64) {
            distance++;
            current = current.add(0, -1, 0);
        }
        
        return distance;
    }
    
    /**
     * 检查指定位置是否为空气
     */
    private boolean isAir(World world, Vector3i position) {
        int blockState = getBlockState(world, position);
        return blockState == 0;
    }
    
    /**
     * 设置指定位置的方块
     */
    private void setBlock(World world, Vector3i position, int blockState) {
        // TODO: 实现设置方块状态的逻辑
        // 这里需要调用世界的方法来设置方块
        // 暂时留空，后续实现
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