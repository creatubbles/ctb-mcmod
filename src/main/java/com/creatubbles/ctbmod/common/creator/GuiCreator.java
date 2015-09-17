package com.creatubbles.ctbmod.common.creator;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;

public class GuiCreator extends GuiContainer {

	public GuiCreator(InventoryPlayer inv) {
		super(new ContainerCreator(inv));
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		// TODO Auto-generated method stub

	}
}
