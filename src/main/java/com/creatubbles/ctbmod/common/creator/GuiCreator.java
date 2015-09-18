package com.creatubbles.ctbmod.common.creator;

import java.io.IOException;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import com.creatubbles.ctbmod.CTBMod;
import com.creatubbles.ctbmod.common.config.Configs;
import com.creatubbles.ctbmod.common.http.Login;
import com.creatubbles.ctbmod.common.http.LoginRequest;
import com.creatubbles.ctbmod.common.http.User;
import com.creatubbles.ctbmod.common.http.UserRequest;
import com.creatubbles.repack.enderlib.client.gui.GuiContainerBase;
import com.creatubbles.repack.enderlib.client.gui.widget.TextFieldEnder;

public class GuiCreator extends GuiContainerBase {

	private class PasswordTextField extends TextFieldEnder {

		public PasswordTextField(FontRenderer fnt, int x, int y, int width, int height) {
			super(fnt, x, y, width, height);
		}
		
		@Override
		public void setFocused(boolean state) {
			super.setFocused(state);
			GuiCreator.this.tfActualPassword.setFocused(state);
		}

		private String transformText(String text) {
			if (!"_".equals(text)) {
				text = text.replaceAll(".", "\u2022");
			}
			return text;
		}

		@Override
		public void setText(String text) {
			GuiCreator.this.tfActualPassword.setText(text);
			super.setText(transformText(text));
		}

		@Override
		public void writeText(String text) {
			GuiCreator.this.tfActualPassword.writeText(text);
			super.writeText(transformText(text));
		}
		
		@Override
		public void deleteWords(int cursor) {
			GuiCreator.this.tfActualPassword.deleteWords(cursor);
			super.deleteWords(cursor);
		}
		
		@Override
		public void deleteFromCursor(int cursor) {
			GuiCreator.this.tfActualPassword.deleteFromCursor(cursor);
			super.deleteFromCursor(cursor);
		}
	}

	private enum State {
		LOGGED_OUT,
		LOGGING_IN,
		LOGGED_IN;
	}

	private static final int ID_LOGIN = 0;

	private TextFieldEnder tfEmail, tfActualPassword;
	private PasswordTextField tfVisualPassword;
	private GuiButton loginButton;
	private LoginRequest loginReq;
	private UserRequest userReq;
	private String header = "Plesae log in to Creatubbles:";

	public GuiCreator(InventoryPlayer inv) {
		super(new ContainerCreator(inv));
		ySize += 22;
		tfEmail = new TextFieldEnder(getFontRenderer(), (xSize / 2) - 75, 30, 150, 10);
		tfEmail.setFocused(true);
		textFields.add(tfEmail);
		
		// This is a dummy to store the uncensored PW
		tfActualPassword = new TextFieldEnder(getFontRenderer(), 0, 0, 0, 0);

		tfVisualPassword = new PasswordTextField(getFontRenderer(), (xSize / 2) - 75, 55, 150, 10);
		textFields.add(tfVisualPassword);
	}

	@Override
	public void initGui() {
		super.initGui();
		Configs.cachedUser = null;
		addButton(loginButton = new GuiButton(ID_LOGIN, guiLeft + xSize / 2 - 50, guiTop + 75, 100, 20, "Log in"));
	}
	
	@Override
	protected void keyTyped(char c, int key) throws IOException {
		if ((c == '\r' || c == '\n') && (tfEmail.isFocused() || tfVisualPassword.isFocused())) {
			actionPerformed(loginButton);
		}
		super.keyTyped(c, key);
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		if (getState() != State.LOGGED_OUT) {
			tfEmail.setVisible(false);
			tfVisualPassword.setVisible(false);
			loginButton.visible = false;
		} else {
			tfEmail.setVisible(true);
			tfVisualPassword.setVisible(true);
			loginButton.visible = true;
		}
		if (loginReq != null && loginReq.isComplete() && userReq == null) {
			if (loginReq.failed()) {
				if (loginReq.getException() != null) {
					header = loginReq.getException().getLocalizedMessage();
				} else {
					header = loginReq.getFailedResult().getMessage();
				}
				header = EnumChatFormatting.YELLOW.toString().concat(header);
				loginReq = null;
			} else {
				new Thread(userReq = new UserRequest(loginReq.getSuccessfulResult().getAccessToken())).start();
			}
		}
		if (userReq != null && userReq.isComplete() && getUser() == null) {
			Configs.cachedUser = userReq.getSuccessfulResult();
			Configs.cachedUser.setAccessToken(loginReq.getSuccessfulResult().getAccessToken());
			Configs.cacheUser();
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

		switch(getState()) {
		case LOGGED_IN:
			x += xSize / 2;
			y += 25;
			drawCenteredString(getFontRenderer(), "Logged in as: " + getUser().getUsername(), x, y, 0xFFFFFF);
			break;
		case LOGGED_OUT:
			x += xSize / 2;
			y += 5;
			drawCenteredString(getFontRenderer(), header, x, y, 0xFFFFFF);

			x = guiLeft + 13;
			y = guiTop + 20;
			drawString(getFontRenderer(), "Email:", x, y, 0xFFFFFF);

			y += 25;
			drawString(getFontRenderer(), "Password:", x, y, 0xFFFFFF);
			break;
		case LOGGING_IN:
			x += xSize / 2;
			y += 25;
			drawCenteredString(getFontRenderer(), "Logging in...", x, y, 0xFFFFFF);
			break;
		}
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
	}
	
	private State getState() {
		if (loginReq != null) {
			return userReq != null && userReq.isComplete() && getUser() != null ? State.LOGGED_IN : State.LOGGING_IN;
		} else {
			return getUser() == null ? State.LOGGED_OUT : State.LOGGED_IN;
		}
	}
	
	private User getUser() {
		return Configs.cachedUser;
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		switch (button.id) {
		case ID_LOGIN:
			new Thread(loginReq = new LoginRequest(new Login(tfEmail.getText(), tfActualPassword.getText()))).start();
			break;
		}
	}
}
