package com.creatubbles.ctbmod.common.painting;

import lombok.Getter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.creatubbles.api.core.Creation;
import com.creatubbles.ctbmod.common.http.DownloadableImage;
import com.creatubbles.ctbmod.common.http.DownloadableImage.ImageType;
import com.creatubbles.ctbmod.common.util.NBTUtil;
import com.creatubbles.repack.endercore.common.TileEntityBase;


public class TilePainting extends TileEntityBase {
        
    @Getter
    private int width, height;

    @Getter
    private Creation creation;

    @SideOnly(Side.CLIENT)
    @Getter(onMethod = @__({ @SideOnly(Side.CLIENT) }))
    private transient DownloadableImage image;

    public TilePainting() {
        width = 2;
        height = 2;
    }

    public void setCreation(Creation image) {
        this.creation = image;
        if (worldObj.isRemote) {
            createImage();
        }
    }

    private void createImage() {
        image = new DownloadableImage(creation.image, creation);
        image.download(ImageType.FULL_VIEW);
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
