package com.creatubbles.ctbmod.common.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Collection;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import org.apache.commons.io.FileUtils;

import com.creatubbles.api.core.Creation;
import com.creatubbles.api.core.User;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class DataCache {

    public static final File cacheFolderv1 = new File(".", "creatubbles");
    public static final File cacheFolder = new File(".", "creatubblesv2");
    private static final File cache = new File(cacheFolder, "usercache.json");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final Set<User> savedUsers = Sets.newHashSet();

    @Getter
    private User activeUser;
    
    /**
     * This is not written to file, it is to save creations between openings of the GUI.
     */
    @Getter
    @Setter
    private transient Creation[] creationCache;

    @Getter
    private transient boolean dirty;

    @SneakyThrows
    public static DataCache loadCache() {
        if (cacheFolderv1.exists()) {
            FileUtils.deleteDirectory(cacheFolderv1);
        }
        
        cacheFolder.mkdir();

        if (cache.exists() && Configs.refreshUserCache) {
            cache.delete();
        }
        cache.createNewFile();
        JsonElement parsed = new JsonParser().parse(new FileReader(cache));
        if (parsed != null && !parsed.isJsonNull()) {
            return gson.fromJson(parsed, DataCache.class);
        }
        return new DataCache();
    }

    public void activateUser(User user) {
        if (user != null) {
            savedUsers.add(user);
        }
        activeUser = user;
        save();
    }

    public Collection<User> getSavedUsers() {
        return ImmutableSet.copyOf(savedUsers);
    }

    @SneakyThrows
    public void save() {
        String json = gson.toJson(this);
        FileWriter fw = new FileWriter(cache);
        fw.write(json);
        fw.flush();
        fw.close();
    }

    public void dirty(boolean dirty) {
        this.dirty = dirty;
    }
}
