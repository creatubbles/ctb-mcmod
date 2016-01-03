package com.creatubbles.ctbmod.common.painting;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.AxisAlignedBB;

import com.creatubbles.api.core.Creation;
import com.creatubbles.ctbmod.common.http.DownloadableImage;
import com.creatubbles.ctbmod.common.http.DownloadableImage.ImageType;
import com.creatubbles.ctbmod.common.util.NBTUtil;
import com.creatubbles.repack.endercore.common.TileEntityBase;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TilePainting extends TileEntityBase {

    @Getter
    @Setter
    private int width, height;

    @Getter
    private Creation creation;

    @SideOnly(Side.CLIENT)
    @Getter(onMethod = @__({ @SideOnly(Side.CLIENT) }))
    private transient DownloadableImage image;

    // Client flag to prevent rendering when a dummy TE has been removed on the client
    @Setter
    @Getter
    @Accessors(fluent = true)
    private boolean render = true;

    public void setCreation(Creation image) {
        creation = image;
        if (worldObj.isRemote) {
            createImage();
        }
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        if (image == null) {
            createImage();
        }
    }

    private void createImage() {
        image = new DownloadableImage(creation.image, creation);
        image.download(ImageType.ORIGINAL);
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
        creation = NBTUtil.readJsonFromNBT(Creation.class, root);
        width = root.getInteger("width");
        height = root.getInteger("height");
    }
}
