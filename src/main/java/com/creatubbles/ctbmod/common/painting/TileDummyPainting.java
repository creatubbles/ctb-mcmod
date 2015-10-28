package com.creatubbles.ctbmod.common.painting;

import lombok.Setter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;

import com.creatubbles.repack.endercore.common.TileEntityBase;

public class TileDummyPainting extends TileEntityBase {

    @Setter
    private BlockPos main;

    public TilePainting getDataTile() {
        return (TilePainting) getWorld().getTileEntity(main);
    }

    @Override
    protected void writeCustomNBT(NBTTagCompound root) {
        root.setLong("dataPainting", main.toLong());
    }

    @Override
    protected void readCustomNBT(NBTTagCompound root) {
        main = BlockPos.fromLong(root.getLong("dataPainting"));
    }
}
