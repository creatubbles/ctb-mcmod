package com.creatubbles.ctbmod.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Dimension;
import org.lwjgl.util.Rectangle;

import com.creatubbles.api.core.Creation;
import com.creatubbles.ctbmod.common.http.DownloadableImage;
import com.creatubbles.ctbmod.common.http.DownloadableImage.ImageType;
import com.creatubbles.repack.endercore.client.gui.button.IconButton;

public class OverlaySelectedCreation extends OverlayBase implements ISelectionCallback {

	private final IconButton createButton;

	private Creation selected;

	protected OverlaySelectedCreation(int x, int y, OverlayCreationList creationList, IconButton createButton) {
		super(x, y, new Dimension());
		this.createButton = createButton;
	}

	@Override
	protected void doDraw(int mouseX, int mouseY, float partialTick) {

		Minecraft.getMinecraft().getTextureManager().bindTexture(GuiCreator.OVERLAY_TEX);
		FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
		GL11.glEnable(GL11.GL_BLEND);

		drawTexturedModalRect(xRel, yRel, 94, 0, 54, 54);

		int x = xRel + 3;
		int y = yRel + 3;

		if (selected != null) {
            DownloadableImage img = getGui().images.get(selected);
			ResourceLocation res;
			int imgWidth = 16, imgHeight = 16;
			if (img.hasSize(ImageType.FULL_VIEW)) {
				res = img.getResource(ImageType.FULL_VIEW);
				imgWidth = img.getWidth(ImageType.FULL_VIEW);
				imgHeight = img.getHeight(ImageType.FULL_VIEW);
			} else {
				res = OverlayCreationList.LOADING_TEX;
				img.download(ImageType.FULL_VIEW);
			}

			Minecraft.getMinecraft().getTextureManager().bindTexture(res);

			Rectangle bounds = new Rectangle(x, y, 48, 48);
			if (imgWidth > imgHeight) {
				bounds.setHeight((int) (bounds.getHeight() * ((double) imgHeight / (double) imgWidth)));
				bounds.translate(0, (48 - bounds.getHeight()) / 2);
			} else {
				bounds.setWidth((int) (bounds.getWidth() * ((double) imgWidth / (double) imgHeight)));
				bounds.translate((48 - bounds.getWidth()) / 2, 0);
			}
			int scaledSize = img.getScaledSize(ImageType.FULL_VIEW);

			func_152125_a(bounds.getX(), bounds.getY(), 0, 0, imgWidth, imgHeight, bounds.getWidth(), bounds.getHeight(), scaledSize, scaledSize);

			x += 24;
			y += 56;
			TextUtil.drawCenteredSplitString(fr, EnumChatFormatting.ITALIC + selected.name, x, y, 54, 0xFFFFFF);
		} else {
			Minecraft.getMinecraft().getTextureManager().bindTexture(OverlayCreationList.LOADING_TEX);
			func_152125_a(x, y, 0, 0, 16, 16, 48, 48, 16, 16);
			x += 24;
			y += 56;
			TextUtil.drawCenteredSplitString(fr, "No Selection", x, y, 54, 0xFFFFFF);
		}
	}

	@Override
	public void callback(Creation selected) {
		this.selected = selected;
		this.createButton.enabled = selected != null;
	}
}
