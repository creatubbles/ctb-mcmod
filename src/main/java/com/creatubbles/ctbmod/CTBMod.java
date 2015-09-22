package com.creatubbles.ctbmod;

import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.creatubbles.ctbmod.common.CommonProxy;
import com.creatubbles.ctbmod.common.command.CommandGetCreators;
import com.creatubbles.ctbmod.common.command.CommandLogin;
import com.creatubbles.ctbmod.common.config.Configs;
import com.creatubbles.ctbmod.common.config.DataCache;
import com.creatubbles.ctbmod.common.creator.BlockCreator;
import com.creatubbles.ctbmod.common.network.PacketHandler;
import com.creatubbles.repack.endercore.common.config.ConfigProcessor;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

import static com.creatubbles.ctbmod.CTBMod.*;

@Mod(modid = MODID, name = NAME, version = VERSION)
public class CTBMod {

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
	
	public static DataCache cache;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		new ConfigProcessor(Configs.class, event.getSuggestedConfigurationFile(), MODID).process(true);

		creator = BlockCreator.create();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.registerRenderers();
		PacketHandler.init();
		cache = DataCache.loadCache();
	}

	@EventHandler
	public void onServerStart(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandLogin());
		event.registerServerCommand(new CommandGetCreators());
	}
}
