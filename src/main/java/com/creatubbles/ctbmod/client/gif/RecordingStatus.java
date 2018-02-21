package com.creatubbles.ctbmod.client.gif;

import java.util.Locale;

import net.minecraft.client.resources.I18n;

public enum RecordingStatus {
    
    OFF,
    PREPARING,
    LIVE,
    SAVING,
    ;

    public String getLocalizedName(Object... args) {
        return I18n.format("ctb.recording.status." + name().toLowerCase(Locale.ROOT), args);
    }
}
