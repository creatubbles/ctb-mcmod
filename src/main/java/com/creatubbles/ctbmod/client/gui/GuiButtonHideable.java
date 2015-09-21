package com.creatubbles.ctbmod.client.gui;

import net.minecraft.client.gui.GuiButton;


public class GuiButtonHideable extends GuiButton implements IHideable {

	public GuiButtonHideable(int buttonId, int x, int y, String buttonText) {
		super(buttonId, x, y, buttonText);
	}

	public GuiButtonHideable(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
		super(buttonId, x, y, widthIn, heightIn, buttonText);
	}

	@Override
	public void setIsVisible(boolean visible) {
		this.visible = visible;
	}

	@Override
	public boolean isVisible() {
		return this.visible;
	}

}
