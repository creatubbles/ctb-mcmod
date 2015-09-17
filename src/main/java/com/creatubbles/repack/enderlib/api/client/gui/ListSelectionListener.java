package com.creatubbles.repack.enderlib.api.client.gui;

import com.creatubbles.repack.enderlib.client.gui.widget.GuiScrollableList;

public interface ListSelectionListener<T> {

  void selectionChanged(GuiScrollableList<T> list, int selectedIndex);

}
