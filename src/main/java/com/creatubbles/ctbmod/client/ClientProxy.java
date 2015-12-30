package com.creatubbles.ctbmod.client;


import com.creatubbles.ctbmod.client.render.RenderPainting;
import com.creatubbles.ctbmod.common.CommonProxy;
import com.creatubbles.ctbmod.common.painting.TilePainting;

import cpw.mods.fml.client.registry.ClientRegistry;

public class ClientProxy extends CommonProxy {

	@Override
	public void registerRenderers() {

//		RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
//		renderItem.getItemModelMesher().register(Item.getItemFromBlock(CTBMod.creator), 0, new ModelResourceLocation(CTBMod.DOMAIN + ":creator", "inventory"));
//		renderItem.getItemModelMesher().register(Item.getItemFromBlock(CTBMod.painting), 0, new ModelResourceLocation(CTBMod.DOMAIN + ":painting", "inventory"));
		
		ClientRegistry.bindTileEntitySpecialRenderer(TilePainting.class, new RenderPainting());
	}
}
