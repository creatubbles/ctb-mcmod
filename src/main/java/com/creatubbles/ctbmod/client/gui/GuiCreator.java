package com.creatubbles.ctbmod.client.gui;

import java.util.Collection;
import java.util.Map.Entry;

import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Rectangle;

import com.creatubbles.ctbmod.CTBMod;
import com.creatubbles.ctbmod.common.creator.ContainerCreator;
import com.creatubbles.ctbmod.common.http.Creation;
import com.creatubbles.ctbmod.common.http.CreationsRequest;
import com.creatubbles.ctbmod.common.http.Creator;
import com.creatubbles.ctbmod.common.http.CreatorsRequest;
import com.creatubbles.ctbmod.common.http.Image;
import com.creatubbles.ctbmod.common.http.Image.ImageType;
import com.creatubbles.ctbmod.common.http.Login;
import com.creatubbles.ctbmod.common.http.LoginRequest;
import com.creatubbles.ctbmod.common.http.User;
import com.creatubbles.ctbmod.common.http.UserRequest;
import com.creatubbles.repack.endercore.client.render.EnderWidget;
import com.creatubbles.repack.enderlib.client.gui.GuiContainerBase;
import com.creatubbles.repack.enderlib.client.gui.button.IconButton;
import com.creatubbles.repack.enderlib.client.gui.widget.TextFieldEnder;
import com.creatubbles.repack.enderlib.client.gui.widget.VScrollbar;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

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
	
	private class LoginRunnable implements Runnable {

		@Override
		public void run() {

			try {
				checkCancel();
				
				if (getUser() == null) {
					setState(State.LOGGING_IN);
					loginReq = new LoginRequest(new Login(tfEmail.getText(), tfActualPassword.getText()));
					loginReq.run();
				}

				checkCancel();

				if (loginReq != null && loginReq.failed()) {
					if (loginReq.getException() != null) {
						header = loginReq.getException().getLocalizedMessage();
					} else {
						header = loginReq.getFailedResult().getMessage();
					}
					header = EnumChatFormatting.YELLOW.toString().concat(header);
					loginReq = null;
					logout();
				} else {
					if (getUser() == null) {
						userReq = new UserRequest(loginReq.getSuccessfulResult().getAccessToken());
						userReq.run();
						CTBMod.cache.activateUser(userReq.getSuccessfulResult());
						CTBMod.cache.getActiveUser().setAccessToken(loginReq.getSuccessfulResult().getAccessToken());
						CTBMod.cache.save();
					}

					checkCancel();

					if (getCreator() == null) {
						setState(State.LOGGING_IN);
						CreatorsRequest creatorsReq = new CreatorsRequest(Integer.toString(getUser().getId()));
						creatorsReq.run();
						CTBMod.cache.setCreators(creatorsReq.getSuccessfulResult().getCreators());
					}

					checkCancel();

					Creation[] creations = creationList.getCreations();
					if (creations == null) {
						setState(State.LOGGING_IN);
						for (Creator c : CTBMod.cache.getCreators()) {
							CreationsRequest creationsReq = new CreationsRequest(c.getId());
							creationsReq.run();
							creations = ArrayUtils.addAll(creations, creationsReq.getSuccessfulResult());
						}
						creationList.setCreations(creations);
						CTBMod.cache.setCreationCache(creations);
					}

					checkCancel();
					// Once we have all creation data, we can say we are "logged in" while the images download
					setState(State.LOGGED_IN);
					
					for (Creation c : creations) {
						c.getImage().download(ImageType.LIST_VIEW);
						checkCancel();
					}
				}
			} catch (InterruptedException e) {
				CTBMod.logger.info("Logging in canceled!");
				// Clear the cache
				logout();
			} finally {
				// Thread cleanup, erase all evidence we were here
				// This assures a fresh start if a new login is attempted
				loginReq = null;
				userReq = null;
				thread = null;
				cancelButton.enabled = true;
			}
		}

		private void checkCancel() throws InterruptedException {
			if (thread.isInterrupted()) {
				throw new InterruptedException();
			}
		}
	}

	public enum State {
		LOGGED_OUT,
		USER_SELECT,
		LOGGING_IN,
		LOGGED_IN;
	}

	private static final int XSIZE_DEFAULT = 176, XSIZE_SIDEBAR = 270;
	private static final int ID_LOGIN = 0, ID_USER = 1, ID_CANCEL = 2, ID_LOGOUT = 3, ID_CREATE = 4;
	private static final ResourceLocation BG_TEX = new ResourceLocation(CTBMod.DOMAIN, "textures/gui/creator.png");
	private static final String DEFAULT_HEADER = "Please log in to Creatubbles:";
	
	static final ResourceLocation OVERLAY_TEX = new ResourceLocation(CTBMod.DOMAIN, "textures/gui/creator_overlays.png");
	static final User DUMMY_USER = new User(0, "No Users", "", "", "", false, false);

	private Multimap<IHideable, State> visibleMap = MultimapBuilder.hashKeys().enumSetValues(State.class).build();
	
	private TextFieldEnder tfEmail, tfActualPassword;
	private VScrollbar scrollbar;
	private PasswordTextField tfVisualPassword;
	private GuiButtonHideable loginButton, userButton, cancelButton;
	private IconButton logoutButton, createButton;
	private OverlayCreationList creationList;
	private OverlayUserSelection userSelection;
	
	private State state;
	
	private Thread thread;
	
	private LoginRequest loginReq;
	private UserRequest userReq;

	private String header = DEFAULT_HEADER;

	public GuiCreator(InventoryPlayer inv) {
		super(new ContainerCreator(inv));
		this.mc = Minecraft.getMinecraft();
		this.state = getUser() == null ? State.LOGGED_OUT : State.LOGGED_IN;

		// Must be done before getState() is called
		userSelection = new OverlayUserSelection(10, 10);
		visibleMap.put(userSelection, State.USER_SELECT);
		addOverlay(userSelection);

		ySize += 44;
		xSize = getState() == State.LOGGED_IN ? XSIZE_SIDEBAR : XSIZE_DEFAULT;
		tfEmail = new TextFieldEnder(getFontRenderer(), (xSize / 2) - 75, 35, 150, 12);
		visibleMap.put(tfEmail, State.LOGGED_OUT);
		tfEmail.setFocused(true);
		textFields.add(tfEmail);
		
		// This is a dummy to store the uncensored PW
		tfActualPassword = new TextFieldEnder(getFontRenderer(), 0, 0, 0, 0);

		tfVisualPassword = new PasswordTextField(getFontRenderer(), (xSize / 2) - 75, 65, 150, 12);
		visibleMap.put(tfVisualPassword, State.LOGGED_OUT);
		textFields.add(tfVisualPassword);

		scrollbar = new VScrollbar(this, XSIZE_SIDEBAR - 11, 0, ySize + 1) {

			@Override
			public int getScrollMax() {
				return creationList.getMaxScroll();
			}
			
			@Override
			public void mouseWheel(int x, int y, int delta) {
				if (!isDragActive()) {
					scrollBy(-Integer.signum(delta) * 4);
				}
			}
		};

		creationList = new OverlayCreationList(XSIZE_DEFAULT, 0);
		visibleMap.put(creationList, State.LOGGED_IN);
		addOverlay(creationList);
		
		logoutButton = new IconButton(this, ID_LOGOUT, 7, 106, EnderWidget.CROSS);
		logoutButton.setToolTip("Log Out");
		addButton(logoutButton);
		
		createButton = new IconButton(this, ID_CREATE, 139, 43, EnderWidget.TICK);
		createButton.setToolTip("Create!");
		addButton(createButton);
	}

	@Override
	@SneakyThrows
	public void initGui() {
//		 CTBMod.cache.activateUser(null);
//		 CTBMod.cache.setCreators(null);
		
		super.initGui();
		buttonList.clear();

		addButton(loginButton = new GuiButtonHideable(ID_LOGIN, guiLeft + xSize / 2 - 75, guiTop + 90, 75, 20, "Log in"));
		addButton(userButton = new GuiButtonHideable(ID_USER, guiLeft + xSize / 2, guiTop + 90, 75, 20, "Saved Users"));
		addButton(cancelButton = new GuiButtonHideable(ID_CANCEL, guiLeft + xSize / 2 - 50, guiTop + 90, 100, 20, "Cancel"));
		logoutButton.onGuiInit();
		createButton.onGuiInit();
		
		visibleMap.put(loginButton, State.LOGGED_OUT);
		visibleMap.put(userButton, State.LOGGED_OUT);
		visibleMap.putAll(cancelButton, Lists.newArrayList(State.USER_SELECT, State.LOGGING_IN));
		visibleMap.put(logoutButton, State.LOGGED_IN);
		visibleMap.put(createButton, State.LOGGED_IN);
		
		addScrollbar(scrollbar);
		visibleMap.put(scrollbar, State.LOGGED_IN);		

		// TODO this needs to go
		if (thread == null && getUser() != null) {
			actionPerformed(loginButton);
		}
	}
	
	@Override
	protected void keyTyped(char c, int key) {
		if ((c == '\r' || c == '\n') && (tfEmail.isFocused() || tfVisualPassword.isFocused())) {
			actionPerformed(loginButton);
		}
		super.keyTyped(c, key);
	}

	@Override
	@SneakyThrows
	public void updateScreen() {
		if (thread != null && !thread.isAlive()) {
			thread = null;
			loginReq = null;
			userReq = null;
		}
		if (CTBMod.cache.isDirty()) {
			if (thread != null) {
				System.out.println("!");
			}
		}
		if (CTBMod.cache.isDirty() && thread == null) {
			actionPerformed(loginButton);
			CTBMod.cache.dirty(false);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glDisable(GL11.GL_LIGHTING);

		State state = getState();

		for (Entry<IHideable, Collection<State>> e : visibleMap.asMap().entrySet()) {
			boolean visible = e.getValue().contains(state);
			e.getKey().setIsVisible(visible);
		}

		mc.getTextureManager().bindTexture(BG_TEX);
		int x = guiLeft;
		int y = guiTop;
		drawTexturedModalRect(x, y, 0, 0, XSIZE_DEFAULT, ySize);

		// TODO localize all the things

		switch(state) {
		case LOGGED_IN:
			creationList.setScroll(scrollbar.getScrollPos());
			
			x += 40;
			y += 5;
			drawCenteredString(getFontRenderer(), EnumChatFormatting.UNDERLINE + getUser().getUsername(), x, y, 0xFFFFFF);
			Creator creator = getCreator();
			if (creator != null) {
				y += 15;
				drawCenteredString(getFontRenderer(), StringUtils.capitalize(getUser().getRole()), x, y, 0xFFFFFF);
				y += 15;
				drawCenteredString(getFontRenderer(), getUser().getCountry(), x, y, 0xFFFFFF);
				y += 15;
				drawCenteredString(getFontRenderer(), creator.getAge(), x, y, 0xFFFFFF);
			}
			
			x = guiLeft + 105;
			y = guiTop + 9;
			
			mc.getTextureManager().bindTexture(OVERLAY_TEX);
			drawTexturedModalRect(x - 3, y - 3, 94, 0, 54, 54);
			
			Creation selected = creationList.getSelected();
			if (selected != null) {
				createButton.setIsVisible(true);
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
				
				mc.getTextureManager().bindTexture(res);

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
				drawCenteredString(getFontRenderer(), EnumChatFormatting.ITALIC + selected.getName(), x, y, 0xFFFFFF);
				y += 12;
				drawCenteredString(getFontRenderer(), "by:", x, y, 0xFFFFFF);
				Creator[] creators = selected.getCreators();
				Creator dummy = new Creator(0, 0, "test", "", "");
				creators = ArrayUtils.addAll(creators, dummy, dummy, dummy);
				for (int i = 0; i < creators.length && i < 4; i++) {
					Creator c = creators[i % creators.length];
					y += 10;
					String s = i == 3 && creators.length > 4 ? (creators.length - 3) + " more..." : c.getName();
					drawCenteredString(getFontRenderer(), s, x, y, 0xFFFFFF);
				}
			} else {
				createButton.setIsVisible(false);
			}
			
			x = guiLeft + 90;
			y = guiTop + 12;
			
			mc.getTextureManager().bindTexture(OVERLAY_TEX);
			drawTexturedModalRect(scrollbar.getX(), scrollbar.getY() + 8, creationList.getWidth() * 2, 8, 11, scrollbar.getWholeArea().height - 16);

			break;
		case LOGGED_OUT:
			x += xSize / 2;
			y += 5;
			drawCenteredString(getFontRenderer(), header, x, y, 0xFFFFFF);

			x = guiLeft + 13;
			y = guiTop + 20;
			drawString(getFontRenderer(), "Email/Username:", tfEmail.xPosition, tfEmail.yPosition - 10, 0xFFFFFF);

			y += 25;
			drawString(getFontRenderer(), "Password:", tfVisualPassword.xPosition, tfVisualPassword.yPosition - 10, 0xFFFFFF);
			break;
		case USER_SELECT:
			break;
		case LOGGING_IN:
			x += xSize / 2;
			y += 25;
			String s = thread.isInterrupted() ? "Canceling..." : "Logging in...";
			drawCenteredString(getFontRenderer(), s, x, y, 0xFFFFFF);
			break;
		}
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
	}
	
	private void updateSize() {
		int newSize = getState() == State.LOGGED_IN ? XSIZE_SIDEBAR : XSIZE_DEFAULT;
		if (xSize != newSize) {
			xSize = newSize;
			initGui();
		}
	}

	State getState() {
		return state;
	}
	
	private void setState(State state) {
		this.state = state;
		updateSize();
	}

	private User getUser() {
		return CTBMod.cache.getActiveUser();
	}
	
	private Creator getCreator() {
		return CTBMod.cache.getActiveCreator();
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		switch (button.id) {
		case ID_LOGIN:
			header = DEFAULT_HEADER;
			thread = new Thread(new LoginRunnable());
			thread.start();
			break;
		case ID_USER:
			userSelection.clear();
			userSelection.addAll(CTBMod.cache.getSavedUsers());
			if (userSelection.isEmpty()) {
				userSelection.add(DUMMY_USER);
			}
			setState(State.USER_SELECT);
			break;
		case ID_CANCEL:
			if (getState() == State.LOGGING_IN) {
				thread.interrupt();
				cancelButton.enabled = false;
			} else {
				logout();
			}
			break;
		case ID_LOGOUT:
			if (thread != null) {
				// Creation/Image data may still be processing, this will potentially save bandwidth
				thread.interrupt();
			}
			logout();
			break;
		}
	}

	private void logout() {
		CTBMod.cache.activateUser(null);
		CTBMod.cache.setCreators(null);
		CTBMod.cache.setCreationCache(null);
		creationList.setCreations(null);
		setState(State.LOGGED_OUT);
	}
}
