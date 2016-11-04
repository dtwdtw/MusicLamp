package com.imt.musiclamp;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.activeandroid.query.Select;
import com.afollestad.materialdialogs.MaterialDialog;
import com.imt.musiclamp.element.WifiAdmin;
import com.imt.musiclamp.elementClass.StringURL;
import com.imt.musiclamp.model.Lamp;
import com.imt.musiclamp.utils.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class SettingWlanActivity extends Activity {

    @InjectView(R.id.listView)
    ListView listView;

    private EditText editTextDialogPassword;

    private ArrayAdapter<String> adapter;
    private List<String> data;
    private WifiManager wifiManager;
    private ConnectivityManager connectivityManager;
    private List<ScanResult> wifiResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_wlan);
        ButterKnife.inject(this);
        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        //打开WiFi
        if (wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        wifiManager.startScan();
        wifiResults = wifiManager.getScanResults();
        data = wifiToArray(wifiResults);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(onItemClickListener);

        wifiManager.enableNetwork(createWifiInfo("IMT-613", "83493199", 3).networkId, true);
    }

    @OnClick({R.id.imageView_back, R.id.imageView_refresh})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageView_back:
                finish();
                break;
            case R.id.imageView_refresh:
                wifiManager.startScan();
                wifiResults = wifiManager.getScanResults();
                data = wifiToArray(wifiResults);
                adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data);
                listView.setAdapter(adapter);
                break;
        }
    }


    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
            MaterialDialog dialog = new MaterialDialog.Builder(SettingWlanActivity.this)
                    .title(wifiResults.get(position).SSID)
                    .customView(R.layout.dialog_wlan, false)
                    .positiveText("连接")
                    .negativeText(R.string.cancel)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            new Thread() {
                                @Override
                                public void run() {
                                    final String ssid = wifiResults.get(position).SSID;
                                    final String password = editTextDialogPassword.getText().toString();
                                    if ("".endsWith(ssid) || "".endsWith(password)) {
                                        return;
                                    }
                                    List<Lamp> lamps = new Select()
                                            .from(Lamp.class)
                                            .where("selected = ?", true)
                                            .execute();
                                    for (final Lamp lamp1 : lamps) {
                                        new Thread() {
                                            @Override
                                            public void run() {
                                                Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, Utils.getSetWifiJson(lamp1.getMacAdress(), ssid, password).getBytes());
                                            }
                                        }.start();
                                        Log.v("wifistate-def",Utils.getSetWifiJson(lamp1.getMacAdress(), ssid, password));
                                    }
                                    WifiAdmin wifiAdmin=new WifiAdmin(SettingWlanActivity.this);
                                    wifiAdmin.CreateWifiInfo(ssid,password,wifiAdmin.getCipherType(ssid));
                                }
                            }.start();
                        }
                    }).build();
            editTextDialogPassword = (EditText) dialog.getCustomView().findViewById(R.id.editText_password);
            dialog.show();
        }
    };

    private List<String> wifiToArray(List<ScanResult> list) {
        List<String> data = new ArrayList<String>();
        for (ScanResult result : list) {
            data.add(result.SSID);
        }
        return data;
    }

    /**
     * 创建一个wifi信息
     *
     * @param ssid     名称
     * @param password 密码
     * @param paramInt 有3个参数，1是无密码，2是wep，3是wap加密
     * @return
     */
    public WifiConfiguration createWifiInfo(String ssid, String password, int paramInt) {
        //配置网络信息类
        WifiConfiguration localWifiConfiguration1 = new WifiConfiguration();
        //设置配置网络属性
        localWifiConfiguration1.allowedAuthAlgorithms.clear();
        localWifiConfiguration1.allowedGroupCiphers.clear();
        localWifiConfiguration1.allowedKeyManagement.clear();
        localWifiConfiguration1.allowedPairwiseCiphers.clear();
        localWifiConfiguration1.allowedProtocols.clear();

        localWifiConfiguration1.SSID = ("\"" + ssid + "\"");
        WifiConfiguration localWifiConfiguration2 = isExsits(ssid);
        if (localWifiConfiguration2 != null) {
            wifiManager.removeNetwork(localWifiConfiguration2.networkId); //从列表中删除指定的网络配置网络
        }
        if (paramInt == 1) { //没有密码
            localWifiConfiguration1.wepKeys[0] = "";
            localWifiConfiguration1.allowedKeyManagement.set(0);
            localWifiConfiguration1.wepTxKeyIndex = 0;
        } else if (paramInt == 2) { //简单密码
            localWifiConfiguration1.hiddenSSID = true;
            localWifiConfiguration1.wepKeys[0] = ("\"" + password + "\"");
        } else { //wap加密
            localWifiConfiguration1.preSharedKey = ("\"" + password + "\"");
            localWifiConfiguration1.hiddenSSID = true;
            localWifiConfiguration1.allowedAuthAlgorithms.set(0);
            localWifiConfiguration1.allowedGroupCiphers.set(2);
            localWifiConfiguration1.allowedKeyManagement.set(1);
            localWifiConfiguration1.allowedPairwiseCiphers.set(1);
            localWifiConfiguration1.allowedGroupCiphers.set(3);
            localWifiConfiguration1.allowedPairwiseCiphers.set(2);
        }

        return localWifiConfiguration1;
    }

    /**
     * 是否存在网络信息
     *
     * @param str 热点名称
     * @return
     */
    private WifiConfiguration isExsits(String str) {
        Iterator localIterator = this.wifiManager.getConfiguredNetworks().iterator();
        WifiConfiguration localWifiConfiguration;
        do {
            if (!localIterator.hasNext()) return null;
            localWifiConfiguration = (WifiConfiguration) localIterator.next();
        } while (!localWifiConfiguration.SSID.equals("\"" + str + "\""));
        return localWifiConfiguration;
    }

}
