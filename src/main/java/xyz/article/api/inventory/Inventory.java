package xyz.article.api.inventory;

import org.geysermc.mcprotocollib.protocol.data.game.inventory.ContainerType;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;

/**
 * 基础物品栏类
 */
public class Inventory {
    private int size;
    private ItemStack[] items;
    private int containerId;
    private ContainerType containerType;

    /**
     * 创建一个新的物品栏
     * @param containerId 物品栏ID
     * @param size 物品栏的大小
     * @param containerType 物品栏的类别
     */
    public Inventory(int containerId, int size, ContainerType containerType) {
        this.containerId = containerId;
        this.size = size;
        this.items = new ItemStack[size];
        this.containerType = containerType;
    }

    public int getSize() {
        return size;
    }
    public void setSize(int size) {
        this.size = size;
    }

    public int getContainerId() {
        return containerId;
    }
    public void setContainerId(int containerId) {
        this.containerId = containerId;
    }

    public ItemStack[] getItems() {
        return items;
    }
    public void setItems(ItemStack[] items) {
        this.items = items;
    }
    public void setItem(int slot, ItemStack item) {
        this.items[slot] = item;
    }

    public ContainerType getContainerType() {
        return containerType;
    }
    public void setContainerType(ContainerType containerType) {
        this.containerType = containerType;
    }
}
