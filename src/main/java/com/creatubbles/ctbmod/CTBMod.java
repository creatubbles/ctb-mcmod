package com.creatubbles.ctbmod;

import static com.creatubbles.ctbmod.CTBMod.MODID;
import static com.creatubbles.ctbmod.CTBMod.NAME;
import static com.creatubbles.ctbmod.CTBMod.VERSION;

import java.util.List;
import java.util.Locale;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.creatubbles.ctbmod.common.CommonProxy;
import com.creatubbles.ctbmod.common.command.CommandGetCreators;
import com.creatubbles.ctbmod.common.command.CommandLogin;
import com.creatubbles.ctbmod.common.config.Configs;
import com.creatubbles.ctbmod.common.creator.BlockCreator;
import com.creatubbles.ctbmod.common.network.PacketHandler;
import com.creatubbles.repack.endercore.common.config.ConfigProcessor;
import com.creatubbles.repack.enderlib.api.config.IConfigHandler;

@Mod(modid = MODID, name = NAME, version = VERSION)
public class CTBMod
{
    public static final String MODID = "CTBMod";
    public static final String NAME = "Creatubbles Mod";
    public static final String DOMAIN = MODID.toLowerCase(Locale.US);
    public static final String VERSION = "@VERSION@";

    @Instance
    public static CTBMod instance;

    @SidedProxy(clientSide = "com.creatubbles.ctbmod.client.ClientProxy", serverSide = "com.creatubbles.ctbmod.common.CommonProxy")
    public static CommonProxy proxy;
    public static Logger logger = LogManager.getLogger(MODID);

    public static BlockCreator creator;

    public List<IConfigHandler> configs;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	new ConfigProcessor(Configs.class, event.getSuggestedConfigurationFile(), MODID).process(true);
    	
        creator = BlockCreator.create();
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.registerRenderers();
        PacketHandler.init();
        Configs.loadAccessToken();
    }

    @EventHandler
    public void onServerStart(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CommandLogin());
        event.registerServerCommand(new CommandGetCreators());
    }
}
