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
 * 红石线处理器
 * 处理红石线的连接和信号传播
 */
public class RedstoneWireHandler implements BlockUpdateHandler {
    
    @Override
    public void onBlockPlaced(World world, Vector3i position, int blockState) {
        updateConnections(world, position, blockState);
        updatePower(world, position, blockState);
    }
    
    @Override
    public void onNeighborChanged(World world, Vector3i position, int blockState, Vector3i neighborPosition) {
        // 当周围方块变化时，重新计算连接和信号
        updateConnections(world, position, blockState);
        updatePower(world, position, blockState);
    }
    
    /**
     * 更新红石线的连接状态
     */
    private void updateConnections(World world, Vector3i position, int blockState) {
        // 检查四个方向的连接
        boolean north = canConnectTo(world, position.add(0, 0, -1));
        boolean south = canConnectTo(world, position.add(0, 0, 1));
        boolean east = canConnectTo(world, position.add(1, 0, 0));
        boolean west = canConnectTo(world, position.add(-1, 0, 0));
        
        // 创建新的属性映射
        Map<String, String> newProperties = new HashMap<>();
        newProperties.put("north", north ? "side" : "none");
        newProperties.put("south", south ? "side" : "none");
        newProperties.put("east", east ? "side" : "none");
        newProperties.put("west", west ? "side" : "none");
        newProperties.put("power", "0"); // 暂时设为0，后续会更新
        
        // TODO: 根据新属性计算新的方块状态ID并设置
    }
    
    /**
     * 更新红石线的信号强度
     */
    private void updatePower(World world, Vector3i position, int blockState) {
        // 计算来自各个方向的信号强度
        int maxPower = 0;
        
        // 检查上方是否有电源
        int powerAbove = getPowerFrom(world, position.add(0, 1, 0));
        maxPower = Math.max(maxPower, powerAbove);
        
        // 检查四个方向的连接
        Vector3i[] directions = {
            position.add(0, 0, -1), // 北
            position.add(0, 0, 1),  // 南
            position.add(1, 0, 0),  // 东
            position.add(-1, 0, 0)  // 西
        };
        
        for (Vector3i dir : directions) {
            if (canConnectTo(world, dir)) {
                int power = getPowerFrom(world, dir);
                maxPower = Math.max(maxPower, power - 1); // 红石线会衰减信号
            }
        }
        
        // 限制信号强度在0-15之间
        maxPower = Math.max(0, Math.min(15, maxPower));
        
        // TODO: 更新方块状态中的power属性
    }
    
    /**
     * 检查是否可以连接到指定位置
     */
    private boolean canConnectTo(World world, Vector3i position) {
        int blockState = getBlockState(world, position);
        
        // 检查是否为红石相关方块
        return BlockStateRegistry.isRedstoneComponent(blockState) || BlockStateRegistry.isRedstoneWire(blockState);
    }
    
    /**
     * 从指定位置获取信号强度
     */
    private int getPowerFrom(World world, Vector3i position) {
        int blockState = getBlockState(world, position);
        
        if (BlockStateRegistry.isRedstoneWire(blockState)) {
            // TODO: 从红石线的power属性获取信号强度
            return 0;
        } else if (BlockStateRegistry.isRedstoneComponent(blockState)) {
            // TODO: 从红石组件的状态获取信号强度
            return 15; // 简化处理
        }
        
        return 0;
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