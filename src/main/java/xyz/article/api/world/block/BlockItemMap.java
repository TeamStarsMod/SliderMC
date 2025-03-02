package xyz.article.api.world.block;

import java.util.HashMap;
import java.util.Map;

public class BlockItemMap {
    private static final Map<Integer, Integer> itemToBlockMap = new HashMap<>(); // 物品ID到方块ID的映射
    private static final Map<Integer, Integer> blockToItemMap = new HashMap<>(); // 方块ID到物品ID的映射

    public static void writeMap() {
        // 初始化映射表
        itemToBlockMap.put(0, 0); // Air?
        itemToBlockMap.put(1, 1); // Stone
        itemToBlockMap.put(2, 2); // Granite
        itemToBlockMap.put(3, 3); // Polished Granite
        itemToBlockMap.put(4, 4); // Oh what's that, Diorite
        itemToBlockMap.put(5, 5); // Andesite
        itemToBlockMap.put(7, 6); // Polished Andesite
        itemToBlockMap.put(27, 9); // Grass Block
        itemToBlockMap.put(28, 10); // Dirt
        itemToBlockMap.put(29, 11); // Coarse Dirt
        itemToBlockMap.put(30, 13); // Podzol
        itemToBlockMap.put(35, 14); // Cobble Stone
        itemToBlockMap.put(36, 15); // Oak Planks
        itemToBlockMap.put(37, 16); // Spruce Planks
        itemToBlockMap.put(38, 17); // Birch Planks
        itemToBlockMap.put(39, 18); // Jungle Planks
        itemToBlockMap.put(40, 19); // Acacia Planks
        itemToBlockMap.put(41, 20); // Cherry Planks
        itemToBlockMap.put(42, 21); // Dark Oak Planks
        itemToBlockMap.put(299, 161); // Chest (错误)

        // 同时填充方块到物品的映射
        for (Map.Entry<Integer, Integer> entry : itemToBlockMap.entrySet()) {
            blockToItemMap.put(entry.getValue(), entry.getKey());
        }
    }

    public static int getBlockID(int itemID) {
        return itemToBlockMap.getOrDefault(itemID, 0);
    }

    public static int getItemID(int blockID) {
        return blockToItemMap.getOrDefault(blockID, 0);
    }
}
