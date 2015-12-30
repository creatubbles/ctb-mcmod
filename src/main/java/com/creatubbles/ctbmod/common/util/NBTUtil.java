package com.creatubbles.ctbmod.common.util;

import lombok.experimental.UtilityClass;
import net.minecraft.nbt.NBTTagCompound;

import com.creatubbles.api.CreatubblesAPI;
import com.google.gson.Gson;

@UtilityClass
public class NBTUtil {

    public static void writeJsonToNBT(Object response, NBTTagCompound tag) {
        writeJsonToNBT(response, tag, CreatubblesAPI.GSON);
    }

    public static void writeJsonToNBT(Object response, NBTTagCompound tag, Gson gson) {
        String json = gson.toJson(response);
        tag.setString("response", json);
    }

    public static <T> T readJsonFromNBT(Class<T> responseClass, NBTTagCompound tag) {
        return readJsonFromNBT(responseClass, tag, CreatubblesAPI.GSON);
    }

    public static <T> T readJsonFromNBT(Class<T> responseClass, NBTTagCompound tag, Gson gson) {
        String json = tag.getString("response");
        return gson.fromJson(json, responseClass);
    }
}
