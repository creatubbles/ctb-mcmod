package com.creatubbles.ctbmod.common.painting;

import java.util.concurrent.Executor;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.creatubbles.api.core.Image.ImageType;
import com.creatubbles.ctbmod.common.http.CreationRelations;
import com.creatubbles.ctbmod.common.http.DownloadableImage;
import com.creatubbles.ctbmod.common.util.ConcurrentUtil;
import com.creatubbles.ctbmod.common.util.NBTUtil;
import com.creatubbles.repack.endercore.common.TileEntityBase;

public class TilePainting extends TileEntityBase {

    @Getter
    @Setter
    private int width, height;

    @Getter
    private CreationRelations creation;

    @SideOnly(Side.CLIENT)
    @Getter(onMethod = @__({ @SideOnly(Side.CLIENT) }))
    private transient DownloadableImage image;
    
    @Getter
    private transient ImageType type;

    // Client flag to prevent rendering when a dummy TE has been removed on the client
    @Setter
    @Getter
    @Accessors(fluent = true)
    private boolean render = true;

    public void setCreation(CreationRelations image) {
        creation = image;
    }
    
    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        if (worldObj != null && worldObj.isRemote && image == null && creation != null) {
            createImage();
        }
    }

    private void createImage() {
        AxisAlignedBB bb = BlockPainting.getCompleteBoundingBox(getWorld(), getPos());
        image = new DownloadableImage(creation.getImage(), creation);
        type = width <= 4 || height <= 4 ? ImageType.full_view : ImageType.original;
        image.download(type, new BlockPos(bb.minX + ((bb.maxX - bb.minX) / 2), bb.minY + ((bb.maxY - bb.minY) / 2), bb.minZ + ((bb.maxZ - bb.minZ) / 2)));
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return BlockPainting.getCompleteBoundingBox(getWorld(), getPos());
    }
    
    @Override
    public double getMaxRenderDistanceSquared() {
        return super.getMaxRenderDistanceSquared() * 4; // Twice as far as default
    }

    @Override
    protected void writeCustomNBT(NBTTagCompound root) {
        NBTUtil.writeJsonToNBT(creation, root);
        root.setInteger("width", width);
        root.setInteger("height", height);
    }

    @Override
    protected void readCustomNBT(NBTTagCompound root) {
        creation = BlockPainting.getCreation(root);
        // If this is an "incomplete" creation, listen for its completion then execute the download afterwards
        if (creation.getRelationships() == null && FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            ConcurrentUtil.addServerThreadListener(creation.getCompletionCallback(), new Runnable() {

                @Override
                public void run() {
                    IBlockState state = worldObj.getBlockState(getPos());
                    worldObj.notifyBlockUpdate(pos, state, state, 8);
                }
            });
        }
        width = root.getInteger("width");
        height = root.getInteger("height");
    }
}
