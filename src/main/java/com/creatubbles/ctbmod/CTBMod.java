package com.creatubbles.ctbmod;

import static com.creatubbles.ctbmod.CTBMod.MODID;
import static com.creatubbles.ctbmod.CTBMod.NAME;
import static com.creatubbles.ctbmod.CTBMod.VERSION;

import java.util.Locale;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.creatubbles.ctbmod.common.CommonProxy;
import com.creatubbles.ctbmod.common.command.CommandGetCreators;
import com.creatubbles.ctbmod.common.command.CommandLogin;
import com.creatubbles.ctbmod.common.command.CommandUpload;
import com.creatubbles.ctbmod.common.config.Configs;
import com.creatubbles.ctbmod.common.config.DataCache;
import com.creatubbles.ctbmod.common.creator.BlockCreator;
import com.creatubbles.ctbmod.common.network.PacketHandler;
import com.creatubbles.ctbmod.common.painting.BlockPainting;
import com.creatubbles.repack.endercore.common.config.ConfigProcessor;

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
    public static BlockPainting painting;

    public static DataCache cache;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        new ConfigProcessor(Configs.class, event.getSuggestedConfigurationFile(), MODID).process(true);

        creator = BlockCreator.create();
        painting = BlockPainting.create();

        proxy.registerRenderers();

        GameRegistry.addRecipe(new ShapedOreRecipe(creator, "ibi", "wpw", "iwi", 'i', "ingotIron", 'b', Items.bucket, 'w', "plankWood", 'p', Blocks.piston));
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        PacketHandler.init();
        cache = DataCache.loadCache();

        if (event.getSide().isClient()) {
            registerCommands();
        }
    }

    public void registerCommands() {
        ClientCommandHandler.instance.registerCommand(new CommandLogin());
        ClientCommandHandler.instance.registerCommand(new CommandGetCreators());
        ClientCommandHandler.instance.registerCommand(new CommandUpload());
    }
}
