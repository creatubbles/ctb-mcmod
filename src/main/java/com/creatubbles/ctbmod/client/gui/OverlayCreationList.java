package com.creatubbles.ctbmod.client.gui;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import lombok.Getter;
import lombok.Synchronized;
import lombok.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.util.Dimension;

import com.creatubbles.ctbmod.CTBMod;
import com.creatubbles.ctbmod.common.http.Creation;
import com.creatubbles.ctbmod.common.http.Creator;
import com.creatubbles.ctbmod.common.http.Image;
import com.creatubbles.ctbmod.common.http.Image.ImageType;
import com.creatubbles.repack.enderlib.api.client.gui.IGuiOverlay;
import com.creatubbles.repack.enderlib.api.client.gui.IGuiScreen;
import com.creatubbles.repack.enderlib.client.gui.widget.GuiToolTip;
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
			this.bounds = new Rectangle(p.x, p.y, thumbnailSize, thumbnailSize);
		}
	}

	public static final ResourceLocation LOADING_TEX = new ResourceLocation(CTBMod.DOMAIN, "textures/gui/bubble_outline.png");

	@Getter
	private Creation[] creations;

	private int xRel, yRel, xAbs, yAbs;

	@Getter
	private int paddingX = 4, paddingY = 4;

	@Getter
	private int minSpacing = 4;
	
	private int rows, cols;

	@Getter
	private int thumbnailSize = 16;

	private List<CreationAndLocation> list = Lists.newArrayList();
	private List<CreationAndLocation> listAbsolute = Lists.newArrayList();

	private final Dimension size = new Dimension(88, 106);

	private IGuiScreen gui;

	private int scroll = 0;

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
		this.gui = screen;
		this.xAbs = xRel + screen.getGuiLeft();
		this.yAbs = yRel + screen.getGuiTop();
		rebuildList();
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(xAbs, yAbs, 88, 106);
	}

	@Synchronized("list")
	private void rebuildList() {
		list.clear();
		listAbsolute.clear();
		this.thumbnailSize = 64;

		gui.clearToolTips();

		Creation[] creations = this.creations == null ? CTBMod.cache.getCreationCache() : this.creations;

		if (creations == null || creations.length == 0) {
			return;
		}

		int row = 0;
		int col = 0;

		// This is the dimensions we have to work with for thumbnails
		int usableWidth = size.getWidth() - (paddingX * 2);

		// The minimum size a thumbnail can take up
		int widthPerThumbnail = thumbnailSize + minSpacing;

		// This fancy math figures out the max creations that can fit in the available width
		// Simple division won't work due to it counting the spacing after the last item
		cols = 0;
		// So the initial width ignores the spacing
		int usedWidth = thumbnailSize;
		while (usedWidth <= usableWidth) {
			cols++;
			usedWidth += widthPerThumbnail;
		}
		
		// The amount of thumbnails on each row/column
		rows = creations.length / cols;

		// Use the max spacing possible, but no less than minSpacing
		int minWidth = thumbnailSize * cols;
		int spacing = usableWidth - minWidth;
		if (cols > 1) {
			spacing /= cols - 1;
		}

		spacing = Math.max(minSpacing, spacing);

		for (Creation c : creations) {
			int xMin = xRel + paddingX, yMin = yRel + paddingY;

			int x = xMin + (col * (thumbnailSize + spacing));
			int y = yMin + (row * (thumbnailSize + minSpacing)) - scroll;

			CreationAndLocation data = new CreationAndLocation(c, new Point(x, y));
			CreationAndLocation absoluteData = new CreationAndLocation(c, new Point(x, y + scroll));

			// Anything below the border is a waste to draw
			if (y > yRel + getHeight()) {
				listAbsolute.add(absoluteData);
				break;
			}

			// Anything completely above the border is a waste to draw
			if (y + thumbnailSize > yRel) {

				list.add(data);

				// TODO more localization here
				List<String> tt = Lists.newArrayList();
				tt.add(c.getName());
				tt.add(c.getCreators().length == 1 ? "Creator:" : "Creators:");
				for (Creator creator : c.getCreators()) {
					tt.add("    " + creator.getName() + " at " + creator.getAge());
				}

				gui.addToolTip(new GuiToolTip(data.getBounds(), tt));
			}
			
			listAbsolute.add(absoluteData);

			col++;
			if (col >= cols) {
				row++;
				col = 0;
			}
		}
	}

	@Override
	@Synchronized("list")
	public void draw(int mouseX, int mouseY, float partialTick) {
		if (visible) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(GuiCreator.OVERLAY_TEX);
			drawTexturedModalRect(xRel, yRel, 0, 0, 87, 106);

			Minecraft mc = Minecraft.getMinecraft();
			FontRenderer fr = mc.fontRendererObj;
			if (list.size() == 0) {
				drawCenteredString(fr, "No Creations", xRel + (getWidth() / 2), yRel + 4, 0xFFFFFF);
			} else {
				for (CreationAndLocation c : list) {
					GlStateManager.pushMatrix();

					int x = c.getLocation().x;
					int y = c.getLocation().y;

					Image img = c.getCreation().getImage();
					ImageType type = ImageType.LIST_VIEW;

					ResourceLocation res = img.getResource(type);

					int w = 16, h = 16;
					if (Image.MISSING_TEXTURE.equals(res)) {
						GlStateManager.enableBlend();
						res = LOADING_TEX;
					} else {
						w = img.getWidth(type);
						h = img.getHeight(type);
					}

					if (res != null) {
						if (res != LOADING_TEX) {
							// Draw selection box
							Rectangle bounds = c.getBounds();
							if (isMouseInBounds(mouseX, mouseY) && c.getBounds().contains(mouseX - gui.getGuiLeft(), mouseY - gui.getGuiTop())) {
								drawRect((int) bounds.getMinX() - 1, (int) Math.max(bounds.getMinY() - 1, yRel + 1), (int) bounds.getMaxX() + 1, (int) Math.min(bounds.getMaxY() + 1, yRel + getHeight()), 0xFFFFFFFF);
								GlStateManager.enableBlend();
								GlStateManager.color(1, 1, 1, 1);
							}
						}
						mc.getTextureManager().bindTexture(res);
					}

					int height = thumbnailSize;
					float v = 0;
					int pastBottom = (y + height) - (yRel + getHeight() - 1);
					int pastTop = (yRel + 1) - y;
					boolean clipV = false;
					if (pastBottom > 0) {
						height -= pastBottom;
					} else if (pastTop > 0) {
						clipV = true;
						height -= pastTop;
					}
					double heightRatio = (double) height / thumbnailSize;
					if (clipV) {
						v = h - ((float) heightRatio * h);
					}
					drawScaledCustomSizeModalRect(x, y + Math.max(0, pastTop), 0, v, w, (int) (h * heightRatio), thumbnailSize, height, w, h);
					GlStateManager.popMatrix();
				}
			}
			
			Minecraft.getMinecraft().getTextureManager().bindTexture(GuiCreator.OVERLAY_TEX);
			drawTexturedModalRect(xRel, yRel, 88, 0, 88, 106);
		}
	}

	@Override
	public void setIsVisible(boolean visible) {
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
		return false;
	}

	@Override
	public boolean isMouseInBounds(int mouseX, int mouseY) {
		return getBounds().contains(mouseX, mouseY);
	}

	public int getX() {
		return xAbs;
	}

	public void setX(int x) {
		if (this.xAbs != x) {
			this.xAbs = x;
			rebuildList();
		}
	}

	public int getY() {
		return yAbs;
	}

	public void setY(int y) {
		if (this.yAbs != y) {
			this.yAbs = y;
			rebuildList();
		}
	}

	public void setPaddingX(int paddingX) {
		if (this.paddingX != paddingX) {
			this.paddingX = paddingX;
			rebuildList();
		}
	}

	public void setPaddingY(int paddingY) {
		if (this.paddingY != paddingY) {
			this.paddingY = paddingY;
			rebuildList();
		}
	}

	public void setMinSpacing(int minSpacing) {
		if (this.minSpacing != minSpacing) {
			this.minSpacing = minSpacing;
			rebuildList();
		}
	}

	public void setThumbnailSize(int thumbnailSize) {
		if (this.thumbnailSize != thumbnailSize) {
			this.thumbnailSize = thumbnailSize;
			rebuildList();
		}
	}

	public int getMaxScroll() {
		return (rows * (thumbnailSize + minSpacing)) - getHeight() + minSpacing;
	}

	public void setScroll(int scroll) {
		if (this.scroll != scroll) {
			this.scroll = scroll;
			rebuildList();
		}
	}
}
