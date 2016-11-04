package com.imt.musiclamp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.imt.musiclamp.adapter.LocalPlayingListAdapter;
import com.imt.musiclamp.adapter.MusicListAdapter;
import com.imt.musiclamp.adapter.ToplistAdapter;
import com.imt.musiclamp.model.LocalMusicInfo;
import com.imt.musiclamp.model.OnlineMusicInfo;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class DownloadedActivity extends ActionBarActivity {

    @InjectView(R.id.listView)
    ListView listView;
    @InjectView(R.id.textView_title)
    TextView textViewTitle;

    private List<OnlineMusicInfo> myLikeMusicInfos;
    private List<LocalMusicInfo> myLocalLikeMusicInfos;
    //    private ToplistAdapter adapter;
    private MusicListAdapter adapter;
    private MyApplication myApplication;

    private TextView empty;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            adapter.notifyDataSetChanged();
            listView.setVisibility(View.VISIBLE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toplist);
        myApplication = (MyApplication) getApplication();
        ButterKnife.inject(this);
        myLocalLikeMusicInfos = new ArrayList<>();
        myLikeMusicInfos = new Select().from(OnlineMusicInfo.class)
                .where("downloaded = ?", true)
                .execute();

        empty = (TextView) this.findViewById(R.id.empty);

        for (OnlineMusicInfo onlineMusicInfo : myLikeMusicInfos) {
            LocalMusicInfo localMusicInfo = new LocalMusicInfo();
            localMusicInfo.setArtist(onlineMusicInfo.getArtist());
            localMusicInfo.setTitle(onlineMusicInfo.getTitle());
            localMusicInfo.setSize(onlineMusicInfo.getDuration());
            localMusicInfo.setAlbum(onlineMusicInfo.getAlbum());
            localMusicInfo.setDownloaded(true);
            localMusicInfo.setFilePath(Environment.getExternalStorageDirectory().getPath()
                    + "/IMT_MUSIC/"
                    + onlineMusicInfo.getArtist()
                    + " - " + onlineMusicInfo.getTitle()
                    + ".mp3");
            localMusicInfo.setAlbumUrl(onlineMusicInfo.getArtworkUrl());
            myLocalLikeMusicInfos.add(localMusicInfo);
        }
        initListview();
        textViewTitle.setText(getResources().getString(R.string.download_songs));

        findViewById(R.id.imageView_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initListview() {
//        adapter = new ToplistAdapter(this, myLikeMusicInfos, true);
        adapter = new MusicListAdapter(this, myLocalLikeMusicInfos);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(onItemClickListener);
    }

    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            myApplication.setOnlineMusicInfos(myLikeMusicInfos);
//            Intent intent = new Intent(Intent.this, OnlinePlayingActivity.class);
//            intent.putExtra("position", position);
//            startActivity(intent);
            myApplication.setLocalMusicInfos(myLocalLikeMusicInfos);
            Intent intent = new Intent(DownloadedActivity.this, LocalPlayingActivity.class);
            intent.putExtra("position", position);
            startActivity(intent);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        if(null != adapter)
            adapter.notifyDataSetChanged();

        if(null == myLikeMusicInfos || 0 == myLikeMusicInfos.size())
            empty.setVisibility(View.VISIBLE);
        else
            empty.setVisibility(View.INVISIBLE);
    }
}
