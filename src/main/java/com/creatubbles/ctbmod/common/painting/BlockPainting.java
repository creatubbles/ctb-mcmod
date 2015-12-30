package com.creatubbles.ctbmod.common.painting;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.creatubbles.api.core.Creation;
import com.creatubbles.ctbmod.CTBMod;
import com.creatubbles.ctbmod.common.util.NBTUtil;
import com.creatubbles.repack.endercore.common.BlockEnder;
import com.creatubbles.repack.endercore.common.TileEntityBase;
import com.creatubbles.repack.endercore.common.util.BlockCoord;

import cpw.mods.fml.common.registry.GameRegistry;

public class BlockPainting extends BlockEnder<TileEntityBase> {
    
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
        setBlockName(CTBMod.DOMAIN + "." + name);
        GameRegistry.registerTileEntity(TileDummyPainting.class, "ctbmod.dummyPainting");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List list) {
        if (CTBMod.cache.getCreationCache() != null && CTBMod.cache.getCreationCache().length > 0) {
            ItemStack stack = new ItemStack(itemIn);
            stack.setTagCompound(new NBTTagCompound());
            NBTUtil.writeJsonToNBT(CTBMod.cache.getCreationCache()[3], stack.getTagCompound());
            list.add(stack);
        }
    }

    public ForgeDirection getFacing(IBlockAccess world, int x, int y, int z) {
    	return getFacing(world.getBlockMetadata(x, y, z));
    }
    
    public ForgeDirection getFacing(int meta) {
    	return ForgeDirection.getOrientation(meta & 3 + 2);
    }
    
    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        ItemStack placed = placer.getHeldItem();
        Creation c = NBTUtil.readJsonFromNBT(Creation.class, placed.getTagCompound());
        BlockCoord pos = new BlockCoord(x, y, z);

        TilePainting painting = getDataPainting(world, pos);
        if (painting != null) {
            painting.setCreation(c);
        }
        
        ForgeDirection facing = getFacing(world, x, y, z);
        ForgeDirection ext = facing.getRotation(ForgeDirection.DOWN);
        for (int x2 = 0; x2 < painting.getWidth(); x2++) {
            for (int y2 = 0; y2 < painting.getHeight(); y2++) {
            	BlockCoord pos2 = pos.add(x2 * ext.offsetX, y2, x2 * ext.offsetZ);
                if (pos2.isAirBlock(world)) {
                    world.setBlockMetadataWithNotify(x, y, z, pos2.getMetadata(world) | 4, 3);
                    ((TileDummyPainting) pos2.getTileEntity(world)).setMain(pos);
                }
            }
        }
    }

    @Override
    public void breakBlock(World worldIn, int x, int y, int z, Block block, int meta) {
        ForgeDirection facing = getFacing(meta);
        ForgeDirection ext = facing.getRotation(ForgeDirection.DOWN);
        BlockCoord pos = new BlockCoord(x, y, z);
        TilePainting painting = getDataPainting(worldIn, pos);
        pos = new BlockCoord(painting);
        for (int x2 = 0; x2 < painting.getWidth(); x2++) {
            for (int y2 = 0; y2 < painting.getHeight(); y2++) {
                pos.add(x2 * ext.offsetX, y2, x2 * ext.offsetZ).setBlockToAir(worldIn);
            }
        }
        super.breakBlock(worldIn, x, y, z, block, meta);
    }

    @Override
    public boolean doNormalDrops(IBlockAccess world, int x, int y, int z) {
        return false;
    }

    @Override
    protected void processDrop(IBlockAccess world, int x, int y, int z, TileEntityBase te, ItemStack drop) {
        TilePainting painting = getDataPainting(world, new BlockCoord(x, y, z));
        drop.setTagCompound(new NBTTagCompound());
        NBTUtil.writeJsonToNBT(painting.getCreation(), drop.getTagCompound());
    }

    @Override
    public TileEntity createTileEntity(World world, int meta) {
        return meta > 3 ? new TileDummyPainting() : new TilePainting();
    }

    @Override
    public boolean canPlaceBlockOnSide(World worldIn, int x, int y, int z, int side) {
        return side > 1 && super.canPlaceBlockOnSide(worldIn, x, y, z, side);
    }
    
    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, int x, int y, int z) {
        ForgeDirection facing = getFacing(worldIn, x, y, z);
        switch (facing) {
        case EAST:
            setBlockBounds(0, 0, 0, 1/16f, 1, 1);
            break;
        case NORTH:
            setBlockBounds(0, 0, 15/16f, 1, 1, 1);
            break;
        case SOUTH:
            setBlockBounds(0, 0, 0, 1, 1, 1/16f);
            break;
        case WEST:
            setBlockBounds(15/16f, 0, 0, 1, 1, 1);
            break;
        default:
            setBlockBounds(0, 0, 0, 1, 1, 1);
            break;
        }
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
    	BlockCoord pos = new BlockCoord(x, y, z);
        ForgeDirection facing = getFacing(world, pos.x, pos.y, pos.z);
        ForgeDirection ext = facing.getRotation(ForgeDirection.DOWN);
        TilePainting te = getDataPainting(world, pos);
        pos = new BlockCoord(te);
        AxisAlignedBB bb = super.getSelectedBoundingBoxFromPool(world, pos.x, pos.y, pos.z);
        AxisAlignedBB corner = bb.offset(ext.offsetX * (te.getWidth() - 1), te.getHeight() - 1, ext.offsetZ * (te.getWidth() - 1));
        return bb.func_111270_a(corner); // union
    }
    
    private TilePainting getDataPainting(IBlockAccess world, BlockCoord pos) {
        TileEntity te = pos.getTileEntity(world);
        if (te instanceof TilePainting) {
            return (TilePainting) te;
        } else if (te instanceof TileDummyPainting) {
            return ((TileDummyPainting) te).getDataTile();
        }
        return null;
    }

    @Override
    public int onBlockPlaced(World worldIn, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int meta) {
        return side - 2;
    }

    @Override
    public int getRenderType() {
        return -1;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }
}
