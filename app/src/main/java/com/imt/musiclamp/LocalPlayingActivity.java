package com.imt.musiclamp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;


import com.activeandroid.query.Select;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.imt.musiclamp.event.FindDevicesEvent;
import com.imt.musiclamp.event.PauseEvent;
import com.imt.musiclamp.event.PlayCompleEvent;
import com.imt.musiclamp.event.PlayEvent;
import com.imt.musiclamp.event.PlayProgressEvent;
import com.imt.musiclamp.event.PlayingListPlayEvent;
import com.imt.musiclamp.event.ProgressThreadEvent;
import com.imt.musiclamp.event.ResumeEvent;
import com.imt.musiclamp.event.SeekEvent;
import com.imt.musiclamp.model.Lamp;
import com.imt.musiclamp.model.LocalControlBottonSheet;
import com.imt.musiclamp.model.LocalMusicInfo;
import com.imt.musiclamp.utils.Utils;
import com.imt.musiclamp.view.ControlBottomSheet;
import com.imt.musiclamp.view.PlayingListBottomSheet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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


public class LocalPlayingActivity extends Activity {

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

    private Bitmap bitmapArtwork;

    private WifiManager wifiManager;

    AlertDialog.Builder builder;

    private static final int UPDATE_PROGRESS = 1;
    private static final int UPDATE_ARTWORK = 2;
    private static final int UPDATE_ARTWORK_BLUR = 3;

    private int[] playModeImages = new int[]{R.drawable.ic_playing_loop, R.drawable.ic_playing_shuffle, R.drawable.ic_playing_repeat_one};

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_PROGRESS:

                    break;
                case UPDATE_ARTWORK:
                    imageViewArtwork.setImageBitmap(bitmapArtwork);
                    break;
                case UPDATE_ARTWORK_BLUR:
                    break;
            }
        }
    };
    adapter ad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing3);
        myApplication = (MyApplication) getApplication();
        ButterKnife.inject(this);
        EventBus.getDefault().register(this);
        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);

        bundle = getIntent().getExtras();
//        myApplication.setCurrentLocalPosition(bundle.getInt("position"));
        firstPlay(bundle.getInt("position"));

        seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);

        String lrc = getFromAssets("apologize.lrc");
        ILrcBuilder builder = new DefaultLrcBuilder();
        List<LrcRow> rows = builder.getLrcRows(lrc);
        lrcView.setLrc(rows);
        lrcView.setListener(lrcViewListener);
        ad = new adapter();

        if (!myApplication.isLocalDevicesPlaying()) {

            ((ImageView) findViewById(R.id.imageView_music_push)).setImageResource(R.drawable.devicechecked);
        }
        findViewById(R.id.redheart).setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_playing, menu);
//        menu.findItem(R.id.action_playing_list).setActionView(R.layout.menu_item_playing_list);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_playing_list:
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initView() {
        setPlayImage();
        setPlayModeImage();
        if (myApplication.getCurrentLocalMusicInfo().isDownloaded()){
            Glide.with(LocalPlayingActivity.this)
                    .load(myApplication.getCurrentLocalMusicInfo().getAlbumUrl())
                    .centerCrop()
                    .placeholder(R.drawable.ic_audiotrack)
                    .crossFade()
                    .into(imageViewArtwork);
        }else {
            new Thread() {
                @Override
                public void run() {
                    try {
                        Uri imageUri = Utils.getArtworkUri(myApplication.getCurrentLocalMusicInfo().getAlbumId());
                        bitmapArtwork = MediaStore.Images.Media.getBitmap(LocalPlayingActivity.this.getContentResolver(), imageUri);
                        handler.sendEmptyMessage(UPDATE_ARTWORK);
//                    bitmapBlur = BlurKit.blurNatively(bitmapArtwork, 200, false);
//                    handler.sendEmptyMessage(UPDATE_ARTWORK_BLUR);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }

        textViewTitle.setText(myApplication.getCurrentLocalMusicInfo().getTitle());
        textViewArtis.setText(String.format("%s - %s", myApplication.getCurrentLocalMusicInfo().getArtist(), myApplication.getCurrentLocalMusicInfo().getAlbum()));
    }

    @OnClick({R.id.imageView_previous, R.id.imageView_play, R.id.imageView_next, R.id.imageView_list, R.id.imageView_play_mode, R.id.imageView_artwork, R.id.lrcView, R.id.imageView_music_push, R.id.imageView_back, R.id.imageView_share})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageView_previous:
                playPrevious();
                initView();
                break;
            case R.id.imageView_play:
                if (myApplication.isPlaying()) {
                    ((ImageView) findViewById(R.id.imageView_play)).setImageResource(R.drawable.playicon);
                    pause();
                } else {
                    ((ImageView) findViewById(R.id.imageView_play)).setImageResource(R.drawable.ic_playing_pause);
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
                initView();
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
                PlayingListBottomSheet bottomSheet = new PlayingListBottomSheet(LocalPlayingActivity.this, myApplication, true);
                bottomSheet.show();
                break;
//            case R.id.imageView_artwork:
//                imageViewArtwork.setVisibility(View.GONE);
//                lrcView.setVisibility(View.VISIBLE);
//                break;
            case R.id.imageView_artwork:
                LocalControlBottonSheet controlBottomSheet = new LocalControlBottonSheet(LocalPlayingActivity.this, myApplication, false);
                controlBottomSheet.show();
                break;
            case R.id.lrcView:
                lrcView.setVisibility(View.GONE);
                imageViewArtwork.setVisibility(View.VISIBLE);
                break;
            case R.id.imageView_music_push:
                if (myApplication.isLocalDevicesPlaying()) {

                    final LocalMusicInfo localMusicInfo = myApplication.getCurrentLocalMusicInfo();
                    localamps = new Select().from(Lamp.class).execute();
                    Log.v("lampslength", localamps.size() + "");
                    new Thread() {
                        @Override
                        public void run() {
                            android.util.Log.e("Send UDP broadcast", "Sync");
                            Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, Utils.getFindJson().getBytes());
                        }
                    }.start();
                    if(localamps.size()==1){
                        new Thread() {
                            @Override
                            public void run() {
                                String ip = Utils.intToIp(wifiManager.getConnectionInfo().getIpAddress());
                                String url = "http://" + ip + ":8080" + localMusicInfo.getFilePath().replace(Environment.getExternalStorageDirectory().getPath(), "");
                                Log.e("url", url);
                                Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, Utils.getPlayMusicJson(
                                        localamps.get(0).getMacAdress(),
                                        "local",
                                        (int) localMusicInfo.get_ID(),
                                        "http://" + ip + ":8080" + localMusicInfo.getFilePath().replace(Environment.getExternalStorageDirectory().getPath(), ""),
                                        localMusicInfo.getTitle(),
                                        localMusicInfo.getArtist(),
                                        localMusicInfo.getAlbum()
                                ).getBytes());
                            }
                        }.start();
                        pause();
                        ((ImageView) findViewById(R.id.imageView_music_push)).setImageResource(R.drawable.devicechecked);
                        startProgressThread();
                        initView();
                        MyApplication.playingLamp = localamps.get(0);
                        myApplication.setCurrentPlayingLamp(localamps.get(0));
                        myApplication.setLocalDevicesPlaying(false);
                    }
                    else {

                        builder = new AlertDialog.Builder(LocalPlayingActivity.this);
                        builder.setTitle(getResources().getString(R.string.select_device));
//                    builder
//                            .setPositiveButton("本地播放", new DialogInterface.OnClickListener() {
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
                                        String ip = Utils.intToIp(wifiManager.getConnectionInfo().getIpAddress());
                                        String url = "http://" + ip + ":8080" + localMusicInfo.getFilePath().replace(Environment.getExternalStorageDirectory().getPath(), "");
                                        Log.e("url", url);
                                        Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, Utils.getPlayMusicJson(
                                                localamps.get(which).getMacAdress(),
                                                "local",
                                                (int) localMusicInfo.get_ID(),
                                                "http://" + ip + ":8080" + localMusicInfo.getFilePath().replace(Environment.getExternalStorageDirectory().getPath(), ""),
                                                localMusicInfo.getTitle(),
                                                localMusicInfo.getArtist(),
                                                localMusicInfo.getAlbum()
                                        ).getBytes());
                                    }
                                }.start();
                                pause();
                                ((ImageView) findViewById(R.id.imageView_music_push)).setImageResource(R.drawable.devicechecked);
                                startProgressThread();
                                initView();
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
                    MaterialDialog dialog = new MaterialDialog.Builder(LocalPlayingActivity.this)
                            .content("是否切换回本地播放")
                            .positiveText("切换")
                            .negativeText(R.string.cancel)
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    initView();
                                    myApplication.setLocalDevicesPlaying(true);
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
            TextView textView = new TextView(LocalPlayingActivity.this);
            textView.setText(localamps.get(position).getMacAdress());
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 28);
            return textView;
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
        firstPlay(playingListPlayEvent.getPosition());
    }

    public void onEventMainThread(PlayCompleEvent playCompleEvent) {
//        myApplication.setCurrentLocalPosition(playCompleEvent.getPosition());
        initView();
    }

    List<Lamp> localamps = new ArrayList<>();
    Lamp lamp;

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
            localamps = new Select().from(Lamp.class).execute();
            ad.notifyDataSetChanged();
        } else {
            //存在 更新ip
            lamp.setIp(findDevicesEvent.getIp());
            lamp.setOnline(true);
            lamp.save();
        }
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
                //            setPlayImage();
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

    private void firstPlay(int position) {
//        if (myApplication.isLocalMusicPlaying()) {
//            myApplication.setCurrentLocalPosition(position);
//            initView();
//            play();
//            return;
//        }

        if (myApplication.getCurrentLocalPosition() != position || !myApplication.isLocalMusicPlaying()) {
            myApplication.setCurrentLocalPosition(position);
            play();
        }
        initView();
    }

    private void play() {
        if (myApplication.isLocalDevicesPlaying()) {
            PlayEvent playEvent = new PlayEvent();
            playEvent.setLocal(true);
            playEvent.setPosition(myApplication.getCurrentLocalPosition());
            playEvent.setLocalMusicInfo(myApplication.getCurrentLocalMusicInfo());
            EventBus.getDefault().post(playEvent);
            myApplication.setPlaying(true);
        } else {
            new Thread() {
                @Override
                public void run() {
                    pauseRemoteDevices();
                    startProgressThread();
                    String ip = Utils.intToIp(wifiManager.getConnectionInfo().getIpAddress());
                    Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, Utils.getPlayMusicJson(
                            myApplication.getCurrentPlayingLamp().getMacAdress(),
                            "local",
                            (int) myApplication.getCurrentLocalMusicInfo().get_ID(),
                            "http://" + ip + ":8080" + myApplication.getCurrentLocalMusicInfo().getFilePath().replace(Environment.getExternalStorageDirectory().getPath(), ""),
                            myApplication.getCurrentLocalMusicInfo().getTitle(),
                            myApplication.getCurrentLocalMusicInfo().getArtist(),
                            myApplication.getCurrentLocalMusicInfo().getAlbum()
                    ).getBytes());
                }
            }.start();
        }
        myApplication.setLocalMusicPlaying(true);
        myApplication.setProgressEnabler(true);
    }

    private void playPrevious() {
        //越界
        if (0 == myApplication.getCurrentLocalPosition()) {
            myApplication.setCurrentLocalPosition(myApplication.getLocalMusicInfos().size() - 1);
        } else {
            myApplication.setCurrentLocalPosition(myApplication.getCurrentLocalPosition() - 1);
        }
        play();
    }

    private void playNext() {
        if (myApplication.getLocalMusicInfos().size() - 1 == myApplication.getCurrentLocalPosition()) {
            myApplication.setCurrentLocalPosition(0);
        } else {
            myApplication.setCurrentLocalPosition(myApplication.getCurrentLocalPosition() + 1);
        }
        play();
    }

    private void playRandom() {
        myApplication.setCurrentLocalPosition(new Random().nextInt(myApplication.getLocalMusicInfos().size()));
        play();
    }

    private void playRepeat() {
        myApplication.setCurrentLocalPosition(myApplication.getCurrentOnlinePosition());
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

    public String getFromAssets(String fileName) {
        try {
            InputStreamReader inputReader = new InputStreamReader(
                    getResources().getAssets().open(fileName));
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line = "";
            String Result = "";
            while ((line = bufReader.readLine()) != null) {
                if (line.trim().equals(""))
                    continue;
                Result += line + "\r\n";
            }
            return Result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

}
