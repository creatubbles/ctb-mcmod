package com.creatubbles.ctbmod.common.http;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.util.Dimension;

import com.creatubbles.api.core.Creation;
import com.creatubbles.api.core.Image;
import com.creatubbles.api.core.Image.ImageType;
import com.creatubbles.ctbmod.CTBMod;
import com.creatubbles.ctbmod.client.gui.GuiUtil;
import com.creatubbles.ctbmod.client.gui.LazyLoadedTexture;
import com.creatubbles.ctbmod.common.config.DataCache;
import com.creatubbles.ctbmod.common.util.JsonUtil;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;

@ToString
public class DownloadableImage {

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
            dimension = new Dimension(width, height);
        }

        public static Size create(BufferedImage actual, BufferedImage rescale) {
            return new Size(actual.getWidth(), actual.getHeight(), rescale.getWidth());
        }
    }

    private static class RescaledTexture extends LazyLoadedTexture {

        @Getter
        private final Size size;

        public RescaledTexture(BufferedImage actual, BufferedImage rescale) {
            super(rescale);
            size = Size.create(actual, rescale);
        }

        @Override
        public void loadTexture(IResourceManager resourceManager) throws IOException {}
    }
    
    public static final Vec3 ZERO_DISTANCE = Vec3.createVectorHelper(0, 0, 0);
    
    private class ImageDownloadTask implements Runnable {

        private final ImageType type;
        private final String filepath;
        private final ResourceLocation res;
        private final Vec3 position;
        private double distSq;
        
        private ImageDownloadTask(ImageType type, String filepath, ResourceLocation res, Vec3 position) {
            this.type = type;
            this.filepath = filepath;
            this.res = res;
            this.position = position;
            updateDistance();
        }
        
        @Override
        @SneakyThrows
        public void run() {
            String url = parent.links == null ? parent.url : parent.links.get(type);
            File cache = new File(DataCache.cacheFolder, filepath);
            BufferedImage image = null;
            if (cache.exists()) {
                image = ImageIO.read(cache);
            } else {
                image = ImageIO.read(new URL(url));
                cache.getParentFile().mkdirs();
                try {
                    // Cache the original, not the resize, this way we do not lose original size data
                    ImageIO.write(image, "png", cache);
                } catch (IOException e) {
                    // This is not strictly necessary, so we'll just log an error and download the image
                    // again next time
                    CTBMod.logger.error("Could not save image {} to {}. Stacktrace: ", cache.getName(), cache.getAbsolutePath());
                    e.printStackTrace();
                }
            }

            final BufferedImage original = image;
            final BufferedImage resized = GuiUtil.upsize(original, true);
            
            final RescaledTexture texture = new RescaledTexture(original, resized);

            // Do this on the main thread with GL context
            Minecraft.getMinecraft().func_152344_a(new Runnable() {

                @Override
                public void run() {
                    texture.uploadTexture();
                    Minecraft.getMinecraft().getTextureManager().loadTexture(res, texture);

                    // Don't populate size and location data until after the texture is loaded
                    sizes.put(type, Size.create(original, resized));
                    locations.put(type, res);
                    Set<ImageType> prog = inProgress.get(DownloadableImage.this);
                    prog.remove(type);
                    if (prog.isEmpty()) {
                        inProgress.remove(DownloadableImage.this);
                    }
                }
            });
        }
        
        private void updateDistance() {
            distSq = Minecraft.getMinecraft().thePlayer.getDistanceSq(position.xCoord, position.xCoord, position.zCoord);
        }
    }

    private static class TaskComparator implements Comparator<Runnable> {
        
        private TaskComparator() {
            MinecraftForge.EVENT_BUS.register(this);
        }
        
        @SubscribeEvent
        public void onClientTick(ClientTickEvent event) {
            if (Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().theWorld.getTotalWorldTime() % 20 == 0) {
                synchronized (tasks) {
                    Set<ImageDownloadTask> temp = Sets.newHashSet();
                    for (Runnable r : tasks) {
                        temp.add((ImageDownloadTask) r);
                    }
                    tasks.clear();
                    for (ImageDownloadTask task : temp) {
                        task.updateDistance();
                        tasks.offer(task);
                    }
                }
            }
        }

        @Override
        public int compare(Runnable o1, Runnable o2) {
            if (o1 == null && o2 == null)
                return 0;
            else if (o1 == null)
                return -1;
            else if (o2 == null)
                return 1;
            else {
                return Double.compare(((ImageDownloadTask) o1).distSq, ((ImageDownloadTask) o2).distSq);
            }
        }
    }

    public static final ResourceLocation MISSING_TEXTURE = new ResourceLocation("missingno");
    
    private static PriorityBlockingQueue<Runnable> tasks = new PriorityBlockingQueue<Runnable>(10, new TaskComparator());
    private static Executor downloadExecutor = new ThreadPoolExecutor(0, 1 /*Runtime.getRuntime().availableProcessors()*/, 20, TimeUnit.SECONDS, tasks); 
    
    private static Map<DownloadableImage, Set<ImageType>> inProgress = Maps.newConcurrentMap();

    static {
        Minecraft.getMinecraft().getTextureManager().loadTexture(MISSING_TEXTURE, TextureUtil.missingTexture);
    }

    @Getter
    private Creation owner;

    private transient final EnumMap<ImageType, ResourceLocation> locations = Maps.newEnumMap(ImageType.class);
    private transient EnumMap<ImageType, Size> sizes = Maps.newEnumMap(ImageType.class);
    private transient Image parent;

    public DownloadableImage() {
        initDefaults();
    }

    public DownloadableImage(Image image, Creation owner) {
        this.parent = image;
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
     * @return A {@link ResourceLocation}, which may be a dummy if this Image has not been downloaded, or is in the
     *         process of being downloaded.
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
     * To avoid issues with certain GPUs, the in-memory image is scaled up to the nearest power of two square dimension.
     * This method returns that value for use in rendering.
     *
     * @param type
     *            The {@link ImageType} to get the size for.
     * @return The scaled size of this image.
     */
    public int getScaledSize(ImageType type) {
        return getSize(type).getScaled();
    }

    /**
     * This method is not blocking, but note that {@link #getSize(ImageType)} will return a 0-size rectangle before the
     * image finishes downloading. Check for this with {@link #hasSize(ImageType)}.
     * 
     * Use {@link #download(ImageType, Vec3)} to specify distance. This method defaults to {@link #ZERO_DISTANCE}.
     * 
     * @param type
     *            The {@link ImageType} to download.
     * @see #updateSize(ImageType)
     */
    public void download(ImageType type) {
        download(type, ZERO_DISTANCE);
    }
    
    /**
     * This method is not blocking, but note that {@link #getSize(ImageType)} will return a 0-size rectangle before the
     * image finishes downloading. Check for this with {@link #hasSize(ImageType)}.
     * 
     * @param type
     *            The {@link ImageType} to download.
     * @param position
     *            The {@link Vec3} representing the position of the image in the world. Images closer to the client
     *            player will be prioritized. Use {@link #ZERO_DISTANCE} for GUI.
     * @see #updateSize(ImageType)
     */
    @SneakyThrows
    public void download(final ImageType type, Vec3 position) {
        Set<ImageType> prog = inProgress.get(this);
        if (locations.get(type) == MISSING_TEXTURE && (prog == null || !prog.contains(type))) {
            TextureManager texturemanager = Minecraft.getMinecraft().getTextureManager();
            final String filepath = "creations/" + owner.user_id + "/" + type.name() + "/" + owner.id + ".jpg";
            final ResourceLocation res = new ResourceLocation(CTBMod.DOMAIN, filepath);
            ITextureObject texture = texturemanager.getTexture(res);

            if (texture == null) {

                if (!inProgress.containsKey(this)) {
                    inProgress.put(this, Sets.<ImageType>newConcurrentHashSet());
                }
                
                inProgress.get(this).add(type);
                downloadExecutor.execute(new ImageDownloadTask(type, filepath, res, position));
                
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
        builder.registerTypeAdapter(new TypeToken<EnumMap<ImageType, ResourceLocation>>() {}.getType(), new JsonUtil.EnumMapInstanceCreator<ImageType, ResourceLocation>(ImageType.class));
        builder.registerTypeAdapter(new TypeToken<EnumMap<ImageType, Size>>() {}.getType(), new JsonUtil.EnumMapInstanceCreator<ImageType, Size>(ImageType.class));
        return builder;
    }
}
