package com.imt.musiclamp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.imt.musiclamp.api.APIClient;
import com.imt.musiclamp.event.FindDevicesEvent;
import com.imt.musiclamp.event.OnHitEvent;
import com.imt.musiclamp.event.PauseEvent;
import com.imt.musiclamp.event.PlayCompleEvent;
import com.imt.musiclamp.event.PlayEvent;
import com.imt.musiclamp.event.PlayProgressEvent;
import com.imt.musiclamp.event.PlayingListPlayEvent;
import com.imt.musiclamp.event.ProgressThreadEvent;
import com.imt.musiclamp.event.ResumeEvent;
import com.imt.musiclamp.event.SeekEvent;
import com.imt.musiclamp.model.Lamp;
import com.imt.musiclamp.model.OnlineMusicInfo;
import com.imt.musiclamp.utils.Utils;
import com.imt.musiclamp.view.ControlBottomSheet;
import com.imt.musiclamp.view.PlayingListBottomSheet;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import douzi.android.view.DefaultLrcBuilder;
import douzi.android.view.ILrcBuilder;
import douzi.android.view.ILrcView;
import douzi.android.view.LrcRow;
import douzi.android.view.LrcView;


public class OnlinePlayingActivity extends Activity {

    @InjectView(R.id.imageView_artwork)
    ImageView imageViewArtwork;
    @InjectView(R.id.imageView_previous)
    ImageView imageViewPrevious;
    @InjectView(R.id.imageView_play)
    ImageView imageViewPlay;
    @InjectView(R.id.imageView_next)
    ImageView imageViewNext;
    @InjectView(R.id.imageView_list)
    ImageView imageViewList;
    @InjectView(R.id.imageView_play_mode)
    ImageView imageViewPlayMode;
    @InjectView(R.id.textView_progress_current)
    TextView textViewProgressCurrent;
    @InjectView(R.id.textView_progress_duration)
    TextView textViewProgressDuration;
    @InjectView(R.id.seekBar)
    SeekBar seekBar;
    @InjectView(R.id.lrcView)
    LrcView lrcView;
    @InjectView(R.id.textView_artis)
    TextView textViewArtis;
    @InjectView(R.id.textView_title)
    TextView textViewTitle;
    @InjectView(R.id.imageView_back)
    ImageView imageViewBack;
    @InjectView(R.id.imageView_share)
    ImageView imageViewShare;

    private Bundle bundle;
    private MyApplication myApplication;
    List<Lamp> localamps = new ArrayList<>();
    adapter ad;
    private int[] playModeImages = new int[]{R.drawable.ic_playing_loop, R.drawable.ic_playing_shuffle, R.drawable.ic_playing_repeat_one};

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            }
        }
    };
    AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing3);
        myApplication = (MyApplication) getApplication();
        ButterKnife.inject(this);
        EventBus.getDefault().register(this);

        bundle = getIntent().getExtras();
        try {
            firstPlay(bundle.getInt("position"));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        ILrcBuilder builder = new DefaultLrcBuilder();
        lrcView.setListener(lrcViewListener);
        ad = new adapter();
        initView(false);
        if (!myApplication.isLocalDevicesPlaying()) {
            ((ImageView) findViewById(R.id.imageView_music_push)).setImageResource(R.drawable.devicechecked);
        }
        OnlineMusicInfo newon = myApplication.getCurrentOnlineMusicInfo();
        if (newon.isLike()) {
            ((ImageView)findViewById(R.id.redheart)).setImageResource(R.drawable.ic_playing_like_full);
        } else {
            ((ImageView)findViewById(R.id.redheart)).setImageResource(R.drawable.ic_like_outline_red);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initView(boolean isInitArtwork) {
        setPlayImage();
        setPlayModeImage();
        try {
            textViewTitle.setText(myApplication.getCurrentOnlineMusicInfo().getTitle());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        textViewArtis.setText(String.format("%s - %s", myApplication.getCurrentOnlineMusicInfo().getArtist(), myApplication.getCurrentOnlineMusicInfo().getAlbum()));
        if (isInitArtwork) {
            Glide.with(OnlinePlayingActivity.this)
                    .load(myApplication.getCurrentOnlineMusicInfo().getArtworkUrl())
                    .centerCrop()
                    .crossFade()
                    .into(imageViewArtwork);
        }

        OnlineMusicInfo newon = myApplication.getCurrentOnlineMusicInfo();
        if (newon.isLike()) {
            ((ImageView)findViewById(R.id.redheart)).setImageResource(R.drawable.ic_playing_like_full);
        } else {
            ((ImageView)findViewById(R.id.redheart)).setImageResource(R.drawable.ic_like_outline_red);
        }
    }

    private void initLrc(String lrc) {
        ILrcBuilder builder = new DefaultLrcBuilder();
        List<LrcRow> rows = builder.getLrcRows(lrc);
        lrcView.setLrc(rows);
        lrcView.setListener(lrcViewListener);
    }

    @OnClick({R.id.imageView_previous, R.id.imageView_play, R.id.imageView_next, R.id.imageView_list, R.id.imageView_play_mode, R.id.imageView_artwork,R.id.redheart,
            R.id.lrcView, R.id.imageView_music_push, R.id.imageView_back, R.id.imageView_share})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageView_previous:
                if (MyApplication.PLAY_MODE_CYCLE == myApplication.getCurrentPlayMode() || MyApplication.PLAY_MODE_REPAET == myApplication.getCurrentPlayMode()) {
                    playPrevious();
                } else if (MyApplication.PLAY_MODE_RANDOM == myApplication.getCurrentPlayMode()) {
                    playRandom();
                }
                break;
            case R.id.redheart:
                OnlineMusicInfo newon = myApplication.getCurrentOnlineMusicInfo();
                newon.setLike(!newon.isLike());
                newon.save();

                if (newon.isLike()) {
                    Toast.makeText(OnlinePlayingActivity.this, R.string.add_to_red_heart, Toast.LENGTH_SHORT).show();
                    ((ImageView)findViewById(R.id.redheart)).setImageResource(R.drawable.ic_playing_like_full);
                } else {
                    Toast.makeText(OnlinePlayingActivity.this, R.string.cancelled_red_heart, Toast.LENGTH_SHORT).show();
                    ((ImageView)findViewById(R.id.redheart)).setImageResource(R.drawable.ic_like_outline_red);
                }
                break;
            case R.id.imageView_play:
                if (myApplication.isPlaying()) {
                    ((ImageView) findViewById(R.id.imageView_play)).setImageResource(R.drawable.playicon);
                    Log.v("pause", "pause");
                    pause();
                } else {
                    ((ImageView) findViewById(R.id.imageView_play)).setImageResource(R.drawable.ic_playing_pause);
                    Log.v("resume", "resume");
                    resume();
                }
                myApplication.setPlaying(!myApplication.isPlaying());
//                setPlayImage();
                break;
            case R.id.imageView_next:
                if (MyApplication.PLAY_MODE_CYCLE == myApplication.getCurrentPlayMode() || MyApplication.PLAY_MODE_REPAET == myApplication.getCurrentPlayMode()) {
                    playNext();
                } else if (MyApplication.PLAY_MODE_RANDOM == myApplication.getCurrentPlayMode()) {
                    playRandom();
                }
                break;
            case R.id.imageView_play_mode:
                if (myApplication.getCurrentPlayMode() < 2) {
                    myApplication.setCurrentPlayMode(myApplication.getCurrentPlayMode() + 1);
                } else {
                    myApplication.setCurrentPlayMode(0);
                }
                setPlayModeImage();
                break;
            case R.id.imageView_list:
                PlayingListBottomSheet bottomSheet = new PlayingListBottomSheet(OnlinePlayingActivity.this, myApplication, false);
                bottomSheet.show();
                break;
            case R.id.imageView_artwork:
                ControlBottomSheet controlBottomSheet = new ControlBottomSheet(OnlinePlayingActivity.this, myApplication, false);
                controlBottomSheet.show();
                break;
            case R.id.lrcView:
                lrcView.setVisibility(View.GONE);
                imageViewArtwork.setVisibility(View.VISIBLE);
                break;
            case R.id.imageView_music_push:
                if (myApplication.isLocalDevicesPlaying()) {
                    final OnlineMusicInfo onlineMusicInfo = myApplication.getCurrentOnlineMusicInfo();
                    localamps = new Select().from(Lamp.class).execute();
                    Log.v("lampslength", localamps.size() + "");
                    new Thread() {
                        @Override
                        public void run() {
                            android.util.Log.e("Send UDP broadcast", "Sync");
                            Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, Utils.getFindJson().getBytes());
                        }
                    }.start();

//                final List<Lamp> lamps = new Select()
//                        .from(Lamp.class)
//                        .execute();
//                List<String> lampname = new ArrayList<>();
//                for (Lamp lamp : lamps) {
//                    lampname.add(lamp.getMacAdress());
//                }
                    if(localamps.size()==1){
                        new Thread() {
                            @Override
                            public void run() {
//                                String ip = Utils.intToIp(wifiManager.getConnectionInfo().getIpAddress());
//                                Log.e("getFilePath", "http://" + ip + ":8080" + localMusicInfo.getFilePath().replace(Environment.getExternalStorageDirectory().getPath(), ""));
                                Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, Utils.getPlayMusicJson(
                                        localamps.get(0).getMacAdress(),
                                        "online",
                                        (int) onlineMusicInfo.getSongId(),
                                        onlineMusicInfo.getUrl(),
                                        onlineMusicInfo.getTitle(),
                                        onlineMusicInfo.getArtist(),
                                        onlineMusicInfo.getAlbum()
                                ).getBytes());
                                EventBus.getDefault().post(new PauseEvent());
                            }
                        }.start();
                        ((ImageView) findViewById(R.id.imageView_music_push)).setImageResource(R.drawable.devicechecked);
                        pause();
                        startProgressThread();
                        initView(true);
                        MyApplication.playingLamp = localamps.get(0);
                        myApplication.setCurrentPlayingLamp(localamps.get(0));
                        myApplication.setLocalDevicesPlaying(false);
                    }
                    else {
                        builder = new AlertDialog.Builder(OnlinePlayingActivity.this);
                        builder.setTitle("选择本地设备");
//                    builder.setPositiveButton("本地播放", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            EventBus.getDefault().post(new ResumeEvent());
//                            final JSONObject jsonObject = new JSONObject();
//                            try {
//                                jsonObject.put("action", "music_control");
//                                jsonObject.put("MAC", MyApplication.playingLamp.getMacAdress());
//                                jsonObject.put("type", "pause");
//                                jsonObject.put("position", -1);
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                            new Thread(new Runnable() {
//                                @Override
//                                public void run() {
//
//                                    Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, jsonObject.toString().getBytes());
//                                }
//                            }).start();
//                        }
//                    });
                        builder.setAdapter(ad, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, final int which) {

                                new Thread() {
                                    @Override
                                    public void run() {
//                                String ip = Utils.intToIp(wifiManager.getConnectionInfo().getIpAddress());
//                                Log.e("getFilePath", "http://" + ip + ":8080" + localMusicInfo.getFilePath().replace(Environment.getExternalStorageDirectory().getPath(), ""));
                                        Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, Utils.getPlayMusicJson(
                                                localamps.get(which).getMacAdress(),
                                                "online",
                                                (int) onlineMusicInfo.getSongId(),
                                                onlineMusicInfo.getUrl(),
                                                onlineMusicInfo.getTitle(),
                                                onlineMusicInfo.getArtist(),
                                                onlineMusicInfo.getAlbum()
                                        ).getBytes());
                                        EventBus.getDefault().post(new PauseEvent());
                                    }
                                }.start();
                                ((ImageView) findViewById(R.id.imageView_music_push)).setImageResource(R.drawable.devicechecked);
                                pause();
                                startProgressThread();
                                initView(true);
                                MyApplication.playingLamp = localamps.get(which);
                                myApplication.setCurrentPlayingLamp(localamps.get(which));
                                myApplication.setLocalDevicesPlaying(false);
                            }
                        });
                        if (localamps.size() < 1) {
                        }
                        builder.show();
                    }
                } else {
                    MaterialDialog dialog = new MaterialDialog.Builder(OnlinePlayingActivity.this)
                            .content("是否切换回本地播放")
                            .positiveText("切换")
                            .negativeText(R.string.cancel)
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    initView(true);
                                    myApplication.setLocalDevicesPlaying(true);
//                                    firstPlay(myApplication.getCurrentOnlinePosition());
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            pauseRemoteDevices();
                                        }
                                    }.start();
                                    ((ImageView) findViewById(R.id.imageView_music_push)).setImageResource(R.drawable.deviceunchecke);
                                    play();
                                }
                            })
                            .build();
                    dialog.show();
                }
                break;
            case R.id.imageView_back:
                finish();
                break;
            case R.id.imageView_share:

                break;
        }
    }

    class adapter extends BaseAdapter {

        @Override
        public int getCount() {
            return localamps.size();
        }

        @Override
        public Object getItem(int position) {
            return localamps.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view=View.inflate(OnlinePlayingActivity.this,R.layout.chousedevice,null);

            TextView textView = (TextView)view.findViewById(R.id.devicemac);
            textView.setText(localamps.get(position).getMacAdress());
            return view;
        }
    }

    public void onEvent(PlayProgressEvent playProgressEvent) {
        seekBar.setOnSeekBarChangeListener(null);
        seekBar.setMax(playProgressEvent.getDuration());
        seekBar.setProgress(playProgressEvent.getCurrentPosition());
        seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        final int minute = playProgressEvent.getDuration() / 1000 / 60;
        final int second = (playProgressEvent.getDuration() / 1000) % 60;
        final int currentMinute = playProgressEvent.getCurrentPosition() / 1000 / 60;
        final int currentSecond = (playProgressEvent.getCurrentPosition() / 1000) % 60;
        final int currentPosition = playProgressEvent.getCurrentPosition();
        handler.post(new Runnable() {
            @Override
            public void run() {
                textViewProgressCurrent.setText(String.format("%02d:%02d", currentMinute, currentSecond));
                textViewProgressDuration.setText(String.format("%02d:%02d", minute, second));
                lrcView.seekLrcToTime(currentPosition);
            }
        });
    }

    public void onEvent(PlayingListPlayEvent playingListPlayEvent) {
        myApplication.setCurrentOnlinePosition(playingListPlayEvent.getPosition());
        APIClient.getMusicInfo(OnlinePlayingActivity.this, String.valueOf(myApplication.getCurrentOnlineMusicInfo().getSongId()), musicInfoResponseHandler);
        APIClient.getMusicLrc(OnlinePlayingActivity.this, String.valueOf(myApplication.getCurrentOnlineMusicInfo().getSongId()), lrcResponseHandler);
        initView(false);
    }

    public void onEvent(PlayCompleEvent playCompleEvent) {
//        myApplication.setCurrentOnlinePosition(playCompleEvent.getPosition());
        APIClient.getMusicInfo(OnlinePlayingActivity.this, String.valueOf(myApplication.getCurrentOnlineMusicInfo().getSongId()), musicInfoResponseHandler);
        APIClient.getMusicLrc(OnlinePlayingActivity.this, String.valueOf(myApplication.getCurrentOnlineMusicInfo().getSongId()), lrcResponseHandler);
        initView(false);
    }

    SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            PauseEvent pauseEvent = new PauseEvent();
            EventBus.getDefault().post(pauseEvent);
        }

        @Override
        public void onStopTrackingTouch(final SeekBar seekBar) {
            if (myApplication.isLocalDevicesPlaying()) {
                SeekEvent seekEvent = new SeekEvent();
                seekEvent.setProgress(seekBar.getProgress());
                EventBus.getDefault().post(seekEvent);
                myApplication.setPlaying(true);
            } else {
                new Thread() {
                    @Override
                    public void run() {
                        Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, Utils.getMusicSeekJson(
                                myApplication.getCurrentPlayingLamp().getMacAdress(),
                                seekBar.getProgress()
                        ).getBytes());
                    }
                }.start();
            }
        }
    };

    ILrcView.LrcViewListener lrcViewListener = new ILrcView.LrcViewListener() {
        @Override
        public void onLrcSeeked(int newPosition, LrcRow row) {
            SeekEvent seekEvent = new SeekEvent();
            seekEvent.setProgress((int) row.time);
            EventBus.getDefault().post(seekEvent);
            myApplication.setPlaying(true);
            setPlayImage();
        }
    };


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
                myApplication.getCurrentOnlineMusicInfo().setArtworkUrl(jsonAlbum.getString("picUrl") + "?param=400y400");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            play();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    initView(true);
                }
            });
        }

        @Override
        public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {

        }
    };

    public void onEventMainThread(OnHitEvent onHitEvent){
                        initView(true);
                        myApplication.setLocalDevicesPlaying(true);
//                                    firstPlay(myApplication.getCurrentOnlinePosition());
//                        new Thread() {
//                            @Override
//                            public void run() {
//                                pauseRemoteDevices();
//                            }
//                        }.start();
                        ((ImageView) findViewById(R.id.imageView_music_push)).setImageResource(R.drawable.deviceunchecke);
//                        play();
    }

    public void onEventMainThread(FindDevicesEvent findDevicesEvent) {
        android.util.Log.e("onEvent", "newevent");
//        lamp = new Lamp("Lamp", findDevicesEvent.getMacAddress(), findDevicesEvent.getIp());
//        if (localamps.size() < 1) {
//            localamps.add(lamp);
//            ad.notifyDataSetChanged();
//        } else {
//            int i=0;
//            for (i = 0; i < localamps.size(); i++) {
//                if (localamps.get(i).getMacAdress().equals(lamp.getMacAdress())) {
//                    break;
//                }
//            }
//            if(i==localamps.size()) {
//                localamps.add(lamp);
//                ad.notifyDataSetChanged();
//            }
//        }

        Lamp lamp = new Select()
                .from(Lamp.class)
                .where("MacAddress = ?", findDevicesEvent.getMacAddress())
                .executeSingle();
        if (lamp == null) {
            //不存在 创建
            lamp = new Lamp(findDevicesEvent.getMacAddress(), findDevicesEvent.getMacAddress(), findDevicesEvent.getIp());
            lamp.save();
//            localamps.add(lamp);
        } else {
            //存在 更新ip
            lamp.setIp(findDevicesEvent.getIp());
            lamp.setOnline(true);
            lamp.save();
        }
        localamps = new Select().from(Lamp.class).execute();
        ad.notifyDataSetChanged();
    }

    AsyncHttpResponseHandler lrcResponseHandler = new AsyncHttpResponseHandler() {
        @Override
        public void onSuccess(int i, Header[] headers, byte[] bytes) {
            Log.e("onSuccess", new String(bytes));
            try {
                JSONObject jsonObject = new JSONObject(new String(bytes));
                String lrc = jsonObject.getString("lyric");
                myApplication.getCurrentOnlineMusicInfo().setLrc(lrc);
                initLrc(lrc);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {

            //会抛nullpinpinterexception
//         Log.e("onFailure", new String(bytes));
        }
    };

    private void firstPlay(int position) {
//        if (myApplication.isLocalMusicPlaying()) {
//            myApplication.setCurrentOnlinePosition(position);
//            APIClient.getMusicInfo(OnlinePlayingActivity.this, String.valueOf(myApplication.getCurrentOnlineMusicInfo().getSongId()), musicInfoResponseHandler);
//            APIClient.getMusicLrc(OnlinePlayingActivity.this, String.valueOf(myApplication.getCurrentOnlineMusicInfo().getSongId()), lrcResponseHandler);
//            return;
//        }

        if (myApplication.getCurrentOnlinePosition() != position || myApplication.isLocalMusicPlaying()) {
            myApplication.setCurrentOnlinePosition(position);
            APIClient.getMusicInfo(OnlinePlayingActivity.this, String.valueOf(myApplication.getCurrentOnlineMusicInfo().getSongId()), musicInfoResponseHandler);
            APIClient.getMusicLrc(OnlinePlayingActivity.this, String.valueOf(myApplication.getCurrentOnlineMusicInfo().getSongId()), lrcResponseHandler);
        } else {
            initView(true);
            initLrc(myApplication.getCurrentOnlineMusicInfo().getLrc());
        }
    }

    private void play() {
        if (myApplication.isLocalDevicesPlaying()) {
            PlayEvent playEvent = new PlayEvent();
            playEvent.setSongId(myApplication.getCurrentOnlineMusicInfo().getSongId());
            playEvent.setLocal(false);
            playEvent.setOnlineMusicInfo(myApplication.getCurrentOnlineMusicInfo());
            EventBus.getDefault().post(playEvent);
            myApplication.setPlaying(true);
            myApplication.setCurrentSongId(myApplication.getCurrentOnlineMusicInfo().getSongId());
            myApplication.getCurrentOnlineMusicInfo().setRecently(true);
            myApplication.getCurrentOnlineMusicInfo().save();
        } else {
            new Thread() {
                @Override
                public void run() {
                    pauseRemoteDevices();
                    startProgressThread();
                    Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, Utils.getPlayMusicJson(
                            myApplication.getCurrentPlayingLamp().getMacAdress(),
                            "online",
                            (int) myApplication.getCurrentOnlineMusicInfo().getSongId(),
                            myApplication.getCurrentOnlineMusicInfo().getUrl(),
                            myApplication.getCurrentOnlineMusicInfo().getTitle(),
                            myApplication.getCurrentOnlineMusicInfo().getArtist(),
                            myApplication.getCurrentOnlineMusicInfo().getAlbum()
                    ).getBytes());

                }
            }.start();
        }
        myApplication.setLocalMusicPlaying(false);
        myApplication.setProgressEnabler(true);
    }

    private void playPrevious() {
        //越界
        if (0 == myApplication.getCurrentOnlinePosition()) {
            myApplication.setCurrentOnlinePosition(myApplication.getOnlineMusicInfos().size() - 1);
        } else {
            myApplication.setCurrentOnlinePosition(myApplication.getCurrentOnlinePosition() - 1);
        }
        APIClient.getMusicInfo(OnlinePlayingActivity.this, String.valueOf(myApplication.getCurrentOnlineMusicInfo().getSongId()), musicInfoResponseHandler);
        APIClient.getMusicLrc(OnlinePlayingActivity.this, String.valueOf(myApplication.getCurrentOnlineMusicInfo().getSongId()), lrcResponseHandler);
        initView(false);
    }

    private void playNext() {
        if (myApplication.getOnlineMusicInfos().size() - 1 == myApplication.getCurrentOnlinePosition()) {
            myApplication.setCurrentOnlinePosition(0);
        } else {
            myApplication.setCurrentOnlinePosition(myApplication.getCurrentOnlinePosition() + 1);
        }

        APIClient.getMusicInfo(OnlinePlayingActivity.this, String.valueOf(myApplication.getCurrentOnlineMusicInfo().getSongId()), musicInfoResponseHandler);
        APIClient.getMusicLrc(OnlinePlayingActivity.this, String.valueOf(myApplication.getCurrentOnlineMusicInfo().getSongId()), lrcResponseHandler);
        initView(false);

    }

    private void playRandom() {
        myApplication.setCurrentOnlinePosition(new Random().nextInt(myApplication.getOnlineMusicInfos().size()));
        APIClient.getMusicInfo(OnlinePlayingActivity.this, String.valueOf(myApplication.getCurrentOnlineMusicInfo().getSongId()), musicInfoResponseHandler);
        APIClient.getMusicLrc(OnlinePlayingActivity.this, String.valueOf(myApplication.getCurrentOnlineMusicInfo().getSongId()), lrcResponseHandler);
        initView(false);
    }

    private void playRepeat() {
        play();
    }

    private void pause() {
        if (myApplication.isLocalDevicesPlaying()) {
            PauseEvent pauseEvent = new PauseEvent();
            EventBus.getDefault().post(pauseEvent);
        } else {
            new Thread() {
                @Override
                public void run() {
                    pauseRemoteDevices();
                    stopProgressThread();
                }
            }.start();
        }
    }

    private void resume() {
        if (myApplication.isLocalDevicesPlaying()) {
            ResumeEvent resumeEvent = new ResumeEvent();
            EventBus.getDefault().post(resumeEvent);
        } else {
            new Thread() {
                @Override
                public void run() {
                    resumeRemoteDevices();
                    startProgressThread();
                }
            }.start();
        }
    }

    private void pauseRemoteDevices() {
        Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, Utils.getMusicPauseJson(
                myApplication.getCurrentPlayingLamp().getMacAdress()
        ).getBytes());

    }

    private void resumeRemoteDevices() {
        Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, Utils.getMusicResumeJson(
                myApplication.getCurrentPlayingLamp().getMacAdress()
        ).getBytes());
    }

    private void startProgressThread() {
        ProgressThreadEvent event = new ProgressThreadEvent();
        event.setStart(true);
        EventBus.getDefault().post(event);
    }

    private void stopProgressThread() {
        ProgressThreadEvent event = new ProgressThreadEvent();
        event.setStart(false);
        EventBus.getDefault().post(event);
    }

    private void setPlayImage() {
        if (myApplication.isPlaying()) {
            imageViewPlay.setImageResource(R.drawable.ic_playing_pause);
        } else {
            imageViewPlay.setImageResource(R.drawable.ic_playing_pause);
        }
    }

    private void setPlayModeImage() {
        imageViewPlayMode.setImageResource(playModeImages[myApplication.getCurrentPlayMode()]);
    }

}
