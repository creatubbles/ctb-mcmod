package com.creatubbles.ctbmod.common.painting;

import lombok.Setter;
import net.minecraft.nbt.NBTTagCompound;

import com.creatubbles.repack.endercore.common.TileEntityBase;
import com.creatubbles.repack.endercore.common.util.BlockCoord;

public class TileDummyPainting extends TileEntityBase {

    @Setter
    private BlockCoord main;

    public TilePainting getDataTile() {
        return (TilePainting) main.getTileEntity(getWorldObj());
    }

    @Override
    protected void writeCustomNBT(NBTTagCompound root) {
    	NBTTagCompound tag = new NBTTagCompound();
    	main.writeToNBT(tag);
        root.setTag("dataPainting", tag);
    }

    @Override
    protected void readCustomNBT(NBTTagCompound root) {
        main = BlockCoord.readFromNBT(root.getCompoundTag("dataPainting"));
    }
}
