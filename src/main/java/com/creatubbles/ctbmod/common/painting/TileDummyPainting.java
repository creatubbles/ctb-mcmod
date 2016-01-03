package com.creatubbles.ctbmod.common.painting;

import lombok.NonNull;
import lombok.Setter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import com.creatubbles.repack.endercore.common.TileEntityBase;
import com.creatubbles.repack.endercore.common.util.BlockCoord;

public class TileDummyPainting extends TileEntityBase {

    @Setter
    @NonNull
    private BlockCoord main = new BlockCoord();

    public TilePainting getDataTile() {
        return (TilePainting) main.getTileEntity(getWorldObj());
    }

    @Override
    public void invalidate() {
        super.invalidate();
        TileEntity te = main.getTileEntity(worldObj);
        if (te != null && te instanceof TilePainting) {
            ((TilePainting) te).render(true);
        }
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
