package com.creatubbles.ctbmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;

import com.creatubbles.ctbmod.CTBMod;
import com.creatubbles.ctbmod.common.CommonProxy;

public class ClientProxy extends CommonProxy {

	@Override
	public void registerRenderers() {
		RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
		renderItem.getItemModelMesher().register(Item.getItemFromBlock(CTBMod.creator), 0, new ModelResourceLocation(CTBMod.DOMAIN + ":creator", "inventory"));
	}
}
