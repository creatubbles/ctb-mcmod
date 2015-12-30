package com.creatubbles.ctbmod.client.gui;

import java.util.List;

import lombok.experimental.UtilityClass;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

@UtilityClass
public class TextUtil {

    private static final Gui GUI = new Gui();

    public void drawCenteredSplitString(FontRenderer fr, String string, int x, int y, int width, int color) {
        List<String> strings = fr.listFormattedStringToWidth(string, width);
        for (int i = 0; i < strings.size(); i++) {
            GUI.drawCenteredString(fr, strings.get(i), x, y + i * (fr.FONT_HEIGHT + 2), color);
        }
    }
}
