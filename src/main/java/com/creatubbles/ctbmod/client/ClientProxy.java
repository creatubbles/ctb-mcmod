package com.creatubbles.ctbmod.client;

import com.creatubbles.ctbmod.CTBMod;
import com.creatubbles.ctbmod.client.gui.GuiUtil;
import com.creatubbles.ctbmod.client.render.RenderPainting;
import com.creatubbles.ctbmod.common.CommonProxy;
import com.creatubbles.ctbmod.common.painting.PaintingHighlightHandler;
import com.creatubbles.ctbmod.common.painting.TilePainting;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.Item;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import team.chisel.ctmlib.CTMRenderer;

public class ClientProxy extends CommonProxy {

    @Override
    public void registerRenderers() {
        GuiUtil.init();

        RenderingRegistry.registerBlockHandler(new CTMRenderer(CTBMod.renderIdPainting = RenderingRegistry.getNextAvailableRenderId()) {

            @Override
            public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks rendererOld) {
                int ao = Minecraft.getMinecraft().gameSettings.ambientOcclusion;
                Minecraft.getMinecraft().gameSettings.ambientOcclusion = 0;
                boolean ret = super.renderWorldBlock(world, x, y, z, block, modelId, rendererOld);
                Minecraft.getMinecraft().gameSettings.ambientOcclusion = ao;
                return ret;
            }
        });
        RenderingRegistry.registerBlockHandler(new CTMRenderer(CTBMod.renderIdCreator = RenderingRegistry.getNextAvailableRenderId()));
        
        RenderPainting rp = new RenderPainting();
        ClientRegistry.bindTileEntitySpecialRenderer(TilePainting.class, rp);
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(CTBMod.painting), rp);

        MinecraftForge.EVENT_BUS.register(new PaintingHighlightHandler());
        FMLCommonHandler.instance().bus().register(ClientTickHandler.INSTANCE);
    }
    
    @Override
    public long getTicksElapsed() {
        return ClientTickHandler.INSTANCE.getTicksElapsed();
    }

    @Override
    public World getClientWorld() {
        return Minecraft.getMinecraft().theWorld;
    }
}
