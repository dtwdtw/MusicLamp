package com.imt.musiclamp.event;

/**
 * Created by MASAILA on 15/5/15.
 */
public class ProgressThreadEvent {
    private boolean start = true;

    public boolean isStart() {
        return start;
    }

    public void setStart(boolean start) {
        this.start = start;
    }
}
