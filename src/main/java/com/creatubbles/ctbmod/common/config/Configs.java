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
    @Comment("The blocks that the painting item can be placed on in adventure mode")
    public static String[] canPlacePaintingOn = {};
    
    @Config("Developer Settings")
    @Comment("Developers only. Tells the mod to use the creatubbles staging server for testing purposes.")
    public static boolean staging = false;

    @Config("GIF Recording")
    @Comment("Maximum width of recorded GIFs. Values larger than the default can greatly impact performance! Reduce this if recording gifs causes lag.")
    public static int maxGifWidth = 500;
    
    @Config("GIF Recording")
    @Comment("Maximum height of recorded GIFs. Values larger than the default can greatly impact performance! Reduce this if recording gifs causes lag.")
    public static int maxGifHeight = 500;
}
