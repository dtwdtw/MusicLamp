package com.imt.musiclamp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by dtw on 15/6/11.
 */
public class RecomendMusicActivity extends Activity{
    MyApplication myApplication;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myApplication=(MyApplication)getApplication();

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
                myApplication.getCurrentOnlineMusicInfo().setArtworkUrl(jsonAlbum.getString("picUrl") + "?param=400y400");
            } catch (JSONException e) {
                e.printStackTrace();
            }
//            play();
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//                    initView(true);
//                }
//            });
        }

        @Override
        public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {

        }
    };
}
