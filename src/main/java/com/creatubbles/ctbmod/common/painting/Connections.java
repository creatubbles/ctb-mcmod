package com.creatubbles.ctbmod.common.painting;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Connections {

    UP(0, 1),
    RIGHT(1, 0),
    DOWN(0, -1),
    LEFT(-1, 0);
    
    private int offsetX, offsetY;

    public static Connections forOffset(int x, int y) {
        for (Connections c : values()) {
            if (c.offsetX == x && c.offsetY == y) {
                return c;
            }
        }
        return null;
    }
}
