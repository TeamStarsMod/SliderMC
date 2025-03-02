package xyz.article.api.entities.player;

import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;

public class Hand {
    private ItemStack currentItem;
    private int currentSlot;

    public Hand() {
        currentItem = null;
        currentSlot = 0;
    }

    public void setCurrentItem(ItemStack currentItem) {
        this.currentItem = currentItem;
    }

    public ItemStack getCurrentItem() {
        return currentItem;
    }

    public void setCurrentSlot(int currentSlot) {
        this.currentSlot = currentSlot;
    }

    public int getCurrentSlot() {
        return currentSlot;
    }
}
