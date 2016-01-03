package com.creatubbles.repack.endercore.api.client.gui;

import com.creatubbles.repack.endercore.client.gui.widget.GuiScrollableList;

public interface ListSelectionListener<T> {

    void selectionChanged(GuiScrollableList<T> list, int selectedIndex);

}
