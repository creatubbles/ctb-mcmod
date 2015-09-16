package com.creatubbles.ctbmod;

import java.util.Locale;

import com.creatubbles.ctbmod.common.command.CommandGetCreators;
import com.creatubbles.ctbmod.common.command.CommandLogin;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import static com.creatubbles.ctbmod.CTBMod.*;

@Mod(modid = MODID, name = NAME, version = VERSION)
public class CTBMod
{
    public static final String MODID = "CTBMod";
    public static final String NAME = "Creatubbles Mod";
    public static final String DOMAIN = MODID.toLowerCase(Locale.US);
    public static final String VERSION = "@VERSION@";

    @EventHandler
    public void onServerStart(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CommandLogin());
        event.registerServerCommand(new CommandGetCreators());
    }
}
