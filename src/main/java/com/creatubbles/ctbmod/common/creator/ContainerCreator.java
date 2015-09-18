package com.creatubbles.ctbmod.common.creator;

import java.awt.Point;

import net.minecraft.entity.player.InventoryPlayer;

import com.creatubbles.repack.enderlib.common.ContainerEnder;

public class ContainerCreator extends ContainerEnder<InventoryPlayer> {

	public ContainerCreator(InventoryPlayer playerInv) {
		super(playerInv, playerInv);
	}
	
	@Override
	public Point getPlayerInventoryOffset() {
		Point p = super.getPlayerInventoryOffset();
		p.translate(0, 22);
		return p;
	}
}
