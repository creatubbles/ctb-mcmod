package com.creatubbles.ctbmod.common.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;

import com.creatubbles.ctbmod.CTBMod;
import com.creatubbles.repack.endercore.common.config.PacketConfigSync;
import com.creatubbles.repack.endercore.common.util.ChatUtil.PacketNoSpamChat;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class PacketHandler {

	public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(CTBMod.MODID);

	private static int ID = 0;

	public static int nextID() {
		return ID++;
	}

	public static void init() {
		INSTANCE.registerMessage(PacketNoSpamChat.Handler.class, PacketNoSpamChat.class, nextID(), Side.CLIENT);
		INSTANCE.registerMessage(PacketConfigSync.Handler.class, PacketConfigSync.class, nextID(), Side.CLIENT);
		INSTANCE.registerMessage(MessageDimensionChange.Handler.class, MessageDimensionChange.class, nextID(), Side.SERVER);
	}

	public static void sendToAllAround(IMessage message, TileEntity te, int range) {
		INSTANCE.sendToAllAround(message, new TargetPoint(te.getWorldObj().provider.dimensionId, te.xCoord, te.yCoord, te.zCoord, range));
	}

	public static void sendToAllAround(IMessage message, TileEntity te) {
		sendToAllAround(message, te, 64);
	}

	public static void sendTo(IMessage message, EntityPlayerMP player) {
		INSTANCE.sendTo(message, player);
	}
}