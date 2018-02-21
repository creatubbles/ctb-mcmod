package com.creatubbles.ctbmod.client.gui.upload;

import java.awt.Rectangle;
import java.io.IOException;

import org.lwjgl.input.Keyboard;

import com.creatubbles.ctbmod.client.gif.GifRecorder;
import com.creatubbles.ctbmod.client.gif.GifState;
import com.creatubbles.ctbmod.client.gif.RecordingStatus;
import com.creatubbles.ctbmod.client.gui.GuiUtil;
import com.creatubbles.repack.endercore.client.gui.GuiContainerBase;
import com.creatubbles.repack.endercore.client.gui.widget.GuiToolTip;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiSlider;

public class GuiRecordNew extends GuiContainerBase {
    
    private final GuiMediaList parent;
    
    private GuiButton buttonBack;
    private GuiButton buttonRecord;
    
    private GuiSlider sliderQuality;
    private GuiSlider sliderCompression;
    private GuiSlider sliderLength;

    protected GuiRecordNew(GuiMediaList parent) {
        super(GuiUtil.dummyContainer());
        this.parent = parent;
    }

    @Override
    public void initGui() {
        int x = (width / 2) + (width / 4);
        int w = width / 5;
        addButton(buttonBack = new GuiButton(-99, x + (w / 10), height - 30, w, 20, "Back"));

        int y = 60;
        addButton(sliderQuality = new GuiSlider(1, x - (w / 2), y, w, 20, "Quality: ", "", 1, 20, 10, false, true));
        addToolTip(new GuiToolTip(new Rectangle(sliderQuality.xPosition, sliderQuality.yPosition, sliderQuality.width, sliderQuality.height), 
                "The quality of the GIF colors", "Higher values will take longer to create, but will look better."));
        y += 25;
        addButton(sliderCompression = new GuiSlider(2, x - (w / 2), y, w, 20, "Compression: ", "", 0, 10, 2, false, true));
        addToolTip(new GuiToolTip(new Rectangle(sliderCompression.xPosition, sliderCompression.yPosition, sliderCompression.width, sliderCompression.height), 
                "How much colors are compressed between frames", "Higher values will result in smaller files, but may look worse.", "", TextFormatting.ITALIC + "Leave this at default if you aren't sure what it means."));
        
        y += 25;
        addButton(sliderLength = new GuiSlider(3, x - (w / 2), y, w, 20, "Max Length: ", "s", 1, 30, 30, false, true));
        addToolTip(new GuiToolTip(new Rectangle(sliderLength.xPosition, sliderLength.yPosition, sliderLength.width, sliderLength.height),
                "Maximum recording length", "After this amount of time, the recording will automatically stop.", "You can press " + Keyboard.getKeyName(GifRecorder.KEY_RECORD.getKeyCode()) + " again at any time to stop."));
        
        y += 25;
        addButton(buttonRecord = new GuiButton(0, x - (w / 2), y, w, 20, "Record!"));
        
        this.xSize = width;
        this.ySize = height;
        
        updateScreen();
    }
    
    @Override
    public void updateScreen() {
        super.updateScreen();
        buttonRecord.enabled = GifRecorder.state.getStatus() == RecordingStatus.OFF;
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int mouseX, int mouseY) {
        this.drawBackground(0);
        super.drawGuiContainerBackgroundLayer(par1, mouseX, mouseY);
        GuiUtil.drawSlotBackground(0, 40, width, height - 80);
    }
    
    @Override
    protected void drawForegroundImpl(int mouseX, int mouseY) {
        super.drawForegroundImpl(mouseX, mouseY);
    }
    
    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        parent.onGuiClosed();
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == buttonBack.id) {
            Minecraft.getMinecraft().displayGuiScreen(parent);
        } else if (button.id == buttonRecord.id) {
            GifRecorder.state = new GifState(21 - sliderQuality.getValueInt(), sliderCompression.getValueInt() / 255f, sliderLength.getValueInt() * 20);
            Minecraft.getMinecraft().displayGuiScreen(null);
        }
    }
}
