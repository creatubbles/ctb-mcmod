package com.creatubbles.ctbmod.client.gui.upload;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;

import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.lwjgl.input.Mouse;

import com.creatubbles.ctbmod.client.gui.GuiUtil;
import com.creatubbles.ctbmod.client.gui.creator.OverlayBase;
import com.creatubbles.ctbmod.client.gui.upload.ThumbnailStitcher.Progress;
import com.creatubbles.repack.endercore.api.client.gui.IGuiScreen;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public class OverlayScreenshotThumbs extends OverlayBase<GuiScreenshotList> {

    private static final ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());

    private static ThumbnailStitcher prevStitcher = null;
    private static File[] prevFiles = null;

    ThumbnailStitcher stitcher;
    private ListenableFuture<?> stitchTask;

    private List<File> screenshots;
    private List<ThumbnailAndLocation> thumbnails = Lists.newArrayList();
    private ListenableFuture<?> listTask;

    private int thumbSize = 64;
    private int padding = 16;

    @Getter
    private int pages;
    @Getter
    private int page;

    public OverlayScreenshotThumbs(int x, int y, Dimension dimension) {
        super(x, y, dimension);

        final File[] files = new File(Minecraft.getMinecraft().mcDataDir, "screenshots").listFiles((FilenameFilter) FileFilterUtils.suffixFileFilter(".png"));
        if (Arrays.equals(files, prevFiles)) {
            stitcher = prevStitcher;
        } else {
            prevFiles = files;
            if (prevStitcher != null) {
                prevStitcher.dispose();
            }
            stitcher = prevStitcher = new ThumbnailStitcher();
            stitchTask = executor.submit(new Runnable() {

                @Override
                public void run() {
                    stitcher.loadFiles(files);
                }
            });
        }

        screenshots = Lists.newArrayList(files);
        Collections.sort(screenshots, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
    }

    @Value
    private class ThumbnailAndLocation {

        private File file;
        private Rectangle slot;
        private Point location;
        private Rectangle bounds;
        private int page;

        private ThumbnailAndLocation(File f, Point p, int page) {
            this.file = f;
            this.slot = new Rectangle(stitcher.getRect(f));
            this.location = p;
            this.page = page;

            int widthOff = ((stitcher.getThumbWidth() - slot.width) / 2);
            int heightOff = ((stitcher.getThumbHeight() - slot.height) / 2);

            float resizeRatio = 64f / stitcher.getThumbWidth();

            bounds = new Rectangle(p.x + (int) (widthOff * resizeRatio), p.y + (int) (heightOff * resizeRatio), 64 - (int) (widthOff * resizeRatio * 2), 64 - (int) (heightOff * resizeRatio * 2));
        }
    }

    @Override
    public void init(IGuiScreen screen) {
        super.init(screen);

        thumbnails.clear();

        Runnable listBuilder = new Runnable() {

            public void run() {
                // If the stitcher is invalid, there will be a crash next tick
                if (stitcher.isValid()) {
                    int perRow = (getWidth() - padding) / (padding + thumbSize);
                    int perCol = (getHeight() - padding) / (padding + thumbSize);

                    pages = (int) Math.ceil(((double) screenshots.size() / (perRow * perCol)));
                    page = MathHelper.clamp_int(page, 0, pages - 1);

                    int xOff = (getWidth() - (padding + ((thumbSize + padding) * perRow))) / 2;
                    int yOff = (getHeight() - (padding + ((thumbSize + padding) * perCol))) / 2;

                    int row = 0, col = 0, page = 0;
                    for (File f : screenshots) {
                        int x = getX() + xOff + padding + ((thumbSize + padding) * col);
                        int y = getY() + yOff + padding + ((thumbSize + padding) * row);
                        thumbnails.add(new ThumbnailAndLocation(f, new Point(x, y), page));
                        if (++col >= perRow) {
                            col = 0;
                            row++;
                        }
                        if (row >= perCol) {
                            row = col = 0;
                            page++;
                        }
                    }
                }
            }
        };

        if (stitchTask == null || stitchTask.isDone()) {
            listBuilder.run();
        } else {
            listTask = executor.submit(listBuilder);
        }
    }

    /**
     * Runs a task after the list is built. Not guaranteed to run on the client thread so this needs to be a threadsafe
     * operation.
     * 
     * @param toRun
     *            The task to run.
     */
    public void onListBuilt(Runnable toRun) {
        if (listTask == null || listTask.isDone()) {
            toRun.run();
        } else {
            listTask.addListener(toRun, MoreExecutors.sameThreadExecutor());
        }
    }

    @Override
    @SneakyThrows
    protected void doDraw(int mouseX, int mouseY, float partialTick) {
        if (stitchTask != null && (stitchTask.isDone() || stitchTask.isCancelled())) {
            try {
                stitchTask.get();
            } catch (ExecutionException e) {
                throw e.getCause();
            }
        }
        
        super.doDraw(mouseX, mouseY, partialTick);
        
        GuiUtil.drawSlotBackground(getX(), getY(), getWidth(), getHeight());

        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.pushMatrix();

        if ((listTask == null || listTask.isDone()) && stitcher.getRes() != null) {
            Minecraft.getMinecraft().getTextureManager().bindTexture(stitcher.getRes());
            for (ThumbnailAndLocation thumb : thumbnails) {
                if (thumb.getPage() == page) {
                    Rectangle bounds = thumb.getBounds();
                    Rectangle slot = thumb.getSlot();
                    if (bounds.contains(mouseX, mouseY)) {
                        drawRect(bounds.x - 1, bounds.y - 1, bounds.x + bounds.width + 1, bounds.y + bounds.height + 1, 0xFFFFFFFF);
                    }
                    drawScaledCustomSizeModalRect(bounds.x, bounds.y, slot.x, slot.y, slot.width, slot.height, bounds.width, bounds.height, stitcher.getWidth(), stitcher.getHeight());
                }
            }
        } else {
            GlStateManager.enableBlend();
            int x = getX() + (getWidth() / 2), y = getY() + (getHeight() / 2);
            GuiUtil.drawLoadingTex(x - 32, y - 32, 64, 64);
            Progress prog = stitcher.getProgress();
            
            y += 42;
            drawCenteredString(getGui().getFontRenderer(), prog.getDesc(), x, y, 0xFFFFFF);
            
            y += 20;
            int w = 70, h = 7, b = 1;
            drawRect(x - w - b, y - h - b, x + w + b, y - h, 0xFFFFFFFF);
            drawRect(x - w - b, y + h + b, x + w + b, y + h, 0xFFFFFFFF);
            drawRect(x - w - b, y - h - b, x - w, y + h + b, 0xFFFFFFFF);
            drawRect(x + w + b, y - h - b, x + w, y + h + b, 0xFFFFFFFF);

            float progress = prog.getMax() == 0 ? 0 : (float) prog.getCurrent() / prog.getMax();
            int bar = (int) (w * 2 * progress);
            drawRect(x - w, y - h, x - w + bar, y + h, 0xFF5DC5E8);
            
            y -= 4;
            drawCenteredString(getGui().getFontRenderer(), prog.getCurrent() + " / " + prog.getMax(), x, y, 0xFFFFFF);
        }

        GlStateManager.popMatrix();
    }

    @Override
    public boolean handleMouseInput(int x, int y, int b) {
        if (b == 0 && Mouse.getEventButtonState() /* ignore releases */) {
            for (ThumbnailAndLocation thumb : thumbnails) {
                if (thumb.getPage() == page && thumb.getBounds().contains(x, y)) {
                    Minecraft.getMinecraft().displayGuiScreen(new GuiUploadScreenshot(getGui(), screenshots.toArray(new File[0]), screenshots.indexOf(thumb.getFile())));
                }
            }
        }
        return super.handleMouseInput(x, y, b);
    }

    public void page(int delta) {
        this.page = MathHelper.clamp_int(page + delta, 0, pages - 1);
    }
}
