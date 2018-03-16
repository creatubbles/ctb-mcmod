package com.creatubbles.ctbmod.client.gui.upload;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL11;

import com.creatubbles.ctbmod.CTBMod;
import com.creatubbles.ctbmod.client.gui.LazyLoadedTexture;
import com.google.common.collect.Maps;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class ThumbnailStitcher {
    
    @Getter
    public static class Progress {
        private int current = 0;
        private int max = 0;
        private String desc;
    }

    private static final int MAX_SIZE = Minecraft.getGLMaximumTextureSize();

    private BufferedImage map;
    
    @Getter
    private int thumbWidth = 128, thumbHeight = thumbWidth;

    @Getter
    private ResourceLocation res = null;
    
    @Getter
    private final Progress progress = new Progress();

    private Map<File, Rectangle> rects = Maps.newHashMap();

    public void loadFiles(final File... files) throws IOException {
        Map<File, Image> images = Maps.newHashMap();
        progress.desc = "Resizing images to " + thumbWidth + "x" + thumbHeight;
        progress.max = files.length;
        progress.current = 0; // Could be a recursive call
        for (File f : files) {
            progress.current++;
            BufferedImage img = null;
            try {
                img = ImageIO.read(f);
            } catch (IOException e) {
                progress.desc = "Error!";
                progress.current = progress.max = 1;
                throw new IIOException("Could not load image file: " + f.getAbsolutePath(), e);
            }
            if (img == null) {
                progress.desc = "Error!";
                progress.current = progress.max = 1;
                throw new IIOException("Could not determine loader for image file: " + f.getAbsolutePath());
            }
            int w = img.getWidth(), h = img.getHeight();
            int scaledW, scaledH;
            if (w > h) {
                scaledW = thumbWidth;
                scaledH = (int) (((double) h / w) * thumbHeight);
            } else {
                scaledH = thumbHeight;
                scaledW = (int) (((double) w / h) * thumbWidth);
            }
            // Force INT_ARGB type to avoid color indexing issues with GIFs
            BufferedImage thumb = new BufferedImage(scaledW, scaledH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = thumb.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(img, 0, 0, scaledW, scaledH, null);
            images.put(f, thumb);
            g.dispose();
        }
        
        double sqrt = Math.sqrt(images.size());
        int width = (int) sqrt;
        if (sqrt != width || width == 0) {
            width++;
        }
        width *= thumbWidth;

        int scaled = 2 << (32 - Integer.numberOfLeadingZeros(width - 1)) - 1;

        if (scaled > MAX_SIZE) {
            if (thumbWidth == 32) {
                throw new IllegalStateException("Cannot load thumbnail sheet, not enough size! Max: " + MAX_SIZE + "  Needed: " + scaled);
            } else {
                int newMax = thumbWidth / 2;
                CTBMod.logger.warn("Could not load thumbnail sheet at optimal thumbnail size. Downsizing to {}x{} and retrying...", newMax, newMax);
                thumbWidth = thumbHeight = newMax;
                loadFiles(files);
                return;
            }
        }
        
        map = new BufferedImage(scaled, scaled, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D out = map.createGraphics();

        try {
            int x, y;
            x = y = 0;
            progress.desc = "Stitching";
            progress.max = images.size();
            progress.current = 0;
            for (Entry<File, Image> e : images.entrySet()) {
                progress.current++;
                Image thumb = e.getValue();
                out.drawImage(thumb, x, y, null);
                rects.put(e.getKey(), new Rectangle(x, y, thumb.getWidth(null), thumb.getHeight(null)));
                
                x += thumbWidth;
                if (x >= width) {
                    x = 0;
                    y += thumbHeight;
                }
            }
        } finally {
            out.dispose();
        }
        
//        ImageIO.write(map, "png", new File(files[0].getParent(), "map.png"));

        progress.desc = "Loading";
        progress.current = 0;
        progress.max = 1;
        final LazyLoadedTexture tex = new LazyLoadedTexture(map);
        
        Minecraft.getMinecraft().addScheduledTask(new Runnable() {

            @Override
            public void run() {
                tex.uploadTexture();
                ResourceLocation loc = new ResourceLocation("ctbmod", toString());
                Minecraft.getMinecraft().getTextureManager().loadTexture(loc, tex);
                progress.current = 1;
                res = loc;
            }
        });
    }

    public Rectangle getRect(File f) {
        return rects.get(f);
    }

    public int getWidth() {
        return map.getWidth();
    }
    
    public int getHeight() {
        return map.getHeight();
    }

    public void dispose() {
        if (getRes() != null) {
            GL11.glDeleteTextures(Minecraft.getMinecraft().getTextureManager().getTexture(getRes()).getGlTextureId());
        }
    }
    
    public boolean isValid() {
        return map != null;
    }
}
