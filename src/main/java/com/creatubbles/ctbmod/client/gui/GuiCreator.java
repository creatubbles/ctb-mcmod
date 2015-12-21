package com.creatubbles.ctbmod.client.gui;

import java.awt.Rectangle;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lombok.SneakyThrows;
import lombok.Synchronized;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.creatubbles.api.core.Creation;
import com.creatubbles.api.core.Creator;
import com.creatubbles.api.core.User;
import com.creatubbles.api.request.auth.OAuthAccessTokenRequest;
import com.creatubbles.api.request.creation.GetCreationsRequest;
import com.creatubbles.api.request.creator.GetCreatorsRequest;
import com.creatubbles.api.request.user.UserProfileRequest;
import com.creatubbles.api.response.creation.GetCreationsResponse;
import com.creatubbles.ctbmod.CTBMod;
import com.creatubbles.ctbmod.common.config.Configs;
import com.creatubbles.ctbmod.common.creator.ContainerCreator;
import com.creatubbles.ctbmod.common.creator.SlotCreator;
import com.creatubbles.ctbmod.common.creator.TileCreator;
import com.creatubbles.ctbmod.common.http.DownloadableImage;
import com.creatubbles.ctbmod.common.http.DownloadableImage.ImageType;
import com.creatubbles.ctbmod.common.network.MessageCreate;
import com.creatubbles.ctbmod.common.network.PacketHandler;
import com.creatubbles.repack.endercore.client.gui.GuiContainerBase;
import com.creatubbles.repack.endercore.client.gui.button.IconButton;
import com.creatubbles.repack.endercore.client.gui.button.MultiIconButton;
import com.creatubbles.repack.endercore.client.gui.widget.GuiToolTip;
import com.creatubbles.repack.endercore.client.gui.widget.TextFieldEnder;
import com.creatubbles.repack.endercore.client.gui.widget.VScrollbar;
import com.creatubbles.repack.endercore.client.render.EnderWidget;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import static net.minecraft.util.EnumChatFormatting.*;
import static org.lwjgl.opengl.GL11.*;

public class GuiCreator extends GuiContainerBase implements ISelectionCallback {

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
					setState(State.LOGGING_IN, true);
					loginReq = new OAuthAccessTokenRequest(tfEmail.getText(), tfActualPassword.getText());
					loginReq.execute();
				}

				checkCancel();

				if (loginReq != null && !loginReq.wasSuccessful()) {
					header = EnumChatFormatting.YELLOW.toString().concat(loginReq.getResponse().message);
					loginReq = null;
					logout();
                } else {
                    if (getUser() == null) {
                        userReq = new UserProfileRequest(loginReq.getResponse().access_token);
                        userReq.execute();
                        CTBMod.cache.activateUser(userReq.getResponse().user);
                        CTBMod.cache.getActiveUser().access_token = loginReq.getResponse().access_token;
                        CTBMod.cache.save();
                    }

                    checkCancel();

                    if (getCreator() == null) {
                        setState(State.LOGGING_IN, true);
                        GetCreatorsRequest creatorsReq = new GetCreatorsRequest(getUser().id, getUser().access_token);
                        creatorsReq.execute();
                        CTBMod.cache.setCreators(creatorsReq.getResponse().creators.toArray(new Creator[0]));
                    }

					checkCancel();

					Creation[] creations = creationList.getCreations();
					if (creations == null) {
						setState(State.LOGGING_IN, true);
						for (Creator c : CTBMod.cache.getCreators()) {
							GetCreationsRequest creationsReq = new GetCreationsRequest(getUser().id, getUser().access_token);
							creationsReq.execute();
							GetCreationsResponse resp = creationsReq.getResponse();
							creations = ArrayUtils.addAll(creations, resp.creations.toArray(new Creation[]{}));
							for (int i = 2; i <= resp.total_pages; i++) {
								creationsReq = new GetCreationsRequest(getUser().id, i, getUser().access_token);
								creationsReq.execute();
								resp = creationsReq.getResponse();
								creations = ArrayUtils.addAll(creations, resp.creations.toArray(new Creation[]{}));
							}
						}
						creationList.setCreations(creations);
                        CTBMod.cache.setCreationCache(creations);
                    }

                    checkCancel();
                    // Once we have all creation data, we can say we are "logged in" while the images download
                    setState(State.LOGGED_IN, true);

                    for (Creation c : creations) {
                        DownloadableImage img = new DownloadableImage(c.image, c);
                        images.put(c, img);
                        img.download(ImageType.LIST_VIEW);
                        checkCancel();
                    }
                }
            } catch (InterruptedException e) {
                CTBMod.logger.info("Logging in canceled!");
                // Clear the cache
                logout();
            } catch (Exception e) {
                CTBMod.logger.error("Logging in uncountered an unknown error.", e);
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
    private static final int ID_H_PLUS = 5, ID_H_MINUS = 6, ID_W_PLUS = 7, ID_W_MINUS = 8;
    private static final ResourceLocation BG_TEX = new ResourceLocation(CTBMod.DOMAIN, "textures/gui/creator.png");
    private static final String DEFAULT_HEADER = "Please log in to Creatubbles:";

    static final ResourceLocation OVERLAY_TEX = new ResourceLocation(CTBMod.DOMAIN, "textures/gui/creator_overlays.png");
    static final User DUMMY_USER = new User();
    static {
        DUMMY_USER.username = "No Users";
    }

    private final Multimap<IHideable, State> visibleMap = MultimapBuilder.hashKeys().enumSetValues(State.class).build();

    Map<Creation, DownloadableImage> images = Maps.newHashMap();

    private final Map<Slot, Pair<Integer, Integer>> slotPositions = Maps.newHashMap();

    private TextFieldEnder tfEmail, tfActualPassword;
    private VScrollbar scrollbar;
    private PasswordTextField tfVisualPassword;
    private GuiButtonHideable loginButton, userButton, cancelButton;
    private IconButton logoutButton, createButton;
    private MultiIconButton heightUpButton, heightDownButton, widthUpButton, widthDownButton;
    private GuiToolTip userInfo, resourceInfo, creationInfo;
    private OverlayCreationList creationList;
    private OverlayUserSelection userSelection;
    private OverlaySelectedCreation selectedCreation;
    private Creation selected;

    private State state;

    private Thread thread;

    private OAuthAccessTokenRequest loginReq;
    private UserProfileRequest userReq;

    private String header = DEFAULT_HEADER;

    private TileCreator te;

    private boolean init;

    public GuiCreator(InventoryPlayer inv, TileCreator creator) {
        super(new ContainerCreator(inv, creator));

        init = getUser() != null;

        initSlotPositions();

        te = creator;
        mc = Minecraft.getMinecraft();
        setState(getUser() == null ? State.LOGGED_OUT : State.LOGGED_IN, false);

        // Must be done before getState() is called
        userSelection = new OverlayUserSelection(10, 10);
        addOverlay(userSelection);

        ySize += 44;
        xSize = getState() == State.LOGGED_IN ? XSIZE_SIDEBAR : XSIZE_DEFAULT;
        tfEmail = new TextFieldEnder(getFontRenderer(), XSIZE_DEFAULT / 2 - 75, 35, 150, 12);
        tfEmail.setFocused(getState() == State.LOGGED_OUT);
        textFields.add(tfEmail);

        // This is a dummy to store the uncensored PW
        tfActualPassword = new TextFieldEnder(getFontRenderer(), 0, 0, 0, 0);

        tfVisualPassword = new PasswordTextField(getFontRenderer(), XSIZE_DEFAULT / 2 - 75, 65, 150, 12);
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
        addOverlay(creationList);


        logoutButton = new IconButton(this, ID_LOGOUT, 7, 106, EnderWidget.CROSS);
        logoutButton.setToolTip("Log Out");

        createButton = new IconButton(this, ID_CREATE, 110, 29, EnderWidget.TICK) {

            @Override
            public boolean isEnabled() {
                return super.isEnabled() && te.canCreate();
            }
        };
        createButton.setToolTip("Create!");

        selectedCreation = new OverlaySelectedCreation(10, 10, creationList, createButton);
        creationList.addCallback(selectedCreation);
        creationList.addCallback(this);
        addOverlay(selectedCreation);

        userInfo = new GuiToolTip(new Rectangle()) {

            @Override
            public Rectangle getBounds() {
                if (getUser() == null) {
                    return new Rectangle();
                }
                FontRenderer fr = getFontRenderer();
                return new Rectangle(25, 110, fr.getStringWidth(getUser().username), 8);
            }

            @Override
            protected void updateText() {
                User u = getUser();
                setToolTipText(StringUtils.capitalize(u.role), getCreator().age, u.country);
            }
        };
        addToolTip(userInfo);

        resourceInfo = new GuiToolTip(new Rectangle(69, 79, 40, 30)) {

            @Override
            protected void updateText() {
                setToolTipText("Creating paintings takes resources!", "A painting of this size needs:", "- " + te.getRequiredPaper() + " pieces of paper", "- " + te.getRequiredDye()
                        + " of each dye (red, green, blue)");
            }
        };
        addToolTip(resourceInfo);

        creationInfo = new GuiToolTip(new Rectangle(10, 10, 54, 54)) {

            @Override
            protected void updateText() {
                if (selected == null) {
                    setToolTipText("No creation selected!", "Select a creation from the pane on the right.");
                } else {
                    setToolTipText();
                }
            }
        };
        addToolTip(creationInfo);

        int y = 61;
        int x = 97;
        widthUpButton = MultiIconButton.createAddButton(this, ID_W_PLUS, x, y);
        widthDownButton = MultiIconButton.createMinusButton(this, ID_W_MINUS, x, y + 8);
        x += 50;
        heightUpButton = MultiIconButton.createAddButton(this, ID_H_PLUS, x, y);
        heightDownButton = MultiIconButton.createMinusButton(this, ID_H_MINUS, x, y + 8);
    }

    @Override
    @SneakyThrows
    public void initGui() {
        // logout();

        super.initGui();
        buttonList.clear();

        addButton(loginButton = new GuiButtonHideable(ID_LOGIN, guiLeft + xSize / 2 - 75, guiTop + 90, 75, 20, "Log in"));
        addButton(userButton = new GuiButtonHideable(ID_USER, guiLeft + xSize / 2, guiTop + 90, 75, 20, "Saved Users"));
        addButton(cancelButton = new GuiButtonHideable(ID_CANCEL, guiLeft + xSize / 2 - 50, guiTop + 90, 100, 20, "Cancel"));
        logoutButton.onGuiInit();
        createButton.onGuiInit();
        heightUpButton.onGuiInit();
        heightDownButton.onGuiInit();
        widthUpButton.onGuiInit();
        widthDownButton.onGuiInit();

        addScrollbar(scrollbar);

        // Avoids CME when state is set during this method
        synchronized (visibleMap) {
            visibleMap.put(tfEmail, State.LOGGED_OUT);
            visibleMap.put(tfVisualPassword, State.LOGGED_OUT);
            visibleMap.put(userSelection, State.USER_SELECT);
            visibleMap.put(creationList, State.LOGGED_IN);
            visibleMap.put(selectedCreation, State.LOGGED_IN);
            visibleMap.put(userInfo, State.LOGGED_IN);
            visibleMap.put(resourceInfo, State.LOGGED_IN);
            visibleMap.put(creationInfo, State.LOGGED_IN);
            visibleMap.put(loginButton, State.LOGGED_OUT);
            visibleMap.put(userButton, State.LOGGED_OUT);
            visibleMap.putAll(cancelButton, Lists.newArrayList(State.USER_SELECT, State.LOGGING_IN));
            visibleMap.put(logoutButton, State.LOGGED_IN);
            visibleMap.put(createButton, State.LOGGED_IN);
            visibleMap.put(heightUpButton, State.LOGGED_IN);
            visibleMap.put(heightDownButton, State.LOGGED_IN);
            visibleMap.put(widthUpButton, State.LOGGED_IN);
            visibleMap.put(widthDownButton, State.LOGGED_IN);
            visibleMap.put(scrollbar, State.LOGGED_IN);
        }

        creationList.setCreations(CTBMod.cache.getCreationCache());

        updateVisibility();

        if (init) {
            init = false;
            actionPerformed(loginButton);
        }
    }

    @Override
    public void callback(Creation selected) {
        this.selected = selected;
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
    public void onGuiClosed() {
        super.onGuiClosed();
        if (thread != null) {
            thread.interrupt();
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        glColor4f(1, 1, 1, 1);
        glDisable(GL_LIGHTING);

        State state = getState();

        mc.getTextureManager().bindTexture(BG_TEX);
        int x = guiLeft;
        int y = guiTop;
        drawTexturedModalRect(x, y, 0, 0, XSIZE_DEFAULT, ySize);

        // TODO localize all the things

        switch (state) {
            case LOGGED_IN:
                if (getUser() == null) {
                    System.out.println("!");
                }

                for (int i = 0; i < 4; i++) {
                    SlotCreator slot = (SlotCreator) inventorySlots.getSlot(i);
                    drawGhostSlotGrayout(slot.getGhostStack(), slot);
                }

                creationList.setScroll(scrollbar.getScrollPos());

                x += 25;
                y += 110;
                drawString(getFontRenderer(), getUser().username, x, y, 0xFFFFFF);

                x = guiLeft + 90;
                y = guiTop + 12;

                mc.getTextureManager().bindTexture(OVERLAY_TEX);
                drawTexturedModalRect(scrollbar.getX(), scrollbar.getY() + 8, creationList.getWidth() * 2, 8, 11, scrollbar.getWholeArea().height - 16);

                x = guiLeft + 70;
                y = guiTop + 19;
                drawTexturedModalRect(x, y, 94, 54, 36, 36);

                x += 74;
                y += 9;

                drawTexturedModalRect(x, y, 94, 90, 18, 18);

                x -= 23;
                y += 3;
                mc.getTextureManager().bindTexture(OVERLAY_TEX);
                int progress = te.getProgressScaled(16);
                if (progress > 0) {
                    progress += 6;
                    glColor3f(88f / 255f, 196f / 255f, 61f / 255f);
                    drawTexturedModalRect(x, y, 0, 210, progress, 12);
                    glColor3f(1, 1, 1);
                    drawTexturedModalRect(x + progress, y, progress, 210, 22 - progress, 12);
                } else {
                    if (createButton.isEnabled()) {
                        glColor3f(1, 1, 1);
                    } else {
                        glColor3f(0.5f, 0.5f, 0.5f);
                    }
                    drawTexturedModalRect(x, y, 0, 210, 22, 12);
                }

                x = guiLeft + 70;
                y = guiTop + 65;
                drawString(getFontRenderer(), "W: " + te.getWidth(), x, y, 0xFFFFFF);
                drawString(getFontRenderer(), "H: " + te.getHeight(), x + 50, y, 0xFFFFFF);

                y += 18;
                EnumChatFormatting color = te.getRequiredPaper() > te.getPaperCount() ? RED : GREEN;
                drawString(getFontRenderer(), "Paper: " + color + te.getRequiredPaper(), x, y, 0xFFFFFF);
                if (Configs.harderPaintings) {
                    y += 10;
                    color = te.getRequiredDye() > te.getLowestDyeCount() ? RED : GREEN;
                    drawString(getFontRenderer(), "Dye: " + color + te.getRequiredDye(), x, y, 0xFFFFFF);
                }
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

    /**
<<<<<<< HEAD
     * Gray out the item that was just painted into a GhostSlot by overpainting it with 50% transparent background. This
     * gives the illusion that the item was painted with 50% transparency. (100%*a ° 100%*b ° 50%*a == 100%*a ° 50%*b)
=======
     * Gray out the item that was just painted into a GhostSlot by overpainting it with 50% transparent background. This gives the illusion that the item was painted with 50% transparency. (100%*a ï¿½
     * 100%*b ï¿½ 50%*a == 100%*a ï¿½ 50%*b)
>>>>>>> 9157c69... api v2
     */
    protected void drawGhostSlotGrayout(ItemStack stack, Slot slot) {
        int x = guiLeft + slot.xDisplayPosition;
        int y = guiTop + slot.yDisplayPosition;

        glDisable(GL_LIGHTING);
        itemRender.renderItemAndEffectIntoGUI(getFontRenderer(), Minecraft.getMinecraft().getTextureManager(), stack, x, y);
        itemRender.renderItemOverlayIntoGUI(getFontRenderer(), Minecraft.getMinecraft().getTextureManager(), stack, x + 1, y, "");

        glEnable(GL_BLEND);
        glDisable(GL_LIGHTING);
        glDisable(GL_DEPTH_TEST);
        glColor4f(1, 1, 1, 0.25f);
        Minecraft.getMinecraft().getTextureManager().bindTexture(BG_TEX);
        drawTexturedModalRect(x, y, 8, 127, 16, 16);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
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

    private void setState(State state, boolean update) {
        this.state = state;
        if (update) {
            updateSize();
            updateVisibility();
        }
        if (tfEmail != null) {
            tfEmail.setFocused(state == State.LOGGED_OUT);
            tfVisualPassword.setFocused(false);
            if (state == State.LOGGED_IN) {
                tfEmail.setText("");
                tfVisualPassword.setText("");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void initSlotPositions() {
        for (Slot s : (List<Slot>) inventorySlots.inventorySlots) {
            slotPositions.put(s, Pair.of(s.xDisplayPosition, s.yDisplayPosition));
        }
    }

    @SuppressWarnings("unchecked")
    @Synchronized("visibleMap")
    private void updateVisibility() {
        for (Entry<IHideable, Collection<State>> e : visibleMap.asMap().entrySet()) {
            boolean visible = e.getValue().contains(getState());
            e.getKey().setIsVisible(visible);
        }
        int offset = getState() == State.LOGGED_IN ? 0 : 5000;
        for (Slot s : (List<Slot>) inventorySlots.inventorySlots) {
            Pair<Integer, Integer> pos = slotPositions.get(s);
            s.xDisplayPosition = pos.getLeft() + offset;
            s.yDisplayPosition = pos.getRight() + offset;
        }
    }

    private User getUser() {
        return CTBMod.cache.getActiveUser();
    }

    private Creator getCreator() {
        return CTBMod.cache.getActiveCreator();
    }

    @Override
    protected void mouseClicked(int x, int y, int button) {
        super.mouseClicked(x, y, button);
        // This works around the fact that changing the visibility in actionPerformed
        // will cause buttons that didn't exist when the user clicked to be valid targets
        updateVisibility();
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
                setState(State.USER_SELECT, false);
                break;
            case ID_CANCEL:
                if (getState() == State.LOGGING_IN) {
                    thread.interrupt();
                    cancelButton.setEnabled(false);
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
            case ID_W_MINUS:
                te.setWidth(te.getWidth() - 1);
                break;
            case ID_W_PLUS:
                te.setWidth(te.getWidth() + 1);
                break;
            case ID_H_MINUS:
                te.setHeight(te.getHeight() - 1);
                break;
            case ID_H_PLUS:
                te.setHeight(te.getHeight() + 1);
                break;
            case ID_CREATE:
                PacketHandler.INSTANCE.sendToServer(new MessageCreate(selected, te));
                break;
        }
        widthDownButton.setEnabled(te.getWidth() > te.getMinSize());
        widthUpButton.setEnabled(te.getWidth() < te.getMaxSize());
        heightDownButton.setEnabled(te.getHeight() > te.getMinSize());
        heightUpButton.setEnabled(te.getHeight() < te.getMaxSize());
    }

    private void logout() {
        setState(State.LOGGED_OUT, true);
        CTBMod.cache.activateUser(null);
        CTBMod.cache.setCreators(null);
        CTBMod.cache.setCreationCache(null);
        creationList.setCreations(null);
    }
}
