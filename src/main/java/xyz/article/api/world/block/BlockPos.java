package xyz.article.api.world.block;

import org.cloudburstmc.math.vector.Vector3i;
import xyz.article.api.world.World;

/**
 * 用于保存方块位置
 * @param world 方块所处的世界
 * @param pos 方块所处的世界中的坐标
 */
public record BlockPos(World world, Vector3i pos) {
}
