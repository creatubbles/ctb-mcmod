package com.creatubbles.ctbmod.client.render;

import java.awt.geom.Rectangle2D;

import org.lwjgl.opengl.GL11;

import com.creatubbles.api.core.Image.ImageType;
import com.creatubbles.ctbmod.CTBMod;
import com.creatubbles.ctbmod.client.gui.GuiUtil;
import com.creatubbles.ctbmod.common.http.DownloadableImage;
import com.creatubbles.ctbmod.common.painting.BlockPainting;
import com.creatubbles.ctbmod.common.painting.TilePainting;
import com.creatubbles.repack.endercore.client.render.IWidgetIcon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class RenderPainting extends TileEntitySpecialRenderer<TilePainting> {
    
    @Override
    public boolean isGlobalRenderer(TilePainting te) {
         return te.getWidth() > 1 || te.getHeight() > 1;
    }

    @Override
    public void renderTileEntityAt(TilePainting te, double x, double y, double z, float partialTicks, int destroyStage) {
        if (te.getWorld().getBlockState(te.getPos()).getBlock() == CTBMod.painting && te instanceof TilePainting && te.getImage() != null && te.render()) {
            TilePainting painting = te;
            DownloadableImage image = painting.getImage();

            EnumFacing facing = te.getWorld().getBlockState(te.getPos()).getValue(BlockPainting.FACING);

            ImageType type = te.getType();
            
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GlStateManager.disableLighting();
            renderPaintingImage(image, type, facing, painting.getWidth(), painting.getHeight(), getColorMultiplierForFace(facing));
            GlStateManager.enableLighting();
            GlStateManager.popMatrix();
        }
    }
    
    protected void renderPaintingImage(DownloadableImage image, ImageType type, EnumFacing facing) {
        renderPaintingImage(image, type, facing, 1, 1, 1);
    }
    
    protected void renderPaintingImage(DownloadableImage image, ImageType type, EnumFacing facing, int blockwidth, int blockheight, float colorMultiplier) {
        int width = image.getWidth(type);
        int height = image.getHeight(type);
        double scaledW = image.getScaledWidth(type);
        double scaledH = image.getScaledHeight(type);
        ResourceLocation res = image.getResource(type);
        
        if (res == DownloadableImage.MISSING_TEXTURE) {
            res = GuiUtil.Bubbles.TEXTURE;
            scaledW = scaledH = width = height = 16;
            GlStateManager.enableBlend();
        }

        Minecraft.getMinecraft().renderEngine.bindTexture(res);

        VertexBuffer renderer = Tessellator.getInstance().getBuffer();

        // TODO this code is duped between here and GuiUtil.drawRectInscribed (kinda)

        Rectangle2D.Double bounds = new Rectangle2D.Double(2 / 16f, 2 / 16f, blockwidth - 4 / 16f, blockheight - 4 / 16f);
        if (width / blockwidth > height / blockheight) {
            double h = bounds.height;
            bounds.height = bounds.getHeight() * ((double) height / width) * ((double) blockwidth / blockheight);
            bounds.y += (h - bounds.getHeight()) / 2;
        } else {
            double w = bounds.width;
            bounds.width = bounds.getWidth() * ((double) width / height) * ((double) blockheight / blockwidth);
            bounds.x += (w - bounds.getWidth()) / 2;
        }

        double minU = 0, minV = 0;
        double maxU = width / scaledW;
        double maxV = height / scaledH;

        // FIXME Bit of a hack for now
        if (res == GuiUtil.Bubbles.TEXTURE) {
            IWidgetIcon icon = GuiUtil.Bubbles.CLEAR;
            minU = (float) icon.getX() / icon.getMap().getSize();
            minV = (float) icon.getY() / icon.getMap().getSize();
            maxU = minU + ((float) icon.getWidth() / icon.getMap().getSize());
            maxV = minV + ((float) icon.getHeight() / icon.getMap().getSize());
        }
        
        GlStateManager.pushMatrix();

        switch (facing) {
            case EAST:
                GlStateManager.translate(0, 0, 1);
                GlStateManager.rotate(90, 0, 1, 0);
                break;
            case NORTH:
                GlStateManager.translate(1, 0, 1);
                GlStateManager.rotate(180, 0, 1, 0);
                break;
            case WEST:
                GlStateManager.translate(1, 0, 0);
                GlStateManager.rotate(-90, 0, 1, 0);
                break;
            default:
                break;
        }

        GlStateManager.doPolygonOffset(-3.0F, -1.5F);
        GlStateManager.enablePolygonOffset();
        renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);

        float c = colorMultiplier;
        double depth = 1 / 16d;

        renderer.pos(bounds.getX(), bounds.getY() + bounds.getHeight(), depth).tex(minU, minV).color(c, c, c, 1).normal(0, 0, 1).endVertex();
        renderer.pos(bounds.getX(), bounds.getY(), depth).tex(minU, maxV).color(c, c, c, 1).normal(0, 0, 1).endVertex();
        renderer.pos(bounds.getX() + bounds.getWidth(), bounds.getY(), depth).tex(maxU, maxV).color(c, c, c, 1).normal(0, 0, 1).endVertex();
        renderer.pos(bounds.getX() + bounds.getWidth(), bounds.getY() + bounds.getHeight(), depth).tex(maxU, minV).color(c, c, c, 1).normal(0, 0, 1).endVertex();

        Tessellator.getInstance().draw();
        renderer.setTranslation(0, 0, 0);

        GlStateManager.doPolygonOffset(0.0F, 0.0F);
        GlStateManager.disablePolygonOffset();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private static float getColorMultiplierForFace(EnumFacing face) {
        if (face == EnumFacing.UP) {
            return 1;
        }
        if (face == EnumFacing.DOWN) {
            return 0.5f;
        }
        if (face.getFrontOffsetX() != 0) {
            return 0.6f;
        }
        return 0.8f; // z
    }
}
