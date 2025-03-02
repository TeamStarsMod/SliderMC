package xyz.article.api.inventory;

import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;

/**
 * 玩家物品栏类，继承自基础物品栏类
 */
public class PlayerInventory extends Inventory {
    private ItemStack draggingItem;

    /**
     * 创建一个新的玩家物品栏(ID 0, 大小46, 类别null)
     */
    public PlayerInventory() {
        super(0, 46, null);
    }

    /**
     * 获取当前正在拖动的物品(可能不准确)
     * @return 当前正在拖动的物品
     */
    public ItemStack getDraggingItem() {
        return draggingItem;
    }

    /**
     * 设置当前正在拖动的物品
     * @param draggingItem 将被设置为拖动物品的物品
     */
    public void setDraggingItem(ItemStack draggingItem) {
        this.draggingItem = draggingItem;
    }
}
