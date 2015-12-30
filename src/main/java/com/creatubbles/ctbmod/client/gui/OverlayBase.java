package com.creatubbles.ctbmod.client.gui;

import java.awt.Rectangle;

import lombok.Getter;
import net.minecraft.client.gui.Gui;

import org.lwjgl.util.Dimension;

import com.creatubbles.repack.endercore.api.client.gui.IGuiOverlay;
import com.creatubbles.repack.endercore.api.client.gui.IGuiScreen;

public abstract class OverlayBase extends Gui implements IGuiOverlay {

	protected final int xRel, yRel;

	@Getter
	private final Dimension size;

	private int xAbs, yAbs;

	@Getter
	private GuiCreator gui;

	@Getter
	private boolean visible;

	protected OverlayBase(int x, int y, Dimension dimension) {
		this.xRel = x;
		this.yRel = y;
		this.size = dimension;
	}

	@Override
	public void init(IGuiScreen screen) {
	    // Hack for now
		this.gui = (GuiCreator) screen;
		this.xAbs = xRel + screen.getGuiLeft();
		this.yAbs = yRel + screen.getGuiTop();
	}

	@Override
	public final void draw(int mouseX, int mouseY, float partialTick) {
		if (isVisible()) {
			doDraw(mouseX, mouseY, partialTick);
		}
	}

	protected void doDraw(int mouseX, int mouseY, float partialTick) {
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(getX(), getY(), getSize().getWidth(), getSize().getHeight());
	}

	@Override
	public boolean handleMouseInput(int x, int y, int b) {
		return false;
	}

	@Override
	public boolean isMouseInBounds(int mouseX, int mouseY) {
		return getBounds().contains(mouseX, mouseY);
	}

	/**
	 * This automatically converts the mouse x/y to relative coordinates
	 */
	protected boolean isMouseIn(int x, int y, Rectangle rect) {
		return rect.contains(x - getGui().getGuiLeft(), y - getGui().getGuiTop());
	}

	@Override
	public void setIsVisible(boolean visible) {
		this.visible = visible;
	}

	public int getWidth() {
		return getSize().getWidth();
	}

	public int getHeight() {
		return getSize().getHeight();
	}

	public int getX() {
		return xAbs;
	}

	public void setX(int x) {
		this.xAbs = x;
	}

	public int getY() {
		return yAbs;
	}

	public void setY(int y) {
		this.yAbs = y;
	}
}
