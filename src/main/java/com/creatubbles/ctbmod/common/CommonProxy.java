package com.creatubbles.ctbmod.common;

import com.creatubbles.ctbmod.common.config.DataCache;

import net.minecraft.world.World;

public class CommonProxy {

    public void registerRenderers() {}

    public World getClientWorld() {
        return null;
    }

    public long getTicksElapsed() {
        return 0;
    }

    public void updateRecordingData(DataCache cache) {}
}
