package com.creatubbles.ctbmod.client.gui.upload;

import java.awt.Dimension;
import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import com.creatubbles.ctbmod.client.gui.GuiButtonHideable;
import com.creatubbles.repack.endercore.client.gui.GuiContainerBase;


public class GuiMediaList extends GuiContainerBase {
   
    final GuiContainer parent;
    
    private final OverlayMediaThumbs thumbs;
    
    private GuiButton pgPrev, pgNext;
    private GuiButton cancel;
    private GuiButton mediaSelector;
    
    private MediaType type = MediaType.SCREENSHOT;
    
    public GuiMediaList(GuiContainer parent) {
        super(new Container() {

            @Override
            public boolean canInteractWith(EntityPlayer playerIn) {
                return true;
            }
        });
        
        this.parent = parent;
        
        thumbs = new OverlayMediaThumbs(0, 30, new Dimension(), type);
        thumbs.setIsVisible(true);
        addOverlay(thumbs);
    }
    
    @Override
    public void initGui() {
        this.xSize = width;
        this.ySize = height;
        thumbs.setSize(new Dimension(width, height - 60));
        
        super.initGui();
        
        addButton(pgPrev = new GuiButtonHideable(-1, guiLeft + (width / 2) - 50 - 100 - 20, height - 25, 100, 20, "<< Prev"));
        addButton(pgNext = new GuiButtonHideable(1, guiLeft + (width / 2) + 50 + 20, height - 25, 100, 20, "Next >>"));
        addButton(cancel = new GuiButtonHideable(0, guiLeft + (width / 2) - 50, height - 25, 100, 20, "Cancel"));
        addButton(mediaSelector = new GuiButton(2, guiLeft + width - 106, 6, 100, 20, type.getName()));
        
        pgPrev.visible = false;
        pgNext.visible = false;

        thumbs.onListBuilt(new Runnable() {

            @Override
            public void run() {
                pgPrev.visible = true;
                pgNext.visible = true;
                if (thumbs.getPage() == thumbs.getPages() - 1) {
                    pgNext.enabled = false;
                }
                if (thumbs.getPage() == 0) {
                    pgPrev.enabled = false;
                }
            }
        });
    }
    
    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int mouseX, int mouseY) {
        this.drawBackground(0);
        super.drawGuiContainerBackgroundLayer(par1, mouseX, mouseY);
    }
    
    @Override
    protected void drawForegroundImpl(int mouseX, int mouseY) {
        super.drawForegroundImpl(mouseX, mouseY);
        drawCenteredString(fontRendererObj, "Choose Media", width / 2, 10, 0xFFFFFF);
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);

        if (button.id == cancel.id) {
            Minecraft.getMinecraft().displayGuiScreen(parent);
        } else if (button.id == mediaSelector.id) {
            type = MediaType.values()[(type.ordinal() + 1) % MediaType.values().length];
            mediaSelector.displayString = type.getName();
            thumbs.setType(type);
            thumbs.init(this);
        } else {
            thumbs.page(button.id);
        }
        
        pgNext.enabled = thumbs.getPage() != thumbs.getPages() - 1;
        pgPrev.enabled = thumbs.getPage() != 0;
    }
}
