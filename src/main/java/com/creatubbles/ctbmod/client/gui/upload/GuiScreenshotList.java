package com.creatubbles.ctbmod.client.gui.upload;

import java.awt.Dimension;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import com.creatubbles.ctbmod.client.gui.GuiButtonHideable;
import com.creatubbles.ctbmod.client.gui.GuiUtil;
import com.creatubbles.repack.endercore.client.gui.GuiContainerBase;


public class GuiScreenshotList extends GuiContainerBase {
   
    final GuiContainer parent;
    
    private final OverlayScreenshotThumbs thumbs;
    
    private GuiButton pgPrev, pgNext;
    private GuiButton cancel;
    
    public GuiScreenshotList(GuiContainer parent) {
        super(new Container() {

            @Override
            public boolean canInteractWith(EntityPlayer playerIn) {
                return true;
            }
        });

        GuiUtil.toggleNEI(false);

        this.parent = parent;
        
        thumbs = new OverlayScreenshotThumbs(0, 30, new Dimension());
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
        
        pgPrev.visible = false;
        pgNext.visible = false;

        thumbs.onListBuilt(new Runnable() {

            @Override
            public void run() {
                pgPrev.visible = true;
                pgNext.visible = true;
                if (thumbs.getPages() <= 1) {
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
        drawCenteredString(fontRendererObj, "Choose a Screenshot", width / 2, 10, 0xFFFFFF);
    }
    
    @Override
    protected void actionPerformed(GuiButton button) {
        super.actionPerformed(button);

        if (button.id == cancel.id) {
            Minecraft.getMinecraft().displayGuiScreen(parent);
            GuiUtil.toggleNEI(true);
            return;
        }

        thumbs.page(button.id);
        
        pgNext.enabled = thumbs.getPage() != thumbs.getPages() - 1;
        pgPrev.enabled = thumbs.getPage() != 0;
    }
}
