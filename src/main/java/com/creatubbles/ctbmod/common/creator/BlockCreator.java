package com.creatubbles.ctbmod.common.creator;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
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
