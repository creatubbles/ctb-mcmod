package com.creatubbles.repack.endercore.client.render;

import org.lwjgl.opengl.GL11;

import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

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
			GL11.glColor4f(1, 1, 1, 1);
			Tessellator renderer = Tessellator.instance;

			if (doDraw) {
				Minecraft.getMinecraft().getTextureManager().bindTexture(getTexture());
				renderer.startDrawingQuads();
			}
			double minU = (double) widget.getX() / getSize();
			double maxU = (double) (widget.getX() + widget.getWidth()) / getSize();
			double minV = (double) widget.getY() / getSize();
			double maxV = (double) (widget.getY() + widget.getHeight()) / getSize();

			if (flipY) {
				renderer.addVertexWithUV(x, y + height, zLevel, minU, minV);
				renderer.addVertexWithUV(x + width, y + height, zLevel, maxU, minV);
				renderer.addVertexWithUV(x + width, y + 0, zLevel, maxU, maxV);
				renderer.addVertexWithUV(x, y + 0, zLevel, minU, maxV);
			} else {
				renderer.addVertexWithUV(x, y + height, zLevel, minU, maxV);
				renderer.addVertexWithUV(x + width, y + height, zLevel, maxU, maxV);
				renderer.addVertexWithUV(x + width, y + 0, zLevel, maxU, minV);
				renderer.addVertexWithUV(x, y + 0, zLevel, minU, minV);
			}
			if (widget.getOverlay() != null) {
				widget.getOverlay().getMap().render(widget.getOverlay(), x, y, width, height, zLevel, false, flipY);
			}
			if (doDraw) {
				renderer.draw();
			}
		}
	}
}
