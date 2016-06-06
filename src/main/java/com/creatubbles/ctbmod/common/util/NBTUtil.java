package com.creatubbles.ctbmod.common.util;

import javax.annotation.Nonnull;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.creatubbles.api.CreatubblesAPI;
import com.creatubbles.ctbmod.CTBMod;
import com.google.gson.Gson;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NBTUtil {
	
	public static final int NBT_VERSION = 1;
	public static final String NBT_VERSION_TAG = CTBMod.MODID + ":nbtversion";
    
	@Nonnull
    public static NBTTagCompound getTag(@Nonnull ItemStack stack) {
        if (stack.hasTagCompound()) {
            return stack.getTagCompound();
        }
        stack.setTagCompound(new NBTTagCompound());
        return getTag(stack);
    }
    
    public static int tagVersion(@Nonnull NBTTagCompound tag) {
    	return tag.getInteger(NBT_VERSION_TAG);
    }
    
    public static int tagVersion(@Nonnull ItemStack stack) {
    	return tagVersion(getTag(stack));
    }
    
    public static boolean tagUpToDate(@Nonnull NBTTagCompound tag) {
    	return tagVersion(tag) == NBT_VERSION;
    }
    
    public static boolean tagUpToDate(@Nonnull ItemStack stack) {
    	return tagUpToDate(getTag(stack));
    }

    public static void writeJsonToNBT(Object response, NBTTagCompound tag) {
        writeJsonToNBT(response, tag, CreatubblesAPI.GSON);
    }

    public static void writeJsonToNBT(Object response, NBTTagCompound tag, Gson gson) {
        String json = gson.toJson(response);
        tag.setString("response", json);
        //writeVersion(tag);
    }

    public static <T> T readJsonFromNBT(Class<T> responseClass, NBTTagCompound tag) {
        return readJsonFromNBT(responseClass, tag, CreatubblesAPI.GSON);
    }

    public static <T> T readJsonFromNBT(Class<T> responseClass, NBTTagCompound tag, Gson gson) {
        String json = tag.getString("response");
        return gson.fromJson(json, responseClass);
    }
    
    public static void writeVersion(NBTTagCompound tag) {
        tag.setInteger(NBT_VERSION_TAG, NBT_VERSION);
    }
}
