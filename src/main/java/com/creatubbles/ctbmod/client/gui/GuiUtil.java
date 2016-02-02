package com.creatubbles.ctbmod.client.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;

import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

import com.creatubbles.ctbmod.CTBMod;

import static org.lwjgl.opengl.GL11.*;

public class GuiUtil extends Gui {
    
    private static final GuiUtil INSTANCE = new GuiUtil();

    public static final ResourceLocation LOADING_TEX_FULL = new ResourceLocation(CTBMod.DOMAIN, "textures/gui/bubble_outline.png");
    public static final ResourceLocation LOADING_TEX_BG = new ResourceLocation(CTBMod.DOMAIN, "textures/gui/bubble_outer.png");
    public static final ResourceLocation LOADING_TEX_ANIM = new ResourceLocation(CTBMod.DOMAIN, "textures/gui/bubble_inner.png");

    public static void drawLoadingTex(int x, int y, int width, int height) {
        long millis = System.currentTimeMillis();
        float rot = (float) (((double) millis / 10) % 360f);

        TextureManager engine = Minecraft.getMinecraft().getTextureManager();
        engine.bindTexture(LOADING_TEX_BG);
        ITextureObject tex = engine.getTexture(LOADING_TEX_BG);

        glPushMatrix();
        {
            glTranslatef(x, y, 0);

            rotateAroundCenter(-rot, width, height);
            func_152125_a(0, 0, 0, 0, 16, 16, width, height, 16, 16);

            engine.bindTexture(LOADING_TEX_ANIM);
            rotateAroundCenter(rot * 2, width, height);
            func_152125_a(0, 0, 0, 0, 16, 16, width, height, 16, 16);
        }
        glPopMatrix();
    }

    private static void rotateAroundCenter(float rot, int width, int height) {
        glTranslated((double) width / 2, (double) height / 2, 0);
        glRotatef(rot, 0, 0, 1);
        glTranslated((double) width / -2, (double) height / -2, 0);
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

    public static BufferedImage upsize(Image img, boolean square, int type) {
        Color fill = Color.BLACK;
        return upsize(img, square, type, fill);
    }

    public static BufferedImage upsize(Image img, boolean square, int type, Color fill) {
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

        func_152125_a(bounds.x, bounds.y, 0, 0, toDraw.width, toDraw.height, bounds.width, bounds.height, texWidth, texHeight);
    }
    
    public static void drawSlotBackground(int left, int top, int width, int height) {

        Tessellator tess = Tessellator.instance;
        Minecraft.getMinecraft().getTextureManager().bindTexture(Gui.optionsBackground);

        glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        
        float f = 32.0F;
        tess.startDrawingQuads();
        
        int right = left + width, bottom = top + height;
        tess.setColorRGBA(32, 32, 32, 255);
        tess.addVertexWithUV(left, bottom, 0.0D, left / f, bottom / f);
        tess.addVertexWithUV(right, bottom, 0.0D, right / f, bottom / f);
        tess.addVertexWithUV(right, top, 0.0D, right / f, top / f);
        tess.addVertexWithUV(left, top, 0.0D, left / f, top / f);

        tess.draw();

        INSTANCE.drawGradientRect(left, top, width, top + 5, 0xFF000000, 0);
        INSTANCE.drawGradientRect(left, top + height - 5, width, top + height, 0, 0xFF000000);
    }
    
    private static Method method;
    private static boolean neiPresent = true;
    
    @SneakyThrows
    public static void toggleNEI(boolean toggle) {
        // This is me being tired of NEI
        // ༼ つ ◕_◕ ༽つ  JEI ༼ つ ◕_◕ ༽つ
        if (!neiPresent) {
            return;
        }
        
        if (method == null) {
            try {
                Class<?> c = Class.forName("codechicken.nei.NEIClientConfig");
                method = c.getDeclaredMethod("setInternalEnabled", boolean.class);
            } catch (Exception e) {
                neiPresent = false;
            }
        }
        
        method.invoke(null, toggle);
    }
}
