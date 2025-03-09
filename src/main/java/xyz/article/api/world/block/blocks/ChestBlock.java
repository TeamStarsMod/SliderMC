package xyz.article.api.world.block.blocks;

import xyz.article.api.inventory.Inventory;
import xyz.article.api.world.block.BlockPos;

public class ChestBlock {
    private Inventory inventory;
    private final BlockPos blockPos;

    public ChestBlock(BlockPos pos, Inventory inventory) {
        this.inventory = inventory;
        this.blockPos = pos;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
