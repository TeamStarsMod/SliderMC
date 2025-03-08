package xyz.article.api.world.block.blocktypes;

import xyz.article.api.inventory.Inventory;
import xyz.article.api.world.block.BlockFace;

public class ChestBlockData extends BlockData {
    private boolean isWaterLogged;
    private int type; // 0 == single, 1 == left, 2 == right
    private final Inventory inventory;

    public ChestBlockData(BlockFace blockFace, boolean isWaterLogged, int type, Inventory inventory) {
        super(blockFace);
        this.isWaterLogged = isWaterLogged;
        this.type = type;
        this.inventory = inventory;
    }

    public boolean isWaterLogged() {
        return isWaterLogged;
    }

    public int getType() {
        return type;
    }

    public void setWaterLogged(boolean waterLogged) {
        isWaterLogged = waterLogged;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Inventory getInventory() {
        return inventory;
    }
}
