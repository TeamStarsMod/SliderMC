package xyz.article.api.world.chunk;

import org.cloudburstmc.math.vector.Vector2i;
import xyz.article.api.world.World;

/**
 * 用于储存区块坐标
 * @param world 区块所在的位置
 * @param pos 区块所在世界中的坐标
 */
public record ChunkPos(World world, Vector2i pos) {
    @Override
    public String toString() {
        return "ChunkPos(world=" + world.getKey() + ", x=" + pos.getX() + ", z=" + pos.getY() + ")";
    }
}
