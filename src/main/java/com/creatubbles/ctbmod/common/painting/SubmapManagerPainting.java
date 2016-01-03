package com.creatubbles.ctbmod.common.painting;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import team.chisel.ctmlib.CTM;
import team.chisel.ctmlib.RenderBlocksCTM;
import team.chisel.ctmlib.SubmapManagerCTM;

import com.google.common.base.Optional;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class SubmapManagerPainting extends SubmapManagerCTM {

    @SideOnly(Side.CLIENT)
    private static class RenderBlocksPainting extends RenderBlocksCTM {

        @Override
        public void renderFaceXNeg(Block block, double x, double y, double z, IIcon icon) {
            overrideIf(blockAccess, x, y, z, icon, ForgeDirection.WEST);
            super.renderFaceXNeg(block, x, y, z, icon);
            clearOverride();
        }

        @Override
        public void renderFaceXPos(Block block, double x, double y, double z, IIcon icon) {
            overrideIf(blockAccess, x, y, z, icon, ForgeDirection.EAST);
            super.renderFaceXPos(block, x, y, z, icon);
            clearOverride();
        }

        @Override
        public void renderFaceZNeg(Block block, double x, double y, double z, IIcon icon) {
            overrideIf(blockAccess, x, y, z, icon, ForgeDirection.NORTH);
            super.renderFaceZNeg(block, x, y, z, icon);
            clearOverride();
        }

        @Override
        public void renderFaceZPos(Block block, double x, double y, double z, IIcon icon) {
            overrideIf(blockAccess, x, y, z, icon, ForgeDirection.SOUTH);
            super.renderFaceZPos(block, x, y, z, icon);
            clearOverride();
        }

        @Override
        public void renderFaceYNeg(Block block, double x, double y, double z, IIcon icon) {
            override(icon);
            super.renderFaceYNeg(block, x, y, z, icon);
            clearOverride();
        }

        @Override
        public void renderFaceYPos(Block block, double x, double y, double z, IIcon icon) {
            override(icon);
            super.renderFaceYPos(block, x, y, z, icon);
            clearOverride();
        }

        private boolean overriden = false;

        private void overrideIf(IBlockAccess world, double x, double y, double z, IIcon icon, ForgeDirection direction) {
            ForgeDirection facing = BlockPainting.getFacing(world, MathHelper.floor_double(x), MathHelper.floor_double(y), MathHelper.floor_double(z));
            if (facing != direction && facing.getOpposite() != direction) {
                override(icon);
            }
        }

        private void override(IIcon icon) {
            if (!hasOverrideBlockTexture()) {
                setOverrideBlockTexture(icon);
                overriden = true;
            }
        }

        private void clearOverride() {
            if (overriden) {
                clearOverrideBlockTexture();
                overriden = false;
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private static RenderBlocksPainting rb;

    public SubmapManagerPainting(String textureName) {
        super(textureName);
    }

    @Override
    public void registerIcons(String modName, Block block, IIconRegister register) {
        super.registerIcons(modName, block, register);
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return side == 2 || side == 3 ? super.getIcon(side, meta) : getSubmapSmall().getBaseIcon();
    }

    @Override
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
        ForgeDirection facing = BlockPainting.getFacing(world, x, y, z);
        if (side == facing.ordinal() || side == facing.getOpposite().ordinal()) {
            return super.getIcon(world, x, y, z, side);
        }
        return getSubmapSmall().getBaseIcon();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public RenderBlocks createRenderContext(RenderBlocks rendererOld, Block block, IBlockAccess world) {
        if (rb == null) {
            rb = new RenderBlocksPainting();
            rb.ctm = new CTM() {

                @Override
                public int getBlockOrFacadeMetadata(IBlockAccess world, int x, int y, int z, int side) {
                    return super.getBlockOrFacadeMetadata(world, x, y, z, side) & 3;
                }

                @Override
                public boolean isConnected(IBlockAccess world, int x, int y, int z, ForgeDirection dir, Block block, int meta) {
                    return super.isConnected(world, x, y, z, dir, block, meta & 3);
                }
            };
            rb.ctm.disableObscuredFaceCheck = Optional.of(true);
        }
        rb.setRenderBoundsFromBlock(block);
        rb.submap = getSubmap();
        rb.submapSmall = getSubmapSmall();
        return rb;
    }
}
