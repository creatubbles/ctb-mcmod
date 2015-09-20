package com.creatubbles.ctbmod.client.gui;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import lombok.Getter;
import lombok.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Dimension;

import com.creatubbles.ctbmod.CTBMod;
import com.creatubbles.ctbmod.common.http.Creation;
import com.creatubbles.ctbmod.common.http.Image;
import com.creatubbles.ctbmod.common.http.Image.ImageType;
import com.creatubbles.repack.enderlib.api.client.gui.IGuiOverlay;
import com.creatubbles.repack.enderlib.api.client.gui.IGuiScreen;
import com.google.common.collect.Lists;

public class OverlayCreationList extends Gui implements IGuiOverlay {

	@Value
	private class CreationAndLocation {

		private Creation creation;
		private Point location;
		private Rectangle bounds;
		
		private CreationAndLocation(Creation c, Point p) {
			this.creation = c;
			this.location = p;
			this.bounds = new Rectangle(p.x, p.y, thumbnailSize.getWidth(), thumbnailSize.getHeight());
		}
	}

	public static final ResourceLocation LOADING_TEX = new ResourceLocation(CTBMod.DOMAIN, "textures/gui/loading.png");

	@Getter
	private Creation[] creations;

	private int xRel, yRel, xAbs, yAbs;

	@Getter
	private int paddingX = 8, paddingY = 4;

	@Getter
	private int minSpacing = 6;

	@Getter
	private Dimension thumbnailSize = new Dimension(20, 20);

	private List<CreationAndLocation> list = Lists.newArrayList();

	private final Dimension size = new Dimension(88, 106);

	private int scroll;

	private boolean visible;

	public OverlayCreationList(int x, int y) {
		this.xRel = x;
		this.yRel = y;
	}

	public void setCreations(Creation[] creations) {
		this.creations = creations;
		rebuildList();
	}

	@Override
	public void init(IGuiScreen screen) {
		this.xAbs = xRel + screen.getGuiLeft();
		this.yAbs = yRel + screen.getGuiTop();
		rebuildList();
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(xAbs, yAbs, 88, 106);
	}

	private void rebuildList() {
		list.clear();

		Creation[] creations = this.creations == null ? CTBMod.cache.getCreationCache() : this.creations;

		if (creations == null || creations.length == 0) {
			return;
		}
				
		int row = 0;
		int col = 0;

		// This is the dimensions we have to work with for thumbnails
		int usableWidth = size.getWidth() - (paddingX * 2);
		int usableHeight = size.getHeight() - (paddingY * 2);

		// The minimum size a thumbnail can take up
		int widthPerThumbnail = thumbnailSize.getWidth() + minSpacing;
		int heightPerThumbnail = thumbnailSize.getHeight() + minSpacing;

		// The amount of thumbnails on each row/column
		int rows = usableHeight / heightPerThumbnail;

		// This fancy math figures out the max creations that can fit in the available width
		// Simple division won't work due to it counting the spacing after the last item
		int cols = 0;
		// So the initial width ignores the spacing
		int usedWidth = thumbnailSize.getWidth();
		while (usedWidth <= usableWidth) {
			cols++;
			usedWidth += widthPerThumbnail;
		}

		// Use the max spacing possible, but no less than minSpacing
		int minWidth = thumbnailSize.getWidth() * cols;
		int spacing = (usableWidth - minWidth) / (cols - 1);
		spacing = Math.max(minSpacing, spacing);

		for (Creation c : creations) {
			int xMin = xRel + paddingX, yMin = yRel + paddingY;

			int x = xMin + (col * (thumbnailSize.getWidth() + spacing));
			int y = yMin + (row * (thumbnailSize.getHeight() + minSpacing)) + scroll;
			
			list.add(new CreationAndLocation(c, new Point(x, y)));

			col++;
			if (col >= cols) {
				row++;
				col = 0;
			}
			if (row >= rows) {
				break;
			}
		}
	}

	@Override
	public void draw(int mouseX, int mouseY, float partialTick) {
		if (visible) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(GuiCreator.OVERLAY_TEX);
			drawTexturedModalRect(xRel, yRel, 0, 0, 88, 106);

			Minecraft mc = Minecraft.getMinecraft();
			FontRenderer fr = mc.fontRendererObj;
			if (list.size() == 0) {
				drawCenteredString(fr, "No Creations", xRel + (getWidth() / 2), yRel + 4, 0xFFFFFF);
			} else {
				for (CreationAndLocation c : list) {
					GlStateManager.pushMatrix();

					int x = c.getLocation().x;
					int y = c.getLocation().y;

					// Draw selection box
					Rectangle bounds = c.getBounds();
					if (c.getBounds().contains(mouseX, mouseY)) {
						drawRect((int) bounds.getMinX() - 1, (int) bounds.getMinY() - 1, (int) bounds.getMaxX() + 1, (int) bounds.getMaxY() + 1, 0xFFFFFFFF);
						GlStateManager.color(1, 1, 1, 1);
					}

					Image img = c.getCreation().getImage();
					ImageType type = ImageType.LIST_VIEW;

					ResourceLocation res = img.getResource(type);
					GL11.glTranslatef(x + (thumbnailSize.getWidth() / 2), y + (thumbnailSize.getHeight() / 2), 0);

					// so the rotation is centered
					x = -10;
					y = -10;
					int w = 46, h = 46;
					if (Image.MISSING_TEXTURE.equals(res)) {
						GlStateManager.enableBlend();
						res = LOADING_TEX;
						float rot = (Minecraft.getMinecraft().theWorld.getTotalWorldTime() + partialTick) * 4;
						GL11.glRotatef(rot, 0, 0, 1);
					} else {
						w = img.getWidth(type);
						h = img.getHeight(type);
					}
					if (res != null) {
						mc.getTextureManager().bindTexture(res);
					}

					drawScaledCustomSizeModalRect(x, y, 0, 0, w, h, 20, 20, w, h);
					GlStateManager.popMatrix();
				}
			}
		}
	}

	@Override
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	@Override
	public boolean isVisible() {
		return visible;
	}
	
	public int getWidth() {
		return (int) getBounds().getWidth();
	}
	
	public int getHeight() {
		return (int) getBounds().getHeight();
	}

	@Override
	public boolean handleMouseInput(int x, int y, int b) {
		return isMouseInBounds(x, y);
	}

	@Override
	public boolean isMouseInBounds(int mouseX, int mouseY) {
		return getBounds().contains(mouseX, mouseY);
	}

	public int getX() {
		return xAbs;
	}

	public void setX(int x) {
		this.xAbs = x;
		rebuildList();
	}

	public int getY() {
		return yAbs;
	}

	public void setY(int y) {
		this.yAbs = y;
		rebuildList();
	}

	public void setPaddingX(int paddingX) {
		this.paddingX = paddingX;
		rebuildList();
	}

	public void setPaddingY(int paddingY) {
		this.paddingY = paddingY;
		rebuildList();
	}

	public void setMinSpacing(int minSpacing) {
		this.minSpacing = minSpacing;
		rebuildList();
	}

	public void setThumbnailSize(Dimension thumbnailSize) {
		this.thumbnailSize = thumbnailSize;
		rebuildList();
	}

}
