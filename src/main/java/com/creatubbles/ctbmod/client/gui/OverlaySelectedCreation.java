package com.creatubbles.ctbmod.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Dimension;
import org.lwjgl.util.Rectangle;

import com.creatubbles.ctbmod.common.http.Creation;
import com.creatubbles.ctbmod.common.http.Image;
import com.creatubbles.ctbmod.common.http.Image.ImageType;
import com.creatubbles.repack.enderlib.client.gui.button.IconButton;

public class OverlaySelectedCreation extends OverlayBase {

	private final OverlayCreationList creationList;
	private final IconButton createButton;

	protected OverlaySelectedCreation(int x, int y, OverlayCreationList creationList, IconButton createButton) {
		super(x, y, new Dimension());
		this.creationList = creationList;
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

		Creation selected = creationList.getSelected();
		if (selected != null) {
			createButton.enabled = true;
			Image img = selected.getImage();
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
			
			func_152125_a(bounds.getX(), bounds.getY(), 0, 0, imgWidth, imgHeight, bounds.getWidth(), bounds.getHeight(), imgWidth, imgHeight);

			x += 24;
			y += 56;
			drawCenteredString(fr, EnumChatFormatting.ITALIC + selected.getName(), x, y, 0xFFFFFF);
//			y += 12;
//			drawCenteredString(fr, "by:", x, y, 0xFFFFFF);
//			Creator[] creators = selected.getCreators();
//			creators = ArrayUtils.add(creators, new Creator(0, 0, "test", "", ""));
//			creators = ArrayUtils.add(creators, new Creator(0, 0, "test", "", ""));
//			creators = ArrayUtils.add(creators, new Creator(0, 0, "test", "", ""));
//			for (int i = 0; i < creators.length && i < 3; i++) {
//				Creator c = creators[i];
//				y += 10;
//				String s = i == 2 && creators.length > 3 ? "- " + (creators.length - 2) + " more -" : c.getName();
//				drawCenteredString(fr, s, x, y, 0xFFFFFF);
//			}
		} else {
			createButton.enabled = false;
			Minecraft.getMinecraft().getTextureManager().bindTexture(OverlayCreationList.LOADING_TEX);
			func_152125_a(x, y, 0, 0, 16, 16, 48, 48, 16, 16);
			x += 24;
			y += 56;
			drawCenteredString(fr, "No Selection", x, y, 0xFFFFFF);
		}
	}

}
