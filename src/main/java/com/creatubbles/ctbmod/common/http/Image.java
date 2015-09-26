package com.creatubbles.ctbmod.common.http;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.EnumMap;
import java.util.Locale;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.util.Dimension;

import com.creatubbles.ctbmod.CTBMod;
import com.creatubbles.ctbmod.common.config.DataCache;
import com.google.common.collect.Maps;

@ToString
public class Image {

	public enum ImageType {
		ORIGINAL,
		FULL_VIEW,
		LIST_VIEW;

		public String toString() {
			return name().toLowerCase(Locale.US);
		}
	}

	public static final ResourceLocation MISSING_TEXTURE = new ResourceLocation("missingno");
	static {
		Minecraft.getMinecraft().getTextureManager().loadTexture(MISSING_TEXTURE, TextureUtil.missingTexture);
	}

	@Getter
	private final String fileName, urlBase;

	@Getter
	@Setter
	private Creation owner;

	private final EnumMap<ImageType, ResourceLocation> locations = Maps.newEnumMap(ImageType.class);
	private EnumMap<ImageType, Dimension> sizes = Maps.newEnumMap(ImageType.class);

	public Image(String url) {
		fileName = url.substring(url.lastIndexOf("/") + 1, url.length());
		urlBase = url.substring(0, url.indexOf("original"));
		for (ImageType type : ImageType.values()) {
			locations.put(type, new ResourceLocation("missingno"));
			sizes.put(type, new Dimension());
		}
	}

	/**
	 * Gets the bindable {@link ResourceLocation} for the given {@link ImageType type}.
	 * 
	 * @param type
	 *            The {@link ImageType} to get the resource for.
	 * @return A {@link ResourceLocation}, which may be a dummy if this Image has not been downloaded, or is in the process of being downloaded.
	 */
	public ResourceLocation getResource(ImageType type) {
		return locations.get(type);
	}

	/**
	 * The dimensions for this image.
	 * 
	 * @param type
	 *            The {@link ImageType} to get the dimensions for.
	 * @return An {@link Dimension} representing the size of this image. May be zero if the image is not downloaded.
	 */
	public Dimension getSize(ImageType type) {
		return sizes.get(type);
	}

	/**
	 * The width of this image.
	 * 
	 * @param type
	 *            The {@link ImageType} to get the width for.
	 * @return The width of this image. May be zero if the image is not downloaded.
	 */
	public int getWidth(ImageType type) {
		return getSize(type).getWidth();
	}

	/**
	 * The height of this image.
	 * 
	 * @param type
	 *            The {@link ImageType} to get the height for.
	 * @return The height of this image. May be zero if the image is not downloaded.
	 */
	public int getHeight(ImageType type) {
		return getSize(type).getHeight();
	}

	/**
	 * This method is <strong>blocking</strong>. Call it from your own separate thread.
	 * 
	 * @param type
	 *            The {@link ImageType} to download.
	 * @param block
	 *            If true, the method will block until the download is finished. If false, a thread will be spawned to download the image. In this case {@link #updateSize(ImageType)} <b>MUST BE
	 *            CALLED</b> to initialize the size data once downloaded.
	 * @see #updateSize(ImageType)
	 */
	@SneakyThrows
	public void download(ImageType type, boolean block) {
		TextureManager texturemanager = Minecraft.getMinecraft().getTextureManager();

		String url = urlBase.concat(type.toString()).concat("/").concat(fileName);
		String filepath = "creations/" + owner.getUserId() + "/" + type + "/" + owner.getId() + ".jpg";
		File cache = new File(DataCache.cacheFolder, filepath);
		ResourceLocation res = new ResourceLocation(CTBMod.DOMAIN, filepath);
		ITextureObject texture = texturemanager.getTexture(res);
		ThreadDownloadImageData dl = null;

		if (texture == null) {
			texture = dl = new ThreadDownloadImageData(cache, url, null, new IImageBuffer() {

				@Override
				public BufferedImage parseUserSkin(BufferedImage p_78432_1_) {
					return p_78432_1_;
				}

				@Override
				public void skinAvailable() {
				}
			});
			texturemanager.loadTexture(res, texture);
			if (dl.imageThread != null) {
				((ThreadDownloadImageData) texture).imageThread.join(); // block until download is finished
			}
		} else if (texture instanceof ThreadDownloadImageData) {
			dl = (ThreadDownloadImageData) texture;
		}

		locations.put(type, res);

		if (block) {
			while (dl.bufferedImage == null) {
				Thread.sleep(100);
			}

			updateSize(dl.bufferedImage, type);
		}
	}

	/**
	 * Checks if the size for the given type has been initialized
	 * 
	 * @param type
	 *            The {@link ImageType} to check for.
	 * @return True if the size for this type has been initialized. False otherwise.
	 */
	public boolean hasSize(ImageType type) {
		return sizes.get(type).getHeight() == 0;
	}

	/**
	 * Call this to manually update the size once an image is downloaded. Will not fail if the image hasn't been downloaded, so this can be called while checking {@link #hasSize(ImageType)} to check
	 * successfulness.
	 * 
	 * @param type
	 *            The {@link ImageType} to update the size for.
	 */
	public void updateSize(ImageType type) {
		ITextureObject obj = Minecraft.getMinecraft().getTextureManager().getTexture(getResource(type));
		if (obj != null && obj instanceof ThreadDownloadImageData) {
			updateSize(((ThreadDownloadImageData) obj).bufferedImage, type);
		}
	}

	private void updateSize(BufferedImage img, ImageType type) {
		if (img != null) {
			sizes.put(type, new Dimension(img.getWidth(), img.getHeight()));
		}
	}
}
