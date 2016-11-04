package com.imt.musiclamp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.afollestad.materialdialogs.MaterialDialog;
import com.imt.musiclamp.element.StartTimerEvent;
import com.imt.musiclamp.element.TimeEndEvent;
import com.imt.musiclamp.element.TimerEvent;
import com.imt.musiclamp.event.TimePickEvent;
import com.imt.musiclamp.model.Lamp;
import com.imt.musiclamp.model.Scene;
import com.imt.musiclamp.utils.Utils;
import com.imt.musiclamp.view.TimePickBottomSheet;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;


public class LampSettingActivity extends Activity {

    @InjectView(R.id.button_timing_turn_off)
    Button buttonTimingTurnOff;
    @InjectView(R.id.button_timing_5min)
    Button buttonTiming5min;
    @InjectView(R.id.button_timing_custom)
    Button buttonTimingCustom;
    @InjectView(R.id.textView_countdown)
    TextView textViewCountDown;
    @InjectView(R.id.button_add_scene)
    Button buttonAddScene;
    @InjectView(R.id.restart)
    ImageView restart;

    private boolean thread = true;

    private Handler handler = new Handler();
    static boolean issend = false;
    static CountDownTimer countDownTimer;
    AlertDialog alertDialog=null;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ButterKnife.inject(this);
        EventBus.getDefault().register(this);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        new UpdateTimeThread().start();
        ((Switch) findViewById(R.id.musicjumpswitch)).setChecked(preferences.getBoolean("jump",false));
        ((Switch) findViewById(R.id.musicjumpswitch)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                List<Lamp> lamps = new Select()
                        .from(Lamp.class)
                        .where("selected = ?", true)
                        .execute();
                if (isChecked) {
                    final JSONObject jsonObject = new JSONObject();
                    for (Lamp lamp : lamps) {
                        try {
                            jsonObject.put("action", "set_music_flow");
                            jsonObject.put("value", 1);
                            jsonObject.put("MAC", lamp.getMacAdress());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, jsonObject.toString().getBytes());
                            }
                        }).start();
                    }
                    preferences.edit().putBoolean("jump",true).commit();
                } else {
                    final JSONObject jsonObject = new JSONObject();
                    for (Lamp lamp : lamps) {
                        try {
                            jsonObject.put("action", "set_music_flow");
                            jsonObject.put("value", 0);
                            jsonObject.put("MAC", lamp.getMacAdress());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, jsonObject.toString().getBytes());
                            }
                        }).start();
                    }

                    preferences.edit().putBoolean("jump",false).commit();
                }
            }
        });
    }

    private ListView listViewDialog;


    @OnClick({R.id.restart,R.id.button_timing_turn_off, R.id.button_timing_5min, R.id.button_timing_custom, R.id.button_restore_wifi, R.id.button_set_wifi, R.id.imageView_back,
            R.id.button_add_scene, R.id.button_custom_scene})
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.restart:
                ListView serverList=new ListView(this);
                serverList.setAdapter(new ArrayAdapter(this,android.R.layout.simple_list_item_1,new String[]{"Airplay","Dlan"}));
                serverList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        List<Lamp> lamps = new Select()
                                .from(Lamp.class)
                                .where("selected = ?", true)
                                .execute();
                        switch (position) {
                            case 0:
                                for (final Lamp lamp1 : lamps) {
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, Utils.getRestartServiceJson(lamp1.getMacAdress(), "restart", "airplay").getBytes());
                                        }
                                    }.start();
                                }
                                alertDialog.dismiss();
                                break;
                            case 1:
                                for (final Lamp lamp1 : lamps) {
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, Utils.getRestartServiceJson(lamp1.getMacAdress(), "restart", "dlna").getBytes());
                                        }
                                    }.start();
                                }
                                alertDialog.dismiss();
                                break;
                        }
                    }
                });
                AlertDialog.Builder builder=new AlertDialog.Builder(this);
                builder.setView(serverList);
                builder.setTitle("选择需要重启的服务");
                alertDialog=builder.show();
                break;
            case R.id.button_timing_turn_off:

                break;
            case R.id.button_timing_5min:
                List<Lamp> lamps = new Select()
                        .from(Lamp.class)
                        .where("selected = ?", true)
                        .execute();
                for (final Lamp lamp1 : lamps) {
                    new Thread() {
                        @Override
                        public void run() {
                            Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, Utils.getSetTimingJson(lamp1.getMacAdress(), "led_off", -1, -1, -1, -1, 5).getBytes());
                        }
                    }.start();
                }
                DateTime dateTime = DateTime.now().plusMinutes(5);
                preferences.edit().putBoolean("isTiming", true).commit();
                preferences.edit().putLong("timing", dateTime.getMillis()).commit();

//                if (!issend) {
//                issend = true;

                for (final Lamp lamp1 : lamps) {
                    new Thread() {
                        @Override
                        public void run() {
                            Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, Utils.getSetTimingJson(lamp1.getMacAdress(), "led_off", -1, -1, -1, -1, 5).getBytes());
                        }
                    }.start();
                }
                StartTimerEvent startTimerEvent = new StartTimerEvent();
                startTimerEvent.setEndtime(5);
                EventBus.getDefault().post(startTimerEvent);
//                }
                break;
            case R.id.button_timing_custom:
                TimePickBottomSheet timePickBottomSheet = new TimePickBottomSheet(LampSettingActivity.this);
                timePickBottomSheet.show();
                break;
            case R.id.button_restore_wifi:
                MaterialDialog dialog = new MaterialDialog.Builder(LampSettingActivity.this)
                        .content("是否恢复成AP模式")
                        .negativeText("取消")
                        .positiveText("恢复")
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                List<Lamp> lamps = new Select()
                                        .from(Lamp.class)
                                        .where("selected = ?", true)
                                        .execute();
                                for (final Lamp lamp1 : lamps) {
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, Utils.getSetApJson(lamp1.getMacAdress(), "IMT", "111222333").getBytes());
                                        }
                                    }.start();
                                }
                            }
                        })
                        .build();
                dialog.show();
                break;
            case R.id.button_set_wifi:
                startActivity(new Intent(LampSettingActivity.this, SettingWlanActivity.class));
                break;
            case R.id.imageView_back:
                finish();
                break;
            case R.id.button_add_scene:
                startActivity(new Intent(LampSettingActivity.this, AddSceneActivity.class));
                break;
            case R.id.button_custom_scene:
                final MaterialDialog dialogCustomScene = new MaterialDialog.Builder(LampSettingActivity.this)
                        .customView(R.layout.dialog_select_scene, false)
                        .build();
                listViewDialog = (ListView) dialogCustomScene.getCustomView().findViewById(R.id.listView);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(LampSettingActivity.this, android.R.layout.simple_list_item_1);
                final List<Scene> scenes = new Select().from(Scene.class)
                        .execute();
                for (Scene scene : scenes) {
                    adapter.add(scene.getName());
                }
                listViewDialog.setAdapter(adapter);
                listViewDialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                        dialogCustomScene.dismiss();
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
                    }
                });
                dialogCustomScene.show();
                break;
        }
    }

    public void onEventMainThread(TimerEvent timerEvent) {
        ((TextView) findViewById(R.id.textView_countdown)).setText(timerEvent.getEndsecond() / 1000 / 60 + "分" + timerEvent.getEndsecond() / 1000 % 60 + "秒后关灯");
    }

    public void onEventMainThread(TimeEndEvent timeEndEvent) {
        ((TextView) findViewById(R.id.textView_countdown)).setText("");
        issend = false;
    }

    public void onEvent(final TimePickEvent timePickEvent) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                textViewCountDown.setText(String.format("%s:%s" + getResources().getString(R.string.turn_off_lamps), timePickEvent.getHour(), timePickEvent.getMinute()));
                List<Lamp> lamps = new Select()
                        .from(Lamp.class)
                        .where("selected = ?", true)
                        .execute();
                for (final Lamp lamp1 : lamps) {
                    new Thread() {
                        @Override
                        public void run() {
                            Calendar calendar = Calendar.getInstance();
                            Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, Utils.getSetTimingJson(lamp1.getMacAdress(), "led_off", calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH), timePickEvent.getHour(), timePickEvent.getMinute(), -0).getBytes());
                        }
                    }.start();
                }
            }
        });
    }

    class UpdateTimeThread extends Thread {
        @Override
        public void run() {
            while (thread) {
                if (preferences.getBoolean("isTiming", false)) {
                    //加判断 prefrecens获取为0时 还有定时生效后
                    //oncreate后判断是否有定时 显示

                    if (preferences.getLong("timing", 0) - System.currentTimeMillis() > 0) {
                        final DateTime dateTime = new DateTime(preferences.getLong("timing", 0) - System.currentTimeMillis());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                textViewCountDown.setText(String.format("剩余时间:%s:%s", dateTime.getMinuteOfHour(), dateTime.getSecondOfMinute()));
                            }
                        });
                    } else {
                        textViewCountDown.setText("");
                    }
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        thread = false;
    }
}
