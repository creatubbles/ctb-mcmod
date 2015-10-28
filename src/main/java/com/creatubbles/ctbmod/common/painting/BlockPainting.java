package com.creatubbles.ctbmod.common.painting;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

import org.apache.commons.lang3.BooleanUtils;

import com.creatubbles.api.core.Creation;
import com.creatubbles.ctbmod.CTBMod;
import com.creatubbles.ctbmod.common.util.NBTUtil;
import com.creatubbles.repack.endercore.common.BlockEnder;
import com.creatubbles.repack.endercore.common.TileEntityBase;

public class BlockPainting extends BlockEnder<TileEntityBase> {

    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    public static final PropertyBool DUMMY = PropertyBool.create("dummy");
    
    public static BlockPainting create() {
        BlockPainting res = new BlockPainting();
        res.init();
        return res;
    }

    protected BlockPainting() {
        super("painting", TilePainting.class, Material.cloth);
        setHardness(0.25f);
        setStepSound(soundTypeCloth);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(DUMMY, false));
    }

    @Override
    protected void init() {
        super.init();
        setCreativeTab(CreativeTabs.tabDecorations);
        setUnlocalizedName(CTBMod.DOMAIN + "." + name);
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

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        ItemStack placed = placer.getHeldItem();
        Creation c = NBTUtil.readJsonFromNBT(Creation.class, placed.getTagCompound());
        TilePainting painting = getDataPainting(worldIn, pos);
        if (painting != null) {
            painting.setCreation(c);
        }
        
        EnumFacing facing = ((EnumFacing) getState(worldIn, pos).getValue(FACING));
        EnumFacing ext = facing.rotateYCCW();
        for (int x = 0; x < painting.getWidth(); x++) {
            for (int y = 0; y < painting.getHeight(); y++) {
                BlockPos pos2 = pos.add(x * ext.getFrontOffsetX(), y, x * ext.getFrontOffsetZ());
                if (worldIn.isAirBlock(pos2)) {
                    worldIn.setBlockState(pos2, state.withProperty(DUMMY, true));
                    ((TileDummyPainting) worldIn.getTileEntity(pos2)).setMain(pos);
                }
            }
        }
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        EnumFacing facing = ((EnumFacing) state.getValue(FACING));
        EnumFacing ext = facing.rotateYCCW();
        TilePainting painting = getDataPainting(worldIn, pos);
        pos = painting.getPos();
        for (int x = 0; x < painting.getWidth(); x++) {
            for (int y = 0; y < painting.getHeight(); y++) {
                worldIn.setBlockToAir(pos.add(x * ext.getFrontOffsetX(), y, x * ext.getFrontOffsetZ()));
            }
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public boolean doNormalDrops(IBlockAccess world, BlockPos pos) {
        return false;
    }

    @Override
    protected void processDrop(IBlockAccess world, BlockPos pos, TileEntityBase te, ItemStack drop) {
        TilePainting painting = getDataPainting(world, pos);
        drop.setTagCompound(new NBTTagCompound());
        NBTUtil.writeJsonToNBT(painting.getCreation(), drop.getTagCompound());
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return state.getValue(DUMMY).equals(true) ? new TileDummyPainting() : new TilePainting();
    }

    @Override
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side) {
        return side.getFrontOffsetY() == 0 && super.canPlaceBlockOnSide(worldIn, pos, side);
    }
    
    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
        EnumFacing facing = (EnumFacing) getState(worldIn, pos).getValue(FACING);
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
    public AxisAlignedBB getSelectedBoundingBox(World worldIn, BlockPos pos) {
        EnumFacing facing = ((EnumFacing) getState(worldIn, pos).getValue(FACING));
        EnumFacing ext = facing.rotateYCCW();
        TilePainting te = getDataPainting(worldIn, pos);
        pos = te.getPos();
        AxisAlignedBB bb = super.getSelectedBoundingBox(worldIn, pos);
        AxisAlignedBB corner = bb.offset(ext.getFrontOffsetX() * (te.getWidth() - 1), te.getHeight() - 1, ext.getFrontOffsetZ() * (te.getWidth() - 1));
        return bb.union(corner);
    }
    
    private TilePainting getDataPainting(IBlockAccess world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TilePainting) {
            return (TilePainting) te;
        } else if (te instanceof TileDummyPainting) {
            return ((TileDummyPainting) te).getDataTile();
        }
        return null;
    }

    private IBlockState getState(IBlockAccess world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() == this) {
            if (state.getValue(DUMMY).equals(this)) {
                return getState(world, getDataPainting(world, pos).getPos());
            }
            return state;
        }
        return null;
    }

    @Override
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return getDefaultState().withProperty(FACING, facing);
    }

    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta)).withProperty(DUMMY, (meta & 8) == 1);
    }

    public int getMetaFromState(IBlockState state) {
        return ((EnumFacing) state.getValue(FACING)).getHorizontalIndex() | (BooleanUtils.toInteger((Boolean) state.getValue(DUMMY)) << 3);
    }

    protected BlockState createBlockState() {
        return new BlockState(this, FACING, DUMMY);
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
