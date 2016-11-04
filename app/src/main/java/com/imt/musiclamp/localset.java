package com.imt.musiclamp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.imt.musiclamp.model.Lamp;
import com.imt.musiclamp.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by dtw on 15/5/5.
 */
public class localset extends Activity{
    Bundle bundle;
    Button resetairplay,restartdlna;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.locatsetlayout);

        resetairplay=(Button)findViewById(R.id.restartairplay);
        restartdlna=(Button)findViewById(R.id.restartdlna);
        bundle=getIntent().getExtras();
        final Lamp lamp= (Lamp) bundle.getSerializable("lamp");
        resetairplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final JSONObject jsonObject=new JSONObject();
                try {
                    jsonObject.put("MAC",lamp.getMacAdress());
                    jsonObject.put("action","restart");
                    jsonObject.put("type","airplay");

                    new Thread() {
                        @Override
                        public void run() {
                            Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, jsonObject.toString().getBytes());
                        }
                    }.start();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        restartdlna.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final JSONObject jsonObject=new JSONObject();
                try {
                    jsonObject.put("MAC",lamp.getMacAdress());
                    jsonObject.put("action","restart");
                    jsonObject.put("type","dlna");
                    new Thread() {
                        @Override
                        public void run() {
                            Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, jsonObject.toString().getBytes());
                        }
                    }.start();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
