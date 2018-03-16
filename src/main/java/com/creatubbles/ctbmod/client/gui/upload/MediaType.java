package com.creatubbles.ctbmod.client.gui.upload;

import java.io.FilenameFilter;
import java.util.Locale;

import org.apache.commons.io.filefilter.FileFilterUtils;

import lombok.Getter;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.IStringSerializable;

@Getter
public enum MediaType implements IStringSerializable {
    
    SCREENSHOT("screenshots", ".png", false),
    GIF("gifs", ".gif", true),
    
    ;
    
    private final String folder;
    private final FilenameFilter filter;
    private final boolean isRecordable;
    
    private MediaType(String folder, String ext, boolean recordable) {
        this.folder = folder;
        this.filter = FileFilterUtils.suffixFileFilter(ext);
        this.isRecordable = recordable;
    }

    @Override
    public String getName() {
        return I18n.format("ctb.media.type") + ": " + I18n.format("ctb.media.type." + name().toLowerCase(Locale.US));
    }

    public boolean isRecordable() {
        return isRecordable;
    }
   
}
