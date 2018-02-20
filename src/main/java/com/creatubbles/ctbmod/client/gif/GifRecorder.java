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
import com.madgag.gif.fmsware.AnimatedGifEncoder;

import jersey.repackaged.com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.util.math.MathHelper;
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
    
    @RequiredArgsConstructor
    private static class GifWriteTask implements Callable<File> {
        
        private final Queue<int[]> frames;
        private final Framebuffer buffer;
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
//                enc.setQuality(1); TODO quality configurable?

                BufferedImage prevFrame = null;
                
                try {
                    
                    while (!finished || !frames.isEmpty()) {
                        int[] frame = frames.poll();
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
    
                            if (OpenGlHelper.isFramebufferEnabled()) {
                                bufferedimage = new BufferedImage(buffer.framebufferWidth, buffer.framebufferHeight, 1);
                                bufferedimage.setRGB(0, 0, buffer.framebufferTextureWidth, buffer.framebufferTextureHeight, frame, 0, buffer.framebufferTextureWidth);
                            } else {
                                bufferedimage = new BufferedImage(width, height, 1);
                                bufferedimage.setRGB(0, 0, width, height, frame, 0, width);
                            }
                            
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
                            
                            if (prevFrame != null) { // Basic inter-frame compression by discarding similar pixels
                                frame = ((DataBufferInt)bufferedimage.getRaster().getDataBuffer()).getData();
                                int[] prevFrameData = ((DataBufferInt)prevFrame.getRaster().getDataBuffer()).getData();
                                for (int i = 0; i < frame.length; i++) {
                                    int newColor = frame[i];
                                    int oldColor = prevFrameData[i];
                                    // If this pixel is reasonably similar to the previous color, set it transparent
                                    if (almostEqual(newColor, oldColor, 2 / 255f)) {
                                        frame[i] = TRANSPARENT.getRGB();
                                    } else {
                                        prevFrameData[i] = newColor;
                                    }
                                }
                                enc.setDispose(1);
                            } else { // First frame, no transparency
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

    /** A buffer to hold pixel values returned by OpenGL. */
    private static IntBuffer pixelBuffer;

    private static Queue<int[]> frames = Lists.newLinkedList();

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static Future<File> future = null;
    private static GifWriteTask task = null;
    
    private static final int MAX_STEPS = 30;
    private static final Color TRANSPARENT = new Color(0, true);
    
    private static int framesRecorded, framesProcessed;
    private static int lastStep;
    
    public static RecordingStatus status = RecordingStatus.OFF;

    @SubscribeEvent
    @SneakyThrows
    public void onClientTick(ClientTickEvent event) {
        if (event.phase == Phase.START) {
            return;
        }

        final Framebuffer buffer = Minecraft.getMinecraft().getFramebuffer();

        int width = Minecraft.getMinecraft().displayWidth;
        int height = Minecraft.getMinecraft().displayHeight;

        if (Keyboard.isKeyDown(Keyboard.KEY_F7)) {
            
            status = RecordingStatus.LIVE;

            if (OpenGlHelper.isFramebufferEnabled()) {
                width = buffer.framebufferTextureWidth;
                height = buffer.framebufferTextureHeight;
            }

            int i = width * height;

            int[] pixelValues = new int[i];

            if (pixelBuffer == null || pixelBuffer.capacity() < i) {
                pixelBuffer = BufferUtils.createIntBuffer(i);
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
            frames.add(pixelValues);
            framesRecorded++;
            
            if (task == null) {
                future = executor.submit(task = new GifWriteTask(frames, buffer, width, height));
            }
        } else if (task != null) {            
            task.finished(true);
            StringBuffer sb = new StringBuffer("Writing Gif: ");
            int step = MathHelper.ceiling_float_int(MAX_STEPS * ((float) framesProcessed / framesRecorded));
            if (step != lastStep) {
                status = RecordingStatus.SAVING;
                sb.append("[");
                for (int i = 0; i < step; i++) {
                    sb.append("||");
                }
                for (int i = step; i < MAX_STEPS; i++) {
                    sb.append(" ");
                }
                sb.append("]");
                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new TextComponentString(sb.toString()), task.hashCode());
                lastStep = step;
            } else if (future.isDone()) {
                status = RecordingStatus.OFF;
                TextComponentString msg = new TextComponentString(sb.toString());
                if (future.get() == null) {
                    msg.appendSibling(new TextComponentString(TextFormatting.RED + "Failed! See console for errors."));
                } else {
                    TextComponentString doneTag = new TextComponentString(TextFormatting.GREEN.toString() + "[Done!]");
                    doneTag.setStyle(new Style().setClickEvent(
                            new ClickEvent(ClickEvent.Action.OPEN_FILE, future.get().getAbsolutePath())).setHoverEvent(
                            new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Click to view gif: " + future.get().getName()))
                    ));

                    msg.appendSibling(doneTag);
                }
                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(msg, task.hashCode());
                future = null;
                task = null;
                framesRecorded = framesProcessed = lastStep = 0;
            }
        }
    }
}
