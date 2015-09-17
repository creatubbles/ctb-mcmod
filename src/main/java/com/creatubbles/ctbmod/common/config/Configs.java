package com.creatubbles.ctbmod.common.config;

import java.io.File;
import java.io.FileInputStream;
import java.util.Scanner;

import lombok.SneakyThrows;

import com.creatubbles.repack.endercore.common.config.annot.Comment;
import com.creatubbles.repack.endercore.common.config.annot.Config;

public class Configs {

	@Config
	@Comment("Setting this to true will cause the cached access token to be refreshed every time the game is launched.")
	public static boolean refreshAccessToken = false;

	public static String cachedAccessToken = null;

	@SneakyThrows
	public static void loadAccessToken() {
		File accessTokenCache = new File(".", ".access_token");
		if (accessTokenCache.createNewFile()) {
			return;
		}
		Scanner scan = new Scanner(new FileInputStream(accessTokenCache));
		if (scan.hasNextLine()) {
			String token = scan.nextLine();
			if (!token.isEmpty()) {
				cachedAccessToken = token;
			}
		}
	}
}
