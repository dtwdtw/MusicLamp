package com.imt.musiclamp.event;

/**
 * Created by mac on 15/5/7.
 */
public class TimePickEvent {
    private boolean set;
    private int hour;
    private int minute;

    public boolean isSet() {
        return set;
    }

    public void setSet(boolean set) {
        this.set = set;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }
}
