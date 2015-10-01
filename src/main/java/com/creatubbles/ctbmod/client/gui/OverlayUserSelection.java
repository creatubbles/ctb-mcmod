package com.creatubbles.ctbmod.client.gui;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.List;

import lombok.Value;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import org.lwjgl.util.Dimension;

import com.creatubbles.ctbmod.CTBMod;
import com.creatubbles.ctbmod.common.http.User;
import com.creatubbles.repack.enderlib.api.client.gui.IGuiScreen;
import com.google.common.collect.Lists;

public class OverlayUserSelection extends OverlayBase {

	@Value
	private static class UserAndLocation {

		private User user;
		private Point location;
		private Rectangle bounds;

		private UserAndLocation(User user, Point p) {
			this.user = user;
			this.location = p;
			this.bounds = createBounds(user.getUsername());
		}

		private Rectangle createBounds(String username) {
			FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
			int height = fr.FONT_HEIGHT;
			int width = fr.getStringWidth(username);
			return new Rectangle(location.x - (width / 2), location.y, width, height);
		}
	}

	private final List<User> users = Lists.newArrayList();

	private final List<UserAndLocation> list = Lists.newArrayList();

	protected OverlayUserSelection(int x, int y) {
		super(x, y, new Dimension(100, 50));
	}

	@Override
	public void init(IGuiScreen screen) {
		super.init(screen);
		rebuildList();
	}

	public void add(User user) {
		users.add(user);
		rebuildList();
	}

	public void addAll(Collection<User> collection) {
		users.addAll(collection);
		rebuildList();
	}

	public void clear() {
		users.clear();
		rebuildList();
	}

	public boolean isEmpty() {
		return users.isEmpty();
	}

	private void rebuildList() {
		list.clear();

		// TODO support for multiple columns...too lazy atm

		int count = users.size();
		int height = 60;
		int width = 170;
		int x = getGui().getXSize() / 2;
		int y = 10;
		FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
		int maxHeight = fr.FONT_HEIGHT * count;
		int cols = 1;
		// while (maxHeight > height) {
		// cols++;
		// maxHeight = (fr.FONT_HEIGHT * count) / cols;
		// }

		for (User u : users) {
			list.add(new UserAndLocation(u, new Point(x, y)));
			y += 12;
		}
	}

	@Override
	protected void doDraw(int mouseX, int mouseY, float partialTick) {
		for (UserAndLocation u : list) {
			Point p = u.getLocation();
			boolean hover = isMouseIn(mouseX, mouseY, u.getBounds());
			drawCenteredString(Minecraft.getMinecraft().fontRendererObj, u.getUser().getUsername(), p.x, p.y, hover ? 0xFFFF00 : 0xFFFFFF);
		}
	}

	@Override
	public boolean handleMouseInput(int x, int y, int b) {
		if (super.handleMouseInput(x, y, b)) {
			return true;
		}
		if (b != 0) {
			return false;
		}
		User found = null;
		for (UserAndLocation u : list) {
			if (isMouseIn(x, y, u.getBounds())) {
				found = u.getUser();
				break;
			}
		}
		if (found != null && found != GuiCreator.DUMMY_USER) {
			clear();
			CTBMod.cache.activateUser(found);
			CTBMod.cache.dirty(true);
		}
		return found != null;
	}

}
