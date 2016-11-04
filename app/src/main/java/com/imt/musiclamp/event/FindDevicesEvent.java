package com.imt.musiclamp.event;

public class FindDevicesEvent {

    private String ip = null;
    private String macAddress;
    private String player;
    private int volume;
    private String state;
    private String date;
    private String network;
    private boolean flow;
    private int r, g, b, w;
    private byte devicesId;
    private byte[] packetData = null;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public byte getDevicesId() {
        return devicesId;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public boolean isFlow() {
        return flow;
    }

    public void setFlow(boolean flow) {
        this.flow = flow;
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

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

    public void setDevicesId(byte devicesId) {
        this.devicesId = devicesId;
    }

    public byte[] getPacketData() {
        return packetData;
    }

    public void setPacketData(byte[] packetData) {
        this.packetData = packetData;
    }
}
