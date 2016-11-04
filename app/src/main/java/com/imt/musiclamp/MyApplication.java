package com.imt.musiclamp;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.imt.musiclamp.element.GetMusicInfoByID;
import com.imt.musiclamp.model.CustomPlayList;
import com.imt.musiclamp.model.Lamp;
import com.imt.musiclamp.model.LocalMusicInfo;
import com.imt.musiclamp.model.MessageModlesave;
import com.imt.musiclamp.model.OnlineMusicInfo;
import com.imt.musiclamp.model.OnlinePlaylist;
import com.imt.musiclamp.service.PlayerService;
import com.imt.musiclamp.service.RongPush;
import com.imt.musiclamp.service.ServerSocketService;
import com.imt.musiclamp.utils.Utils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import net.danlew.android.joda.JodaTimeAndroid;

import org.apache.http.Header;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.rong.imlib.RongIMClient;

public class MyApplication extends Application {

    private List<LocalMusicInfo> localMusicInfos;
    private List<OnlineMusicInfo> onlineMusicInfos;
    private OnlinePlaylist currentOnlinePlaylist;
    private Lamp currentPlayingLamp;

    public static Context context;

    private boolean isPlaying = false;
    private boolean isLocalDevicesPlaying = true;
    private boolean isLocalMusicPlaying = true;
    private boolean isSelectMusic = false;

    private int currentLocalPosition = -1;
    private int currentOnlinePosition = -1;
    private int currentSongId = -1;

    private int currentPlayMode = 0;

    private boolean progressEnabler = false;

    public static final int PLAY_MODE_CYCLE = 0;
    public static final int PLAY_MODE_RANDOM = 1;
    public static final int PLAY_MODE_REPAET = 2;

    public static final byte DEVICES_ID_GATEWAY = 0x00;
    public static final byte DEVICES_ID_LAMP = 0x01;

    public static final byte CMD_FIND_DEVICES = 0x11;
    public static final byte CMD_RECEIVE_LAMP = 0x20;

    public static final int UDP_SERVER_PORT = 6000;

    private String gateWayIP;
    private byte[] gateMacAddress;

    private boolean LogInfo = false;

    public static String userID;
    public static String rongToken;
    public static Bitmap myHead = null;
    public static boolean firstLogin = true;
    public static HashMap<String, List<MessageModlesave>> modlesaveList = new HashMap<>();
    public static boolean remoteplayingflag = false;
    public static boolean remoteisplaying = false;
    public static Lamp playingLamp;
    public static String headUri;
    public static String userName;

    public static boolean autochangedevicewifi=false;
    public static boolean wifiinfosended=false;
    public static boolean iswanttoconnectwifi=false;
    public static boolean colorpickactive=false;
    public static boolean popfragmentactive=false;

    public static boolean isAlreadyplaying=false;

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();

        localMusicInfos = new ArrayList<>();
        onlineMusicInfos = new ArrayList<>();
        try {
            localMusicInfos = Utils.getAllMusic(this);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        startService(new Intent(this, PlayerService.class));
        startService(new Intent(this, ServerSocketService.class));

        ActiveAndroid.initialize(this);
        JodaTimeAndroid.init(this);

        if (Utils.isFirst(this)) {
            CustomPlayList customPlayList = new CustomPlayList();
            customPlayList.setName(getResources().getString(R.string.romantic));
            customPlayList.setReadOnly(true);
            customPlayList.save();
            CustomPlayList cus = new CustomPlayList();
            cus.setName(getResources().getString(R.string.comfortable));
            cus.setReadOnly(true);
            cus.save();

            try {
                final List<CustomPlayList> playLists=new Select().from(CustomPlayList.class).execute();
                new GetMusicInfoByID(new Handler(), "5037884") {
                    @Override
                    public void MusicInfo(OnlineMusicInfo onlineMusicInfo) {
                        onlineMusicInfo.setCustomPlaylistId(playLists.get(0).getId().intValue());
                        onlineMusicInfo.save();
                        playLists.get(0).setImgUrl(onlineMusicInfo.getArtworkUrl());
                        playLists.get(0).save();
//
//                        Log.v("onlinemusicinfo", onlineMusicInfo.getAlbum());
//                        Log.v("onlinemusicinfo", onlineMusicInfo.getArtist());
//                        Log.v("onlinemusicinfo", onlineMusicInfo.getArtworkUrl());
//                        Log.v("onlinemusicinfo", onlineMusicInfo.getSongId()+"");
//                        Log.v("onlinemusicinfo", onlineMusicInfo.getDuration()+"");
//                        Log.v("onlinemusicinfo", onlineMusicInfo.getUrl());
//                        Log.v("onlinemusicinfo", onlineMusicInfo.getTitle());
                    }
                };
                new GetMusicInfoByID(new Handler(), "1879676") {
                    @Override
                    public void MusicInfo(OnlineMusicInfo onlineMusicInfo) {
                        onlineMusicInfo.setCustomPlaylistId(playLists.get(0).getId().intValue());
                        onlineMusicInfo.save();
                    }
                };
                new GetMusicInfoByID(new Handler(), "443242") {
                    @Override
                    public void MusicInfo(OnlineMusicInfo onlineMusicInfo) {
                        onlineMusicInfo.setCustomPlaylistId(playLists.get(0).getId().intValue());
                        onlineMusicInfo.save();
                    }
                };
                new GetMusicInfoByID(new Handler(), "5264598") {
                    @Override
                    public void MusicInfo(OnlineMusicInfo onlineMusicInfo) {
                        onlineMusicInfo.setCustomPlaylistId(playLists.get(1).getId().intValue());
                        onlineMusicInfo.save();
                        playLists.get(1).setImgUrl(onlineMusicInfo.getArtworkUrl());
                        playLists.get(1).save();
                    }
                };
                new GetMusicInfoByID(new Handler(), "5264597") {
                    @Override
                    public void MusicInfo(OnlineMusicInfo onlineMusicInfo) {
                        onlineMusicInfo.setCustomPlaylistId(playLists.get(1).getId().intValue());
                        onlineMusicInfo.save();
                    }
                };
                new GetMusicInfoByID(new Handler(), "139774") {
                    @Override
                    public void MusicInfo(OnlineMusicInfo onlineMusicInfo) {
                        onlineMusicInfo.setCustomPlaylistId(playLists.get(1).getId().intValue());
                        onlineMusicInfo.save();
                    }
                };
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void onTerminate() {
        super.onTerminate();
        ActiveAndroid.dispose();
    }

    public List<LocalMusicInfo> getLocalMusicInfos(boolean needUpdate) {
        if (needUpdate || localMusicInfos == null) {
            localMusicInfos = Utils.getAllMusic(this);
        }
        return localMusicInfos;
    }

    public OnlineMusicInfo getCurrentOnlineMusicInfo() {
        try {
            return onlineMusicInfos.get(currentOnlinePosition);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public LocalMusicInfo getCurrentLocalMusicInfo() {
        return localMusicInfos.get(currentLocalPosition);
    }

    public List<LocalMusicInfo> getLocalMusicInfos() {
        return localMusicInfos;
    }

    public void setLocalMusicInfos(List<LocalMusicInfo> localMusicInfos) {
        this.localMusicInfos = localMusicInfos;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }

    public int getCurrentLocalPosition() {
        return currentLocalPosition;
    }

    public void setCurrentLocalPosition(int currentLocalPosition) {
        this.currentLocalPosition = currentLocalPosition;
    }

    public int getCurrentPlayMode() {
        return currentPlayMode;
    }

    public void setCurrentPlayMode(int currentPlayMode) {
        this.currentPlayMode = currentPlayMode;
    }

    public int getCurrentSongId() {
        return currentSongId;
    }

    public void setCurrentSongId(int currentSongId) {
        this.currentSongId = currentSongId;
    }

    public int getCurrentOnlinePosition() {
        return currentOnlinePosition;
    }

    public void setCurrentOnlinePosition(int currentOnlinePosition) {
        this.currentOnlinePosition = currentOnlinePosition;
    }

    public List<OnlineMusicInfo> getOnlineMusicInfos() {
        return onlineMusicInfos;
    }

    public void setOnlineMusicInfos(List<OnlineMusicInfo> onlineMusicInfos) {
        this.onlineMusicInfos = onlineMusicInfos;
    }

    public boolean getLogInfo() {
        return LogInfo;
    }

    public void serLogInfo(boolean info) {
        LogInfo = info;
    }

    public String getGateWayIP() {
        return gateWayIP;
    }

    public void setGateWayIP(String gateWayIP) {
        this.gateWayIP = gateWayIP;
    }

    public byte[] getGateMacAddress() {
        return gateMacAddress;
    }

    public void setGateMacAddress(byte[] gateMacAddress) {
        this.gateMacAddress = gateMacAddress;
    }

    public boolean isLocalDevicesPlaying() {
        return isLocalDevicesPlaying;
    }

    public void setLocalDevicesPlaying(boolean isLocalDevicesPlaying) {
        this.isLocalDevicesPlaying = isLocalDevicesPlaying;
    }

    public boolean isSelectMusic() {
        return isSelectMusic;
    }

    public void setSelectMusic(boolean isSelectMusic) {
        this.isSelectMusic = isSelectMusic;
    }

    public Lamp getCurrentPlayingLamp() {
        return currentPlayingLamp;
    }

    public void setCurrentPlayingLamp(Lamp currentPlayingLamp) {
        this.currentPlayingLamp = currentPlayingLamp;
    }

    public boolean isProgressEnabler() {
        return progressEnabler;
    }

    public void setProgressEnabler(boolean progressEnabler) {
        this.progressEnabler = progressEnabler;
    }

    public boolean isLocalMusicPlaying() {
        return isLocalMusicPlaying;
    }

    public void setLocalMusicPlaying(boolean isLocalMusicPlaying) {
        this.isLocalMusicPlaying = isLocalMusicPlaying;
    }

    public OnlinePlaylist getCurrentOnlinePlaylist() {
        return currentOnlinePlaylist;
    }

    public void setCurrentOnlinePlaylist(OnlinePlaylist currentOnlinePlaylist) {
        this.currentOnlinePlaylist = currentOnlinePlaylist;
    }
}
