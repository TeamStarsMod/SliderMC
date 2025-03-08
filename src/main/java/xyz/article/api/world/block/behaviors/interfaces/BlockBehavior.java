package xyz.article.api.world.block.behaviors.interfaces;

import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;
import xyz.article.api.entities.player.Player;
import xyz.article.api.world.World;
import xyz.article.api.world.block.BlockFace;

/**
 * 方块行为接口
 */
public interface BlockBehavior {
    int getPlacedBlockState(Player player, Vector3i clickPos, BlockFace face, ItemStack item);
    default void postPlace(World world, Vector3i blockPos, int blockState) {}
}

