package com.creatubbles.ctbmod.client.gui;

import java.awt.image.BufferedImage;

import net.minecraft.client.renderer.texture.TextureUtil;

public class AnimatedTexture extends LazyLoadedTexture {
    
    private final int[][] frameData;
    
    private int frame;
    
    public AnimatedTexture(BufferedImage... frames) {
        super(frames[0]);
        frameData = new int[frames.length][];
        for (int i = 0; i < frames.length; i++) {
            frameData[i] = frames[i].getRGB(0, 0, getWidth(), getHeight(), frameData[i], 0, getWidth());
        }
    }

    @Override
    public void updateTexture() {
        frame = (frame + 1) % frameData.length;
        TextureUtil.uploadTexture(getGlTextureId(), frameData[frame], getWidth(), getHeight());
        super.updateTexture();
    }
}
