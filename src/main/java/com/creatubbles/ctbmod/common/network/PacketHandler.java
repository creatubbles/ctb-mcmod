package com.creatubbles.ctbmod.common.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import com.creatubbles.ctbmod.CTBMod;
import com.creatubbles.repack.endercore.common.config.PacketConfigSync;
import com.creatubbles.repack.endercore.common.network.PacketProgress;
import com.creatubbles.repack.endercore.common.util.ChatUtil.PacketNoSpamChat;

public class PacketHandler {

	public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(CTBMod.MODID);

	private static int ID = 0;

    public static int nextID() {
        return ID++;
    }

    public static void init() {
        INSTANCE.registerMessage(PacketNoSpamChat.Handler.class, PacketNoSpamChat.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketConfigSync.Handler.class, PacketConfigSync.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketProgress.Handler.class, PacketProgress.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(MessageDimensionChange.Handler.class, MessageDimensionChange.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(MessageCreate.Handler.class, MessageCreate.class, nextID(), Side.SERVER);
    }

    public static void sendToAllAround(IMessage message, TileEntity te, int range) {
		BlockPos pos = te.getPos();
		INSTANCE.sendToAllAround(message, new TargetPoint(te.getWorld().provider.getDimensionId(), pos.getX(), pos.getY(), pos.getZ(), range));
	}

	public static void sendToAllAround(IMessage message, TileEntity te) {
		sendToAllAround(message, te, 64);
	}

	public static void sendTo(IMessage message, EntityPlayerMP player) {
		INSTANCE.sendTo(message, player);
	}
}