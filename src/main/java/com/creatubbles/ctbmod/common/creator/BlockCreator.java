package com.creatubbles.ctbmod.common.creator;

import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import com.creatubbles.ctbmod.CTBMod;
import com.creatubbles.ctbmod.client.gui.GuiCreator;
import com.creatubbles.repack.endercore.common.BlockEnder;

import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkRegistry;

public class BlockCreator extends BlockEnder<TileCreator> implements IGuiHandler {

	public static BlockCreator create() {
		BlockCreator res = new BlockCreator();
		res.init();
		return res;
	}

	public BlockCreator() {
		super("creator", TileCreator.class, Material.rock);
	}

	@Override
	protected void init() {
		super.init();
		setCreativeTab(CreativeTabs.tabDecorations);
		setBlockName(CTBMod.DOMAIN + "." + name);
		setBlockTextureName(CTBMod.DOMAIN + ":" + name);
		NetworkRegistry.INSTANCE.registerGuiHandler(CTBMod.instance, this);
	}

	@Override
	protected boolean openGui(World world, int x, int y, int z, EntityPlayer entityPlayer, int side) {
		entityPlayer.openGui(CTBMod.instance, 0, world, x, y, z);
		return true;
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileCreator te = getTileEntity(world, x, y, z);
		return te == null ? null : new ContainerCreator(player.inventory, te);
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileCreator te = getTileEntity(world, x, y, z);
		return te == null ? null : new GuiCreator(player.inventory, te);
	}
}
