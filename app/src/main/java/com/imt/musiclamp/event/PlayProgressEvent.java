package com.imt.musiclamp.event;

/**
 * Created by MASAILA on 15/2/9.
 */
public class PlayProgressEvent {

    private boolean isLocalDevices;
    private int duration;
    private int currentPosition;
    private String state;

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isLocalDevices() {
        return isLocalDevices;
    }

    public void setLocalDevices(boolean isLocalDevices) {
        this.isLocalDevices = isLocalDevices;
    }
}
