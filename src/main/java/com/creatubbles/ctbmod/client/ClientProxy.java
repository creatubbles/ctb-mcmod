package com.creatubbles.ctbmod.client;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.Item;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import team.chisel.ctmlib.CTMRenderer;

import com.creatubbles.ctbmod.CTBMod;
import com.creatubbles.ctbmod.client.render.RenderPainting;
import com.creatubbles.ctbmod.common.CommonProxy;
import com.creatubbles.ctbmod.common.painting.TilePainting;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy {

    @Override
    public void registerRenderers() {
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
    	RenderPainting rp = new RenderPainting();
        ClientRegistry.bindTileEntitySpecialRenderer(TilePainting.class, rp);
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(CTBMod.painting), rp);
    }
    
    @Override
    public World getClientWorld() {
        return Minecraft.getMinecraft().theWorld;
    }
}
