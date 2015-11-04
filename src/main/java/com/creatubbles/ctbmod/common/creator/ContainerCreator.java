package com.creatubbles.ctbmod.common.creator;

import java.awt.Point;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.creatubbles.repack.endercore.common.ContainerEnder;
import com.creatubbles.repack.endercore.common.util.DyeColor;

public class ContainerCreator extends ContainerEnder<TileCreator> {

	public ContainerCreator(InventoryPlayer playerInv, TileCreator creator) {
		super(playerInv, creator);
	}

	@Override
    protected void addSlots(InventoryPlayer playerInv) {
        int x = 71;
        int y = 20;
        addSlotToContainer(new SlotCreator(new ItemStack(Items.paper), getInv(), 0, x, y));
        addSlotToContainer(new SlotCreator(new ItemStack(Items.dye, 1, DyeColor.RED.ordinal()), getInv(), 1, x + 18, y));
        addSlotToContainer(new SlotCreator(new ItemStack(Items.dye, 1, DyeColor.GREEN.ordinal()), getInv(), 2, x, y + 18));
        addSlotToContainer(new SlotCreator(new ItemStack(Items.dye, 1, DyeColor.BLUE.ordinal()), getInv(), 3, x + 18, y + 18));

        addSlotToContainer(new Slot(getInv(), 4, 145, 29) {

            @Override
            public boolean isItemValid(ItemStack stack) {
                return false;
			}
		});
	}

	@Override
	public Point getPlayerInventoryOffset() {
		Point p = super.getPlayerInventoryOffset();
		p.translate(0, 44);
		return p;
	}
}
