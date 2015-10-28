package com.creatubbles.ctbmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import com.creatubbles.ctbmod.CTBMod;
import com.creatubbles.ctbmod.client.render.RenderPainting;
import com.creatubbles.ctbmod.common.CommonProxy;
import com.creatubbles.ctbmod.common.painting.TilePainting;

public class ClientProxy extends CommonProxy {

	@Override
	public void registerRenderers() {
		RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
		renderItem.getItemModelMesher().register(Item.getItemFromBlock(CTBMod.creator), 0, new ModelResourceLocation(CTBMod.DOMAIN + ":creator", "inventory"));
		renderItem.getItemModelMesher().register(Item.getItemFromBlock(CTBMod.painting), 0, new ModelResourceLocation(CTBMod.DOMAIN + ":painting", "inventory"));
		
		ClientRegistry.bindTileEntitySpecialRenderer(TilePainting.class, new RenderPainting());
	}
}
