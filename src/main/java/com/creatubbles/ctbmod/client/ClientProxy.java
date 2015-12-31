package com.creatubbles.ctbmod.client;

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
    	RenderingRegistry.registerBlockHandler(new CTMRenderer(CTBMod.renderIdPainting = RenderingRegistry.getNextAvailableRenderId()));
        ClientRegistry.bindTileEntitySpecialRenderer(TilePainting.class, new RenderPainting());
    }
}
