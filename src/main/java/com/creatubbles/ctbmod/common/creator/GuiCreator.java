package com.creatubbles.ctbmod.common.creator;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import com.creatubbles.ctbmod.CTBMod;
import com.creatubbles.ctbmod.common.config.Configs;
import com.creatubbles.repack.enderlib.client.gui.GuiContainerBase;
import com.creatubbles.repack.enderlib.client.gui.widget.TextFieldEnder;

public class GuiCreator extends GuiContainerBase {

	private static class PasswordTextField extends TextFieldEnder {

		String actualText;
		
		public PasswordTextField(FontRenderer fnt, int x, int y, int width, int height) {
			super(fnt, x, y, width, height);
		}

		private String transformText(String text) {
			actualText = text;
			if (!"_".equals(text)) {
				text = text.replaceAll(".", "\u2022");
			}
			return text;
		}
		
		@Override
		public void setText(String text) {
			super.setText(transformText(text));
		}
		
		@Override
		public void writeText(String text) {
			super.writeText(transformText(text));
		}
	}

	private TextFieldEnder tfEmail, tfPassword;
	
	public GuiCreator(InventoryPlayer inv) {
		super(new ContainerCreator(inv));
		ySize += 22;
		tfEmail = new TextFieldEnder(getFontRenderer(), (xSize / 2) - 75, 30, 150, 10);
		tfEmail.setFocused(true);
		textFields.add(tfEmail);
		
		tfPassword = new PasswordTextField(getFontRenderer(), (xSize / 2) - 75, 55, 150, 10);
		textFields.add(tfPassword);
	}
	
	@Override
	public void initGui() {
		super.initGui();
		
		addButton(new GuiButton(0, guiLeft + xSize / 2 - 50, guiTop + 75, 100, 20, "Log in"));

		if (Configs.cachedUser != null) {
			tfEmail.setVisible(false);
			tfEmail.setFocused(false);
			tfPassword.setVisible(false);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1, 1, 1, 1);

		this.mc.getTextureManager().bindTexture(new ResourceLocation(CTBMod.DOMAIN, "textures/gui/creator.png"));
		int x = guiLeft;
		int y = guiTop;
		drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
		
		// TODO localize all the things
		
		x += xSize / 2;
		y += 5;
		drawCenteredString(getFontRenderer(), "Please log in to Creatubbles", x, y, 0xFFFFFF);
		
		x = guiLeft + 13;
		y = guiTop + 20;
		drawString(getFontRenderer(), "Email:", x, y, 0xFFFFFF);
		
		y += 25;
		drawString(getFontRenderer(), "Password:", x, y, 0xFFFFFF);
				
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
	}
}
