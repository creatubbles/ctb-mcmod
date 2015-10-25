package com.creatubbles.ctbmod.common.painting;

import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

import com.creatubbles.ctbmod.CTBMod;
import com.creatubbles.repack.endercore.common.BlockEnder;

public class BlockPainting extends BlockEnder<TilePainting> {

	public static BlockPainting create() {
		BlockPainting res = new BlockPainting();
		res.init();
		return res;
	}

	protected BlockPainting() {
		super("painting", TilePainting.class, Material.cloth);
		setHardness(0.25f);
		setStepSound(soundTypeCloth);
	}
	
	@Override
	protected void init() {
		super.init();
		setCreativeTab(CreativeTabs.tabDecorations);
		setUnlocalizedName(CTBMod.DOMAIN + "." + name);
	}

	@Override
	public int getRenderType() {
		return -1;
	}
	
	@Override
	public boolean isOpaqueCube() {
		return false;
	}
	
	@Override
	public boolean isFullBlock() {
		return false;
	}
	
	@Override
	public boolean isFullCube() {
		return false;
	}
}
