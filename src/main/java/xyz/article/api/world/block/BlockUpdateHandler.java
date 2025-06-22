package xyz.article.api.world.block;

import org.cloudburstmc.math.vector.Vector3i;
import xyz.article.api.world.World;

/**
 * 方块更新处理器接口
 * 定义方块状态变化时的处理逻辑
 */
public interface BlockUpdateHandler {
    
    /**
     * 方块被放置时调用
     */
    default void onBlockPlaced(World world, Vector3i position, int blockState) {}
    
    /**
     * 方块被移除时调用
     */
    default void onBlockRemoved(World world, Vector3i position, int blockState) {}
    
    /**
     * 周围方块发生变化时调用
     */
    default void onNeighborChanged(World world, Vector3i position, int blockState, Vector3i neighborPosition) {}
    
    /**
     * 延迟更新时调用
     */
    default void onScheduledUpdate(World world, Vector3i position, int blockState) {}
} 