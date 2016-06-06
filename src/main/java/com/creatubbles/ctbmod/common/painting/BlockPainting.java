package com.creatubbles.ctbmod.common.painting;

import java.util.List;
import java.util.Set;

import jersey.repackaged.com.google.common.collect.Sets;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

import org.apache.commons.lang3.BooleanUtils;

import com.creatubbles.api.core.Creation;
import com.creatubbles.ctbmod.CTBMod;
import com.creatubbles.ctbmod.common.config.Configs;
import com.creatubbles.ctbmod.common.http.CreationRelations;
import com.creatubbles.ctbmod.common.util.NBTUtil;
import com.creatubbles.repack.endercore.common.BlockEnder;
import com.creatubbles.repack.endercore.common.TileEntityBase;

public class BlockPainting extends BlockEnder<TileEntityBase> {

    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    public static final PropertyBool DUMMY = PropertyBool.create("dummy");
    public static final PropertyEnum<ConnectionType> CONNECTION = PropertyEnum.create("connection", ConnectionType.class);

    public static BlockPainting create() {
        BlockPainting res = new BlockPainting();
        res.init();
        return res;
    }

    protected BlockPainting() {
        super("painting", TilePainting.class, Material.CLOTH);
        setHardness(0.25f);
        setSoundType(SoundType.CLOTH);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(DUMMY, false).withProperty(CONNECTION, ConnectionType.NONE));
        setItemBlock(new ItemBlockPainting(this));
    }

    @Override
    protected void init() {
        super.init();
        setCreativeTab(CreativeTabs.DECORATIONS);
        setUnlocalizedName(CTBMod.DOMAIN + "." + name);
        GameRegistry.registerTileEntity(TileDummyPainting.class, "ctbmod.dummyPainting");
    }

    public static ItemStack create(CreationRelations creation, int width, int height) {
        ItemStack stack = new ItemStack(CTBMod.painting);
        stack.setTagCompound(new NBTTagCompound());
        NBTUtil.writeJsonToNBT(creation, NBTUtil.getTag(stack));
        stack.getTagCompound().setInteger("pWidth", width);
        stack.getTagCompound().setInteger("pHeight", height);
        if (Configs.canPlacePaintingOn.length > 0) {
            NBTTagList blocks = new NBTTagList();
            for (String s : Configs.canPlacePaintingOn) {
                blocks.appendTag(new NBTTagString(s));
            }
            stack.getTagCompound().setTag("CanPlaceOn", blocks);
        }
        return stack;
    }

    public static CreationRelations getCreation(ItemStack painting) {
    	return getCreation(NBTUtil.getTag(painting));
    }

    public static CreationRelations getCreation(NBTTagCompound tag) {
        if (!NBTUtil.tagUpToDate(tag)) {
            switch (NBTUtil.tagVersion(tag)) {
            case 0:
                Creation c = NBTUtil.readJsonFromNBT(Creation.class, tag);
                return CreationRelations.complete(c, tag);
            default:
                break;
            }
        }
        return NBTUtil.readJsonFromNBT(CreationRelations.class, tag);
    }

    public static int getWidth(ItemStack painting) {
        return painting.getTagCompound().getInteger("pWidth");
    }

    public static int getHeight(ItemStack painting) {
        return painting.getTagCompound().getInteger("pHeight");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List list) {
        if (CTBMod.cache.getCreationCache() != null && CTBMod.cache.getCreationCache().size() > 0) {
            for (CreationRelations c : CTBMod.cache.getCreationCache()) {
                list.add(create(c, 2, 2));
            }
        }
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        CreationRelations c = getCreation(stack);
        
        TilePainting painting = getDataPainting(worldIn, pos);
        if (painting != null) {
            painting.setCreation(c);
        }
        painting.setWidth(stack.getTagCompound().getInteger("pWidth"));
        painting.setHeight(stack.getTagCompound().getInteger("pHeight"));

        EnumFacing facing = getState(worldIn, pos).getValue(FACING);
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
        EnumFacing facing = state.getValue(FACING);
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
        ItemStack data = create(painting.getCreation(), painting.getWidth(), painting.getHeight());
        drop.setTagCompound(data.getTagCompound());
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
    @Deprecated
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        IBlockState realstate = getState(source, pos);
        if (realstate == null) {
            return super.getBoundingBox(state, source, pos);
        }
        EnumFacing facing = state.getValue(FACING);
        switch (facing) {
            case EAST:
                return new AxisAlignedBB(0, 0, 0, 1 / 16f, 1, 1);
            case NORTH:
                return new AxisAlignedBB(0, 0, 15 / 16f, 1, 1, 1);
            case SOUTH:
                return new AxisAlignedBB(0, 0, 0, 1, 1, 1 / 16f);
            case WEST:
                return new AxisAlignedBB(15 / 16f, 0, 0, 1, 1, 1);
            default:
                return new AxisAlignedBB(0, 0, 0, 1, 1, 1);
        }
    }
    
    @Override
    @Deprecated
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos) {
        return getCompleteBoundingBox(worldIn, pos);
    }

    public static AxisAlignedBB getCompleteBoundingBox(World worldIn, BlockPos pos) {
        IBlockState state = getState(worldIn, pos);
        EnumFacing facing = EnumFacing.EAST;
        if (state != null) {
            facing = state.getValue(FACING);
        }

        EnumFacing ext = facing.rotateYCCW();
        TilePainting te = getDataPainting(worldIn, pos);
        if (te != null) {
            pos = te.getPos();
        }

        BlockPainting painting = CTBMod.painting;
        AxisAlignedBB blockbb = painting.getBoundingBox(state, worldIn, pos);
        AxisAlignedBB bb = new AxisAlignedBB(pos.getX() + blockbb.minX, pos.getY() + blockbb.minY, pos.getZ() + blockbb.minZ, pos.getX() + blockbb.maxX, pos.getY() + blockbb.maxY, pos.getZ()
                + blockbb.maxZ);
        if (te == null) {
            return bb;
        }
        AxisAlignedBB corner = bb.offset(ext.getFrontOffsetX() * (te.getWidth() - 1), te.getHeight() - 1, ext.getFrontOffsetZ() * (te.getWidth() - 1));
        return bb.union(corner);
    }

    public static TilePainting getDataPainting(IBlockAccess world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TilePainting) {
            return (TilePainting) te;
        } else if (te instanceof TileDummyPainting) {
            return ((TileDummyPainting) te).getDataTile();
        }
        return null;
    }

    private static IBlockState getState(IBlockAccess world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() == CTBMod.painting) {
            if (state.getValue(DUMMY).equals(CTBMod.painting)) {
                return getState(world, getDataPainting(world, pos).getPos());
            }
            return state;
        }
        return null;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        TilePainting te = getDataPainting(world, pos);
        return create(te.getCreation(), te.getWidth(), te.getHeight());
    }

    @Override
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta)).withProperty(DUMMY, (meta & 8) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex() | BooleanUtils.toInteger(state.getValue(DUMMY)) << 3;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, DUMMY, CONNECTION);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        EnumFacing facing = state.getValue(FACING);
        EnumFacing ext = facing.rotateYCCW();
        Set<Connections> set = Sets.newHashSet();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                if ((x != 0 || y != 0) && !(x != 0 && y != 0)) {
                    BlockPos pos2 = pos.add(x * ext.getFrontOffsetX(), y, x * ext.getFrontOffsetZ());
                    Connections c = Connections.forOffset(x, y);
                    if (world.getBlockState(pos2).getBlock() == this) {
                        if (getDataPainting(world, pos) == getDataPainting(world, pos2)) {
                            set.add(c);
                        }
                    }
                }
            }
        }
        ConnectionType type = ConnectionType.forConnections(set);
        if (type == null) {
            type = ConnectionType.NONE;
        }
        return state.withProperty(CONNECTION, type);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }
}
