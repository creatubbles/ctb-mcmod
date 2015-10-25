package com.creatubbles.repack.endercore.client.gui.button;

import com.creatubbles.repack.endercore.api.client.gui.IGuiScreen;
import com.creatubbles.repack.endercore.client.render.EnderWidget;

public class CheckBox extends ToggleButton {

	public CheckBox(IGuiScreen gui, int id, int x, int y) {
		super(gui, id, x, y, EnderWidget.BUTTON, EnderWidget.BUTTON_CHECKED);
	}

}
