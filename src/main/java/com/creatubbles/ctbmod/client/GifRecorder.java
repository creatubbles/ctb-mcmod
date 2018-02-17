package com.creatubbles.ctbmod.client;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
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
import com.creatubbles.repack.dragon66.AnimatedGIFWriter;

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

                final AnimatedGIFWriter writer = new AnimatedGIFWriter(true);
                final FileOutputStream os = new FileOutputStream(res);

                try {
                    writer.prepareForWrite(os, -1, -1);
                    
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
                            
                            float scale;
                            if (width > height) {
                                scale = 500.0f / width;
                            } else {
                                scale = 500.0f / height;
                            }
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
                            
                            writer.writeFrame(os, bufferedimage, 50);
                            framesProcessed++;
                        }
                    }

                    writer.finishWrite(os);
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
    }

    /** A buffer to hold pixel values returned by OpenGL. */
    private static IntBuffer pixelBuffer;

    private static Queue<int[]> frames = Lists.newLinkedList();

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static Future<File> future = null;
    private static GifWriteTask task = null;
    
    private static final int MAX_STEPS = 30;
    
    private static int framesRecorded, framesProcessed;
    private static int lastStep;

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
