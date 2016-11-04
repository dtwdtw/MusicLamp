package com.imt.musiclamp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.imt.musiclamp.elementClass.StringURL;
import com.imt.musiclamp.elementClass.UserServer;
import com.imt.musiclamp.event.FindDevicesEvent;
import com.imt.musiclamp.model.Lamp;
import com.imt.musiclamp.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by dtw on 15/5/4.
 */
public class DeviceSetActivity extends Activity {
    ListView localdevice, remotedevice;
    localadapter localadapter0 = new localadapter();
    remoteadapter remoteadapter1 = new remoteadapter();
    List<Lamp> localamps = new ArrayList<>();
    List<Lamp> remotelamps = new ArrayList<>();
    final int CHANGE_DATA = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setdevicelayout);
        localdevice = (ListView) findViewById(R.id.localdevice);
        remotedevice = (ListView) findViewById(R.id.remotedevice);
        EventBus.getDefault().register(this);
        new Thread() {
            @Override
            public void run() {
                android.util.Log.e("Send UDP broadcast", "Sync");
                Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, Utils.getFindJson().getBytes());
            }
        }.start();

        setLocal(localdevice);
        setRemotedevice(remotedevice);

        localdevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent();
                Bundle bundle=new Bundle();
                bundle.putSerializable("lamp",localamps.get(position));
                intent.setClass(DeviceSetActivity.this,localset.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        remotedevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }

    private void setLocal(ListView listView) {
//        localamps = new Select()
//                .from(Lamp.class)
////                .where("selected = ?", true)
//                .execute();
        listView.setAdapter(localadapter0);

    }

    private void setRemotedevice(final ListView listView) {
        listView.setAdapter(remoteadapter1);
        new UserServer(this, new Handler(), new StringURL(StringURL.getDivece).setUserID(MyApplication.userID).toString()) {

            @Override
            public void httpBack(JSONObject jsonObject) throws JSONException {
                JSONArray jsonArray = jsonObject.getJSONArray("deviceList");
                for (int i = 0; i < jsonArray.length(); i++) {
                    Lamp lamp = new Lamp(jsonArray.getJSONObject(i).getString("deviceName"), jsonArray.getJSONObject(i).getString("UUID"), "null");
                    remotelamps.add(lamp);
                }
                Log.v("showdivece", "showdevice");
                remoteadapter1.notifyDataSetChanged();
            }
        };
    }

    public void onEvent(FindDevicesEvent findDevicesEvent) {
        android.util.Log.e("onEvent", "newevent");
        final Lamp lamp = new Lamp("Lamp", findDevicesEvent.getMacAddress(), findDevicesEvent.getIp());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                    if (localamps.size() < 1) {
                        localamps.add(lamp);
                    } else {
                        for (int i=0;i<localamps.size();i++) {
                            if (localamps.get(i).getMacAdress().equals(lamp.getMacAdress())) {
                                continue;
                            }
                            localamps.add(lamp);
                        }
                    }
                localadapter0.notifyDataSetChanged();
            }
        });
    }

    class localadapter extends BaseAdapter {

        @Override
        public int getCount() {
            return localamps.size();
        }

        @Override
        public Object getItem(int position) {
            return localamps.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = View.inflate(DeviceSetActivity.this, R.layout.localampitems, null);
            TextView textView = (TextView) view.findViewById(R.id.lampname);
            textView.setText(localamps.get(position).getMacAdress());
            return view;
        }
    }

    class remoteadapter extends BaseAdapter {

        @Override
        public int getCount() {
            return remotelamps.size();
        }

        @Override
        public Object getItem(int position) {
            return remotelamps.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = View.inflate(DeviceSetActivity.this, R.layout.remotelampitems, null);
            TextView textView = (TextView) view.findViewById(R.id.lampname);
            textView.setText(remotelamps.get(position).getMacAdress());
            return view;
        }
    }
}
