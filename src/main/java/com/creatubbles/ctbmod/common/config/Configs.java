package com.creatubbles.ctbmod.common.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import lombok.SneakyThrows;

import com.creatubbles.ctbmod.common.http.User;
import com.creatubbles.repack.endercore.common.config.annot.Comment;
import com.creatubbles.repack.endercore.common.config.annot.Config;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class Configs {

	@Config
	@Comment("Setting this to true will cause the cached access token to be refreshed every time the game is launched.")
	public static boolean refreshAccessToken = false;

	public static User cachedUser = null;
	
	public static final File cacheFolder = new File(".", "creatubbles");
	
	private static File userCache;

	@SneakyThrows
	public static void loadUser() {
		cacheFolder.mkdir();
		userCache = new File(cacheFolder, "usercache.json");
		
		if (userCache.exists() && refreshAccessToken) {
			userCache.delete();
			return;
		} else if (userCache.createNewFile()) {
			return;
		}
		JsonElement parsed = new JsonParser().parse(new FileReader(userCache));
		if (parsed != null && !parsed.isJsonNull()) {
			cachedUser = new Gson().fromJson(parsed, User.class);
		}
	}

	@SneakyThrows
	public static void cacheUser() {
		String json = new Gson().toJson(cachedUser);
		FileWriter fw = new FileWriter(userCache);
		fw.write(json);
		fw.flush();
		fw.close();
	}
}
