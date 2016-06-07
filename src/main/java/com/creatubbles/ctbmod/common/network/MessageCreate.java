package com.creatubbles.ctbmod.common.network;

import com.creatubbles.api.CreatubblesAPI;
import com.creatubbles.ctbmod.common.creator.TileCreator;
import com.creatubbles.ctbmod.common.http.CreationRelations;
import com.creatubbles.repack.endercore.common.network.MessageTileEntity;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class MessageCreate extends MessageTileEntity<TileCreator> {

    private CreationRelations creation;
    private int width, height;

    public MessageCreate(CreationRelations creation, TileCreator creator) {
        super(creator);
        this.creation = creation;
        width = creator.getWidth();
        height = creator.getHeight();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        ByteBufUtils.writeUTF8String(buf, CreatubblesAPI.GSON.toJson(creation));
        buf.writeByte(width);
        buf.writeByte(height);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);
        creation = CreatubblesAPI.GSON.fromJson(ByteBufUtils.readUTF8String(buf), CreationRelations.class);
        width = buf.readByte();
        height = buf.readByte();
    }

    public static class Handler implements IMessageHandler<MessageCreate, IMessage> {

        @Override
        public IMessage onMessage(final MessageCreate message, MessageContext ctx) {
            TileCreator te = message.getTileEntity(ctx.getServerHandler().playerEntity.worldObj);
            te.create(message.creation);
            te.markDirty();
            return null;
        }
    }
}
