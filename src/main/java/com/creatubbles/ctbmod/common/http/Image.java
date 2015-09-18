package com.creatubbles.ctbmod.common.http;

import java.awt.image.BufferedImage;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

import com.creatubbles.ctbmod.CTBMod;

@Value
@RequiredArgsConstructor
public class Image {

	private String url;

	@NonFinal
	private transient ResourceLocation resource;

	public boolean downloaded() {
		return resource != null;
	}

	public void download(final Creation owner) {
		TextureManager texturemanager = Minecraft.getMinecraft().getTextureManager();
		ResourceLocation res = new ResourceLocation(CTBMod.DOMAIN, "textures/creations5/" + owner.getUserId() + "/" + owner.getId());
		ITextureObject texture = texturemanager.getTexture(res);

		if (texture == null) {
			texture = new ThreadDownloadImageData(null, url, null, new IImageBuffer() {

				@Override
				public BufferedImage parseUserSkin(BufferedImage p_78432_1_) {
					return p_78432_1_;
				}

				@Override
				public void func_152634_a() {
				}
			});
			texturemanager.loadTexture(res, texture);
		}
		resource = res;
	}
}
