package com.imt.musiclamp.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;

import java.io.Serializable;

public class OnlineMusicInfo extends Model implements Serializable {

    @Column(name = "songId")
    private int songId;
    @Column(name = "title")
    private String title;
    @Column(name = "artist")
    private String artist;
    @Column(name = "album")
    private String album;
    @Column(name = "url")
    private String url;
    @Column(name = "artworkUrl")
    private String artworkUrl;
    @Column(name = "duration")
    private long duration;
    @Column(name = "customPlaylistId")
    private int customPlaylistId;
    @Column(name = "like")
    private boolean like = false;
    @Column(name = "downloaded")
    private boolean downloaded = false;
    @Column(name = "recently")
    private boolean recently = false;
    @Column(name = "lrc")
    private String lrc = "正在加载";

    private boolean sceneCheck = false;

    public int getSongId() {
        return songId;
    }

    public void setSongId(int songId) {
        this.songId = songId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }


    public String getArtworkUrl() {
        return artworkUrl;
    }

    public void setArtworkUrl(String artworkUrl) {
        this.artworkUrl = artworkUrl;
    }

    public int getCustomPlaylistId() {
        return customPlaylistId;
    }

    public void setCustomPlaylistId(int customPlaylistId) {
        this.customPlaylistId = customPlaylistId;
    }

    public boolean isLike() {
        return like;
    }

    public void setLike(boolean like) {
        this.like = like;
    }

    public boolean isDownloaded() {
        return downloaded;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }

    public boolean isRecently() {
        return recently;
    }

    public void setRecently(boolean recently) {
        this.recently = recently;
    }

    public String getLrc() {
        return lrc;
    }

    public void setLrc(String lrc) {
        this.lrc = lrc;
    }

    public boolean isSceneCheck() {
        return sceneCheck;
    }

    public void setSceneCheck(boolean sceneCheck) {
        this.sceneCheck = sceneCheck;
    }
}


