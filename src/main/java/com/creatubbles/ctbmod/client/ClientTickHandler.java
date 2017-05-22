package com.creatubbles.ctbmod.client;

import lombok.Getter;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

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
