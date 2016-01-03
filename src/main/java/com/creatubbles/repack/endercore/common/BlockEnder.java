package com.creatubbles.repack.endercore.common;

import java.util.ArrayList;

import javax.annotation.Nullable;

import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.creatubbles.ctbmod.CTBMod;
import com.google.common.collect.Lists;

import cpw.mods.fml.common.registry.GameRegistry;

public abstract class BlockEnder<T extends TileEntityBase> extends Block {

    protected final Class<? extends T> teClass;
    protected final String name;

    @Setter
    private Class<? extends ItemBlock> itemBlockClass;

    protected BlockEnder(String name, Class<? extends T> teClass) {
        this(name, teClass, new Material(MapColor.ironColor));
    }

    protected BlockEnder(String name, Class<? extends T> teClass, Material mat) {
        super(mat);
        this.teClass = teClass;
        this.name = name;
        setHardness(0.5F);
        setBlockName(name);
        setStepSound(Block.soundTypeMetal);
        setHarvestLevel("pickaxe", 0);
    }

    protected void init() {
        if (itemBlockClass != null) {
            GameRegistry.registerBlock(this, itemBlockClass, name);
        } else {
            GameRegistry.registerBlock(this, name);
        }
        if (teClass != null) {
            GameRegistry.registerTileEntity(teClass, name + "TileEntity");
        }
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return teClass != null;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        if (teClass != null) {
            try {
                T te = teClass.newInstance();
                te.init();
                return te;
            } catch (Exception e) {
                CTBMod.logger.error("Could not create tile entity for block " + name + " for class " + teClass);
            }
        }
        return null;
    }

    /* Subclass Helpers */

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float par7, float par8, float par9) {
        if (entityPlayer.isSneaking()) {
            return false;
        }
        return openGui(world, x, y, z, entityPlayer, side);
    }

    protected boolean openGui(World world, int x, int y, int z, EntityPlayer entityPlayer, int side) {
        return false;
    }

    public boolean doNormalDrops(IBlockAccess world, int x, int y, int z) {
        return true;
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
        if (willHarvest) {
            return true;
        }
        return super.removedByPlayer(world, player, x, y, z, willHarvest);
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, int x, int y, int z, int meta) {
        super.harvestBlock(worldIn, player, x, y, z, meta);
        worldIn.setBlockToAir(x, y, z);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int meta, int fortune) {
        if (doNormalDrops(world, x, y, z)) {
            return super.getDrops(world, x, y, z, meta, fortune);
        }
        return Lists.newArrayList(getNBTDrop(world, x, y, z, (T) world.getTileEntity(x, y, z)));
    }

    public ItemStack getNBTDrop(IBlockAccess world, int x, int y, int z, T te) {
        int meta = damageDropped(world.getBlockMetadata(x, y, z));
        ItemStack itemStack = new ItemStack(this, 1, meta);
        processDrop(world, x, y, z, te, itemStack);
        return itemStack;
    }

    protected void processDrop(IBlockAccess world, int x, int y, int z, @Nullable T te, ItemStack drop) {}

    @SuppressWarnings("unchecked")
    protected T getTileEntity(World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (teClass.isInstance(te)) {
            return (T) te;
        }
        return null;
    }

    protected boolean shouldDoWorkThisTick(World world, int x, int y, int z, int interval) {
        T te = getTileEntity(world, x, y, z);
        if (te == null) {
            return world.getTotalWorldTime() % interval == 0;
        } else {
            return te.shouldDoWorkThisTick(interval);
        }
    }

    protected boolean shouldDoWorkThisTick(World world, int x, int y, int z, int interval, int offset) {
        T te = getTileEntity(world, x, y, z);
        if (te == null) {
            return (world.getTotalWorldTime() + offset) % interval == 0;
        } else {
            return te.shouldDoWorkThisTick(interval, offset);
        }
    }

    // Because the vanilla method takes floats...
    public void setBlockBounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public void setBlockBounds(AxisAlignedBB bb) {
        setBlockBounds(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
    }
}
