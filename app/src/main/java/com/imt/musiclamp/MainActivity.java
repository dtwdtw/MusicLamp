package com.imt.musiclamp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.imt.musiclamp.event.PlayCompleEvent;
import com.imt.musiclamp.fragment.ColorPickerFragment;
import com.imt.musiclamp.fragment.IndexFragment;
import com.imt.musiclamp.fragment.LeftFragment;
import com.imt.musiclamp.model.Lamp;
import com.imt.musiclamp.utils.Utils;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

import java.io.IOException;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;


public class MainActivity extends SlidingFragmentActivity {

    @InjectView(R.id.imageView_circle_artwork)
    ImageView imageViewCircleArtwork;

    private MyApplication myApplication;
    private LeftFragment leftFragment;

    private Bitmap bitmapArtwork;
    private IndexFragment indexFragment;
    private ColorPickerFragment colorPickerFragment;

    private TextView music_text, device_text;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_ARTWORK:
                    imageViewCircleArtwork.setImageBitmap(bitmapArtwork);
                    break;
            }
        }
    };

    private static final int UPDATE_ARTWORK = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myApplication = (MyApplication) getApplication();
        initSlidingMenu(savedInstanceState);
        ButterKnife.inject(this);
        indexFragment = new IndexFragment();
        colorPickerFragment = new ColorPickerFragment();

        music_text = (TextView) this.findViewById(R.id.music_text);

        device_text = (TextView) this.findViewById(R.id.device_text);

        List<Lamp> lamps = new Select()
                .from(Lamp.class)
                .execute();
        for (final Lamp lamp1 : lamps) {
            lamp1.delete();
        }
        EventBus.getDefault().register(this);
    }

    @OnClick({R.id.layout_dock_lamp, R.id.layout_dock_music, R.id.layout_progress})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.layout_dock_music:
//                if (!indexFragment.isAdded()) {
//                    getSupportFragmentManager()
//                            .beginTransaction()
//                            .hide(colorPickerFragment).add(R.id.content_frame, indexFragment, "index")
//                            .commit();
//                } else {
//                    getSupportFragmentManager()
//                            .beginTransaction()
//                            .hide(colorPickerFragment).show(indexFragment)
//                            .commit();
//                }
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.content_frame, new IndexFragment())
                        .commit();
                ((ImageView)findViewById(R.id.music)).setImageResource(R.drawable.musicchouse);
                ((ImageView)findViewById(R.id.devicebutton)).setImageResource(R.drawable.ic_devices);

                music_text.setTextColor(getResources().getColor(R.color.purple_text));
                device_text.setTextColor(getResources().getColor(R.color.white));
                break;
            case R.id.layout_dock_lamp:
//                if (!colorPickerFragment.isAdded()) {
//                    getSupportFragmentManager()
//                            .beginTransaction()
//                            .hide(indexFragment).add(R.id.content_frame, colorPickerFragment, "color")
//                            .commit();
//                } else {
//                    getSupportFragmentManager()
//                            .beginTransaction()
//                            .hide(indexFragment).show(colorPickerFragment)
//                            .commit();
//                }
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.content_frame, new ColorPickerFragment())
                        .commit();
                ((ImageView)findViewById(R.id.devicebutton)).setImageResource(R.drawable.devicechouse);
                ((ImageView)findViewById(R.id.music)).setImageResource(R.drawable.ic_music);

                music_text.setTextColor(getResources().getColor(R.color.white));
                device_text.setTextColor(getResources().getColor(R.color.purple_text));
                break;
            case R.id.layout_progress:
                if (myApplication.isSelectMusic()) {
                    if (myApplication.isLocalMusicPlaying()) {
                        Intent intentLocal = new Intent(MainActivity.this, LocalPlayingActivity.class);
                        intentLocal.putExtra("position", myApplication.getCurrentLocalPosition());
                        startActivity(intentLocal);
                    } else {
                        Intent intentOnline = new Intent(MainActivity.this, OnlinePlayingActivity.class);
                        intentOnline.putExtra("position", myApplication.getCurrentOnlinePosition());
                        startActivity(intentOnline);
                    }
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
//        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //resume时候刷新封面
        try {
            updateArtwork();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void onEvent(PlayCompleEvent playCompleEvent) {
        updateArtwork();
    }

    private void updateArtwork() {
        if (myApplication.isSelectMusic()) {
            if (myApplication.isLocalMusicPlaying()) {
                new Thread() {
                    public void run() {
                        try {
                            Uri imageUri = Utils.getArtworkUri(myApplication.getCurrentLocalMusicInfo().getAlbumId());
                            bitmapArtwork = MediaStore.Images.Media.getBitmap(MainActivity.this.getContentResolver(), imageUri);
                            handler.sendEmptyMessage(UPDATE_ARTWORK);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            } else {
                Glide.with(MainActivity.this)
                        .load(myApplication.getCurrentOnlineMusicInfo().getArtworkUrl())
                        .centerCrop()
                        .crossFade()
                        .listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        imageViewCircleArtwork.setImageResource(R.drawable.defalthead);
                                    }
                                });
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                return false;
                            }
                        })
                        .into(imageViewCircleArtwork);
            }
        }
    }

    private void initSlidingMenu(Bundle savedInstanceState) {
        setBehindContentView(R.layout.menu_frame);
        if (savedInstanceState == null) {
            leftFragment = new LeftFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.menu_frame, leftFragment)
                    .commit();
        } else {
            leftFragment = (LeftFragment) getSupportFragmentManager().findFragmentById(R.id.menu_frame);
        }

        setContentView(R.layout.content_frame);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, new IndexFragment(), "index")
                .commit();

        // customize the SlidingMenu
        SlidingMenu sm = getSlidingMenu();
        sm.setShadowWidthRes(R.dimen.shadow_width);
        sm.setShadowDrawable(R.drawable.shadow);
        sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        sm.setFadeDegree(0.35f);
        sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        setSlidingActionBarEnabled(false);
        sm.setBehindScrollScale(0.0f);
    }

}
