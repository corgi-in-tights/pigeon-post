package net.thecorgi.pigeonpost.common.item.envelope;

import net.minecraft.client.item.TooltipData;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public class EnvelopeTooltipData implements TooltipData {
    private final DefaultedList<ItemStack> inventory;
    private final int inventorySize;

    public EnvelopeTooltipData(DefaultedList<ItemStack> inventory, int inventorySize) {
        this.inventory = inventory;
        this.inventorySize = inventorySize;
    }

    public DefaultedList<ItemStack> getInventory() {
        return this.inventory;
    }

    public int getInventorySize() {
        return this.inventorySize;
    }
}
