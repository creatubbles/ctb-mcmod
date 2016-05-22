package com.creatubbles.ctbmod.common.painting;

import java.util.Locale;
import java.util.Set;

import net.minecraft.util.IStringSerializable;

import com.google.common.collect.Sets;

import static com.creatubbles.ctbmod.common.painting.Connections.*;

public enum ConnectionType implements IStringSerializable {

    NONE,
    UP_ONLY(UP),
    DOWN_ONLY(DOWN),
    LEFT_ONLY(LEFT),
    RIGHT_ONLY(RIGHT),
    UP_DOWN(UP, DOWN),
    LEFT_RIGHT(LEFT, RIGHT),
    TOP_LEFT(UP, LEFT),
    TOP_RIGHT(UP, RIGHT),
    BOTTOM_RIGHT(DOWN, RIGHT),
    BOTTOM_LEFT(DOWN, LEFT),
    TOP_SIDE(DOWN, LEFT, RIGHT),
    BOTTOM_SIDE(UP, LEFT, RIGHT),
    LEFT_SIDE(RIGHT, UP, DOWN),
    RIGHT_SIDE(LEFT, UP, DOWN),
    ALL(UP, DOWN, LEFT, RIGHT);

    private Set<Connections> connections;

    private ConnectionType(Connections... connections) {
        this.connections = Sets.newHashSet(connections);
    }

    public static ConnectionType forConnections(Set<Connections> iter) {
        for (ConnectionType type : values()) {
            if (iter.equals(type.connections)) {
                return type;
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return name().toLowerCase(Locale.US);
    }
}
