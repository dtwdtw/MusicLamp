package com.imt.musiclamp.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;


import com.imt.musiclamp.MyApplication;
import com.imt.musiclamp.R;
import com.imt.musiclamp.api.APIClient;
import com.imt.musiclamp.event.DownloadEvent;
import com.imt.musiclamp.event.PauseEvent;
import com.imt.musiclamp.event.PlayCompleEvent;
import com.imt.musiclamp.event.PlayEvent;
import com.imt.musiclamp.event.PlayProgressEvent;
import com.imt.musiclamp.event.ProgressThreadEvent;
import com.imt.musiclamp.event.ResumeEvent;
import com.imt.musiclamp.event.SeekEvent;
import com.imt.musiclamp.model.LocalMusicInfo;
import com.imt.musiclamp.model.OnlineMusicInfo;
import com.imt.musiclamp.utils.NanoHTTPD;
import com.imt.musiclamp.utils.Utils;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Random;

import de.greenrobot.event.EventBus;

public class PlayerService extends Service {

    private MyApplication myApplication;
    private MediaPlayer mediaPlayer;

    private NanoHTTPD httpServer;

    private boolean isLocal = false;

    private boolean progressEnabler = false;

    private ProgressThread thread;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = (MyApplication) getApplication();
        EventBus.getDefault().register(this);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(onCompletionListener);

        progressEnabler = true;

        if(null != thread)
            thread.setFlag(false);

        thread = new ProgressThread();
        thread.setFlag(true);
        thread.start();

        try {
            httpServer = new NanoHTTPD(8080, Environment.getExternalStorageDirectory());
        } catch (IOException e) {
            e.printStackTrace();
        }

        TelephonyManager telephony = (TelephonyManager) getSystemService(
                Context.TELEPHONY_SERVICE);
        telephony.listen(new PhoneStateListener() {
                             @Override
                             public void onCallStateChanged(int state, String incomingNumber) {
                                 Log.i("dtw", "[Listener]电话号码:" + incomingNumber);
                                 switch (state) {
                                     case TelephonyManager.CALL_STATE_RINGING:
                                         Log.i("dtw", "[Listener]等待接电话:" + incomingNumber);
                                         if (myApplication.isPlaying())
                                             MyApplication.isAlreadyplaying = true;
                                         else
                                             MyApplication.isAlreadyplaying = false;
                                         EventBus.getDefault().post(new PauseEvent());
                                         break;
                                     case TelephonyManager.CALL_STATE_IDLE:
                                         Log.i("dtw", "[Listener]电话挂断:" + incomingNumber);
                                         if (MyApplication.isAlreadyplaying)
                                             EventBus.getDefault().post(new ResumeEvent());
                                         break;
                                     case TelephonyManager.CALL_STATE_OFFHOOK:
                                         Log.i("dtw", "[Listener]通话中:" + incomingNumber);
                                         if (myApplication.isPlaying())
                                             MyApplication.isAlreadyplaying = true;
                                         else
                                             MyApplication.isAlreadyplaying = false;
                                         EventBus.getDefault().post(new PauseEvent());
                                         break;
                                 }
                                 super.onCallStateChanged(state, incomingNumber);
                             }
                         },
                PhoneStateListener.LISTEN_CALL_STATE);
    }

    class ProgressThread extends Thread {

        private boolean flag;

        public void setFlag(boolean flag) {
            this.flag = flag;
        }

        public void run() {
            while (flag) {
                if (myApplication.isLocalDevicesPlaying()) {
                    if (mediaPlayer.isPlaying()) {
                        PlayProgressEvent playProgressEvent = new PlayProgressEvent();
                        playProgressEvent.setLocalDevices(true);
                        playProgressEvent.setDuration(mediaPlayer.getDuration());
                        playProgressEvent.setCurrentPosition(mediaPlayer.getCurrentPosition());
                        EventBus.getDefault().post(playProgressEvent);
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, Utils.getMusicStateJson(
                                myApplication.getCurrentPlayingLamp().getMacAdress()
                        ).getBytes());
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    ;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public void onEvent(PlayEvent playEvent) {
        //接收发送
        Log.e("onEvent", "PlayEvent");
        isLocal = playEvent.isLocal();
        myApplication.setSelectMusic(true);
        if (playEvent.isLocal()) {
            LocalMusicInfo musicInfo = playEvent.getLocalMusicInfo();
            play(musicInfo.getFilePath());
            myApplication.setLocalMusicPlaying(true);
        } else {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            OnlineMusicInfo onlineMusicInfo = playEvent.getOnlineMusicInfo();
            play(onlineMusicInfo.getUrl());
            myApplication.setLocalMusicPlaying(false);
        }

    }

    public void onEvent(PauseEvent pauseEvent) {
        pause();

    }

    public void onEvent(ResumeEvent resumeEvent) {
        resume();

    }

    public void onEvent(SeekEvent seekEvent) {
        seek(seekEvent.getProgress());
    }

    public void onEvent(ProgressThreadEvent event) {
        if (event.isStart()) {
            if (!progressEnabler) {
                progressEnabler = true;

                if(null != thread)
                    thread.setFlag(false);

                thread = new ProgressThread();
                thread.setFlag(true);
                thread.start();
            }
        } else {
            progressEnabler = false;
        }
    }

    MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {

            if (MyApplication.PLAY_MODE_CYCLE == myApplication.getCurrentPlayMode()) {
                if (isLocal) {
                    myApplication.setCurrentLocalPosition(myApplication.getCurrentLocalPosition() + 1);
                } else {
                    myApplication.setCurrentOnlinePosition(myApplication.getCurrentOnlinePosition() + 1);
                }
            } else if (MyApplication.PLAY_MODE_RANDOM == myApplication.getCurrentPlayMode()) {
                if (isLocal) {
                    myApplication.setCurrentLocalPosition(new Random().nextInt(myApplication.getLocalMusicInfos().size()));
                } else {
                    myApplication.setCurrentOnlinePosition(new Random().nextInt(myApplication.getOnlineMusicInfos().size()));
                }
            }

            //comple事件是通知界面更新
            PlayCompleEvent playCompleEvent = new PlayCompleEvent();
            EventBus.getDefault().post(playCompleEvent);

            if (isLocal) {
                play(myApplication.getCurrentLocalMusicInfo().getFilePath());
            } else {
                APIClient.getMusicInfo(PlayerService.this, String.valueOf(myApplication.getCurrentOnlineMusicInfo().getSongId()), musicInfoResponseHandler);
            }

        }
    };

    private boolean play(String filePath) {
        Log.e("filePath", filePath);
        try {
            if (mediaPlayer != null) {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(filePath); // 设置数据源
                mediaPlayer.prepare();
                mediaPlayer.start();
                progressEnabler = true;

                if(null != thread)
                    thread.setFlag(false);

                thread = new ProgressThread();
                thread.setFlag(true);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            progressEnabler = false;
        }
    }

    private void resume() {
        mediaPlayer.start();
        progressEnabler = true;

        if(null != thread)
            thread.setFlag(false);

        thread = new ProgressThread();
        thread.setFlag(true);
        thread.start();
    }

    private void seek(final int progress) {
        if (myApplication.isLocalDevicesPlaying()) {
            if (mediaPlayer != null) {
                mediaPlayer.seekTo(progress);
                resume();
            }
        } else {
            new Thread() {
                @Override
                public void run() {
                    Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, Utils.getMusicSeekJson(
                            myApplication.getCurrentPlayingLamp().getMacAdress(),
                            progress
                    ).getBytes());
                }
            }.start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        progressEnabler = false;
    }

    AsyncHttpResponseHandler musicInfoResponseHandler = new AsyncHttpResponseHandler() {
        @Override
        public void onSuccess(int i, Header[] headers, byte[] bytes) {
            String jsonContent = new String(bytes);
            Log.e("onsuccess", jsonContent);
            try {
                JSONObject jsonObject = new JSONObject(jsonContent);
                JSONObject jsonSong = jsonObject.getJSONArray("songs").getJSONObject(0);
                myApplication.getCurrentOnlineMusicInfo().setSongId(jsonSong.getInt("id"));
                myApplication.getCurrentOnlineMusicInfo().setTitle(jsonSong.getString("name"));
                myApplication.getCurrentOnlineMusicInfo().setUrl(jsonSong.getString("mp3Url"));
                myApplication.getCurrentOnlineMusicInfo().setDuration(jsonSong.getInt("duration"));

                JSONObject jsonArtist = jsonSong.getJSONArray("artists").getJSONObject(0);
                myApplication.getCurrentOnlineMusicInfo().setArtist(jsonArtist.getString("name"));

                JSONObject jsonAlbum = jsonSong.getJSONObject("album");
                myApplication.getCurrentOnlineMusicInfo().setAlbum(jsonAlbum.getString("name"));
                myApplication.getCurrentOnlineMusicInfo().setArtworkUrl(jsonAlbum.getString("picUrl"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            play(myApplication.getCurrentOnlineMusicInfo().getUrl());
        }

        @Override
        public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {

        }
    };

    public void onEventMainThread(DownloadEvent event) {
        OnlineMusicInfo onlineMusicInfo = myApplication.getOnlineMusicInfos().get(event.getPosition());
        onlineMusicInfo.setDownloaded(true);
        onlineMusicInfo.save();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("下载成功")
                .setContentText(onlineMusicInfo.getArtist() + " - " + onlineMusicInfo.getAlbum());
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }
}

