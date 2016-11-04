package com.imt.musiclamp.elementClass;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import com.imt.musiclamp.FriendActivity;
import com.imt.musiclamp.model.User;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by dtw on 15/3/18.
 */
public abstract class UserServer {
    String urlAll = null;
    String urlImage = null;
    JSONObject jsonObject = null;

    public abstract void httpBack(JSONObject jsonObject) throws JSONException;

    public void BitmapBack(Bitmap bitmap) {
    }

    public UserServer(Context context, Handler handler, String urlstr) {
        new UserServer(handler, urlstr) {
            @Override
            public void httpBack(JSONObject jsonObject) throws JSONException {
                UserServer.this.httpBack(jsonObject);
            }
        };
    }

    public UserServer(final Handler handler, String urlstr) {
        this.urlAll = urlstr;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(urlAll);
                    Log.v("out", url.toString());
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    InputStreamReader inputStreamReader = new InputStreamReader(httpURLConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String str = bufferedReader.readLine().toString();

                    Log.v("out2", str);

                    JSONTokener jsonTokener = new JSONTokener(str);

                    jsonObject = (JSONObject) jsonTokener.nextValue();
                    handler.post(new postJson(jsonObject));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    //等待用户头像。。
//                    URL url=new URL(StrUrl.getIcon(jsonObject));
//                    HttpURLConnection httpURLConnection=(HttpURLConnection)url.openConnection();
//                    Bitmap bitmap = BitmapFactory.decodeStream(httpURLConnection.getInputStream());
//                    handler.post(new upImg(bitmap));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

//    public  void setUrlImage(String strType, ImageView imageView) {
//        final String u = strType;
//        final ImageView i = imageView;
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//                URL url = null;
//                try {
//                    url = new URL(urlAll);
//                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
//                    Bitmap bitmap = BitmapFactory.decodeStream(httpURLConnection.getInputStream());
//                    httpURLConnection.disconnect();
//                    handler.post(new upImg(bitmap, i));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//
//    }

    class upImg implements Runnable {
        Bitmap bitmap;

        public upImg(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        @Override
        public void run() {
            BitmapBack(bitmap);
        }
    }

    class postJson implements Runnable {
        JSONObject jsonObject;

        public postJson(JSONObject jsonObject1) {
            this.jsonObject = jsonObject1;
        }

        @Override
        public void run() {
            try {
                httpBack(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}