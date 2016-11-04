package com.imt.musiclamp.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.activeandroid.query.Select;
import com.imt.musiclamp.MyApplication;
import com.imt.musiclamp.R;
import com.imt.musiclamp.model.Lamp;
import com.imt.musiclamp.model.Scene;
import com.imt.musiclamp.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

/**
 * Created by dtw on 15/6/24.
 */
public class customScensBottomDialog extends Dialog implements DialogInterface {

    Context context;

    public customScensBottomDialog(Context context) {
        super(context, com.cocosw.bottomsheet.R.style.BottomSheet_Dialog);
        this.context=context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCanceledOnTouchOutside(true);

        setContentView(R.layout.customscensbottomdialog);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.customlistitem);
        final List<Scene> scenes = new Select().from(Scene.class)
                .execute();
        for (Scene scene : scenes) {
            adapter.add(scene.getName());
        }

        ListView customScense=(ListView)findViewById(R.id.customscense);
        customScense.setAdapter(adapter);

        customScense.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,final int position, long id) {
                new Thread() {
                    @Override
                    public void run() {
                        List<Lamp> lamps = new Select()
                                .from(Lamp.class)
                                .where("selected = ?", true)
                                .execute();
                        for (final Lamp lamp1 : lamps) {
                            new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        if (scenes.get(position).isColor()) {
                                            Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT,
                                                    Utils.getAddSceneJson(lamp1.getMacAdress(),
                                                            scenes.get(position).getR() * scenes.get(position).getBrightness() / 100,
                                                            scenes.get(position).getG() * scenes.get(position).getBrightness() / 100,
                                                            scenes.get(position).getB() * scenes.get(position).getBrightness() / 100,
                                                            0x00,
                                                            new JSONArray(scenes.get(position).getJsonArrayMusicList())
                                                    ).getBytes());
                                        } else {
                                            Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT,
                                                    Utils.getAddSceneJson(lamp1.getMacAdress(),
                                                            scenes.get(position).getR() * scenes.get(position).getBrightness() / 100,
                                                            scenes.get(position).getG() * scenes.get(position).getBrightness() / 100,
                                                            scenes.get(position).getB() * scenes.get(position).getBrightness() / 100,
                                                            scenes.get(position).getBrightness() / 100 * 255,
                                                            new JSONArray(scenes.get(position).getJsonArrayMusicList())
                                                    ).getBytes());
                                        }

                                        if (scenes.get(position).isTiming()) {
                                            Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT,
                                                    Utils.getSetTimingJson(lamp1.getMacAdress(),
                                                            "led_off",
                                                            -1, -1, -1,
                                                            scenes.get(position).getHour(),
                                                            scenes.get(position).getMinute()
                                                    ).getBytes());
                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }.start();
                        }
                    }
                }.start();
                customScensBottomDialog.this.dismiss();
            }
        });

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM;

        TypedArray a = getContext().obtainStyledAttributes(new int[]{android.R.attr.layout_width});
        try {
            params.width = a.getLayoutDimension(0, ViewGroup.LayoutParams.MATCH_PARENT);
        } finally {
            a.recycle();
        }

        getWindow().setAttributes(params);

    }
}
