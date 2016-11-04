package com.imt.musiclamp;

import android.os.Handler;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by dtw on 15/3/4.
 */
public class showTv {
    int minutes;
    public static TextView tv;
    Handler h = new Handler();

    int m;
    Timer timer;

    public void startTime(int minutes) {

        m = minutes + 1;
        try {
            timer.cancel();
        } catch (Exception execption) {
        }

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                m--;
                h.post(new run(m + "分钟后关灯"));
                if (m == 0) {
                    timer.cancel();
                    h.post(new run(""));
                }
            }
        }, 0, 60000);
    }


    class run implements Runnable {

        String text;

        public run(String text) {
            this.text = text;
        }

        @Override
        public void run() {
            tv.setText(text);
        }
    }

}
