package com.creatubbles.ctbmod.client.render;

import java.awt.geom.Rectangle2D;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.common.util.ForgeDirection;
import team.chisel.ctmlib.Drawing;

import com.creatubbles.api.core.Creation;
import com.creatubbles.ctbmod.CTBMod;
import com.creatubbles.ctbmod.client.gui.OverlayCreationList;
import com.creatubbles.ctbmod.common.http.DownloadableImage;
import com.creatubbles.ctbmod.common.http.DownloadableImage.ImageType;
import com.creatubbles.ctbmod.common.painting.BlockPainting;
import com.creatubbles.ctbmod.common.painting.TilePainting;

import static org.lwjgl.opengl.GL11.*;

public class RenderPainting extends TileEntitySpecialRenderer implements IItemRenderer {

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        if (te instanceof TilePainting && ((TilePainting) te).getImage() != null && ((TilePainting) te).render()) {
            TilePainting painting = (TilePainting) te;
            DownloadableImage image = painting.getImage();

            ForgeDirection facing = BlockPainting.getFacing(te.getBlockMetadata());

            glPushMatrix();
            {
                glTranslated(x, y, z);

                switch (facing) {
                    case EAST:
                        glTranslatef(0, 0, 1);
                        glRotatef(90, 0, 1, 0);
                        break;
                    case NORTH:
                        glTranslatef(1, 0, 1);
                        glRotatef(180, 0, 1, 0);
                        break;
                    case WEST:
                        glTranslatef(1, 0, 0);
                        glRotatef(-90, 0, 1, 0);
                        break;
                    default:
                        break;
                }

                int c = (int) (0xFF * getColorMultiplierForFace(facing));
                c = c << 16 | c << 8 | c;
                glPolygonOffset(-3.0F, -1.5F);
                glEnable(GL_POLYGON_OFFSET_FILL);
                glDisable(GL_LIGHTING);

                renderPainting(image, painting.getHeight(), painting.getWidth(), c);

                glPolygonOffset(0, 0);
                glDisable(GL_POLYGON_OFFSET_FILL);
                glEnable(GL_LIGHTING);
            }
            glPopMatrix();
        }
    }

    private void renderPainting(DownloadableImage image, int pheight, int pwidth, int color) {

        int width = image.getWidth(ImageType.ORIGINAL);
        int height = image.getHeight(ImageType.ORIGINAL);
        double scaledSize = image.getScaledSize(ImageType.ORIGINAL);

        ResourceLocation res = image.getResource(ImageType.ORIGINAL);
        if (res == DownloadableImage.MISSING_TEXTURE) {
            res = OverlayCreationList.LOADING_TEX;
            width = 16;
            height = 16;
            scaledSize = 16;
            glEnable(GL_BLEND);
        }

        Minecraft.getMinecraft().renderEngine.bindTexture(res);

        Tessellator renderer = Tessellator.instance;

        // TODO this code is duped between here and OverlaySelectedCreation (kinda)

        Rectangle2D.Double bounds = new Rectangle2D.Double(2 / 16f, 2 / 16f, pwidth - 4 / 16f, pheight - 4 / 16f);
        if (width / pwidth > height / pheight) {
            double h = bounds.height;
            bounds.height = bounds.getHeight() * ((double) height / width) * ((double) pwidth / pheight);
            bounds.y += (h - bounds.getHeight()) / 2;
        } else {
            double w = bounds.width;
            bounds.width = bounds.getWidth() * ((double) width / height) * ((double) pheight / pwidth);
            bounds.x += (w - bounds.getWidth()) / 2;
        }

        double maxU = width / scaledSize;
        double maxV = height / scaledSize;

        glPushMatrix();
        {
            double depth = 1 / 16d;

            glPushAttrib(GL_COLOR_BUFFER_BIT);
            glNormal3d(0, 0, 1);
            renderer.startDrawingQuads();
            renderer.setColorOpaque_I(color);

            renderer.addVertexWithUV(bounds.getX(), bounds.getY() + bounds.getHeight(), depth, 0, 0);
            renderer.addVertexWithUV(bounds.getX(), bounds.getY(), depth, 0, maxV);
            renderer.addVertexWithUV(bounds.getX() + bounds.getWidth(), bounds.getY(), depth, maxU, maxV);
            renderer.addVertexWithUV(bounds.getX() + bounds.getWidth(), bounds.getY() + bounds.getHeight(), depth, maxU, 0);

            renderer.draw();
            renderer.setTranslation(0, 0, 0);

            glPopAttrib();
            glDisable(GL_BLEND);
        }
        glPopMatrix();
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return helper != ItemRendererHelper.INVENTORY_BLOCK;
    }

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return type != ItemRenderType.FIRST_PERSON_MAP;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        RenderBlocks rb = (RenderBlocks) data[0];
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        Tessellator tess = Tessellator.instance;
        IIcon frontIcon = CTBMod.painting.getIcon(3, 0);

        Creation c = BlockPainting.getCreation(item);
        DownloadableImage img = new DownloadableImage(c.image, c);
        img.download(ImageType.ORIGINAL);

        glPushMatrix();
        {
            switch (type) {
                case ENTITY:
                    glTranslatef(-1, 0, -0.5f);
                    break;
                case EQUIPPED:
                    glRotatef(-45, 0, 1, 0);
                    glRotatef(60, 0, 0, 1);
                    glTranslatef(0.95f, -0.75f, -0.5f);
                    break;
                case EQUIPPED_FIRST_PERSON:
                    glRotatef(45, 0, 1, 0);
                    glTranslatef(-0.1f, 0.25f, 0.25f);
                    break;
                case INVENTORY:
                    glPushMatrix();
                    glScalef(16, 16, 1);
                    tess.startDrawingQuads();
                    tess.addVertexWithUV(0, 1, 0, frontIcon.getMinU(), frontIcon.getMaxV());
                    tess.addVertexWithUV(1, 1, 0, frontIcon.getMaxU(), frontIcon.getMaxV());
                    tess.addVertexWithUV(1, 0, 0, frontIcon.getMaxU(), frontIcon.getMinV());
                    tess.addVertexWithUV(0, 0, 0, frontIcon.getMinU(), frontIcon.getMinV());
                    tess.draw();
                    glTranslatef(0, 1, 1);
                    glRotatef(180, 0, 1, 0);
                    glRotatef(180, 0, 0, 1);
                    renderPainting(img, 1, 1, 0xFFFFFF);
                    glPopMatrix();
                    glPopMatrix(); // pop switch matrix
                    return;
                default:
                    break;
            }

            CTBMod.painting.setBlockBoundsBasedOnFacing(ForgeDirection.EAST);
            rb.setRenderBoundsFromBlock(CTBMod.painting);
            Drawing.drawBlock(CTBMod.painting, frontIcon, rb);

            glDisable(GL_CULL_FACE);
            if (type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
                glRotatef(-90, 0, 1, 0);
            } else {
                glRotatef(90, 0, 1, 0);
            }
            glTranslatef(0, 0, -0.06f);
            if (type == ItemRenderType.EQUIPPED) {
                glTranslatef(-1, 0, 1 / 16f);
            }
            renderPainting(img, 1, 1, 0xFFFFFF);
        }
        glPopMatrix();
    }

    private static float getColorMultiplierForFace(ForgeDirection face) {
        if (face == ForgeDirection.UP) {
            return 1;
        }
        if (face == ForgeDirection.DOWN) {
            return 0.5f;
        }
        if (face.offsetX != 0) {
            return 0.6f;
        }
        return 0.8f; // z
    }
}
