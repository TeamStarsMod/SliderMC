package xyz.article.api.world.block;

import xyz.article.api.world.block.behaviors.interfaces.BlockBehavior;
import xyz.article.api.world.block.blocks.OtherBlocks;

import java.util.concurrent.ConcurrentHashMap;

public class BlockItemMap {
    public static ConcurrentHashMap<Integer, BlockBehavior> blockBlockBehaviorConcurrentHashMap = new ConcurrentHashMap<>();

    public static void writeMap() {
        OtherBlocks.writeMap();
    }

    public static int getBlockID(int itemID) {
        return OtherBlocks.getItemToBlockMap().getOrDefault(itemID, 0);
    }

    public static int getItemID(int blockID) {
        return OtherBlocks.getBlockToItemMap().getOrDefault(blockID, 0);
    }

    public static ConcurrentHashMap<Integer, BlockBehavior> getBlockBlockBehaviorConcurrentHashMap() {
        return blockBlockBehaviorConcurrentHashMap;
    }
}