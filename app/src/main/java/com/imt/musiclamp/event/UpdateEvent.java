package com.imt.musiclamp.event;

import android.support.annotation.IntDef;
import android.util.Log;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by dtw on 15/5/19.
 */
public class UpdateEvent{
    String updateInfo;

    public String getInfo() {
        return info;
    }


    String info;
    public final static int NEWVERSATION=1;
    public final static int NOTHING=0;
    public final static int BADNET=-1;
    public final static int DOWNLOADINT=2;

    public int getUpdateInfo() {
        switch (updateInfo){
            case "Network Error":
                return BADNET;
            case "Checking":
                break;
            case "Downloading":
                return DOWNLOADINT;
            case "Unkonw Error":
                break;
            case "Undiscover":
                return NOTHING;

        }
        info=updateInfo;
        return NEWVERSATION;
    }

    public void setUpdateInfo(String updateInfo) {
        this.updateInfo = updateInfo;
        Log.v("update", updateInfo);
    }


//    public enum {NEWVERSAIN}
}
