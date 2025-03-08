package xyz.article.api.world.block.behaviors;

import org.cloudburstmc.math.vector.Vector3i;
import xyz.article.api.entities.player.Player;
import xyz.article.api.world.block.behaviors.interfaces.BlockBehavior;
import xyz.article.api.world.block.blocktypes.BlockData;
import xyz.article.api.world.block.blocktypes.ChestBlockData;

public class ChestBlockBehavior implements BlockBehavior {
    @Override
    public int getPlacedBlockState(Player player, Vector3i clickPos, BlockData blockData) {
        ChestBlockData chestBlockData = (ChestBlockData) blockData;

        // TODO: 未完成
        return 0;
    }
}
