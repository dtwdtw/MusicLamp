package com.imt.musiclamp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
//import android.support.v7.internal.view.menu.MenuBuilder;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.imt.musiclamp.adapter.ToplistAdapter;
import com.imt.musiclamp.model.OnlineMusicInfo;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by dtw on 15/5/29.
 */
public class SearchActivity extends Activity {
    @InjectView(R.id.listView)
    ListView listView;
    int type;

    String serachUrl = "http://music.163.com/api/search/get/web";

    private ToplistAdapter adapter;
    private MyApplication myApplication;
    private List<OnlineMusicInfo> onlineMusicInfos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.searchlayout);

        ButterKnife.inject(this);
        myApplication = (MyApplication) getApplication();
        adapter = new ToplistAdapter(this, onlineMusicInfos, false);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(onItemClickListener);

        ((EditText) findViewById(R.id.edittextsearch)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId== EditorInfo.IME_ACTION_SEARCH){

                    findViewById(R.id.search).callOnClick();
                }
                return false;
            }
        });

        findViewById(R.id.search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((InputMethodManager) ((EditText) findViewById(R.id.edittextsearch)).getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(
                                SearchActivity.this
                                        .getCurrentFocus()
                                        .getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);

                onlineMusicInfos.clear();
                adapter.notifyDataSetChanged();
                String searchkey = ((TextView) findViewById(R.id.edittextsearch)).getText().toString();
//                LayoutInflater inflater = LayoutInflater.from(SearchActivity.this);
                // 引入窗口配置文件
//                View view = inflater.inflate(R.layout.activity_playing3, null);
                final Map<String, String> map = new HashMap<String, String>();
                map.put("s", ((EditText) findViewById(R.id.edittextsearch)).getText().toString());
                map.put("offset", "0");
                map.put("type", "1");
                map.put("total", "true");
                map.put("limit", "100");

                new Thread(new Runnable() {
                    @TargetApi(Build.VERSION_CODES.KITKAT)
                    @Override
                    public void run() {

                        try {
                            String backstr = submitDataByDoPost(map, serachUrl);
                            Log.v("searchstr-back", backstr);
                            Log.v("searchstr-jsliststr", new JSONObject(backstr).get("result").toString());
                            Log.v("searchstr-jsliststr", ((JSONObject) new JSONObject(backstr).get("result")).getJSONArray("songs").toString());
                            JSONArray jsonArray = ((JSONObject) new JSONObject(backstr).get("result")).getJSONArray("songs");
                            for (int j = 0; j < jsonArray.length(); j++) {
                                OnlineMusicInfo onlineMusicInfo = new OnlineMusicInfo();
                                JSONObject songObject = jsonArray.getJSONObject(j);
                                onlineMusicInfo.setSongId(Integer.valueOf(songObject.getString("id")));
                                onlineMusicInfo.setTitle(songObject.getString("name"));
                                onlineMusicInfo.setArtist((songObject.getJSONArray("artists").getJSONObject(0).getString("name")));
                                onlineMusicInfos.add(onlineMusicInfo);
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            }
        });
        findViewById(R.id.imageView_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            myApplication.setOnlineMusicInfos(onlineMusicInfos);
            myApplication.setLocalDevicesPlaying(true);
            Intent intent = new Intent(SearchActivity.this, OnlinePlayingActivity.class);
            intent.putExtra("position", position);
            startActivity(intent);
        }
    };

    public String submitDataByDoPost(final Map<String, String> map, final String path) throws Exception {
        // 注意Post地址中是不带参数的，所以newURL的时候要注意不能加上后面的参数
        URL Url = new URL(path);
        // Post方式提交的时候参数和URL是分开提交的，参数形式是这样子的：name=y&age=6
        StringBuilder sb = new StringBuilder();
        // sb.append("?");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue());
            sb.append("&");
        }
        sb.deleteCharAt(sb.length() - 1);
        String str = sb.toString();
        Log.v("searchstr_requestString", str);

        HttpURLConnection HttpConn = (HttpURLConnection) Url.openConnection();
        HttpConn.setRequestMethod("POST");
        HttpConn.setReadTimeout(5000);
        HttpConn.setDoOutput(true);
        HttpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        HttpConn.setRequestProperty("Referer", "http://music.163.com/search/");
        HttpConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36");
        HttpConn.setRequestProperty("Content-Length", String.valueOf(str.getBytes().length));
        OutputStream os = HttpConn.getOutputStream();
        os.write(str.getBytes());
        BufferedReader bufReader = new BufferedReader(new InputStreamReader(
                HttpConn.getInputStream()));
        String line;
        String result = "";
        while ((line = bufReader.readLine()) != null) {
            result += line + "\n";
        }
        return result;
    }
}
