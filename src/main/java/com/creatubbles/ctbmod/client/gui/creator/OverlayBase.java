package com.creatubbles.ctbmod.client.gui.creator;

import java.awt.Dimension;
import java.awt.Rectangle;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.Gui;

import com.creatubbles.repack.endercore.api.client.gui.IGuiOverlay;
import com.creatubbles.repack.endercore.api.client.gui.IGuiScreen;
import com.creatubbles.repack.endercore.client.gui.GuiContainerBase;

public abstract class OverlayBase<T extends GuiContainerBase> extends Gui implements IGuiOverlay {

    protected final int xRel, yRel;

    @Getter
    @Setter
    private Dimension size;

    private int xAbs, yAbs;

    @Getter
    private T gui;

    @Getter
    private boolean visible;

    protected OverlayBase(int x, int y, Dimension dimension) {
        xRel = x;
        yRel = y;
        size = dimension;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void init(IGuiScreen screen) {
        // Hack for now
        gui = (T) screen;
        xAbs = xRel + screen.getGuiLeft();
        yAbs = yRel + screen.getGuiTop();
    }

    @Override
    public final void draw(int mouseX, int mouseY, float partialTick) {
        if (isVisible()) {
            doDraw(mouseX, mouseY, partialTick);
        }
    }

    protected void doDraw(int mouseX, int mouseY, float partialTick) {}

    @Override
    public Rectangle getBounds() {
        return new Rectangle(getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public boolean handleMouseInput(int x, int y, int b) {
        return false;
    }

    @Override
    public boolean isMouseInBounds(int mouseX, int mouseY) {
        return getBounds().contains(mouseX, mouseY);
    }
    
    @Override
    public void guiClosed() {
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
        return getSize().width;
    }

    public int getHeight() {
        return getSize().height;
    }

    public int getX() {
        return xAbs;
    }

    public void setX(int x) {
        xAbs = x;
    }

    public int getY() {
        return yAbs;
    }

    public void setY(int y) {
        yAbs = y;
    }
}
