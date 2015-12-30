package com.creatubbles.repack.endercore.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.BlockPos;

/**
 * Created by CrazyPants on 27/02/14.
 */
public class NetworkUtil {

    public static byte[] readByteArray(ByteBuf buf) {
        int size = buf.readMedium();
        byte[] res = new byte[size];
        buf.readBytes(res);
        return res;
    }

    public static void writeByteArray(ByteBuf buf, byte[] arr) {
        buf.writeMedium(arr.length);
        buf.writeBytes(arr);
    }

    public static void writePos(BlockPos pos, ByteBuf buf) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
    }

    public static BlockPos readPos(ByteBuf buf) {
        return new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
    }
}
