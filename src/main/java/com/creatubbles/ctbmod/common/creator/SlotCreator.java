package com.creatubbles.ctbmod.common.creator;

import lombok.Getter;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;


public class SlotCreator extends Slot {

    @Getter
    private ItemStack ghostStack;

    public SlotCreator(ItemStack stack, IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
        this.ghostStack = stack;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return super.isItemValid(stack) && stack.isItemEqual(this.ghostStack);
    }
}
