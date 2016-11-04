package com.imt.musiclamp.fragment;


import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.Image;
import android.media.MediaRecorder;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.opengl.Visibility;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.imt.musiclamp.LampSettingActivity;
import com.imt.musiclamp.element.APConnectedEvent;
import com.imt.musiclamp.element.APDisconnectEvent;
import com.imt.musiclamp.element.WifiAdmin;
import com.imt.musiclamp.element.WifiConnectEvent;
import com.imt.musiclamp.element.WifiDisConnectEvent;
import com.imt.musiclamp.view.customScensBottomDialog;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.imt.musiclamp.MyApplication;
import com.imt.musiclamp.R;
import com.imt.musiclamp.event.ColorSceneEvent;
import com.imt.musiclamp.event.SceneEvent;
import com.imt.musiclamp.event.SunSceneEvent;
import com.imt.musiclamp.model.Lamp;
import com.imt.musiclamp.musicLight.AudioProcess;
import com.imt.musiclamp.utils.Utils;
import com.triggertrap.seekarc.SeekArc;
import com.umeng.analytics.MobclickAgent;


import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.Inflater;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class ColorPickerFragment extends Fragment {

    private static Bitmap imageOriginal;
    private static Matrix matrix;
    private int wheelHeight, wheelWidth;

    @InjectView(R.id.imageView_wheel)
    ImageView imageViewColorWheel;

    @InjectView(R.id.imageView_switch)
    ImageView imageViewSwitch;

    @InjectView(R.id.imageView_picker_point)
    ImageView imageViewPickerPoint;

    @InjectView(R.id.seekArc)
    SeekArc seekArc;

    @InjectView(R.id.seekBar_sound)
    SeekBar seekBarSound;

    private int r;
    private int g;
    private int b;
    private int brightness;
    private boolean isColor = true;

    int rm = 0, gm = 0, bm = 0;
    int max = 30000;

    String eventName = null;
    //亮度 中间wheel的取值 默认为1
//    private double bright = 0.5;

    private int wheelValue = 50;

    boolean lampState = true;


    private Handler handler;

    MyApplication myApplication;
    BaseAdapter devicelistAdapter;


    static int frequency = 8000;//分辨率
    static final int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    static final int audioEncodeing = AudioFormat.ENCODING_PCM_16BIT;
    static final int yMax = 50;//Y轴缩小比例最大值
    static final int yMin = 1;//Y轴缩小比例最小值

    int minBufferSize;//采集数据需要的缓冲区大小
    AudioRecord audioRecord;//录音
    AudioProcess audioProcess = AudioProcess.creatAudioProcess();//处理
    TextView smartKey;
    //    Activity activity;
    String wifiPass = null;
    WifiAdmin wifiAdmin = null;
    String lampMac;
    Timer timer;
    PopupWindow popupWindow;
    List<ScanResult> imtresultList = new ArrayList<>();
    ScanResult connectdevice;
    AlertDialog alertidalot;
    PopupWindow guidepop;
    SharedPreferences preferences;

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(getActivity());
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(getActivity());
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_color_picker, null);
        ButterKnife.inject(this, view);


        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        seekBarSound.setProgress(preferences.getInt("volume", 0));
        smartKey = (TextView) view.findViewById(R.id.smartkey);
        smartKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (guidepop != null) {
                    guidepop.dismiss();
                }
                wifiAdmin = new WifiAdmin(getActivity());
                wifiAdmin.startScan();
                List<ScanResult> resultList = wifiAdmin.getWifiList();
                imtresultList.clear();

                for (ScanResult temp : resultList) {


                    if (temp.SSID.contains("IMT_DEVICE")) {
                        imtresultList.add(temp);
                    }
                }
                if (imtresultList.size() > 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(getResources().getString(R.string.select_need_config_device));
                    ListView listView = new ListView(getActivity());
                    listView.setAdapter(devicelistAdapter);
                    builder.setView(listView);
                    alertidalot = builder.show();
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            alertidalot.dismiss();
                            MyApplication.colorpickactive = true;
                            MyApplication.popfragmentactive = false;
                            connectdevice = imtresultList.get(position);
                            lampMac = imtresultList.get(position).BSSID.replace(":", "");
                            startSetDevice(imtresultList.get(position).SSID);
                        }
                    });
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.no_found_unconfig_device), Toast.LENGTH_SHORT).show();
                }
            }

        });

        devicelistAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return imtresultList.size();
            }

            @Override
            public Object getItem(int position) {
                return imtresultList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = new TextView(getActivity());
                textView.setHeight(Utils.dip2px(getActivity(), 45));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                textView.setGravity(Gravity.CENTER_VERTICAL);
                textView.setTextColor(Color.BLACK);
                textView.setPadding(Utils.dip2px(getActivity(), 10), 0, 0, 0);
                textView.setText(imtresultList.get(position).SSID);

                return textView;
            }
        };


        handler = new Handler();
        myApplication = (MyApplication) getActivity().getApplication();


        // load the image only once
        if (imageOriginal == null) {
            imageOriginal = BitmapFactory.decodeResource(getResources(), R.drawable.color_wheel);
        }

        // initialize the matrix only once
        if (matrix == null) {
            matrix = new Matrix();
        } else {
            // not needed, you can also post the matrix immediately to restore the old state
            matrix.reset();
        }

        imageViewColorWheel.setDrawingCacheEnabled(true);
        imageViewColorWheel.setOnTouchListener(onTouchListener);
        imageViewColorWheel.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);

        seekArc.setOnSeekArcChangeListener(new SeekArcListener());
        seekArc.setProgress(50);

        seekBarSound.setOnSeekBarChangeListener(onSeekBarChangeListener);

        EventBus.getDefault().register(this);

        view.findViewById(R.id.imageView_sunlight).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == (MotionEvent.ACTION_DOWN)) {

                    ((ImageView) getActivity().findViewById(R.id.imageView_sunlight)).setImageResource(R.drawable.ic_lamp_sunlight_check);
                }
                if (event.getAction() == (MotionEvent.ACTION_UP)) {

                    ((ImageView) getActivity().findViewById(R.id.imageView_sunlight)).setImageResource(R.drawable.ic_lamp_sunlight);
                }
                return false;
            }

        });
        view.findViewById(R.id.imageView_color_picker).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction()==(MotionEvent.ACTION_DOWN)) {

                    ((ImageView) getActivity().findViewById(R.id.imageView_color_picker)).setImageResource(R.drawable.ic_lamp_music_chouse);
                }
                if (event.getAction()==(MotionEvent.ACTION_UP)) {

                    ((ImageView) getActivity().findViewById(R.id.imageView_color_picker)).setImageResource(R.drawable.ic_lamp_music);
                }
                return false;
            }

        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences s = getActivity().getSharedPreferences("xxxx", 0);
        boolean isFirst = s.getBoolean("first", false);

        s.edit().putBoolean("first", true).commit();

        if (!isFirst) {
            View view=View.inflate(getActivity(), R.layout.guidipage, null);
            guidepop = new PopupWindow(view, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, true);
            ColorDrawable colorDrawable = new ColorDrawable(0x80000000);
            guidepop.setBackgroundDrawable(colorDrawable);
            //设置点击窗口外边窗口消失
            guidepop.setOutsideTouchable(true);

            guidepop.showAsDropDown(getActivity().findViewById(R.id.smartkey), 0, 0);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    guidepop.dismiss();
                }
            });



        }

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void startSetDevice(final String connectdevicessid) {

        wifiAdmin = new WifiAdmin(getActivity());

        //确定为打开状态
        if (wifiAdmin.checkState() == WifiManager.WIFI_STATE_ENABLED && wifiAdmin.isConnected()) {
            Log.v("wifistate", "true");
            Log.v("wifistate", wifiAdmin.getWifiSSID());
            Log.v("wifistate", wifiAdmin.getWifiFrequency() + "");

            if (!wifiAdmin.getWifiSSID().contains("IMT_DEVICE")) {
                //确定当前链接网络为2.4G后，使用当前网络配置设备
                if (wifiAdmin.getWifiSSID() != null && wifiAdmin.getWifiFrequency() < 3000) {
                    Log.v("wifistate", wifiAdmin.getWifiSSID());

                    //弹出输入密码对话框
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(getResources().getString(R.string.enter_current_wifi_password));
                    final EditText editText = new EditText(getActivity());
                    editText.setHint(getResources().getString(R.string.current_wifi_no_password));
                    builder.setView(editText);
                    builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            InputMethodManager inputmanger = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputmanger.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                            if (editText.getText().toString().length() > 7 || editText.getText().toString().length() == 0) {
                                MyApplication.autochangedevicewifi = true;
                                wifiPass = editText.getText().toString();

                                LinearLayout linearLayout = new LinearLayout(getActivity());
                                ProgressBar progressBar = new ProgressBar(getActivity());
                                TextView windowmsg = new TextView(getActivity());
                                windowmsg.setSingleLine(true);
                                windowmsg.setTextColor(0xffdddddd);
                                windowmsg.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                                windowmsg.setWidth(ActionBar.LayoutParams.WRAP_CONTENT);
                                windowmsg.setText(getResources().getString(R.string.configuring_devices));
                                windowmsg.setGravity(Gravity.CENTER_HORIZONTAL);
                                linearLayout.setOrientation(LinearLayout.VERTICAL);
                                linearLayout.addView(progressBar);
                                linearLayout.addView(windowmsg);
                                linearLayout.setGravity(Gravity.CENTER);

                                popupWindow = new PopupWindow(linearLayout, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, true);
                                ColorDrawable colorDrawable = new ColorDrawable(0x99000000);
                                popupWindow.setBackgroundDrawable(colorDrawable);
                                //设置点击窗口外边窗口消失
                                popupWindow.setOutsideTouchable(true);

                                // 设置此参数获得焦点，否则无法点击
//                popupWindow.setFocusable(true);
                                popupWindow.showAtLocation(smartKey, Gravity.CENTER, 0, 0);

                                timer = new Timer(true);
                                timer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {

                                                popupWindow.dismiss();
                                                WifiAdmin wifiAdmin = new WifiAdmin(getActivity());
                                                if (wifiAdmin.getWifiSSID().contains("IMT_DEVICE")) {
                                                    Toast.makeText(getActivity(), R.string.connected_wifi_hospots_successfully, Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(getActivity(), R.string.automatic_configuration_failure, Toast.LENGTH_SHORT).show();
                                                }

                                                MyApplication.autochangedevicewifi = false;
                                                MyApplication.wifiinfosended = false;
                                                MyApplication.iswanttoconnectwifi = false;
                                                MyApplication.colorpickactive = false;
                                                MyApplication.popfragmentactive = false;
                                            }
                                        });
                                    }
                                }, 20000);

                                //切换手机网络到wifi热点
                                wifiAdmin.CreateWifiInfo(connectdevicessid, "12345678", wifiAdmin.getCipherType(connectdevicessid));
                                Log.v("finddevice-wifitype", wifiAdmin.getCipherType(connectdevicessid) + "");
                                Log.v("finddevice-device", connectdevicessid);
//                                            popupWindow.dismiss();


                            } else {
                                //密码太短
                                Toast.makeText(getActivity(), R.string.password_less_than_6, Toast.LENGTH_SHORT).show();
                                timer.cancel();
                            }
                        }
                    });
                    builder.show();
//                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                        @Override
//                        public void onDismiss(DialogInterface dialog) {
//
//                            MyApplication.autochangedevicewifi = false;
//                            MyApplication.wifiinfosended = false;
//                            MyApplication.iswanttoconnectwifi = false;
//                            MyApplication.colorpickactive = false;
//                        }
//                    });
                }
                //当强网络类型为5.0G
                else {
                    Toast.makeText(getActivity(), R.string.send_device_4g, Toast.LENGTH_SHORT).show();
                    connectNet2_4();
                }
            } else {
                Toast.makeText(getActivity(), R.string.connected_ap_not_suitable_smart_config, Toast.LENGTH_SHORT).show();
            }

        }
        //wifi没有打开
        else {
//            wifiAdmin.openWifi();
            Toast.makeText(getActivity(), R.string.no_conn_cannot_automatic_config, Toast.LENGTH_SHORT).show();

            connectNet2_4();
        }
    }

    public void connectNet2_4() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.select_4g_network));
        final ListView wifiList = new ListView(getActivity());
        wifiAdmin.startScan();
        final List<ScanResult> resultList = wifiAdmin.getWifiList();
        Log.v("wifistate", resultList.size() + "");
        BaseAdapter wifiadapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return resultList.size();
            }

            @Override
            public Object getItem(int position) {
                return resultList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView wifiname = new TextView(getActivity());
                wifiname.setText(resultList.get(position).SSID);
                return wifiname;
            }
        };
        wifiList.setAdapter(wifiadapter);
        builder.setView(wifiList);
        final AlertDialog dialog = builder.show();
        wifiList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                dialog.dismiss();
                AlertDialog.Builder pass = new AlertDialog.Builder(getActivity());
                pass.setTitle(getResources().getString(R.string.enter_wifi_password));
                final EditText passedit = new EditText(getActivity());
                pass.setView(passedit);
                pass.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        wifiAdmin.CreateWifiInfo(resultList.get(position).SSID, passedit.getText().toString(), wifiAdmin.getCipherType(resultList.get(position).SSID));
                        Toast.makeText(getActivity(), R.string.wifi_connected_smart_config, Toast.LENGTH_SHORT).show();
                    }
                });
                pass.show();
            }
        });
    }

    public void onEventMainThread(APConnectedEvent apConnectedEvent) {
        if (MyApplication.colorpickactive) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, Utils.getSetWifiJson(lampMac, wifiAdmin.getWifiSSID().replace("\"", ""), wifiPass).getBytes());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            MyApplication.wifiinfosended = true;
//            Log.v("wifistate", "alreadysend");
//            Log.v("wifistate", lampMac);
//            Log.v("wifistate:", wifiAdmin.getWifiSSID().replace("\"", ""));
//            Log.v("wifistate:", wifiPass);
//            Log.v("wifistate", Utils.getSetWifiJson(lampMac, wifiAdmin.getWifiSSID().replace("\"", ""), wifiPass));
        }
    }

    public void onEventMainThread(WifiDisConnectEvent wifiDisConnectEvent) {

//        if (MyApplication.colorpickactive) {
//            Toast.makeText(getActivity(), "密码错误,1分钟后再试", Toast.LENGTH_SHORT).show();
//            MyApplication.autochangedevicewifi=false;
//            MyApplication.wifiinfosended=false;
//            MyApplication.iswanttoconnectwifi=false;
//            MyApplication.colorpickactive=false;
//            MyApplication.popfragmentactive=false;
//            popupWindow.dismiss();
//            TimerEvent.cancel();
//        }
    }

    public void onEventMainThread(WifiConnectEvent wifiConnectEvent) {


        if (MyApplication.colorpickactive) {
            timer.cancel();
            popupWindow.dismiss();
            Toast.makeText(getActivity(), R.string.connected_refresh_devices_list, Toast.LENGTH_SHORT).show();

            MyApplication.autochangedevicewifi = false;
            MyApplication.wifiinfosended = false;
            MyApplication.iswanttoconnectwifi = false;
            MyApplication.colorpickactive = false;
            MyApplication.popfragmentactive = false;
        }

    }

    public void onEventMainThread(APDisconnectEvent apDisconnectEvent) {

        if (MyApplication.colorpickactive) {
            MyApplication.iswanttoconnectwifi = true;
            wifiAdmin.CreateWifiInfo(wifiAdmin.getWifiSSID().replace("\"", ""), wifiPass, wifiAdmin.getCipherType(wifiAdmin.getWifiSSID().replace("\"", "")));
            Log.v("wifistate", wifiAdmin.getWifiSSID().replace("\"", ""));
            Log.v("wifistate", wifiAdmin.getCipherType(wifiAdmin.getWifiSSID().replace("\"", "")) + "");
        }

    }


    ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            if (wheelHeight == 0 || wheelWidth == 0) {
                wheelHeight = imageOriginal.getHeight();
                wheelWidth = imageOriginal.getWidth();

                // translate to the image view's center
                float translateX = wheelWidth / 2 - imageOriginal.getWidth() / 2;
                float translateY = wheelHeight / 2 - imageOriginal.getHeight() / 2;
                matrix.postTranslate(translateX, translateY);

                imageViewColorWheel.setImageBitmap(imageOriginal);
                imageViewColorWheel.setImageMatrix(matrix);

                getColor(false);
            }
        }
    };

    public void onEvent(SceneEvent sceneEvent) {
        Log.e("sceneEvent", sceneEvent.getName() + "color");
        eventName = sceneEvent.getFragment();
        if ("color".equals(sceneEvent.getFragment())) {
            audioProcess.stop();
            Log.e("sceneEvent", "color");
            if (sceneEvent.isReadOnly()) {
                setRotateDialer(sceneEvent.getDegress());
            } else {
                float[] values;
                values = sceneEvent.getValues();
                matrix.setValues(values);
                imageViewColorWheel.setImageMatrix(matrix);
            }
            getColor(true);
        } else if ("sun".equals(sceneEvent.getFragment())) {
            audioProcess.stop();
            Log.e("sceneEvent", "sun");
            switchToSun();
            SunSceneEvent sunSceneEvent = new SunSceneEvent();
            sunSceneEvent.setName(sceneEvent.getName());
            sunSceneEvent.setValues(sceneEvent.getValues());
            sunSceneEvent.setWheelValue(sceneEvent.getWheelValue());
            sunSceneEvent.setFragment(sceneEvent.getFragment());
            new EventBus().post(sunSceneEvent);
        } else if ("music".equals(eventName)) {
            Log.e("Event", "MusicEvent");

//            new Thread() {
//                @Override
//                public void run() {
//                    while ("music".equals(eventName)) {

            try {
                //录音
                minBufferSize = AudioRecord.getMinBufferSize(frequency,
                        channelConfiguration,
                        audioEncodeing);
                //minBufferSize = 2 * minBufferSize;
                audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency,
                        channelConfiguration,
                        audioEncodeing,
                        minBufferSize);
//                            audioProcess.baseLine = sfv.getHeight()-100;
                audioProcess.frequence = frequency;
                //启动绘图
                audioProcess.start(audioRecord, minBufferSize);
                //停止绘图；
//                audioProcess.stop();
            } catch (Exception e) {
                // TODO: handle exception
            }

//                    }
//                }
//            }.start();
        }
    }

    public void onEvent(final ColorSceneEvent colorSceneEvent) {
        Log.e("sceneEvent", colorSceneEvent.getName());
        if (colorSceneEvent.isReadOnly()) {
            setRotateDialer(colorSceneEvent.getDegress());
        } else {
            float[] values;
            values = colorSceneEvent.getValues();
            matrix.setValues(values);
            imageViewColorWheel.setImageMatrix(matrix);
        }
        getColor(true);
    }

    private  void setIcon(){
        ((ImageView)getActivity().findViewById(R.id.imageView_sunlight)).setImageResource(R.drawable.ic_lamp_sunlight);
        ((ImageView)getActivity().findViewById(R.id.imageView_color_picker)).setImageResource(R.drawable.ic_lamp_music);
        ((ImageView)getActivity().findViewById(R.id.imageView_candle)).setImageResource(R.drawable.ic_lamp_candle);
        ((ImageView)getActivity().findViewById(R.id.imageView_moon)).setImageResource(R.drawable.ic_lamp_night);
        ((ImageView)getActivity().findViewById(R.id.imageView_custom)).setImageResource(R.drawable.ic_lamp_user_mode);
    }

    @OnClick({R.id.imageView_moon,R.id.imageView_custom,R.id.imageView_settings, R.id.imageView_switch, R.id.textView_my_devices, R.id.imageView_sunlight, R.id.imageView_color_picker, R.id.imageView_candle})
    public void onClick(View view) {
        List<Lamp> lamps;
        switch (view.getId()) {
            case R.id.imageView_sunlight:
                isColor = false;
                imageViewColorWheel.setImageResource(R.drawable.sun_light_wheel);
                setIcon();

//                ((ImageView)getActivity().findViewById(R.id.imageView_sunlight)).setImageResource(R.drawable.ic_lamp_sunlight_check);
                break;
            case R.id.imageView_color_picker:
                isColor = true;
                imageViewColorWheel.setImageResource(R.drawable.color_wheel);
                setIcon();
//                ((ImageView)getActivity().findViewById(R.id.imageView_color_picker)).setImageResource(R.drawable.ic_lamp_music_chouse);
                break;
            case R.id.imageView_settings:

                lamps = new Select()
                        .from(Lamp.class)
                        .where("selected = ?", true)
                        .execute();

                if (lamps.size() < 1) {
                    Toast.makeText(getActivity(), R.string.select_device, Toast.LENGTH_LONG).show();
                    LampListFragment lampListFragment = new LampListFragment();
                    android.app.FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
                    fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_UNSET);
                    lampListFragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                    lampListFragment.show(fragmentTransaction, "lampList");
                } else {
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), LampSettingActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                break;
            case R.id.imageView_candle:
                MobclickAgent.onEvent(getActivity(), "candleClick");
                lamps = new Select()
                        .from(Lamp.class)
                        .where("selected = ?", true)
                        .execute();
                for (final Lamp lamp1 : lamps) {
                    new Thread() {
                        @Override
                        public void run() {
                            Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, Utils.getCandleModeJson(lamp1.getMacAdress()).getBytes());
                        }
                    }.start();
                }
                setIcon();

                ((ImageView)getActivity().findViewById(R.id.imageView_candle)).setImageResource(R.drawable.ic_lamp_candle_chekc);
                break;
            case R.id.imageView_moon:

                MobclickAgent.onEvent(getActivity(), "nightClick");
                lamps = new Select()
                        .from(Lamp.class)
                        .where("selected = ?", true)
                        .execute();
                for (final Lamp lamp1 : lamps) {
                    new Thread() {
                        @Override
                        public void run() {
                            Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, Utils.getNightModeJson(lamp1.getMacAdress()).getBytes());
                        }
                    }.start();
                }
                setIcon();

                ((ImageView)getActivity().findViewById(R.id.imageView_moon)).setImageResource(R.drawable.ic_lamp_night_check);
                break;
            case R.id.imageView_custom:
                MobclickAgent.onEvent(getActivity(), "customClick");
//                lamps = new Select()
//                        .from(Lamp.class)
//                        .where("selected = ?", true)
//                        .execute();
//                for (final Lamp lamp1 : lamps) {
//                    new Thread() {
//                        @Override
//                        public void run() {
//                            Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, Utils.getCandleModeJson(lamp1.getMacAdress()).getBytes());
//                        }
//                    }.start();
//                }

                new customScensBottomDialog(getActivity()).show();

                setIcon();

                ((ImageView)getActivity().findViewById(R.id.imageView_custom)).setImageResource(R.drawable.ic_lamp_user_mode_check);
                break;
            case R.id.imageView_switch:
                Log.e("imageView_switch", "click");
                lamps = new Select()
                        .from(Lamp.class)
                        .where("selected = ?", true)
                        .execute();

                if (lampState) {
                    imageViewColorWheel.setVisibility(View.GONE);
                    imageViewPickerPoint.setVisibility(View.GONE);
                    ((ImageView)getActivity().findViewById(R.id.lightdown)).setVisibility(View.GONE);
                    ((ImageView)getActivity().findViewById(R.id.lightup)).setVisibility(View.GONE);
                    seekArc.setVisibility(View.GONE);
                    List<Lamp> lamps1 = new Select()
                            .from(Lamp.class)
                            .where("selected = ?", true)
                            .execute();
                    for (final Lamp lamp1 : lamps1) {
                        new Thread() {
                            @Override
                            public void run() {
                                Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, Utils.getSetLampJson(lamp1.getMacAdress(), false).getBytes());
                            }
                        }.start();
                    }

                } else{
                    imageViewColorWheel.setVisibility(View.VISIBLE);
                    imageViewPickerPoint.setVisibility(View.VISIBLE);
                    ((ImageView)getActivity().findViewById(R.id.lightdown)).setVisibility(View.VISIBLE);
                    ((ImageView)getActivity().findViewById(R.id.lightup)).setVisibility(View.VISIBLE);
                    seekArc.setVisibility(View.VISIBLE);

                    List<Lamp> lamps1 = new Select()

                            .from(Lamp.class)
                            .where("selected = ?", true)
                            .execute();
                    for (final Lamp lamp1 : lamps1) {
                        new Thread() {
                            @Override
                            public void run() {
                                Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, Utils.getSetLampJson(lamp1.getMacAdress(), true).getBytes());
                            }
                        }.start();
                    }

                }
                lampState = !lampState;
                if (lamps.size() == 0) {
                    Toast.makeText(getActivity(), R.string.dont_select_any_lamp, Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.textView_my_devices:
                LampListFragment lampListFragment = new LampListFragment();
                android.app.FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
                fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_UNSET);
                lampListFragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                lampListFragment.show(fragmentTransaction, "lampList");
                break;
        }
    }


    View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        private double startAngle;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startAngle = getAngle(event.getX(), event.getY());
                    break;
                case MotionEvent.ACTION_MOVE:
                    double currentAngle = getAngle(event.getX(), event.getY());
                    rotateDialer((float) (startAngle - currentAngle));
                    startAngle = currentAngle;

                    getColor(true);

                    break;
            }
            return true;
        }
    };

    class SeekArcListener implements SeekArc.OnSeekArcChangeListener {
        @Override
        public void onProgressChanged(SeekArc seekArc, final int progress, boolean fromUser) {
            brightness = 100 - progress;
            Log.e("getColor brightness", String.valueOf(brightness));

//            List<Lamp> lamps = new Select()
//                    .from(Lamp.class)
//                    .where("selected = ?", true)
//                    .execute();
//            for (final Lamp lamp1 : lamps) {
//                new Thread() {
//                    @Override
//                    public void run() {
//                        Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, Utils.getSetLampColorJson(lamp1.getMacAdress(), r * brightness / 100, g * brightness / 100, b * brightness / 100, 0x00).getBytes());
//                    }
//                }.start();
//
//            }
            getColor(false);
        }

        @Override
        public void onStartTrackingTouch(SeekArc seekArc) {

        }

        @Override
        public void onStopTrackingTouch(SeekArc seekArc) {

        }
    }

    SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(final SeekBar seekBar, final int progress, boolean fromUser) {
            List<Lamp> lamps = new Select()
                    .from(Lamp.class)
                    .where("selected = ?", true)
                    .execute();
            for (final Lamp lamp1 : lamps) {
                new Thread() {
                    @Override
                    public void run() {
                        Log.d("volumeJson", Utils.getSetVolumeJson(lamp1.getMacAdress(), "set", progress));
                        Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, Utils.getSetVolumeJson(lamp1.getMacAdress(), "set", progress).getBytes());
                    }
                }.start();
            }
//            getColor(false);
            preferences.edit().putInt("volume", progress).commit();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(final SeekBar seekBar) {

        }
    };

    private void getColor(boolean getColor) {
        //加if为了区分色盘取色和wheel取色 wheel取色时就部重复在色盘取色了
        if (getColor) {
            int color = imageViewColorWheel.getDrawingCache().getPixel(wheelWidth / 2, 50);//x、y为bitmap所对应的位置
            imageViewColorWheel.setDrawingCacheEnabled(false);
            imageViewColorWheel.setDrawingCacheEnabled(true);
            r = Color.red(color);
            g = Color.green(color);
            b = Color.blue(color);
        }

        new Thread() {
            @Override
            public void run() {
                List<Lamp> lamps = new Select()
                        .from(Lamp.class)
                        .where("selected = ?", true)
                        .execute();

                for (final Lamp lamp1 : lamps) {
                    if (isColor) {
                        Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, Utils.getSetLampColorJson(lamp1.getMacAdress(), r * brightness / 100, g * brightness / 100, b * brightness / 100, 0x00).getBytes());
                    } else {
                        Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, Utils.getSetLampColorJson(lamp1.getMacAdress(), r * brightness / 100, g * brightness / 100, b * brightness / 100, brightness / 100 * 255).getBytes());
                    }
                }

            }
        }.start();

    }

    private void switchToSun() {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
        SunlightFragment sunlightFragment = (SunlightFragment) getActivity().getSupportFragmentManager().findFragmentByTag("sun");
        if (sunlightFragment == null) {
            sunlightFragment = new SunlightFragment();
        }
        if (!sunlightFragment.isAdded()) {
            transaction.hide(this).add(R.id.content_frame, sunlightFragment, "sun");
        } else {
            Log.e("sunlightFragment", "isAdd");
            transaction.hide(this).show(sunlightFragment);
        }
        transaction.commit();
    }

    public float[] getMatrixValues() {
        float[] values = new float[9];
        matrix.getValues(values);
        return values;
    }

    private void rotateDialer(float degrees) {
        matrix.postRotate(degrees, wheelWidth / 2, wheelHeight / 2);
        imageViewColorWheel.setImageMatrix(matrix);
    }

    private void setRotateDialer(float degrees) {
        matrix.setRotate(0, wheelWidth / 2, wheelHeight / 2);
        matrix.setRotate(degrees, wheelWidth / 2, wheelHeight / 2);
        imageViewColorWheel.setImageMatrix(matrix);
    }

    private double getAngle(double xTouch, double yTouch) {
        double x = xTouch - (wheelWidth / 2d);
        double y = wheelHeight - yTouch - (wheelHeight / 2d);

        switch (getQuadrant(x, y)) {
            case 1:
                return Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
            case 2:
            case 3:
                return 180 - (Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);

            case 4:
                return 360 + Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;

            default:
                // ignore, does not happen
                return 0;
        }
    }

    private static int getQuadrant(double x, double y) {
        if (x >= 0) {
            return y >= 0 ? 1 : 4;
        } else {
            return y >= 0 ? 2 : 3;
        }
    }

    public int getWheelValue() {
        return wheelValue;
    }

    public void setWheelValue(int wheelValue) {
        this.wheelValue = wheelValue;
    }
}
