package com.creatubbles.repack.enderlib.client.gui.button;

import com.creatubbles.repack.endercore.client.render.EnderWidget;
import com.creatubbles.repack.enderlib.api.client.gui.IGuiScreen;

public class CheckBox extends ToggleButton {

  public CheckBox(IGuiScreen gui, int id, int x, int y) {
    super(gui, id, x, y, EnderWidget.BUTTON, EnderWidget.BUTTON_CHECKED);
  }

}
