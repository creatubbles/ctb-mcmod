package com.creatubbles.ctbmod.client.gui;

import java.awt.image.BufferedImage;
import java.io.IOException;

import lombok.Getter;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;

public class LazyLoadedTexture extends AbstractTexture {

    private int[] textureData;
    @Getter
    private int width, height;

    public LazyLoadedTexture(BufferedImage image) {
        this.width = image.getWidth();
        this.height = image.getHeight();
        textureData = new int[width * height];
        image.getRGB(0, 0, width, height, textureData, 0, width);
    }

    public void uploadTexture() {
        TextureUtil.allocateTexture(this.getGlTextureId(), width, height);
        TextureUtil.uploadTexture(this.getGlTextureId(), textureData, width, height);
        // Dereference the texture data as it uses up a large amount of memory, and is not needed
        textureData = null;
    }

    @Override
    public void loadTexture(IResourceManager resourceManager) throws IOException {}
}