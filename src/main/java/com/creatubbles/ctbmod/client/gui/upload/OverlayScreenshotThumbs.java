package com.creatubbles.ctbmod.client.gui.upload;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

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
import java.util.concurrent.Future;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.experimental.NonFinal;
import net.minecraft.client.Minecraft;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.MathHelper;

import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.creatubbles.ctbmod.client.gui.GuiUtil;
import com.creatubbles.ctbmod.client.gui.creator.OverlayBase;
import com.creatubbles.ctbmod.client.gui.upload.ThumbnailStitcher.Progress;
import com.creatubbles.repack.endercore.api.client.gui.IGuiScreen;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public class OverlayScreenshotThumbs extends OverlayBase<GuiScreenshotList> {

    private final ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(2));

    private static ThumbnailStitcher[] prevStitchers = new ThumbnailStitcher[3];
    private static File[] prevFiles = null;
    private static ScaledResolution prevSize;

    private TIntObjectMap<ThumbnailStitcher> stitchers = new TIntObjectHashMap<ThumbnailStitcher>();
    private TIntObjectMap<ListenableFuture<?>> stitchTasks = new TIntObjectHashMap<ListenableFuture<?>>();

    private List<File> screenshots;
    private List<ThumbnailAndLocation> thumbnails = Lists.newArrayList();
    private ListenableFuture<?> listTask;

    private int thumbSize = 64;
    private int padding = 10;

    @Getter
    private int pages;
    @Getter
    private int page;

    public OverlayScreenshotThumbs(int x, int y, Dimension dimension) {
        super(x, y, dimension);

        final File[] files = new File(Minecraft.getMinecraft().mcDataDir, "screenshots").listFiles((FilenameFilter) FileFilterUtils.suffixFileFilter(".png"));
                
        if (files == null) {
            screenshots = Lists.newArrayList();
            return;
        }
        
        if (!Arrays.equals(files, prevFiles)) {
            prevFiles = files;
            for (int i = 0; i < prevStitchers.length; i++) {
                if (prevStitchers[i] != null) {
                    prevStitchers[i].dispose();
                }
            }
            Arrays.fill(prevStitchers, null);
        }

        screenshots = Lists.newArrayList(files);
        Collections.sort(screenshots, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
    }

    private void load(int... pages) {
        for (int i : pages) {
            if (i < 0 || i >= this.pages || stitchers.containsKey(i)) {
                continue;
            }
            ThumbnailStitcher temp;
            if (i <= 2) {
                if (prevStitchers[i] != null) {
                    temp = prevStitchers[i];
                } else {
                    prevStitchers[i] = temp = new ThumbnailStitcher();
                }
            } else {
                temp = new ThumbnailStitcher();
            }
            
            final ThumbnailStitcher stitcher = temp;
            
            stitchers.put(i, stitcher);
            stitchTasks.put(i, executor.submit(new Runnable() {

                @Override
                public void run() {
                    Iterable<ThumbnailAndLocation> iter = FluentIterable.from(thumbnails).filter(new Predicate<ThumbnailAndLocation>() {

                        @Override
                        public boolean apply(ThumbnailAndLocation input) {
                            return input.page == page;
                        }
                    });
                    if (!stitcher.isValid()) {
                        stitcher.loadFiles(FluentIterable.from(iter).transform(new Function<ThumbnailAndLocation, File>() {

                            public File apply(ThumbnailAndLocation t) {
                                return t.file;
                            }
                        }).toArray(File.class));
                    }
                    for (ThumbnailAndLocation thumb : iter) {
                        thumb.slot = new Rectangle(stitcher.getRect(thumb.file));

                        int widthOff = ((stitcher.getThumbWidth() - thumb.slot.width) / 2);
                        int heightOff = ((stitcher.getThumbHeight() - thumb.slot.height) / 2);

                        float resizeRatio = 64f / stitcher.getThumbWidth();

                        Point p = thumb.location;
                        thumb.bounds = new Rectangle(p.x + (int) (widthOff * resizeRatio), p.y + (int) (heightOff * resizeRatio), 64 - (int) (widthOff * resizeRatio * 2), 64 - (int) (heightOff
                                * resizeRatio * 2));
                    }
                }
            }));
        }
    }

    @Value
    private class ThumbnailAndLocation {

        private File file;
        private Point location;
        private int page;

        @NonFinal
        private Rectangle slot = new Rectangle();
        @NonFinal
        private Rectangle bounds = new Rectangle();

        private ThumbnailAndLocation(File f, Point p, int page) {
            this.file = f;
            this.location = p;
            this.page = page;
         }
    }

    @Override
    public void init(IGuiScreen screen) {
        super.init(screen);

        Minecraft mc = Minecraft.getMinecraft();

        if (prevSize == null) {
            prevSize = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        }
        
        thumbnails.clear();

        int perRow = (getWidth() - padding) / (padding + thumbSize);
        int perCol = (getHeight() - padding) / (padding + thumbSize);

        pages = Math.max(1, (int) Math.ceil(((double) screenshots.size() / (perRow * perCol))));
        page = MathHelper.clamp_int(page, 0, pages - 1);

        int xOff = (getWidth() - (padding + ((thumbSize + padding) * perRow))) / 2;
        int yOff = (getHeight() - (padding + ((thumbSize + padding) * perCol))) / 2;

        int row = 0, col = 0, p = 0;
        for (File f : screenshots) {
            int x = getX() + xOff + padding + ((thumbSize + padding) * col);
            int y = getY() + yOff + padding + ((thumbSize + padding) * row);
            thumbnails.add(new ThumbnailAndLocation(f, new Point(x, y), p));
            if (++col >= perRow) {
                col = 0;
                row++;
            }
            if (row >= perCol) {
                row = col = 0;
                p++;
            }
        }

        ScaledResolution size = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        if (prevStitchers[0] == null || size.getScaledHeight() != prevSize.getScaledHeight() || size.getScaledWidth() != prevSize.getScaledWidth()) {

            for (ThumbnailStitcher s : stitchers.valueCollection()) {
                s.dispose();
            }
        
            stitchers.clear();
            stitchTasks.clear();
            
            Arrays.fill(prevStitchers, null);
            prevSize = size;
        }
        
        load(page, page + 1, page - 1, page + 2, page - 2);
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
        ListenableFuture<?> stitchTask = stitchTasks.get(page);
        if (stitchTask != null && (stitchTask.isDone() || stitchTask.isCancelled())) {
            try {
                stitchTask.get();
            } catch (ExecutionException e) {
                throw e.getCause();
            }
        }
        
        super.doDraw(mouseX, mouseY, partialTick);
        
        GuiUtil.drawSlotBackground(getX(), getY(), getWidth(), getHeight());

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glPushMatrix();

        ThumbnailStitcher stitcher = stitchers.get(page);
        Future<?> task = stitchTasks.get(page);
        if (task != null && task.isDone() && stitcher.getRes() != null) {
            Minecraft.getMinecraft().getTextureManager().bindTexture(stitcher.getRes());
            for (ThumbnailAndLocation thumb : thumbnails) {
                if (thumb.getPage() == page) {
                    Rectangle bounds = thumb.getBounds();
                    Rectangle slot = thumb.getSlot();
                    if (bounds.contains(mouseX, mouseY)) {
                        drawRect(bounds.x - 1, bounds.y - 1, bounds.x + bounds.width + 1, bounds.y + bounds.height + 1, 0xFFFFFFFF);
                    }
                    func_152125_a(bounds.x, bounds.y, slot.x, slot.y, slot.width, slot.height, bounds.width, bounds.height, stitcher.getWidth(), stitcher.getHeight());
                }
            }
        } else if (stitcher != null) {
            GL11.glEnable(GL11.GL_BLEND);
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

        GL11.glPopMatrix();
    }

    @Override
    public boolean handleMouseInput(int x, int y, int b) {
        if (b == 0 && Mouse.getEventButtonState() /* ignore releases */) {
            for (ThumbnailAndLocation thumb : thumbnails) {
                if (thumb.getPage() == page && thumb.getBounds() != null && thumb.getBounds().contains(x, y)) {
                    Minecraft.getMinecraft().displayGuiScreen(new GuiUploadScreenshot(getGui(), screenshots.toArray(new File[0]), screenshots.indexOf(thumb.getFile())));
                }
            }
        }
        return super.handleMouseInput(x, y, b);
    }
    
    @Override
    public void guiClosed() {
        super.guiClosed();
        executor.shutdown();
        for (int i : stitchers.keys()) {
            if (i > 2) {
                stitchers.get(i).dispose();
            }
        }
    }

    public void page(int delta) {
        delta = MathHelper.clamp_int(-1, delta, 1);
        this.page = MathHelper.clamp_int(page + delta, 0, pages - 1);
        load(page + 2, page - 2);
    }
}
