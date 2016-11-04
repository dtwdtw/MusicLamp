package com.imt.musiclamp.event;


import com.imt.musiclamp.model.LocalMusicInfo;
import com.imt.musiclamp.model.OnlineMusicInfo;

/**
 * Created by MASAILA on 15/2/6.
 */
public class PlayEvent {

    private boolean local;
    private LocalMusicInfo localMusicInfo;
    private OnlineMusicInfo onlineMusicInfo;
    private int songId;
    private int position;
    private int state;

    private static final int PLAY_STATE_NORMAL = 1;
    private static final int PLAY_STATE_SHUFFLE = 2;
    private static final int PLAY_STATE_REPEAT = 3;

    public boolean isLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    public LocalMusicInfo getLocalMusicInfo() {
        return localMusicInfo;
    }

    public void setLocalMusicInfo(LocalMusicInfo localMusicInfo) {
        this.localMusicInfo = localMusicInfo;
    }

    public OnlineMusicInfo getOnlineMusicInfo() {
        return onlineMusicInfo;
    }

    public void setOnlineMusicInfo(OnlineMusicInfo onlineMusicInfo) {
        this.onlineMusicInfo = onlineMusicInfo;
    }

    public int getSongId() {
        return songId;
    }

    public void setSongId(int songId) {
        this.songId = songId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
