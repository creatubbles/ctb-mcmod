package com.creatubbles.ctbmod.common.creator;

import java.awt.Point;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.creatubbles.repack.endercore.common.ContainerEnder;

public class ContainerCreator extends ContainerEnder<TileCreator> {

	public ContainerCreator(InventoryPlayer playerInv, TileCreator creator) {
		super(playerInv, creator);
	}

	@Override
	protected void addSlots(InventoryPlayer playerInv) {
		int x = 71;
		int y = 20;
		addSlotToContainer(new Slot(getInv(), 0, x, y));
		addSlotToContainer(new Slot(getInv(), 1, x + 18, y));
		addSlotToContainer(new Slot(getInv(), 2, x, y + 18));
		addSlotToContainer(new Slot(getInv(), 3, x + 18, y + 18));

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
