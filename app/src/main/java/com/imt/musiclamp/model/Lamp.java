package com.imt.musiclamp.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.imt.musiclamp.utils.Utils;

import java.io.Serializable;

@Table(name = "Lamps")
public class Lamp extends Model implements Serializable {

    @Column(name = "Name")
    public String name;

    @Column(name = "ip")
    public String ip;

    @Column(name = "MacAddressByte")
    public byte[] macAddressByte;

    @Column(name = "MacAddress")
    public String macAdress;

    @Column(name = "selected")
    public boolean selected;

    @Column(name = "kind")
    public int kind;

    @Column(name = "pwm")
    public int pwm;

    @Column(name = "online")
    public boolean online;

    public Lamp() {
    }

    public Lamp(String name, String macAddress, String ip) {
        this.name = name;
        this.ip = ip;
        this.macAdress = macAddress;
        this.selected = false;
        this.online = true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public byte[] getMacAddressByte() {
        return macAddressByte;
    }

    public void setMacAddressByte(byte[] macAddressByte) {
        this.macAddressByte = macAddressByte;
    }

    public String getMacAdress() {
        return macAdress;
    }

    public void setMacAdress(String macAdress) {
        this.macAdress = macAdress;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}


