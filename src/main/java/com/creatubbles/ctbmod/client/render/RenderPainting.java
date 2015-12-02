package com.creatubbles.ctbmod.client.render;


import java.awt.geom.Rectangle2D;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.creatubbles.ctbmod.client.gui.OverlayCreationList;
import com.creatubbles.ctbmod.common.http.DownloadableImage;
import com.creatubbles.ctbmod.common.http.DownloadableImage.ImageType;
import com.creatubbles.ctbmod.common.painting.BlockPainting;
import com.creatubbles.ctbmod.common.painting.TilePainting;


public class RenderPainting extends TileEntitySpecialRenderer<TilePainting> {
    
    @Override
    public void renderTileEntityAt(TilePainting te, double x, double y, double z, float partialTicks, int destroyStage) {
        if (te instanceof TilePainting && te.getImage() != null && te.render()) {
            TilePainting painting = te;
            DownloadableImage image = painting.getImage();

            EnumFacing facing = te.getWorld().getBlockState(te.getPos()).getValue(BlockPainting.FACING);

            int width = image.getWidth(ImageType.ORIGINAL);
            int height = image.getHeight(ImageType.ORIGINAL);
            double scaledSize = image.getScaledSize(ImageType.ORIGINAL);

            ResourceLocation res = image.getResource(ImageType.ORIGINAL);
            if (res == DownloadableImage.MISSING_TEXTURE) {
                res = OverlayCreationList.LOADING_TEX;
                width = 16;
                height = 16;
                scaledSize = 16;
                GlStateManager.enableBlend();
            }
            
            Minecraft.getMinecraft().renderEngine.bindTexture(res);
            
            WorldRenderer renderer = Tessellator.getInstance().getWorldRenderer();

            // TODO this code is duped between here and OverlaySelectedCreation (kinda)

            Rectangle2D.Double bounds = new Rectangle2D.Double(2 / 16f, 2 / 16f, painting.getWidth() - 4 / 16f, painting.getHeight() - 4 / 16f);
            if (width / painting.getWidth() > height / painting.getHeight()) {
                double h = bounds.height;
                bounds.height = bounds.getHeight() * ((double) height / width) * ((double) painting.getWidth() / painting.getHeight());
                bounds.y += (h - bounds.getHeight()) / 2;
            } else {
                double w = bounds.width;
                bounds.width = bounds.getWidth() * ((double) width / height) * ((double) painting.getHeight() / painting.getWidth());
                bounds.x += (w - bounds.getWidth()) / 2;
            }

            double maxU = width / scaledSize;
            double maxV = height / scaledSize;
            
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);

            switch(facing) {
            case EAST:
                GL11.glTranslatef(0, 0, 1);
                GL11.glRotatef(90, 0, 1, 0);
                break;
            case NORTH:
                GL11.glTranslatef(1, 0, 1);
                GL11.glRotatef(180, 0, 1, 0);
                break;
            case WEST:
                GL11.glTranslatef(1, 0, 0);
                GL11.glRotatef(-90, 0, 1, 0);
                break;
            default:
                break;
            }
            
            GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT);
            GlStateManager.disableLighting();
            GlStateManager.doPolygonOffset(-3.0F, -1.5F);
            GlStateManager.enablePolygonOffset();
            renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
            
            float c = getColorMultiplierForFace(facing);
            renderer.putColorRGB_F4(c, c, c);
            
            double depth = 1/16d;
            
            renderer.pos(bounds.getX(), bounds.getY() + bounds.getHeight(), depth).tex(0, 0).endVertex();
            renderer.pos(bounds.getX(), bounds.getY(), depth).tex(0, maxV).endVertex();
            renderer.pos(bounds.getX() + bounds.getWidth(), bounds.getY(), depth).tex(maxU, maxV).endVertex();
            renderer.pos(bounds.getX() + bounds.getWidth(), bounds.getY() + bounds.getHeight(), depth).tex(maxU, 0).endVertex();
            
            Tessellator.getInstance().draw();
            renderer.setTranslation(0, 0, 0);

            GL11.glPopAttrib();
            GlStateManager.doPolygonOffset(0.0F, 0.0F);
            GlStateManager.disablePolygonOffset();
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            GlStateManager.popMatrix();
        }
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
