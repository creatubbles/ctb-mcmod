package com.creatubbles.ctbmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import com.creatubbles.ctbmod.CTBMod;
import com.creatubbles.ctbmod.client.render.RenderPainting;
import com.creatubbles.ctbmod.common.CommonProxy;
import com.creatubbles.ctbmod.common.painting.PaintingStateMapper;
import com.creatubbles.ctbmod.common.painting.TilePainting;

public class ClientProxy extends CommonProxy {

    @Override
    public void registerRenderers() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CTBMod.creator), 0, new ModelResourceLocation(CTBMod.DOMAIN + ":creator", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CTBMod.painting), 0, new ModelResourceLocation(CTBMod.DOMAIN + ":painting", "inventory"));

        ModelLoader.setCustomStateMapper(CTBMod.painting, new PaintingStateMapper());
        ClientRegistry.bindTileEntitySpecialRenderer(TilePainting.class, new RenderPainting());
    }

    @Override
    public World getClientWorld() {
        return Minecraft.getMinecraft().theWorld;
    }
}
