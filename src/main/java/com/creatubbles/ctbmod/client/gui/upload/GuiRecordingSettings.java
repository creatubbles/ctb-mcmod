package com.creatubbles.ctbmod.client.gui.upload;

import java.awt.Rectangle;
import java.io.IOException;

import org.lwjgl.input.Keyboard;

import com.creatubbles.ctbmod.client.gif.GifRecorder;
import com.creatubbles.ctbmod.client.gif.GifState;
import com.creatubbles.ctbmod.client.gui.GuiUtil;
import com.creatubbles.repack.endercore.client.gui.GuiContainerBase;
import com.creatubbles.repack.endercore.client.gui.widget.GuiToolTip;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiOptionsRowList;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiSlider;

public class GuiRecordingSettings extends GuiContainerBase {
    
    private final GuiMediaList parent;
    
    private GuiButton buttonBack;
    
    private GuiSlider sliderQuality;
    private GuiSlider sliderCompression;
    private GuiSlider sliderLength;
    
    private GuiListExtended options;
    
    private class ScrollingTooltip extends GuiToolTip {
        
        private ScrollingTooltip(GuiButton bounds, String... lines) {
            super(new Rectangle(bounds.xPosition, bounds.yPosition, bounds.width, bounds.height), lines);
        }
        
        @Override
        public Rectangle getBounds() {
            Rectangle bounds = super.getBounds();
            return new Rectangle(bounds.x, bounds.y - options.getAmountScrolled(), bounds.width, bounds.height);
        }
    }

    protected GuiRecordingSettings(GuiMediaList parent) {
        super(GuiUtil.dummyContainer());
        this.parent = parent;
    }

    @Override
    public void initGui() {
        int x = width / 2;
        int w = 100;
        addButton(buttonBack = new GuiButton(-99, x - w, height - 30, w * 2, 20, "Back"));

        sliderQuality = new GuiSlider(1, x - w - 5, 0, w, 20, "Quality: ", "", 1, 20, 21 - GifRecorder.getState().getQuality(), false, true);
        addToolTip(new ScrollingTooltip(sliderQuality, "The quality of the GIF colors", "Higher values will take longer to create, but will look better."));
        
        sliderCompression = new GuiSlider(2, x + 5, 0, w, 20, "Compression: ", "", 0, 10, GifRecorder.getState().getCompression() * 255f, false, true);
        addToolTip(new ScrollingTooltip(sliderCompression, "How much colors are compressed between frames", "Higher values will result in smaller files, but may look worse.", "", TextFormatting.ITALIC + "Leave this at default if you aren't sure what it means."));

        sliderLength = new GuiSlider(3, x - w - 5, 0, w, 20, "Max Length: ", "s", 1, 30, GifRecorder.getState().getMaxLength() / 20, false, true);
        addToolTip(new ScrollingTooltip(sliderLength, "Maximum recording length", "After this amount of time, the recording will automatically stop.", "You can press " + Keyboard.getKeyName(GifRecorder.KEY_RECORD.getKeyCode()) + " again at any time to stop."));
        
        options = new GuiOptionsRowList(mc, width - 2, height, 40, height - 40, 30) {{
           this.options.add(new Row(sliderQuality, sliderCompression));
           this.options.add(new Row(sliderLength, null));
        }};
        
        this.xSize = width;
        this.ySize = height;
        
        updateScreen();
    }
    
    @Override
    public void updateScreen() {
        super.updateScreen();
    }
    
    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        this.options.handleMouseInput();
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.options.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        this.options.mouseReleased(mouseX, mouseY, state);
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int mouseX, int mouseY) {
        this.drawBackground(0);
        super.drawGuiContainerBackgroundLayer(par1, mouseX, mouseY);
        options.drawScreen(mouseX, mouseY, par1);
    }
    
    @Override
    protected void drawForegroundImpl(int mouseX, int mouseY) {
        super.drawForegroundImpl(mouseX, mouseY);
        drawCenteredString(fontRendererObj, "GIF Settings", width / 2, 16, -1);
    }
    
    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        GifRecorder.setState(new GifState(21 - sliderQuality.getValueInt(), sliderCompression.getValueInt() / 255f, sliderLength.getValueInt() * 20));
        // Prevent the recording from starting, we just want to set the config data
        GifRecorder.getState().stop();
        GifRecorder.getState().saved();
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == buttonBack.id) {
            Minecraft.getMinecraft().displayGuiScreen(parent);
        }
    }
}
