package com.creatubbles.ctbmod.client;

import com.creatubbles.ctbmod.client.render.RenderPainting;
import com.creatubbles.ctbmod.common.CommonProxy;
import com.creatubbles.ctbmod.common.painting.TilePainting;

import cpw.mods.fml.client.registry.ClientRegistry;

public class ClientProxy extends CommonProxy {

    @Override
    public void registerRenderers() {
//        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CTBMod.creator), 0, new ModelResourceLocation(CTBMod.DOMAIN + ":creator", "inventory"));
//        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CTBMod.painting), 0, new ModelResourceLocation(CTBMod.DOMAIN + ":painting", "inventory"));
//
//        ModelLoader.setCustomStateMapper(CTBMod.painting, new PaintingStateMapper());
        ClientRegistry.bindTileEntitySpecialRenderer(TilePainting.class, new RenderPainting());
    }
}
