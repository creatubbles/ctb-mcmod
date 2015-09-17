package com.creatubbles.repack.enderlib.client.render;

import java.lang.reflect.Field;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Timer;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import com.creatubbles.ctbmod.CTBMod;

public class RenderUtil {

  private static Field timerField = initTimer();

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

  @Nullable
  public static Timer getTimer() {
    if (timerField == null) {
      return null;
    }
    try {
      return (Timer) timerField.get(Minecraft.getMinecraft());
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}
