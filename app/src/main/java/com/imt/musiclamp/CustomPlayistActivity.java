package com.imt.musiclamp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.imt.musiclamp.adapter.ToplistAdapter;
import com.imt.musiclamp.api.APIClient;
import com.imt.musiclamp.model.CustomPlayList;
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


public class CustomPlayistActivity extends ActionBarActivity {

    @InjectView(R.id.listView)
    ListView listView;
    @InjectView(R.id.textView_title)
    TextView textViewTitle;

    private List<OnlineMusicInfo> onlineMusicInfos;

    private ToplistAdapter adapter;

    private MyApplication myApplication;

    private int customPlaylistId;
    private CustomPlayList customPlayList;

    private TextView empty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toplist);
        myApplication = (MyApplication) getApplication();
        ButterKnife.inject(this);
        customPlaylistId = (int) getIntent().getExtras().getLong("id");
        customPlayList = new Select().from(CustomPlayList.class)
                .where("id = ?", customPlaylistId)
                .executeSingle();
        onlineMusicInfos = new Select().from(OnlineMusicInfo.class)
                .where("customPlaylistId = ?", customPlaylistId)
                .execute();

        initListview();
        textViewTitle.setText(getIntent().getExtras().getString("name"));

        empty = (TextView) this.findViewById(R.id.empty);

        findViewById(R.id.imageView_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initListview() {
        adapter = new ToplistAdapter(this, onlineMusicInfos, true);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(onItemClickListener);
        listView.setOnItemLongClickListener(onItemLongClickListener);
    }

    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            myApplication.setOnlineMusicInfos(onlineMusicInfos);
            myApplication.setCurrentOnlinePosition(position);
            Intent intent = new Intent(CustomPlayistActivity.this, OnlinePlayingActivity.class);
            startActivity(intent);
        }
    };

    AdapterView.OnItemLongClickListener onItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
            MaterialDialog dialog = new MaterialDialog.Builder(CustomPlayistActivity.this)
                    .title(R.string.settings)
                    .positiveText(R.string.delete)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            onlineMusicInfos.get(position).delete();
                            onlineMusicInfos = new Select().from(OnlineMusicInfo.class)
                                    .where("customPlaylistId = ?", customPlaylistId)
                                    .execute();
                            adapter = new ToplistAdapter(CustomPlayistActivity.this, onlineMusicInfos, true);
                            listView.setAdapter(adapter);
                        }
                    }).build();
            dialog.show();
            return true;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        if(null != adapter)
            adapter.notifyDataSetChanged();

        if(null == onlineMusicInfos || 0 == onlineMusicInfos.size())
            empty.setVisibility(View.VISIBLE);
        else
            empty.setVisibility(View.INVISIBLE);
    }
}
