package com.creatubbles.repack.endercore.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import com.google.common.reflect.TypeToken;

public abstract class MessageTileEntity<T extends TileEntity> implements IMessage {

	protected BlockPos pos;

	protected MessageTileEntity() {
	}

	protected MessageTileEntity(T tile) {
		pos = tile.getPos();
	}

	public void toBytes(ByteBuf buf) {
		NetworkUtil.writePos(pos, buf);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		pos = NetworkUtil.readPos(buf);
	}

	@SuppressWarnings("unchecked")
	protected T getTileEntity(World worldObj) {
		if (worldObj == null) {
			return null;
		}
		TileEntity te = worldObj.getTileEntity(pos);
		if (te == null) {
			return null;
		}
		TypeToken<?> teType = TypeToken.of(getClass()).resolveType(MessageTileEntity.class.getTypeParameters()[0]);
		if (teType.isAssignableFrom(te.getClass())) {
			return (T) te;
		}
		return null;
	}

	protected World getWorld(MessageContext ctx) {
		return ctx.getServerHandler().playerEntity.worldObj;
	}
}
