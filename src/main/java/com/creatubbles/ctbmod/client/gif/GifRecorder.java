package com.creatubbles.ctbmod.client.gif;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.IntBuffer;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.IOUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.creatubbles.ctbmod.CTBMod;
import com.creatubbles.ctbmod.common.config.Configs;
import com.creatubbles.ctbmod.common.config.DataCache;
import com.madgag.gif.fmsware.AnimatedGifEncoder;

import jersey.repackaged.com.google.common.collect.Lists;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.experimental.Accessors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

/** Most code taken from ScreenShotHelper with the File IO code replaced */
public class GifRecorder {
    
    @Value
    private static class GifFrame {
        int[] pixels;
        int width, height;
    }
    
    @RequiredArgsConstructor
    private static class GifWriteTask implements Callable<File> {
        
        private final Queue<GifFrame> frames;
        private final int width, height;
        
        @Setter
        @Accessors(fluent = true)
        private boolean finished;
        
        @Override
        public File call() throws Exception {
            
            File gifFolder = new File(Minecraft.getMinecraft().mcDataDir, "gifs");
            gifFolder.mkdirs();
            File res = ScreenShotHelper.getTimestampedPNGFileForDirectory(gifFolder);
            res = new File(res.getAbsolutePath().replace(".png", ".gif")).getCanonicalFile();
            
            try {

                final AnimatedGifEncoder enc = new AnimatedGifEncoder();
                final FileOutputStream os = new FileOutputStream(res);
                
                enc.start(os);
                enc.setDelay(50);
                enc.setRepeat(0);
                enc.setQuality(state.getQuality());

                BufferedImage prevFrame = null;
                
                try {
                    
                    while (!finished || !frames.isEmpty()) {
                        GifFrame frame = frames.poll();
                        if (frame == null) {
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                break;
                            }
                        } else {

                            // Let's add some inter-frame sleep to avoid CPU spiking and disk thrashing
                            // This makes the output a bit slower but results in a more stable FPS during recording
                            Thread.sleep(25);

                            BufferedImage bufferedimage = null;
    
                            bufferedimage = new BufferedImage(frame.getWidth(), frame.getHeight(), 1);
                            bufferedimage.setRGB(0, 0, frame.getWidth(), frame.getHeight(), frame.getPixels(), 0, frame.getWidth());
                            
                            float scale = Math.min(
                                (float) Configs.maxGifWidth / width,
                                (float) Configs.maxGifHeight / height
                            );
                            if (scale < 1) {
                                int w = (int) (width * scale);
                                int h = (int) (height * scale);
                                BufferedImage resizedImg = new BufferedImage(w, h, bufferedimage.getType());
                                Graphics2D g2 = resizedImg.createGraphics();
                                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                                g2.drawImage(bufferedimage, 0, 0, w, h, null);
                                g2.dispose();
                                bufferedimage = resizedImg;
                            }
                            
                            // Basic inter-frame compression by discarding similar pixels
                            if (prevFrame != null && prevFrame.getWidth() == bufferedimage.getWidth() && prevFrame.getHeight() == bufferedimage.getHeight()) {
                                int[] pixels = ((DataBufferInt)bufferedimage.getRaster().getDataBuffer()).getData();
                                int[] prevPixels = ((DataBufferInt)prevFrame.getRaster().getDataBuffer()).getData();
                                for (int i = 0; i < pixels.length; i++) {
                                    int newColor = pixels[i];
                                    int oldColor = prevPixels[i];
                                    // If this pixel is reasonably similar to the previous color, set it transparent
                                    if (almostEqual(newColor, oldColor, state.getCompression())) {
                                        pixels[i] = TRANSPARENT.getRGB();
                                    } else {
                                        prevPixels[i] = newColor;
                                    }
                                }
                                enc.setDispose(1);
                            } else { // First frame or different size, no transparency
                                prevFrame = bufferedimage;
                            }
                            
                            enc.addFrame(bufferedimage);
                            enc.setTransparent(TRANSPARENT); // Initialize this *after* the first frame
                            framesProcessed++;
                        }
                    }

                    enc.finish();
                } catch (Exception e) {
                    CTBMod.logger.error("Error writing gif.", e);
                    return null;
                } finally {
                    IOUtils.closeQuietly(os);
                }
                                
            } catch (FileNotFoundException e) {
                CTBMod.logger.error("Could not create gif file.", e);
                return null;
            } finally {
                frames.clear();
            }
            
            return res;
        }
        
        private float[] comp1 = new float[4];
        private float[] comp2 = new float[4];

        /**
         * Returns true if the two colors are equal with in an epsilon value.
         * @param col1 The first color
         * @param col2 The second color
         * @param i An epsilon value, in integer pixel color difference.
         * @return
         */
        private boolean almostEqual(int col1, int col2, float eps) {
            new Color(col1, true).getComponents(comp1);
            new Color(col2, true).getComponents(comp2);
            float sum = 0;
            for (int i = 0; i < 4; i++) {
                sum += Math.abs(comp1[i] - comp2[i]);
            }
            return sum <= eps;
        }
    }
    
    public static final KeyBinding KEY_RECORD = new KeyBinding("ctb.key.record", Keyboard.KEY_F4, "key.categories.misc");

    /** A buffer to hold pixel values returned by OpenGL. */
    private static IntBuffer pixelBuffer;

    private static Queue<GifFrame> frames = Lists.newLinkedList();

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static Future<File> future = null;
    private static GifWriteTask task = null;
    
    private static final Color TRANSPARENT = new Color(0, true);
    
    private static int framesRecorded, framesProcessed;
    
    @Getter
    private static GifState state = GifState.DEFAULT;

    @SubscribeEvent
    @SneakyThrows
    public void onClientTick(ClientTickEvent event) {
        if (event.phase == Phase.START) {
            return;
        }

        state.tick();

        switch (state.getStatus()) {
        case OFF:
            // Begin a new recording with the same settings as previous
            if (KEY_RECORD.isPressed()) {
                state = new GifState(state.getQuality(), state.getCompression(), state.getMaxLength());
            }
            break;
        case PREPARING:
            break;
        case LIVE:
            // If record key is pressed again, immediately end the recording
            if (GifRecorder.KEY_RECORD.isPressed()) {
                state.stop();
            }
            final Framebuffer buffer = Minecraft.getMinecraft().getFramebuffer();

            int width = Minecraft.getMinecraft().displayWidth;
            int height = Minecraft.getMinecraft().displayHeight;

            if (OpenGlHelper.isFramebufferEnabled()) {
                width = buffer.framebufferTextureWidth;
                height = buffer.framebufferTextureHeight;
            }

            int size = width * height;

            int[] pixelValues = new int[size];

            if (pixelBuffer == null || pixelBuffer.capacity() < size) {
                pixelBuffer = BufferUtils.createIntBuffer(size);
            }

            GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
            pixelBuffer.clear();

            if (OpenGlHelper.isFramebufferEnabled()) {
                GlStateManager.bindTexture(buffer.framebufferTexture);
                GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, pixelBuffer);
            } else {
                GL11.glReadPixels(0, 0, width, height, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, pixelBuffer);
            }

            pixelBuffer.get(pixelValues);
            TextureUtil.processPixelValues(pixelValues, width, height);
            frames.add(new GifFrame(pixelValues, width, height));
            framesRecorded++;

            if (task == null) {
                future = executor.submit(task = new GifWriteTask(frames, width, height));
            }
            break;
        case SAVING:
            task.finished(true);
            state.setSaveProgress((float) framesProcessed / framesRecorded);
            if (future.isDone()) {
                TextComponentString msg;
                if (future.get() == null) {
                    msg = new TextComponentString(TextFormatting.RED + "Failed! See console for errors.");
                } else {
                    TextComponentString doneTag = new TextComponentString(TextFormatting.GREEN.toString() + "[View " + future.get().getName() + "]");
                    doneTag.setStyle(new Style().setClickEvent(
                            new ClickEvent(ClickEvent.Action.OPEN_FILE, future.get().getCanonicalPath())).setHoverEvent(
                                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Click to open GIF"))
                                    ));

                    msg = doneTag;
                }
                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(msg, task.hashCode());
                future = null;
                task = null;
                state.saved();
                framesRecorded = framesProcessed = 0;
            }
        }
        
        // Clear unused key press
        KEY_RECORD.isPressed();
    }
    
    public static void setState(GifState state) {
        GifRecorder.state = state;
        CTBMod.cache.setRecordingData(state);
        CTBMod.cache.save();
    }
}
