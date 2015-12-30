package com.creatubbles.ctbmod.common.creator;

import java.awt.Point;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.creatubbles.ctbmod.common.config.Configs;
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
        ItemStack paper = new ItemStack(Items.paper);
        addSlotToContainer(new SlotCreator(paper.copy(), getInv(), 0, x, y));
        addSlotToContainer(new SlotCreator(Configs.harderPaintings ? new ItemStack(Items.dye, 1, DyeColor.RED.ordinal()) : paper.copy(), getInv(), 1, x + 18, y));
        addSlotToContainer(new SlotCreator(Configs.harderPaintings ? new ItemStack(Items.dye, 1, DyeColor.GREEN.ordinal()) : paper.copy(), getInv(), 2, x, y + 18));
        addSlotToContainer(new SlotCreator(Configs.harderPaintings ? new ItemStack(Items.dye, 1, DyeColor.BLUE.ordinal()) : paper.copy(), getInv(), 3, x + 18, y + 18));

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
