package com.creatubbles.ctbmod.common.http;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.EnumMap;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.util.Dimension;

import com.creatubbles.api.core.Creation;
import com.creatubbles.api.core.Image;
import com.creatubbles.ctbmod.CTBMod;
import com.creatubbles.ctbmod.common.config.DataCache;
import com.creatubbles.ctbmod.common.util.JsonUtil;
import com.google.common.collect.Maps;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@ToString
public class DownloadableImage {

	public enum ImageType {
		ORIGINAL,
		FULL_VIEW,
		LIST_VIEW;

		public String urlString() {
			return name().toLowerCase(Locale.US);
		}
	}

	@Value
	private static class Size {

		private int width, height, scaled;

		private Dimension dimension;

		private Size() {
			this(0, 0, 0);
		}

		public Size(int width, int height, int scaled) {
			this.width = width;
			this.height = height;
			this.scaled = scaled;
			this.dimension = new Dimension(width, height);
		}
		
		public static Size create(BufferedImage actual, BufferedImage rescale) {
			return new Size(actual.getWidth(), actual.getHeight(), rescale.getWidth());
		}
	}
	
	private static class RescaledTexture extends DynamicTexture {
		
		@Getter
		private Size size;
		
		public RescaledTexture(BufferedImage actual, BufferedImage rescale) {
			super(rescale);
			this.size = Size.create(actual, rescale);
		}
	}

	public static final ResourceLocation MISSING_TEXTURE = new ResourceLocation("missingno");

	private static Executor downloadExecutor = Executors.newFixedThreadPool(3);

	static {
		Minecraft.getMinecraft().getTextureManager().loadTexture(MISSING_TEXTURE, TextureUtil.missingTexture);
	}

	@Getter
	private final String fileName, urlBase;

	@Getter
	private Creation owner;

	private transient final EnumMap<ImageType, ResourceLocation> locations = Maps.newEnumMap(ImageType.class);
	private transient EnumMap<ImageType, Size> sizes = Maps.newEnumMap(ImageType.class);

    public DownloadableImage() {
        fileName = "";
        urlBase = "";
        initDefaults();
    }
    
	public DownloadableImage(Image image, Creation owner) {
		fileName = image.url.substring(image.url.lastIndexOf("/") + 1, image.url.length());
        urlBase = image.url.substring(0, image.url.indexOf("original"));
        this.owner = owner;
        initDefaults();
    }

    private void initDefaults() {
        for (ImageType type : ImageType.values()) {
            locations.put(type, MISSING_TEXTURE);
            sizes.put(type, new Size());
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

	private Size getSize(ImageType type) {
		return sizes.get(type);
	}

	/**
	 * The dimensions for this image.
	 * 
	 * @param type
	 *            The {@link ImageType} to get the dimensions for.
	 * @return An {@link Dimension} representing the size of this image. May be zero if the image is not downloaded.
	 */
	public Dimension getDimensions(ImageType type) {
		return getSize(type).getDimension();
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
	 * To avoid issues with certain GPUs, the in-memory image is scaled up to the nearest power of two square dimension. This method returns that value for use in rendering.
	 * 
	 * @param type
	 *            The {@link ImageType} to get the size for.
	 * @return The scaled size of this image.
	 */
	public int getScaledSize(ImageType type) {
		return getSize(type).getScaled();
	}

	/**
	 * This method is not blocking, but note that {@link #getSize(ImageType)} will return a 0-size rectangle before the image finishes downloading. Check for this with {@link #hasSize(ImageType)}.
	 * 
	 * @param type
	 *            The {@link ImageType} to download.
	 * @see #updateSize(ImageType)
	 */
	@SneakyThrows
	public void download(final ImageType type) {
		if (locations.get(type) == MISSING_TEXTURE) {
			TextureManager texturemanager = Minecraft.getMinecraft().getTextureManager();
			final String filepath = "creations/" + owner.user_id + "/" + type.urlString() + "/" + owner.id + ".jpg";
			final ResourceLocation res = new ResourceLocation(CTBMod.DOMAIN, filepath);
			ITextureObject texture = texturemanager.getTexture(res);

			if (texture == null) {

				downloadExecutor.execute(new Runnable() {

					@Override
					@SneakyThrows
					public void run() {
						String url = urlBase.concat(type.urlString()).concat("/").concat(fileName);
						File cache = new File(DataCache.cacheFolder, filepath);
						BufferedImage image = null;
						if (cache.exists()) {
							image = ImageIO.read(cache);
						} else {
							image = ImageIO.read(new URL(url));
							cache.getParentFile().mkdirs();
							// Cache the original, not the resize, this way we do not lose original size data
							ImageIO.write(image, "jpg", cache);
						}
						
						final BufferedImage original = image;

						// Find the biggest dimension of the image
						int maxDim = Math.max(image.getWidth(), image.getHeight());

						// Find nearest PoT which can contain the downloaded/read image
						int targetDim = 2;
						while (targetDim < maxDim) {
							targetDim *= 2;
						}

						// Create a blank image with PoT size
						final BufferedImage resized = new BufferedImage(targetDim, targetDim, image.getType());
						// Write the downloaded image into the top left of the blank image
						resized.createGraphics().drawImage(image, 0, 0, null);

						// Do this on the main thread with GL context
						Minecraft.getMinecraft().addScheduledTask(new Runnable() {

							@Override
							public void run() {
								RescaledTexture texture = new RescaledTexture(original, resized);
								Minecraft.getMinecraft().getTextureManager().loadTexture(res, texture);

								// Don't populate size and location data until after the texture is loaded
								sizes.put(type, Size.create(original, resized));
								locations.put(type, res);
							}
						});
					}
				});
			} else if (texture instanceof RescaledTexture) {
				// Grab cached size data
				sizes.put(type, ((RescaledTexture) texture).getSize());
				locations.put(type, res);
			}
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
		return sizes.get(type).getHeight() != 0;
	}
	
	public static GsonBuilder registerGsonAdapters(GsonBuilder builder) {
	    builder.registerTypeAdapter(new TypeToken<EnumMap<ImageType, ResourceLocation>>(){}.getType(), new JsonUtil.EnumMapInstanceCreator<ImageType, ResourceLocation>(ImageType.class));
	    builder.registerTypeAdapter(new TypeToken<EnumMap<ImageType, Size>>(){}.getType(), new JsonUtil.EnumMapInstanceCreator<ImageType, Size>(ImageType.class));
	    return builder;
	}
}
