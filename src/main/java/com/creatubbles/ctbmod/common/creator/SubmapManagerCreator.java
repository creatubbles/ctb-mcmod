package com.creatubbles.ctbmod.common.creator;

import java.util.Locale;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import team.chisel.ctmlib.ISubmapManager;
import team.chisel.ctmlib.RenderBlocksCTM;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class SubmapManagerCreator implements ISubmapManager {

    @SideOnly(Side.CLIENT)
    private IIcon[] icons;

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(String modName, Block block, IIconRegister register) {
        icons = new IIcon[6];
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            icons[dir.ordinal()] = register.registerIcon(modName + ":creator_" + dir.name().toLowerCase(Locale.US));
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void preRenderSide(RenderBlocks renderer, IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        if (side == ForgeDirection.UP) {
            int meta = world.getBlockMetadata(x, y, z);
            renderer.uvRotateTop = meta == 0 ? 2 : (meta + 1) % 4;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void postRenderSide(RenderBlocks renderer, IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        renderer.uvRotateTop = 0;
    }

    @Override
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
        return getIcon(side, world.getBlockMetadata(x, y, z));
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        if (side > 1) {
            switch (meta) {
                case 3:
                    side ^= 1;
                    break;
                case 4:
                    side = side > 3 ? side ^ 6 : (~side) & 7;
                    break;
                case 0: // Item form
                case 5:
                    side = side < 4 ? side ^ 6 : (~side) & 7;
                    break;
            }
        }
        return icons[side % icons.length];
    }

    @SideOnly(Side.CLIENT)
    @Override
    public RenderBlocks createRenderContext(RenderBlocks rendererOld, Block block, IBlockAccess world) {
        RenderBlocksCTM rb = new RenderBlocksCTM();
        rb.blockAccess = world;
        return rb;
    }
}
