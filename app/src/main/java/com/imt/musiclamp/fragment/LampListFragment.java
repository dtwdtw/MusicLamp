package com.imt.musiclamp.fragment;


import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.afollestad.materialdialogs.MaterialDialog;
import com.imt.musiclamp.LampSettingActivity;
import com.imt.musiclamp.MyApplication;
import com.imt.musiclamp.R;
import com.imt.musiclamp.element.APDisconnectEvent;
import com.imt.musiclamp.element.WifiAdmin;
import com.imt.musiclamp.element.APConnectedEvent;
import com.imt.musiclamp.element.WifiConnectEvent;
import com.imt.musiclamp.element.WifiDisConnectEvent;
import com.imt.musiclamp.event.FindDevicesEvent;
import com.imt.musiclamp.utils.Utils;
import com.imt.musiclamp.model.Lamp;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import zxing.decoding.Intents;


public class LampListFragment extends DialogFragment {

    private MyApplication myApplication;

    @InjectView(R.id.listView_lamp)
    ListView listViewLamp;

    @InjectView(R.id.imageView_refresh)
    ImageView imageViewRefresh;

    private EditText editTextDialogName;

    private List<Lamp> lamps = new ArrayList<Lamp>();
    LampListAdapter adapter;
    TextView textView;

    private static final int UPDATA_LISTVIEW = 1;
    private static final int SET_LISTVIEW = 2;
    Activity activity;
    String wifiPass = null;
    WifiAdmin wifiAdmin = null;
    String lampMac;
    Timer timer;

    PopupWindow popupWindow;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SET_LISTVIEW:
                    adapter = new LampListAdapter(lamps);
                    listViewLamp.setAdapter(adapter);
                    break;
                case UPDATA_LISTVIEW:
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_left_lamp, null);
        ButterKnife.inject(this, view);


        activity = getActivity();

        textView = new TextView(getActivity());
        textView.setSingleLine(false);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        textView.setTextColor(Color.WHITE);

//        textView.setText("未发现设备！");
        textView.setText(getResources().getString(R.string.confirmation_device));
        EventBus.getDefault().register(this);


        //一键配置wifi
//        textView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                MyApplication.colorpickactive=false;
//                MyApplication.popfragmentactive=true;
//                startSetDevice();
//            }
//
//        });

        lamps = new Select()
                .from(Lamp.class)
                .execute();
        if (lamps.size() < 1) {
            new Thread() {
                @Override
                public void run() {
                    Log.e("Send UDP broadcast", "Sync");
                    Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, Utils.getFindJson().getBytes());
                    Log.v("findevice",Utils.getFindJson());
                }
            }.start();
            listViewLamp.addFooterView(textView);
        } else if (lamps.size() == 1) {
            lamps.get(0).setSelected(true);
            lamps.get(0).save();
        } else {
            listViewLamp.setOnItemClickListener(onItemClickListener);
        }
        adapter = new LampListAdapter(lamps);
        listViewLamp.setAdapter(adapter);
        myApplication = (MyApplication) getActivity().getApplication();


        Window window = getDialog().getWindow();
        window.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);

        WindowManager.LayoutParams params = window.getAttributes();

        // Just an example; edit to suit your needs.
//        params.x = 110; // about half of confirm button size left of source view
        params.y = 100; // above source view

        window.setAttributes(params);

        return view;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void startSetDevice() {

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
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle(getResources().getString(R.string.enter_current_wifi_password));
                    final EditText editText = new EditText(activity);
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

                                LampListFragment.this.dismiss();
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
                                popupWindow.showAtLocation(new TextView(activity), Gravity.CENTER, 0, 0);

                                timer = new Timer(true);
                                timer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {

                                                popupWindow.dismiss();
                                                WifiAdmin wifiAdmin = new WifiAdmin(activity);
                                                if (wifiAdmin.getWifiSSID().contains("IMT_DEVICE")) {
                                                    Toast.makeText(activity, R.string.connected_wifi_hospots_successfully, Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(activity, R.string.autamatic_config_failed, Toast.LENGTH_SHORT).show();
                                                }
                                                MyApplication.autochangedevicewifi = false;
                                                MyApplication.wifiinfosended = false;
                                                MyApplication.iswanttoconnectwifi = false;
                                                MyApplication.colorpickactive = false;
                                                MyApplication.popfragmentactive = false;
                                            }
                                        });
                                    }
                                }, 10000);

                                wifiAdmin.startScan();
                                List<ScanResult> resultList = wifiAdmin.getWifiList();
                                int i = resultList.size() - 1;
                                for (; i > -1; i--) {
                                    Log.v("wifiList", resultList.get(i).SSID);
                                    if (resultList.get(i).SSID.contains("IMT_DEVICE")) {

                                        //切换手机网络到wifi热点
                                        wifiAdmin.CreateWifiInfo(resultList.get(i).SSID, "12345678", wifiAdmin.getCipherType(resultList.get(i).SSID));
                                        Log.v("finddevice-wifitype", wifiAdmin.getCipherType(resultList.get(i).SSID) + "");
                                        Log.v("finddevice-device", resultList.get(i).SSID);
                                        Log.v("finddevice_frecen", resultList.get(i).frequency + "");
//                                            popupWindow.dismiss();
                                        lampMac = resultList.get(i).BSSID.replace(":", "");
                                        break;
                                    }

                                }
                                if (i == -1) {

                                    timer.cancel();
                                    popupWindow.dismiss();
                                    Toast.makeText(activity, R.string.make_sure_device_open, Toast.LENGTH_SHORT).show();
                                }


                            } else {
                                //密码太短
                                Toast.makeText(getActivity(), R.string.password_less_than_6, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    builder.show();
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {

                            MyApplication.autochangedevicewifi = false;
                            MyApplication.wifiinfosended = false;
                            MyApplication.iswanttoconnectwifi = false;
                            MyApplication.colorpickactive = false;
                        }
                    });
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
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(getResources().getString(R.string.select_4g_network));
        final ListView wifiList = new ListView(activity);
        wifiAdmin.startScan();
        final List<ScanResult> resultList = wifiAdmin.getWifiList();
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
                TextView wifiname = new TextView(activity);
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

                AlertDialog.Builder pass = new AlertDialog.Builder(activity);
                pass.setTitle(getResources().getString(R.string.enter_wifi_password));
                final EditText passedit = new EditText(activity);
                pass.setView(passedit);
                pass.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        wifiAdmin.CreateWifiInfo(resultList.get(position).SSID, passedit.getText().toString(), wifiAdmin.getCipherType(resultList.get(position).SSID));
                        Toast.makeText(activity, R.string.wifi_connected_smart_config, Toast.LENGTH_SHORT).show();
                    }
                });
                pass.show();
            }
        });
    }

    public void onEventMainThread(APConnectedEvent apConnectedEvent) {
        if (MyApplication.popfragmentactive) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, Utils.getSetWifiJson(lampMac, wifiAdmin.getWifiSSID().replace("\"", ""), wifiPass).getBytes());
                }
            }).start();
            MyApplication.wifiinfosended = true;
        }
    }

    public void onEventMainThread(WifiDisConnectEvent wifiDisConnectEvent) {

//        if(MyApplication.popfragmentactive) {
//            Toast.makeText(activity, "密码错误,请在大约一分钟后再试", Toast.LENGTH_SHORT).show();
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


        if (MyApplication.popfragmentactive) {
            timer.cancel();
            popupWindow.dismiss();
            Toast.makeText(activity, R.string.connected_refresh_devices_list, Toast.LENGTH_SHORT).show();

            MyApplication.autochangedevicewifi = false;
            MyApplication.wifiinfosended = false;
            MyApplication.iswanttoconnectwifi = false;
            MyApplication.colorpickactive = false;
            MyApplication.popfragmentactive = false;
        }
    }

    public void onEventMainThread(APDisconnectEvent apDisconnectEvent) {

        if (MyApplication.popfragmentactive) {
            MyApplication.iswanttoconnectwifi = true;
            wifiAdmin.CreateWifiInfo(wifiAdmin.getWifiSSID().replace("\"", ""), wifiPass, wifiAdmin.getCipherType(wifiAdmin.getWifiSSID().replace("\"", "")));
            Log.v("wifistate", wifiAdmin.getWifiSSID().replace("\"", ""));
            Log.v("wifistate", wifiAdmin.getCipherType(wifiAdmin.getWifiSSID().replace("\"", "")) + "");
        }
    }

    @OnClick({R.id.imageView_refresh, R.id.imageView_settings})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageView_refresh:
                new Thread() {
                    @Override
                    public void run() {
                        Log.e("Send UDP broadcast", "Sync");
                        Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, Utils.getFindJson().getBytes());
                    }
                }.start();
                break;
            case R.id.imageView_settings:
                Intent intent = new Intent(getActivity(), LampSettingActivity.class);
                startActivity(intent);
                break;
        }
    }

    public void onEventMainThread(FindDevicesEvent findDevicesEvent) {
        Log.e("onEvent", "socketEvent" + "threadId:" + Thread.currentThread().getId());

        String macAddress = findDevicesEvent.getMacAddress();
        Lamp lamp = new Select()
                .from(Lamp.class)
                .where("MacAddress = ?", macAddress)
                .executeSingle();

        listViewLamp.removeFooterView(textView);

        if (lamp == null) {
            //不存在 创建
            lamp = new Lamp(macAddress, macAddress, findDevicesEvent.getIp());
        } else {
            //存在 更新ip
            lamp.setIp(findDevicesEvent.getIp());
            lamp.setOnline(true);
        }
        lamp.save();
        lamps = new Select().from(Lamp.class).execute();
        adapter = new LampListAdapter(lamps);
        handler.sendEmptyMessage(SET_LISTVIEW);
    }

    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
//            MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
//                    .title(R.string.settings)
//                    .customView(R.layout.dialog_set_lamp, false)
//                    .positiveText(R.string.edit)
//                    .negativeText(R.string.cancel)
//                    .callback(new MaterialDialog.ButtonCallback() {
//                        @Override
//                        public void onPositive(MaterialDialog dialog) {
//                            String name = editTextDialogName.getText().toString();
//                            lamps.get(i).setName(name);
//                            lamps.get(i).save();
//                            handler.sendEmptyMessage(UPDATA_LISTVIEW);
//                        }
//                    }).build();
//            editTextDialogName = (EditText) dialog.getCustomView().findViewById(R.id.editText_name);
//            editTextDialogName.setText(lamps.get(i).getName());
//            dialog.show();

//            view.findViewById(R.id.checkBox).setSelected(!((CheckBox)view.findViewById(R.id.checkBox)).isChecked());
//
//                    lamps.get(i).setSelected(!((CheckBox)view.findViewById(R.id.checkBox)).isChecked());
//            ((CheckBox)view.findViewById(R.id.checkBox)).setChecked(!((CheckBox) view.findViewById(R.id.checkBox)).isChecked());
//            lamps.get(i).save();
        }
    };

    class LampListAdapter extends BaseAdapter {

        List<Lamp> lamps;

        public LampListAdapter(List<Lamp> lamps) {
            this.lamps = lamps;
        }

        @Override
        public int getCount() {
            return lamps.size();
        }

        @Override
        public Object getItem(int i) {
            return lamps.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            ViewHolder viewHolder = null;
//            if (view == null) {
            view = inflater.inflate(R.layout.list_item_left_menu, null, false);
            viewHolder = new ViewHolder();
            viewHolder.textViewLampName = (TextView) view.findViewById(R.id.textView_name);
            viewHolder.checkBox = (CheckBox) view.findViewById(R.id.checkBox);
            view.setTag(viewHolder);
//            } else {
//                viewHolder = (ViewHolder) view.getTag();
//            }
            viewHolder.textViewLampName.setText(lamps.get(i).getName());

            if (lamps.get(i).isSelected()) {
                viewHolder.checkBox.setChecked(true);
            } else {
                viewHolder.checkBox.setChecked(false);
            }

//            if (lamps.get(i).isOnline()) {
//                viewHolder.checkBox.setEnabled(true);
//                viewHolder.textViewLampName.setEnabled(true);
//            } else {
//                viewHolder.checkBox.setEnabled(false);
//                viewHolder.textViewLampName.setEnabled(false);
//                viewHolder.textViewLampName.setText(lamps.get(i).getName() + "(" + getResources().getString(R.string.offline) + ")");
//            }

            viewHolder.checkBox.setOnCheckedChangeListener(new CheckListener(i));
            viewHolder.textViewLampName.setOnClickListener(new TextClickListener(i, viewHolder.checkBox));


            return view;
        }

        class TextClickListener implements View.OnClickListener {
            int i;
            CheckBox checkBox;

            public TextClickListener(int i, CheckBox checkBox) {
                this.i = i;
                this.checkBox = checkBox;
            }

            @Override
            public void onClick(View v) {

                lamps.get(i).setSelected(!checkBox.isChecked());
                checkBox.setChecked(!checkBox.isChecked());
                lamps.get(i).save();
            }
        }

        class CheckListener implements CompoundButton.OnCheckedChangeListener {
            int i;

            public CheckListener(int i) {
                this.i = i;
            }

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.e("onCheckedChange", lamps.get(i).getId() + lamps.get(i).getMacAdress());
                lamps.get(i).setSelected(isChecked);
                lamps.get(i).save();
            }
        }
    }

    static class ViewHolder {
        TextView textViewLampName;
        CheckBox checkBox;
    }

}
