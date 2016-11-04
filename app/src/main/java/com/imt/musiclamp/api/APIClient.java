package com.imt.musiclamp.api;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class APIClient {

    public static AsyncHttpClient getSimpleHttpClient() {
        AsyncHttpClient client = new AsyncHttpClient();
        return client;
    }

    public static AsyncHttpClient getNeteaseHttpClient() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("Accept", "*/*");
        client.addHeader("Accept-Encoding", "gzip,deflate,sdch");
        client.addHeader("Accept-Language", "zh-CN,zh;q=0.8,gl;q=0.6,zh-TW;q=0.4");
        client.addHeader("Connection", "keep-alive");
        client.addHeader("Content-Type", "application/x-www-form-urlencoded");
        client.addHeader("Host", "music.163.com");
        client.addHeader("Referer", "http://music.163.com/search/");
        client.addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36");
        return client;
    }

    //获取歌单信息
    public static void getPlaylist(Context context, int playlistId, AsyncHttpResponseHandler responseHandler) {
        String url = String.format("http://git.imt66.com:5000/api/music/songs/%s", playlistId);
        APIClient.getSimpleHttpClient().get(context, url, responseHandler);
    }

    //获取排行榜信息 传入页数
    public static void getTopList(Context context, int page, AsyncHttpResponseHandler responseHandler) {
        String url = String.format("http://git.imt66.com:5000/api/music/toplists/10/%s", page);
        getSimpleHttpClient().get(context, url, responseHandler);
    }

    //获取全部歌单信息 传入页数
    public static void getAllPlayList(Context context, int page, AsyncHttpResponseHandler responseHandler) {
        String url = String.format("http://git.imt66.com:5000/api/music/playlists/15/%s", page);
        getSimpleHttpClient().get(context, url, responseHandler);
    }

    public static void getMusicInfo(Context context, String songId, AsyncHttpResponseHandler responseHandler) {
        String url = String.format("http://music.163.com/api/song/detail/?id=%s&ids=[%s]", songId, songId);
        Log.e("url", url);
        getNeteaseHttpClient().get(context, url, responseHandler);
    }

    public static void getMusicLrc(Context context, String songId, AsyncHttpResponseHandler responseHandler) {
        String url = String.format("http://music.163.com/api/song/media?id=%s&version=0&csrf_token=", songId);
        getNeteaseHttpClient().get(context, url, responseHandler);
    }

    public static void search(Context context, String searchContent, AsyncHttpResponseHandler responseHandler) {
        String url = "http://music.163.com/api/search/get/web";
        RequestParams requestParams=new RequestParams();
        requestParams.put("s",searchContent);
        requestParams.put("type",1);
        requestParams.put("offset",0);
        requestParams.put("total","true");
        requestParams.put("limit","60");
        getNeteaseHttpClient().post(url,requestParams,responseHandler);
    }

}
