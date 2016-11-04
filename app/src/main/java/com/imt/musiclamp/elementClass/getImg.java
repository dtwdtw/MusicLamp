package com.imt.musiclamp.elementClass;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by dtw on 15/4/8.
 */
public abstract class getImg {
    URL url= null;
    Bitmap bitmap=null;
    HttpURLConnection httpURLConnection = null;
    public abstract void bitMapBack(Bitmap bitmap);
    public getImg(final Handler handler,final String urlstr){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(urlstr==null||urlstr==""||urlstr.equals("")||urlstr.equals("baidu.com")){
                        bitMapBack(null);
                        return;
                    }
                    url = new URL(urlstr);
                    httpURLConnection = (HttpURLConnection) url.openConnection();
                    bitmap = BitmapFactory.decodeStream(httpURLConnection.getInputStream());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            bitMapBack(bitmap);
                        }
                    });
                    return;
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                catch (Exception e){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            bitMapBack(null);
                        }
                    });
                    return;
                }

            }
        }).start();
    }
}
