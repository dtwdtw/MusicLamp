package com.imt.musiclamp.receiver;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.imt.musiclamp.MyApplication;
import com.imt.musiclamp.event.DownloadEvent;
import com.imt.musiclamp.model.LocalMusicInfo;

import de.greenrobot.event.EventBus;

public class DownloadReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int downloadPosition = preferences.getInt(String.valueOf(reference), -1);
        if (downloadPosition != -1) {
            DownloadEvent event = new DownloadEvent();
            event.setPosition(downloadPosition);
            EventBus.getDefault().post(event);
        }
    }
}
