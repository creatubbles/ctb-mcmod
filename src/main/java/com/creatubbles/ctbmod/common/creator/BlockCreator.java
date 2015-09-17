package com.creatubbles.ctbmod.common.creator;

import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import com.creatubbles.ctbmod.CTBMod;
import com.creatubbles.repack.enderlib.common.BlockEnder;

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
		setUnlocalizedName(CTBMod.DOMAIN + "." + name);
		NetworkRegistry.INSTANCE.registerGuiHandler(CTBMod.instance, this);
	}

	@Override
	protected boolean openGui(World world, BlockPos pos, EntityPlayer entityPlayer, EnumFacing side) {
		entityPlayer.openGui(CTBMod.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
		return true;
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerCreator(player.inventory);
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GuiCreator(player.inventory);
	}
}
