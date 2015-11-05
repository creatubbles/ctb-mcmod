package com.creatubbles.ctbmod.common.painting;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.creatubbles.api.core.Creation;
import com.creatubbles.ctbmod.common.http.DownloadableImage;
import com.creatubbles.ctbmod.common.http.DownloadableImage.ImageType;
import com.creatubbles.ctbmod.common.util.NBTUtil;
import com.creatubbles.repack.endercore.common.TileEntityBase;


public class TilePainting extends TileEntityBase {
        
    @Getter
    @Setter
    private int width, height;

    @Getter
    private Creation creation;

    @SideOnly(Side.CLIENT)
    @Getter(onMethod = @__({ @SideOnly(Side.CLIENT) }))
    private transient DownloadableImage image;

    public void setCreation(Creation image) {
        this.creation = image;
        if (worldObj.isRemote) {
            createImage();
        }
    }

    @Override
    public void validate() {
        super.validate();
        if (worldObj.isRemote) {
            createImage();
        }
    }
    
    private void createImage() {
        image = new DownloadableImage(creation.image, creation);
        image.download(ImageType.ORIGINAL);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return BlockPainting.getCompleteBoundingBox(getWorld(), getPos());
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
