package com.creatubbles.ctbmod.common.creator;

import net.minecraft.entity.player.InventoryPlayer;

import com.creatubbles.repack.enderlib.common.ContainerEnder;

public class ContainerCreator extends ContainerEnder<InventoryPlayer> {

	public ContainerCreator(InventoryPlayer playerInv) {
		super(playerInv, playerInv);
	}
}
