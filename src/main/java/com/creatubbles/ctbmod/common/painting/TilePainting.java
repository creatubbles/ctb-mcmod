package com.creatubbles.ctbmod.common.painting;

import com.creatubbles.api.core.Image.ImageType;
import com.creatubbles.ctbmod.common.http.CreationRelations;
import com.creatubbles.ctbmod.common.http.DownloadableImage;
import com.creatubbles.ctbmod.common.util.ConcurrentUtil;
import com.creatubbles.ctbmod.common.util.NBTUtil;
import com.creatubbles.repack.endercore.common.TileEntityBase;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

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
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        if (image == null && creation != null) {
            createImage();
        }
    }

    private void createImage() {
        AxisAlignedBB bb = BlockPainting.getCompleteBoundingBox(getWorldObj(), xCoord, yCoord, zCoord);
        image = new DownloadableImage(creation.getImage(), creation);
        type = width <= 4 || height <= 4 ? ImageType.full_view : ImageType.original;
        image.download(type, Vec3.createVectorHelper(bb.minX + ((bb.maxX - bb.minX) / 2), bb.minY + ((bb.maxY - bb.minY) / 2), bb.minZ + ((bb.maxZ - bb.minZ) / 2)));
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return BlockPainting.getCompleteBoundingBox(getWorldObj(), xCoord, yCoord, zCoord);
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
                    worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
                }
            });
        }
        width = root.getInteger("width");
        height = root.getInteger("height");
    }
}
