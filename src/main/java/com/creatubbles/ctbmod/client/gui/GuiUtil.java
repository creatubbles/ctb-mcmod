package com.creatubbles.ctbmod.client.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import com.creatubbles.ctbmod.CTBMod;
import com.creatubbles.repack.endercore.client.render.IWidgetIcon;
import com.creatubbles.repack.endercore.client.render.IWidgetMap;
import com.creatubbles.repack.endercore.client.render.RenderUtil;

public class GuiUtil extends Gui {

    public static final boolean NON_POT_SUPPORTED;

    static {
        String s = GL11.glGetString(GL11.GL_EXTENSIONS);
        NON_POT_SUPPORTED = s.contains("GL_ARB_texture_non_power_of_two");
    }

    public static void init() {}

    @AllArgsConstructor
    @Getter
    public enum Bubbles implements IWidgetIcon {

        BLUE(0, 0),
        GREEN(16, 0),
        CLEAR(32, 0),
        
        OUTER(0, 16),
        INNER(16, 16);
        
        public static final ResourceLocation TEXTURE = new ResourceLocation(CTBMod.MODID, "textures/gui/bubbles.png");

        public static final IWidgetMap map = new IWidgetMap.WidgetMapImpl(64, TEXTURE);

        public final int x;
        public final int y;
        public final int width;
        public final int height;
        public final IWidgetIcon overlay;

        Bubbles(int x, int y) {
            this(x, y, null);
        }

        Bubbles(int x, int y, IWidgetIcon overlay) {
            this(x, y, 16, 16, overlay);
        }

        Bubbles(int x, int y, int width, int height) {
            this(x, y, width, height, null);
        }

        @Override
        public IWidgetMap getMap() {
            return map;
        }
    }
    
    private static final GuiUtil INSTANCE = new GuiUtil();

    public static void drawLoadingTex(int x, int y, int width, int height) {
        double ticks = CTBMod.proxy.getTicksElapsed() + RenderUtil.getTimer().elapsedPartialTicks;
        float rot = (float) ((ticks * 4) % 360d);

        TextureManager engine = Minecraft.getMinecraft().getTextureManager();
        engine.bindTexture(Bubbles.TEXTURE);

        GlStateManager.pushMatrix();
        {
            GlStateManager.translate(x, y, 0);

            rotateAroundCenter(-rot, width, height);
            Bubbles.map.render(Bubbles.OUTER, 0, 0, width, height, 0, true);

            rotateAroundCenter(rot * 2, width, height);
            Bubbles.map.render(Bubbles.INNER, 0, 0, width, height, 0, true);
        }
        GlStateManager.popMatrix();
    }

    private static void rotateAroundCenter(float rot, int width, int height) {
        GlStateManager.translate((double) width / 2, (double) height / 2, 0);
        GlStateManager.rotate(rot, 0, 0, 1);
        GlStateManager.translate((double) width / -2, (double) height / -2, 0);
    }

    public static Container dummyContainer() {
        return new Container() {

            @Override
            public boolean canInteractWith(EntityPlayer playerIn) {
                return true;
            }
        };
    }

    public static int nextPoT(int i) {
        return 2 << (32 - Integer.numberOfLeadingZeros(i - 1)) - 1;
    }

    public static BufferedImage upsize(BufferedImage img, boolean square) {
        int type = img.getType();
        return upsize(img, square, type);
    }

    public static BufferedImage upsize(BufferedImage img, boolean square, int type) {
        Color fill = Color.BLACK;
        return upsize(img, square, type, fill);
    }

    public static BufferedImage upsize(BufferedImage img, boolean square, int type, Color fill) {
        if (NON_POT_SUPPORTED) {
            return img;
        }
        
        int scaledW = nextPoT(img.getWidth(null));
        int scaledH = nextPoT(img.getHeight(null));
        if (square) {
            scaledW = scaledH = Math.max(scaledW, scaledH);
        }

        BufferedImage upscale = new BufferedImage(scaledW, scaledH, type);
        Graphics2D graphics = upscale.createGraphics();
        try {
            graphics.setPaint(fill);
            graphics.fillRect(0, 0, img.getWidth(null), img.getHeight(null));
            graphics.drawImage(img, 0, 0, null);
        } finally {
            graphics.dispose();
        }

        return upscale;
    }
    
    public static void drawRectInscribed(Rectangle toDraw, Rectangle bounds, int texWidth, int texHeight) {
        bounds = new Rectangle(bounds);
        if (toDraw.width * bounds.height > toDraw.height * bounds.width) {
            int h = bounds.height;
            bounds.height = (int) (bounds.getWidth() * (toDraw.getHeight() / toDraw.getWidth()));
            bounds.translate(0, (h - bounds.height) / 2);
        } else {
            int w = bounds.width;
            bounds.width = ((int) (bounds.getHeight() * (toDraw.getWidth() / toDraw.getHeight())));
            bounds.translate((w - bounds.width) / 2, 0);
        }

        drawScaledCustomSizeModalRect(bounds.x, bounds.y, 0, 0, toDraw.width, toDraw.height, bounds.width, bounds.height, texWidth, texHeight);
    }
    
    public static void drawSlotBackground(int left, int top, int width, int height) {

        VertexBuffer worldrenderer = Tessellator.getInstance().getBuffer();
        Minecraft.getMinecraft().getTextureManager().bindTexture(Gui.OPTIONS_BACKGROUND);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        float f = 32.0F;
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        int right = left + width, bottom = top + height;
        worldrenderer.pos(left, bottom, 0.0D).tex(left / f, bottom / f).color(32, 32, 32, 255).endVertex();
        worldrenderer.pos(right, bottom, 0.0D).tex(right / f, bottom / f).color(32, 32, 32, 255).endVertex();
        worldrenderer.pos(right, top, 0.0D).tex(right / f, top / f).color(32, 32, 32, 255).endVertex();
        worldrenderer.pos(left, top, 0.0D).tex(left / f, top / f).color(32, 32, 32, 255).endVertex();

        Tessellator.getInstance().draw();

        INSTANCE.drawGradientRect(left, top, width, top + 5, 0xFF000000, 0);
        INSTANCE.drawGradientRect(left, top + height - 5, width, top + height, 0, 0xFF000000);
    }
}
