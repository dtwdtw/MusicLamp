package com.imt.musiclamp.view;

import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.afollestad.materialdialogs.MaterialDialog;
import com.imt.musiclamp.MyApplication;
import com.imt.musiclamp.R;
import com.imt.musiclamp.model.CustomPlayList;
import com.imt.musiclamp.model.OnlineMusicInfo;
import com.imt.musiclamp.utils.Utils;

import java.util.List;


public class ControlBottomSheet extends Dialog implements DialogInterface {

    private Context context;
    private MyApplication myApplication;
    private boolean isLocal;

    public ControlBottomSheet(Context context, MyApplication myApplication, boolean isLocal) {
        super(context, com.cocosw.bottomsheet.R.style.BottomSheet_Dialog);
        this.context = context;
        this.myApplication = myApplication;
        this.isLocal = isLocal;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setCanceledOnTouchOutside(true);
        final View sheetView = LayoutInflater.from(context).inflate(R.layout.sheet_playing_control, null, false);
        if (myApplication.isLocalDevicesPlaying()) {
            AudioManager mAudioManager = (AudioManager) myApplication.getSystemService(Context.AUDIO_SERVICE);
            ((SeekBar) sheetView.findViewById(R.id.seekbarvolum)).setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * 100 / 15);
        }

        OnlineMusicInfo onlineMusicInfo = myApplication.getCurrentOnlineMusicInfo();

        if (onlineMusicInfo.isLike()) {
            ((ImageView) sheetView.findViewById(R.id.imageView_like)).setImageResource(R.drawable.ic_playing_like_full);
        } else {
            ((ImageView) sheetView.findViewById(R.id.imageView_like)).setImageResource(R.drawable.ic_like_outline_red);
        }

        sheetView.findViewById(R.id.imageView_like).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnlineMusicInfo onlineMusicInfo = myApplication.getCurrentOnlineMusicInfo();
                onlineMusicInfo.setLike(!onlineMusicInfo.isLike());
                onlineMusicInfo.save();
                if (onlineMusicInfo.isLike()) {
                    Toast.makeText(context, R.string.add_to_red_heart, Toast.LENGTH_SHORT).show();
                    ((ImageView) sheetView.findViewById(R.id.imageView_like)).setImageResource(R.drawable.ic_playing_like_full);
                } else {
                    Toast.makeText(context, R.string.cancelled_red_heart, Toast.LENGTH_SHORT).show();
                    ((ImageView) sheetView.findViewById(R.id.imageView_like)).setImageResource(R.drawable.ic_like_outline_red);
                }
            }
        });

        sheetView.findViewById(R.id.imageView_download).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnlineMusicInfo onlineMusicInfo = myApplication.getCurrentOnlineMusicInfo();
                DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(onlineMusicInfo.getUrl()))
                        .setDestinationInExternalPublicDir("IMT_MUSIC", String.format("%s - %s.mp3", onlineMusicInfo.getArtist(), onlineMusicInfo.getTitle()))
                        .setTitle(context.getResources().getString(R.string.downloading) + " - " + onlineMusicInfo.getTitle())
                        .setDescription(onlineMusicInfo.getTitle());
                long downloadId = downloadManager.enqueue(request);
//                myApplication.getCurrentOnlineMusicInfo().setDownloaded(true);
//                myApplication.getCurrentOnlineMusicInfo().save();
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                preferences.edit().putInt(String.valueOf(downloadId), myApplication.getCurrentOnlinePosition()).commit();

//                List<Lamp> lamps = new Select()
//                        .from(Lamp.class)
//                        .where("selected = ?", true)
//                        .execute();
//                final OnlineMusicInfo onlineMusicInfo = myApplication.getCurrentOnlineMusicInfo();
//                for (final Lamp lamp1 : lamps) {
//                    new Thread() {
//                        @Override
//                        public void run() {
//                            Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, Utils.getPlayMusicJson(
//                                    lamp1.getMacAdress(),
//                                    "online",
//                                    onlineMusicInfo.getSongId(),
//                                    onlineMusicInfo.getUrl(),
//                                    onlineMusicInfo.getTitle(),
//                                    onlineMusicInfo.getArtist(),
//                                    onlineMusicInfo.getAlbum()
//                            ).getBytes());
//                        }
//                    }.start();
//                }

            }
        });

        sheetView.findViewById(R.id.imageView_add).setOnClickListener(new View.OnClickListener() {

            OnlineMusicInfo onlineMusicInfo;

            @Override
            public void onClick(View v) {
                final List<CustomPlayList> playLists = new Select().from(CustomPlayList.class).execute();
                String[] nameArray = new String[playLists.size()];
                for (int i = 0; i < playLists.size(); i++) {
                    nameArray[i] = playLists.get(i).getName();
                }
                MaterialDialog dialog = new MaterialDialog.Builder(context)
                        .title(R.string.settings)
                        .items(nameArray)
                        .positiveText(R.string.add)
                        .negativeText(R.string.cancel)
                        .itemsCallbackSingleChoice(2, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                onlineMusicInfo = myApplication.getCurrentOnlineMusicInfo();
                                onlineMusicInfo.setCustomPlaylistId(playLists.get(which).getId().intValue());
                                onlineMusicInfo.save();

                                playLists.get(which).setImgUrl(onlineMusicInfo.getArtworkUrl());
                                playLists.get(which).save();

                                Toast.makeText(context, context.getResources().getString(R.string.add_to_play_list) + text, Toast.LENGTH_SHORT).show();
                                return false;
                            }
                        }).build();
                dialog.show();
            }
        });

        sheetView.findViewById(R.id.button_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ControlBottomSheet.this.dismiss();
            }
        });

        ((SeekBar) sheetView.findViewById(R.id.seekbarvolum)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
                if (MyApplication.playingLamp != null) {
                    new Thread() {
                        @Override
                        public void run() {
                            Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, Utils.getSetVolumeJson(MyApplication.playingLamp.getMacAdress(), "set", progress).getBytes());
                        }
                    }.start();
                } else {
                    AudioManager mAudioManager = (AudioManager) myApplication.getSystemService(Context.AUDIO_SERVICE);
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress * 15 / 100, 0); //tempVolume:音量绝对值
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        setContentView(sheetView);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM;

        TypedArray a = getContext().obtainStyledAttributes(new int[]{android.R.attr.layout_width});
        try {
            params.width = a.getLayoutDimension(0, ViewGroup.LayoutParams.MATCH_PARENT);
        } finally {
            a.recycle();
        }

        getWindow().setAttributes(params);
    }
}
