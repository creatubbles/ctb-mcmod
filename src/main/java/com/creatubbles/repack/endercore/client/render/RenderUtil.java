package com.creatubbles.repack.endercore.client.render;

import java.lang.reflect.Field;

import javax.annotation.Nonnull;

import com.creatubbles.ctbmod.CTBMod;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Timer;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class RenderUtil {

    private static final Field timerField = initTimer();
    private static final @Nonnull Timer dummy = new Timer(20);

    private static Field initTimer() {
        Field f = null;
        try {
            f = ReflectionHelper.findField(Minecraft.class, "field_71428_T", "timer", "Q");
            f.setAccessible(true);
        } catch (Exception e) {
            CTBMod.logger.error("Failed to initialize timer reflection for IO config.");
            e.printStackTrace();
        }
        return f;
    }

    @SuppressWarnings("null")
    @Nonnull
    public static Timer getTimer() {
        if (timerField == null) {
            return dummy;
        }
        try {
            return (Timer) timerField.get(Minecraft.getMinecraft());
        } catch (Exception e) {
            e.printStackTrace();
            return dummy;
        }
    }
}
