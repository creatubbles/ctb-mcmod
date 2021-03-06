package com.creatubbles.ctbmod.common.network;

import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import com.creatubbles.ctbmod.common.creator.TileCreator;
import com.creatubbles.repack.endercore.common.network.MessageTileEntity;

@NoArgsConstructor
public class MessageDimensionChange extends MessageTileEntity<TileCreator> {

    private int width, height;

    public MessageDimensionChange(TileCreator te) {
        super(te);
        width = te.getWidth();
        height = te.getHeight();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        buf.writeByte(width);
        buf.writeByte(height);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);
        width = buf.readByte();
        height = buf.readByte();
    }

    public static class Handler implements IMessageHandler<MessageDimensionChange, IMessage> {

        @Override
        public IMessage onMessage(MessageDimensionChange message, MessageContext ctx) {
            TileCreator te = message.getTileEntity(ctx.getServerHandler().playerEntity.worldObj);
            te.setWidth(message.width);
            te.setHeight(message.height);
            return null;
        }
    }
}
