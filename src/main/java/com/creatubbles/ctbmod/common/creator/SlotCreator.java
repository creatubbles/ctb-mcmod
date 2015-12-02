package com.creatubbles.ctbmod.common.creator;

import lombok.Getter;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotCreator extends Slot {

    @Getter
    private final ItemStack ghostStack;

    private final TileCreator te;

    public SlotCreator(ItemStack stack, TileCreator inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
        this.ghostStack = stack;
        this.te = inventoryIn;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return super.isItemValid(stack) && te.isItemValidForSlot(slotNumber, stack);
    }
}
