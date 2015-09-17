package com.creatubbles.ctbmod;

import static com.creatubbles.ctbmod.CTBMod.MODID;
import static com.creatubbles.ctbmod.CTBMod.NAME;
import static com.creatubbles.ctbmod.CTBMod.VERSION;

import java.util.Locale;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import com.creatubbles.ctbmod.common.command.CommandGetCreators;
import com.creatubbles.ctbmod.common.command.CommandLogin;
import com.creatubbles.ctbmod.common.network.PacketHandler;

@Mod(modid = MODID, name = NAME, version = VERSION)
public class CTBMod
{
    public static final String MODID = "CTBMod";
    public static final String NAME = "Creatubbles Mod";
    public static final String DOMAIN = MODID.toLowerCase(Locale.US);
    public static final String VERSION = "@VERSION@";

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        PacketHandler.init();
    }
    
    @EventHandler
    public void onServerStart(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CommandLogin());
        event.registerServerCommand(new CommandGetCreators());
    }
}
