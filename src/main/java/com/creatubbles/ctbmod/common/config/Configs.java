package com.creatubbles.ctbmod.common.config;

import com.creatubbles.repack.endercore.common.config.annot.Comment;
import com.creatubbles.repack.endercore.common.config.annot.Config;

public class Configs {

	@Config
	@Comment("Setting this to true will cause the cached access token to be refreshed every time the game is launched.")
	public static boolean refreshAccessToken = false;
	
}
