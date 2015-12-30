package com.creatubbles.repack.endercore.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import com.creatubbles.ctbmod.CTBMod;
import com.creatubbles.repack.endercore.api.common.util.IProgressTile;

public class PacketProgress extends MessageTileEntity<TileEntity> {

    private float progress;

    public PacketProgress() {}

    public PacketProgress(IProgressTile tile) {
        super(tile.getTileEntity());
        progress = tile.getProgress();
        if (progress == 0) {
            progress = -1;
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        buf.writeFloat(progress);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);
        progress = buf.readFloat();
    }

    public static class Handler implements IMessageHandler<PacketProgress, IMessage> {

        @Override
        public IMessage onMessage(PacketProgress message, MessageContext ctx) {
            TileEntity tile = message.getTileEntity(CTBMod.proxy.getClientWorld());
            if (tile instanceof IProgressTile) {
                ((IProgressTile) tile).setProgress(message.progress);
            }
            return null;
        }
    }
}
