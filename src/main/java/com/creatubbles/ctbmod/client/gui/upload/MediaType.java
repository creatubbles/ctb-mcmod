package com.creatubbles.ctbmod.client.gui.upload;

import java.io.FilenameFilter;
import java.util.Locale;

import org.apache.commons.io.filefilter.FileFilterUtils;

import lombok.Getter;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.IStringSerializable;

@Getter
public enum MediaType implements IStringSerializable {
    
    SCREENSHOT("screenshots", ".png"),
    GIF("gifs", ".gif"),
    
    ;
    
    private final String folder;
    private final FilenameFilter filter;
    
    private MediaType(String folder, String ext) {
        this.folder = folder;
        this.filter = FileFilterUtils.suffixFileFilter(ext);
    }

    @Override
    public String getName() {
        return I18n.format("ctb.media." + name().toLowerCase(Locale.US) + ".name");
    }
   
}
