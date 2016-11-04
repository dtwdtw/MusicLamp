package com.imt.musiclamp;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.imt.musiclamp.adapter.ToplistAdapter;
import com.imt.musiclamp.api.APIClient;
import com.imt.musiclamp.model.OnlineMusicInfo;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;


public class OnlinePlayistActivity extends ActionBarActivity {

    @InjectView(R.id.listView)
    ListView listView;
    @InjectView(R.id.textView_title)
    TextView textViewTitle;
    private PtrClassicFrameLayout pullToRefresh;
    private LinearLayout loading;

    private List<OnlineMusicInfo> onlineMusicInfos;
    private ToplistAdapter adapter;

    private Gson gson;
    private MyApplication myApplication;

    private int playlistId;

    private TextView empty;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            pullToRefresh.refreshComplete();
            adapter.notifyDataSetChanged();

            if(onlineMusicInfos.size() == 0 || onlineMusicInfos == null)
                empty.setVisibility(View.VISIBLE);
            else
                empty.setVisibility(View.INVISIBLE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toplist);
        myApplication = (MyApplication) getApplication();
        ButterKnife.inject(this);
        initPullToRefresh(ptrHandler);
        initListview();
        gson = new Gson();
        playlistId = getIntent().getExtras().getInt("playlistId");
        findViewById(R.id.imageView_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        textViewTitle.setText(getIntent().getExtras().getString("title"));

        empty = (TextView) this.findViewById(R.id.empty);
    }

    private void initListview() {
        onlineMusicInfos = new ArrayList<>();
        adapter = new ToplistAdapter(this, onlineMusicInfos,false);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(onItemClickListener);
    }

    private void initPullToRefresh(PtrHandler ptrHandler) {
        pullToRefresh = (PtrClassicFrameLayout) findViewById(R.id.pull_to_refresh);
        pullToRefresh.setLastUpdateTimeRelateObject(this);
        pullToRefresh.setPtrHandler(ptrHandler);
        // the following are default settings
        pullToRefresh.setResistance(1.7f);
        pullToRefresh.setRatioOfHeaderHeightToRefresh(1.2f);
        pullToRefresh.setDurationToClose(200);
        pullToRefresh.setDurationToCloseHeader(1000);
        // default is false
        pullToRefresh.setPullToRefresh(false);
        // default is true
        pullToRefresh.setKeepHeaderWhenRefresh(true);
        pullToRefresh.postDelayed(new Runnable() {
            @Override
            public void run() {
                pullToRefresh.autoRefresh();
            }
        }, 100);
    }

    private PtrHandler ptrHandler = new PtrHandler() {
        @Override
        public boolean checkCanDoRefresh(PtrFrameLayout ptrFrameLayout, View view, View view2) {
            return PtrDefaultHandler.checkContentCanBePulledDown(ptrFrameLayout, view, view2);
        }

        @Override
        public void onRefreshBegin(PtrFrameLayout ptrFrameLayout) {
            pullToRefresh.postDelayed(new Runnable() {
                @Override
                public void run() {
                    APIClient.getPlaylist(OnlinePlayistActivity.this, playlistId, toplistResponseHandler);
                }
            }, 1000);
        }
    };

    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            myApplication.setOnlineMusicInfos(onlineMusicInfos);
            Intent intent = new Intent(OnlinePlayistActivity.this, OnlinePlayingActivity.class);
            intent.putExtra("position", position);
            startActivity(intent);
        }
    };

    AsyncHttpResponseHandler toplistResponseHandler = new AsyncHttpResponseHandler() {
        @Override
        public void onSuccess(int i, Header[] headers, byte[] bytes) {
            String jsonContent = new String(bytes);
//            List<OnlineMusicInfo> musicList = gson.fromJson(jsonContent, new TypeToken<List<OnlineMusicInfo>>() {
//            }.getType());
            try {
                JSONArray jsonArray = new JSONArray(jsonContent);
                for (int j = 0; j < jsonArray.length(); j++) {
                    OnlineMusicInfo onlineMusicInfo = new OnlineMusicInfo();
                    JSONObject songObject = jsonArray.getJSONObject(j);
                    onlineMusicInfo.setSongId(Integer.valueOf(songObject.getString("songId")));
                    onlineMusicInfo.setTitle(songObject.getString("name"));
                    onlineMusicInfo.setArtist(songObject.getString("artist"));
                    onlineMusicInfos.add(onlineMusicInfo);
                    handler.sendEmptyMessage(1);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
        }
    };


    @Override
    protected void onResume() {
        super.onResume();

        if(null != adapter)
            adapter.notifyDataSetChanged();

        if(onlineMusicInfos.size() == 0 || onlineMusicInfos == null)
            empty.setVisibility(View.VISIBLE);
        else
            empty.setVisibility(View.INVISIBLE);
    }
}
