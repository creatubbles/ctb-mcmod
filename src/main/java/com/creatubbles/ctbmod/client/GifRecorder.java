package com.creatubbles.ctbmod.client;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.IntBuffer;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import jersey.repackaged.com.google.common.collect.Lists;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ScreenShotHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

import org.apache.commons.io.IOUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.creatubbles.ctbmod.CTBMod;
import com.creatubbles.repack.dragon66.AnimatedGIFWriter;

/** Most code taken from ScreenShotHelper with the File IO code replaced */
public class GifRecorder {
    
    @RequiredArgsConstructor
    private static class GifWriteTask implements Callable<File> {
        
        private final List<int[]> frames;
        private final Framebuffer buffer;
        private final int width, height;
        
        @Getter
        private float percentComplete;
        
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
                    
                    float percPerFrame = 1.0f / frames.size();

                    for (int[] frame : frames) {
                        BufferedImage bufferedimage = null;

                        if (OpenGlHelper.isFramebufferEnabled()) {
                            bufferedimage = new BufferedImage(buffer.framebufferWidth, buffer.framebufferHeight, 1);
                            int j = buffer.framebufferTextureHeight - buffer.framebufferHeight;

                            for (int k = j; k < buffer.framebufferTextureHeight; ++k) {
                                for (int l = 0; l < buffer.framebufferWidth; ++l) {
                                    bufferedimage.setRGB(l, k - j, frame[k * buffer.framebufferTextureWidth + l]);
                                }
                            }
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
                        
                        writer.writeFrame(os, bufferedimage, 30);
                        percentComplete = Math.min(1, percentComplete + percPerFrame);
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

    private static List<int[]> frames = Lists.newArrayList();

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static Future<File> future = null;
    private static GifWriteTask task = null;
    
    private static final int MAX_STEPS = 30;
    private static int lastStep = 0;

    @SubscribeEvent
    @SneakyThrows
    public void onClientTick(ClientTickEvent event) {

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

        } else if (task != null) {
            StringBuffer sb = new StringBuffer("Writing Gif ");
            int step = MathHelper.ceiling_float_int(MAX_STEPS * task.getPercentComplete());
            if (step != lastStep) {
                sb.append("[");
                for (int i = 0; i < step; i++) {
                    sb.append("||");
                }
                for (int i = step; i < MAX_STEPS; i++) {
                    sb.append(" ");
                }
                sb.append("]");
                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new ChatComponentText(sb.toString()), task.hashCode());
                lastStep = step;
            } else if (future.isDone()) {
                ChatComponentText msg = new ChatComponentText(sb.toString());
                if (future.get() == null) {
                    msg.appendSibling(new ChatComponentText(EnumChatFormatting.RED + "Failed! See console for errors."));
                } else {
                    ChatComponentText doneTag = new ChatComponentText(EnumChatFormatting.GREEN.toString() + "[Done!]");
                    doneTag.setChatStyle(new ChatStyle().setChatClickEvent(
                            new ClickEvent(ClickEvent.Action.OPEN_FILE, future.get().getAbsolutePath())).setChatHoverEvent(
                            new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("Click to view gif: " + future.get().getName()))
                    ));

                    msg.appendSibling(doneTag);
                }
                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(msg, task.hashCode());
                future = null;
                task = null;
                lastStep = 0;
            }
        } else if (!frames.isEmpty()) {
            future = executor.submit(task = new GifWriteTask(frames, buffer, width, height));
        }
    }
}
