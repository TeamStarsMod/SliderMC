package xyz.article.api.world.block.behaviors.interfaces;

import org.cloudburstmc.math.vector.Vector3i;
import xyz.article.api.entities.player.Player;
import xyz.article.api.world.block.blocktypes.BlockData;

/**
 * 方块行为接口
 */
public interface BlockBehavior {
    int getPlacedBlockState(Player player, Vector3i clickPos, BlockData blockData);
}

