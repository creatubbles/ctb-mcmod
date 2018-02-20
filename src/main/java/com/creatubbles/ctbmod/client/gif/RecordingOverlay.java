package com.creatubbles.ctbmod.client.gif;

import java.lang.reflect.Field;

import com.creatubbles.repack.endercore.client.render.RenderUtil;

import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class RecordingOverlay extends Gui {

    @SneakyThrows
    public void injectFramebuffer() {
        Field _framebufferMc = ReflectionHelper.findField(Minecraft.class, "field_147124_at", "framebufferMc");
        Framebuffer current = (Framebuffer) _framebufferMc.get(Minecraft.getMinecraft());
        Framebuffer newbuf = new Framebuffer(current.framebufferWidth, current.framebufferHeight, current.useDepth) {
            
            @Override
            public void framebufferRenderExt(int width, int height, boolean p_178038_3_) {
                super.framebufferRenderExt(width, height, p_178038_3_);
                drawOverlay();
            }
            
        };
        _framebufferMc.set(Minecraft.getMinecraft(), newbuf);
        current.deleteFramebuffer();
        
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    // For when VBOs are disabled, somehow this isn't picked up by the recording?
    @SubscribeEvent
    public void renderOverlayNoFBO(RenderGameOverlayEvent.Text event) {
        if (!Minecraft.getMinecraft().gameSettings.useVbo) {
            drawOverlay();
        }
    }
    
    private float pulseTimer;
    
    void drawOverlay() {
        final RecordingStatus status = GifRecorder.status;
        if (status != RecordingStatus.OFF) {
            ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
            
            GlStateManager.pushMatrix();
            GlStateManager.scale(sr.getScaleFactor(), sr.getScaleFactor(), sr.getScaleFactor());

            GlStateManager.enableBlend();
            String toDraw = I18n.format("ctb.recording.live");
            FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
            int strWidth = fr.getStringWidth(toDraw);
            fr.drawStringWithShadow(toDraw, sr.getScaledWidth() - strWidth - 5, 5, 0xCCFFFFFF);

            if (status == RecordingStatus.LIVE) {
                pulseTimer += RenderUtil.getTimer().elapsedPartialTicks * 0.25f;
                GlStateManager.color(1, 1, 1, ((float) Math.sin(pulseTimer) * 0.25f) + 0.75f);
            }
            Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("textures/gui/widgets.png"));
            int u = status == RecordingStatus.PREPARING ? 196 : status == RecordingStatus.LIVE ? 208 : 224;
            drawTexturedModalRect(sr.getScaledWidth() - strWidth - 23, 2, u, 0, 16, 16);
            
            GlStateManager.popMatrix();
        }
    }
}
