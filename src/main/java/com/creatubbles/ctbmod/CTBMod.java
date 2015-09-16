package com.creatubbles.ctbmod;

import java.util.Locale;

import cpw.mods.fml.common.Mod;
import static com.creatubbles.ctbmod.CTBMod.*;

@Mod(modid = MODID, name = NAME, version = VERSION, dependencies = "required-after:forge@[10.13.4.1448,)")
public class CTBMod
{
    public static final String MODID = "CTBMod";
    public static final String NAME = "Creatubbles Mod";
    public static final String DOMAIN = MODID.toLowerCase(Locale.US);
    public static final String VERSION = "@VERSION@";
}
