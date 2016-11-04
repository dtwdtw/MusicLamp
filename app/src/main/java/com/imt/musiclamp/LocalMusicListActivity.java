package com.imt.musiclamp;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.imt.musiclamp.adapter.MusicListAdapter;
import com.imt.musiclamp.model.LocalMusicInfo;

import org.w3c.dom.Text;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class LocalMusicListActivity extends Activity {

    @InjectView(R.id.listView)
    ListView listView;

    private List<LocalMusicInfo> musicInfos;

    MyApplication myApplication;

    private TextView empty;

    private MusicListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_music);
        myApplication = (MyApplication) getApplication();
        musicInfos = myApplication.getLocalMusicInfos(true);
        ButterKnife.inject(this);

        adapter = new MusicListAdapter(this, musicInfos);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(onItemClickListener);
        findViewById(R.id.imageView_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        empty = (TextView) this.findViewById(R.id.empty);
    }

    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            PlayEvent playEvent = new PlayEvent();
//            playEvent.setPosition(position);
//            EventBus.getDefault().post(playEvent);
            Intent intent = new Intent(LocalMusicListActivity.this, LocalPlayingActivity.class);
            intent.putExtra("position", position);
            startActivity(intent);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        if(null != adapter)
            adapter.notifyDataSetChanged();

        if(null == musicInfos || 0 == musicInfos.size())
            empty.setVisibility(View.VISIBLE);
        else
            empty.setVisibility(View.INVISIBLE);
    }
}
