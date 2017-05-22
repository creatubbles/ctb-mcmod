package com.creatubbles.ctbmod.client;

import com.creatubbles.ctbmod.CTBMod;
import com.creatubbles.ctbmod.client.gui.GuiUtil;
import com.creatubbles.ctbmod.client.render.RenderPainting;
import com.creatubbles.ctbmod.client.render.RenderPaintingItem;
import com.creatubbles.ctbmod.common.CommonProxy;
import com.creatubbles.ctbmod.common.painting.PaintingHighlightHandler;
import com.creatubbles.ctbmod.common.painting.PaintingStateMapper;
import com.creatubbles.ctbmod.common.painting.TilePainting;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {
    
    private static RenderPaintingItem.BakedModel paintingDummyModel;

    @SuppressWarnings("deprecation")
    @Override
    public void registerRenderers() {
        GuiUtil.init();
        
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CTBMod.creator), 0, new ModelResourceLocation(CTBMod.DOMAIN + ":creator", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CTBMod.painting), 0, new ModelResourceLocation(CTBMod.DOMAIN + ":painting_tesr", "inventory"));

        ModelLoader.setCustomStateMapper(CTBMod.painting, new PaintingStateMapper());
        ClientRegistry.bindTileEntitySpecialRenderer(TilePainting.class, new RenderPainting());
        
        RenderPaintingItem paintingItemTesr = new RenderPaintingItem();
        ClientRegistry.bindTileEntitySpecialRenderer(RenderPaintingItem.DummyTile.class, paintingItemTesr);
        paintingDummyModel = paintingItemTesr.baked;
        
        ForgeHooksClient.registerTESRItemStack(Item.getItemFromBlock(CTBMod.painting), 0, RenderPaintingItem.DummyTile.class);
        ModelBakery.registerItemVariants(Item.getItemFromBlock(CTBMod.painting), new ModelResourceLocation(new ResourceLocation(CTBMod.DOMAIN, "painting"), "inventory"));

        MinecraftForge.EVENT_BUS.register(new PaintingHighlightHandler());
        MinecraftForge.EVENT_BUS.register(ClientTickHandler.INSTANCE);
    }
    
    @Override
    public long getTicksElapsed() {
        return ClientTickHandler.INSTANCE.getTicksElapsed();
    }
    
    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        event.getModelRegistry().putObject(new ModelResourceLocation(CTBMod.DOMAIN + ":painting_tesr", "inventory"), paintingDummyModel);
    }

    @Override
    public World getClientWorld() {
        return Minecraft.getMinecraft().theWorld;
    }
}
