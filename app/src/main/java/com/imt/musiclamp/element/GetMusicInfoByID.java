package com.imt.musiclamp.element;

import android.os.Handler;
import android.util.Log;

import com.imt.musiclamp.elementClass.UserServer;
import com.imt.musiclamp.model.OnlineMusicInfo;

import org.apache.http.client.HttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by dtw on 15/6/15.
 */
public abstract class GetMusicInfoByID {
    public abstract void MusicInfo(OnlineMusicInfo onlineMusicInfo);
    public  GetMusicInfoByID(final  Handler handler,final String id) throws IOException, JSONException {
        String getidurl="http://music.163.com/api/song/detail/?id="+id+"&ids=["+id+"]";
        final OnlineMusicInfo onlineMusicInfo=new OnlineMusicInfo();

        final URL url=new URL(getidurl);
        new Thread(new Runnable() {
            @Override
            public void run() {

                HttpURLConnection httpURLConnection = null;
                try {
                    httpURLConnection = (HttpURLConnection) url.openConnection();
                    InputStreamReader inputStreamReader = new InputStreamReader(httpURLConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String str = bufferedReader.readLine().toString();
                    JSONObject jsonObject=new JSONObject(str);
                    onlineMusicInfo.setAlbum(jsonObject.getJSONArray("songs").getJSONObject(0).getJSONObject("album").getString("name"));
                    Log.v("onlinemusicinfo", onlineMusicInfo.getAlbum());
                    onlineMusicInfo.setArtist(jsonObject.getJSONArray("songs").getJSONObject(0).getJSONArray("artists").getJSONObject(0).getString("name"));
                    Log.v("onlinemusicinfo", onlineMusicInfo.getArtist());
                    onlineMusicInfo.setArtworkUrl(jsonObject.getJSONArray("songs").getJSONObject(0).getJSONObject("album").getString("blurPicUrl"));
                    Log.v("onlinemusicinfo", onlineMusicInfo.getArtworkUrl());
                    onlineMusicInfo.setSongId(Integer.decode(id));
                    Log.v("onlinemusicinfo", onlineMusicInfo.getSongId() + "");
                    onlineMusicInfo.setDuration(jsonObject.getJSONArray("songs").getJSONObject(0).getLong("duration"));
                    Log.v("onlinemusicinfo", onlineMusicInfo.getDuration() + "");
                    onlineMusicInfo.setUrl(jsonObject.getJSONArray("songs").getJSONObject(0).getString("mp3Url"));
                    Log.v("onlinemusicinfo", onlineMusicInfo.getUrl());
                    onlineMusicInfo.setTitle(jsonObject.getJSONArray("songs").getJSONObject(0).getJSONObject("album").getString("name"));
                    Log.v("onlinemusicinfo", onlineMusicInfo.getTitle());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            MusicInfo(onlineMusicInfo);
                        }
                    });




//                    Log.v("onlinemusicinfo", onlineMusicInfo.getAlbum());
//                    Log.v("onlinemusicinfo", onlineMusicInfo.getArtist());
//                    Log.v("onlinemusicinfo", onlineMusicInfo.getArtworkUrl());
//                    Log.v("onlinemusicinfo", onlineMusicInfo.getSongId() + "");
//                    Log.v("onlinemusicinfo", onlineMusicInfo.getDuration() + "");
//                    Log.v("onlinemusicinfo", onlineMusicInfo.getUrl());
//                    Log.v("onlinemusicinfo", onlineMusicInfo.getTitle());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
}
