package xyz.article.api.world.block.blocktypes;

import xyz.article.api.world.block.BlockFace;

public class BlockData {
    private final BlockFace blockFace;

    public BlockData(BlockFace blockFace) {
        this.blockFace = blockFace;
    }

    public BlockFace getBlockFace() {
        return blockFace;
    }
}
