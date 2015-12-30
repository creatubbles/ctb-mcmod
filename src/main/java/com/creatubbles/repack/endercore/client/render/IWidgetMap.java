package com.creatubbles.repack.endercore.client.render;

import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public interface IWidgetMap {

    int getSize();

    ResourceLocation getTexture();

    void render(IWidgetIcon widget, double x, double y);

    void render(IWidgetIcon widget, double x, double y, boolean doDraw);

    void render(IWidgetIcon widget, double x, double y, double zLevel, boolean doDraw);

    void render(IWidgetIcon widget, double x, double y, double width, double height, double zLevel, boolean doDraw);

    void render(IWidgetIcon widget, double x, double y, double width, double height, double zLevel, boolean doDraw, boolean flipY);

    @RequiredArgsConstructor
    static class WidgetMapImpl implements IWidgetMap {

        private final int size;
        private final ResourceLocation res;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public ResourceLocation getTexture() {
            return res;
        }

        @Override
        public void render(IWidgetIcon widget, double x, double y) {
            render(widget, x, y, false);
        }

        @Override
        public void render(IWidgetIcon widget, double x, double y, boolean doDraw) {
            render(widget, x, y, 0, doDraw);
        }

        @Override
        public void render(IWidgetIcon widget, double x, double y, double zLevel, boolean doDraw) {
            render(widget, x, y, widget.getWidth(), widget.getHeight(), zLevel, doDraw);
        }

        @Override
        public void render(IWidgetIcon widget, double x, double y, double width, double height, double zLevel, boolean doDraw) {
            render(widget, x, y, width, height, zLevel, doDraw, false);
        }

        @Override
        public void render(IWidgetIcon widget, double x, double y, double width, double height, double zLevel, boolean doDraw, boolean flipY) {

            WorldRenderer renderer = Tessellator.getInstance().getWorldRenderer();
            if (doDraw) {
                Minecraft.getMinecraft().getTextureManager().bindTexture(getTexture());
                renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            }
            double minU = (double) widget.getX() / getSize();
            double maxU = (double) (widget.getX() + widget.getWidth()) / getSize();
            double minV = (double) widget.getY() / getSize();
            double maxV = (double) (widget.getY() + widget.getHeight()) / getSize();

            if (flipY) {
                renderer.pos(x, y + height, zLevel).tex(minU, minV).endVertex();
                renderer.pos(x + width, y + height, zLevel).tex(maxU, minV).endVertex();
                renderer.pos(x + width, y + 0, zLevel).tex(maxU, maxV).endVertex();
                renderer.pos(x, y + 0, zLevel).tex(minU, maxV).endVertex();
            } else {
                renderer.pos(x, y + height, zLevel).tex(minU, maxV).endVertex();
                renderer.pos(x + width, y + height, zLevel).tex(maxU, maxV).endVertex();
                renderer.pos(x + width, y + 0, zLevel).tex(maxU, minV).endVertex();
                renderer.pos(x, y + 0, zLevel).tex(minU, minV).endVertex();
            }
            if (widget.getOverlay() != null) {
                widget.getOverlay().getMap().render(widget.getOverlay(), x, y, width, height, zLevel, false, flipY);
            }
            if (doDraw) {
                Tessellator.getInstance().draw();
            }
        }
    }
}
