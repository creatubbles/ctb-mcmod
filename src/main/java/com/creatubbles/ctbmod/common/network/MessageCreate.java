package com.creatubbles.ctbmod.common.network;

import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import com.creatubbles.api.CreatubblesAPI;
import com.creatubbles.api.core.Creation;
import com.creatubbles.ctbmod.common.creator.TileCreator;
import com.creatubbles.ctbmod.common.painting.BlockPainting;
import com.creatubbles.repack.endercore.common.network.MessageTileEntity;

@NoArgsConstructor
public class MessageCreate extends MessageTileEntity<TileCreator> {

    private Creation creation;
    private int width, height;

    public MessageCreate(Creation creation, TileCreator creator) {
        super(creator);
        this.creation = creation;
        this.width = creator.getWidth();
        this.height = creator.getHeight();
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
        creation = CreatubblesAPI.GSON.fromJson(ByteBufUtils.readUTF8String(buf), Creation.class);
        width = buf.readByte();
        height = buf.readByte();
    }

    public static class Handler implements IMessageHandler<MessageCreate, IMessage> {

        @Override
        public IMessage onMessage(MessageCreate message, MessageContext ctx) {
            final TileCreator te = message.getTileEntity(ctx.getServerHandler().playerEntity.worldObj);
            final ItemStack created = BlockPainting.create(message.creation, message.width, message.height);
            MinecraftServer.getServer().addScheduledTask(new Runnable() {

                @Override
                public void run() {
                    te.setOutput(created);
                    te.markDirty();
                }
            });
            return null;
        }
    }
}
