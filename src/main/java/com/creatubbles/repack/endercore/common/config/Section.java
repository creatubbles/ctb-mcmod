package com.creatubbles.repack.endercore.common.config;

import java.util.Locale;

/**
 * Represents a section in a config handler.
 */
public class Section {

    public final String name;
    public final String lang;

    public Section(String name, String lang) {
        this.name = name;
        this.lang = "section." + lang;
    }

    public String lc() {
        return name.toLowerCase(Locale.US);
    }
}
