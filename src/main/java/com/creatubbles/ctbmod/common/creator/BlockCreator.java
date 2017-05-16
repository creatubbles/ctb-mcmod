package com.creatubbles.ctbmod.common.creator;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import com.creatubbles.ctbmod.CTBMod;
import com.creatubbles.ctbmod.client.gui.creator.GuiCreator;
import com.creatubbles.repack.endercore.common.BlockEnder;

public class BlockCreator extends BlockEnder<TileCreator> implements IGuiHandler {
    
    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    public static BlockCreator create() {
        BlockCreator res = new BlockCreator();
        res.init();
        return res;
    }

    public BlockCreator() {
        super("creator", TileCreator.class, Material.ROCK);
        setDefaultState(getDefaultState().withProperty(FACING, EnumFacing.SOUTH));
    }

    @Override
    protected void init() {
        super.init();
        setCreativeTab(CreativeTabs.DECORATIONS);
        setUnlocalizedName(CTBMod.DOMAIN + "." + name);
        setHardness(5);
        setResistance(10);
        setHarvestLevel("pickaxe", 1);
        NetworkRegistry.INSTANCE.registerGuiHandler(CTBMod.instance, this);
    }
    
    @Override
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tileentity = worldIn.getTileEntity(pos);

        if (tileentity instanceof IInventory) {
            InventoryHelper.dropInventoryItems(worldIn, pos, (IInventory) tileentity);
            worldIn.updateComparatorOutputLevel(pos, this);
        }

        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    protected boolean openGui(World world, BlockPos pos, EntityPlayer entityPlayer, EnumFacing side) {
        if (!world.isRemote) {
            entityPlayer.openGui(CTBMod.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileCreator te = getTileEntity(world, new BlockPos(x, y, z));
        return te == null ? null : new ContainerCreator(player.inventory, te);
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileCreator te = getTileEntity(world, new BlockPos(x, y, z));
        return te == null ? null : new GuiCreator(player.inventory, te);
    }
}
