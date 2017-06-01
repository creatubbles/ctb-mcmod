package com.creatubbles.ctbmod.common.creator;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import team.chisel.ctmlib.ICTMBlock;

import com.creatubbles.ctbmod.CTBMod;
import com.creatubbles.ctbmod.client.gui.creator.GuiCreator;
import com.creatubbles.repack.endercore.common.BlockEnder;

import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkRegistry;

public class BlockCreator extends BlockEnder<TileCreator> implements IGuiHandler, ICTMBlock<SubmapManagerCreator> {

    public static BlockCreator create() {
        BlockCreator res = new BlockCreator();
        res.init();
        return res;
    }
    
    private SubmapManagerCreator manager;

    public BlockCreator() {
        super("creator", TileCreator.class, Material.rock);
    }

    @Override
    protected void init() {
        super.init();
        setCreativeTab(CreativeTabs.tabDecorations);
        setBlockName(CTBMod.DOMAIN + "." + name);
        setBlockTextureName(CTBMod.DOMAIN + ":" + name);
        setHardness(5);
        setResistance(10);
        setHarvestLevel("pickaxe", 1);
        NetworkRegistry.INSTANCE.registerGuiHandler(CTBMod.instance, this);
    }
    
    @Override
    public void registerBlockIcons(IIconRegister register) {
        manager = new SubmapManagerCreator();
        manager.registerIcons(CTBMod.DOMAIN, this, register);
    }
    
    @Override
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
        return manager.getIcon(world, x, y, z, side);
    }
    
    @Override
    public IIcon getIcon(int side, int meta) {
        return manager.getIcon(side, meta);
    }

    @Override
    public int getRenderType() {
        return CTBMod.renderIdCreator;
    }
    
    public void breakBlock(World p_149749_1_, int p_149749_2_, int p_149749_3_, int p_149749_4_, Block p_149749_5_, int p_149749_6_)
    {
        TileCreator creator = (TileCreator) p_149749_1_.getTileEntity(p_149749_2_, p_149749_3_, p_149749_4_);

        if (creator != null)
        {
            for (int i1 = 0; i1 < creator.getSizeInventory(); ++i1)
            {
                ItemStack itemstack = creator.getStackInSlot(i1);

                if (itemstack != null)
                {
                    float f = p_149749_1_.rand.nextFloat() * 0.8F + 0.1F;
                    float f1 = p_149749_1_.rand.nextFloat() * 0.8F + 0.1F;
                    EntityItem entityitem;

                    for (float f2 = p_149749_1_.rand.nextFloat() * 0.8F + 0.1F; itemstack.stackSize > 0; p_149749_1_.spawnEntityInWorld(entityitem))
                    {
                        int j1 = p_149749_1_.rand.nextInt(21) + 10;

                        if (j1 > itemstack.stackSize)
                        {
                            j1 = itemstack.stackSize;
                        }

                        itemstack.stackSize -= j1;
                        entityitem = new EntityItem(p_149749_1_, (double)((float)p_149749_2_ + f), (double)((float)p_149749_3_ + f1), (double)((float)p_149749_4_ + f2), new ItemStack(itemstack.getItem(), j1, itemstack.getItemDamage()));
                        float f3 = 0.05F;
                        entityitem.motionX = p_149749_1_.rand.nextGaussian() * f3;
                        entityitem.motionY = p_149749_1_.rand.nextGaussian() * f3 + 0.2F;
                        entityitem.motionZ = p_149749_1_.rand.nextGaussian() * f3;

                        if (itemstack.hasTagCompound())
                        {
                            entityitem.getEntityItem().setTagCompound((NBTTagCompound)itemstack.getTagCompound().copy());
                        }
                    }
                }
            }

            p_149749_1_.func_147453_f(p_149749_2_, p_149749_3_, p_149749_4_, p_149749_5_);
        }

        super.breakBlock(p_149749_1_, p_149749_2_, p_149749_3_, p_149749_4_, p_149749_5_, p_149749_6_);
    }

    
    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        int meta = MathHelper.floor_double((double)(placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        meta = meta == 0 ? 2 : meta == 1 ? 5 : meta == 2 ? 3 : 4;
        world.setBlockMetadataWithNotify(x, y, z, meta, 2);
    }

    @Override
    protected boolean openGui(World world, int x, int y, int z, EntityPlayer entityPlayer, int side) {
        if (!world.isRemote) {
            entityPlayer.openGui(CTBMod.instance, 0, world, x, y, z);
        }
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
    
    @Override
    public SubmapManagerCreator getManager(IBlockAccess world, int x, int y, int z, int meta) {
        return getManager(meta);
    }
    
    @Override
    public SubmapManagerCreator getManager(int meta) {
        return manager;
    }
}
