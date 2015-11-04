package com.creatubbles.ctbmod.common.painting;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

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

    public void setCreation(Creation image) {
        this.creation = image;
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
        createImage();
	    width = root.getInteger("width");
	    height = root.getInteger("height");
	}
	
	@Override
	public boolean shouldRefresh(Block oldBlock, Block newBlock, int oldMeta, int newMeta, World world, int x, int y, int z) {
	    return oldBlock != newBlock;
	}
}
