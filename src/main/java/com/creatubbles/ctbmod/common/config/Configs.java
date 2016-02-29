package com.creatubbles.ctbmod.common.config;

import com.creatubbles.repack.endercore.common.config.annot.Comment;
import com.creatubbles.repack.endercore.common.config.annot.Config;

public class Configs {

    @Config
    @Comment("Setting this to true will cause the cached user and creator info to be refreshed every time the game is launched.")
    public static boolean refreshUserCache = false;

    @Config
    @Comment("Setting this to true will cause paintings to require dye to make.")
    public static boolean harderPaintings = false;

    @Config
    @Comment("If true, ice will not melt in the presence of light sources. Used for the creatubbles competition map.")
    public static boolean noIceMelt = false;

}
