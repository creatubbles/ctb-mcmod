package com.creatubbles.ctbmod.client;

import lombok.Getter;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

public enum ClientTickHandler {
    
    INSTANCE;
    
    @Getter
    private long ticksElapsed;
    
    @SubscribeEvent
    public void onClientTick(ClientTickEvent event) {
        if (event.phase == Phase.START) {
            ticksElapsed++;
        }
    }

}
