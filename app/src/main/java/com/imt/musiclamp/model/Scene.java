package com.imt.musiclamp.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import org.json.JSONArray;

@Table(name = "Scene")
public class Scene extends Model {

    @Column(name = "Name")
    private String name;

    @Column(name = "WheelValue")
    private int wheelValue;

    @Column(name = "ReadOnly")
    private boolean readOnly;

    @Column(name = "Degress")
    private float degress;

    @Column(name = "values0")
    private float values0;
    @Column(name = "values1")
    private float values1;
    @Column(name = "values2")
    private float values2;
    @Column(name = "values3")
    private float values3;
    @Column(name = "values4")
    private float values4;
    @Column(name = "values5")
    private float values5;
    @Column(name = "values6")
    private float values6;
    @Column(name = "values7")
    private float values7;
    @Column(name = "values8")
    private float values8;

    @Column(name = "playlistId")
    private int playlistId;


    @Column(name = "r")
    private int r;
    @Column(name = "g")
    private int g;
    @Column(name = "b")
    private int b;
    @Column(name = "brightness")
    private int brightness;
    @Column(name = "volume")
    private int volume;
    @Column(name = "isColor")
    private boolean isColor;

    @Column(name = "jsonArrayMusicList")
    private String jsonArrayMusicList;

    @Column(name = "isTiming")
    private boolean isTiming;
    @Column(name = "hour")
    private int hour;
    @Column(name = "minute")
    private int minute;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWheelValue() {
        return wheelValue;
    }

    public void setWheelValue(int wheelValue) {
        this.wheelValue = wheelValue;
    }

    public float getDegress() {
        return degress;
    }

    public void setDegress(float degress) {
        this.degress = degress;
    }

    public float getValues0() {
        return values0;
    }

    public void setValues0(float values0) {
        this.values0 = values0;
    }

    public float getValues1() {
        return values1;
    }

    public void setValues1(float values1) {
        this.values1 = values1;
    }

    public float getValues2() {
        return values2;
    }

    public void setValues2(float values2) {
        this.values2 = values2;
    }

    public float getValues3() {
        return values3;
    }

    public void setValues3(float values3) {
        this.values3 = values3;
    }

    public float getValues4() {
        return values4;
    }

    public void setValues4(float values4) {
        this.values4 = values4;
    }

    public float getValues5() {
        return values5;
    }

    public void setValues5(float values5) {
        this.values5 = values5;
    }

    public float getValues6() {
        return values6;
    }

    public void setValues6(float values6) {
        this.values6 = values6;
    }

    public float getValues7() {
        return values7;
    }

    public void setValues7(float values7) {
        this.values7 = values7;
    }

    public float getValues8() {
        return values8;
    }

    public void setValues8(float values8) {
        this.values8 = values8;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public int getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(int playlistId) {
        this.playlistId = playlistId;
    }

    public int getR() {
        return r;
    }

    public void setR(int r) {
        this.r = r;
    }

    public int getG() {
        return g;
    }

    public void setG(int g) {
        this.g = g;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }

    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public boolean isColor() {
        return isColor;
    }

    public void setColor(boolean isColor) {
        this.isColor = isColor;
    }

    public boolean isTiming() {
        return isTiming;
    }

    public void setTiming(boolean isTiming) {
        this.isTiming = isTiming;
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

    public String getJsonArrayMusicList() {
        return jsonArrayMusicList;
    }

    public void setJsonArrayMusicList(String jsonArrayMusicList) {
        this.jsonArrayMusicList = jsonArrayMusicList;
    }
}
