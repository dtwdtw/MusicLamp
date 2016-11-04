package com.imt.musiclamp.model;

import android.nfc.tech.NfcA;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;

/**
 * Created by MASAILA on 3/20/15.
 */
public class CustomPlayList extends Model {

    @Column(name = "name")
    private String name;
    @Column(name = "imgUrl")
    private String imgUrl;
    @Column(name = "readOnly")
    private boolean readOnly = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
}
