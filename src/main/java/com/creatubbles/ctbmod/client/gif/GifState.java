package com.creatubbles.ctbmod.client.gif;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class GifState {
    
    public static final GifState DEFAULT = new GifState(10, 2 / 255f, 5 * 20);
    static {
        DEFAULT.status = RecordingStatus.OFF;
        DEFAULT.countdown = 0;
        DEFAULT.time = DEFAULT.getMaxLength();
    }
    
    private final int quality;
    private final float compression;
    private final int maxLength;
    
    private RecordingStatus status = RecordingStatus.PREPARING;
    private int countdown = 3 * 20;
    private int time;
    
    @Setter
    private float saveProgress;
    
    public void tick() {
        if (countdown <= 0) {
            if (time >= maxLength) {
                // Only change LIVE to SAVING, otherwise ignore
                status = status == RecordingStatus.LIVE ? RecordingStatus.SAVING : status;
            } else {
                time++;
                status = RecordingStatus.LIVE;
            }
        } else {
            countdown--;
            status = RecordingStatus.PREPARING;
        }
    }
    
    public void stop() {
        time = maxLength;
    }
    
    public void saved() {
        status = RecordingStatus.OFF;
    }
}
