package com.imt.musiclamp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.imt.musiclamp.event.FindDevicesEvent;
import com.imt.musiclamp.event.UpdateEvent;
import com.imt.musiclamp.event.UpdateState;
import com.imt.musiclamp.model.Lamp;
import com.imt.musiclamp.model.OnlineMusicInfo;
import com.imt.musiclamp.model.User;
import com.imt.musiclamp.utils.Utils;
import com.umeng.fb.FeedbackAgent;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by dtw on 15/5/11.
 */
public class AppSettingActivity extends Activity implements View.OnClickListener{

    private ImageButton back;
    private FrameLayout clear_musics, feed_back, devices_update, app_update, about;
    private TextView cache_size, local_version, logout;

    private AlertDialog.Builder finddevicedialog;
    List<Lamp> lamps=new ArrayList<>();
    Lamp tempLamp;
    private findeviceAdapter fadapter = new findeviceAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
        setContentView(R.layout.appsettinglayout);

        finddevicedialog = new AlertDialog.Builder(this);

        initView();
    }

    /**
     * 将文件大小换算成B/KB/MB/GB单位格式字符串
     * @param size
     * @return
     */
    private String convertFileSize(long size) {

        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;

        if(gb <= size)
            return String.format("%.1f GB", (float) size / gb);
        else if(mb <= size) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        } else
            return String.format("%d B", size);
    }

    private void initView() {
        back = (ImageButton) this.findViewById(R.id.back);
        back.setOnClickListener(this);

        clear_musics = (FrameLayout) this.findViewById(R.id.clear_musics);
        clear_musics.setOnClickListener(this);

        feed_back = (FrameLayout) this.findViewById(R.id.feed_back);
        feed_back.setOnClickListener(this);

        devices_update = (FrameLayout) this.findViewById(R.id.devices_update);
        devices_update.setOnClickListener(this);

        app_update = (FrameLayout) this.findViewById(R.id.app_update);
        app_update.setOnClickListener(this);

        about = (FrameLayout) this.findViewById(R.id.about);
        about.setOnClickListener(this);

        logout = (TextView) this.findViewById(R.id.logout);
        logout.setOnClickListener(this);

        cache_size = (TextView) this.findViewById(R.id.cache_size);
        try {
            cache_size.setText(convertFileSize(getFileSize(Environment.getExternalStoragePublicDirectory("IMT_MUSIC"))));
        } catch (Exception e) {
            e.printStackTrace();
        }

        local_version = (TextView) this.findViewById(R.id.local_version);
        local_version.setText(Utils.getVersionName(AppSettingActivity.this));
    }

    public long getFileSize(File f) throws Exception {
        long size = 0;
        File flist[] = f.listFiles();
        for (int i = 0; i < flist.length; i++) {
            if (flist[i].isDirectory()) {
                size = size + getFileSize(flist[i]);
            } else {
                size = size + flist[i].length();
            }
        }
        return size;
    }

    private void findDevices() {
        new Thread(new Runnable() {

        @Override
        public void run() {
            android.util.Log.e("Send UDP broadcast", "Sync");
            Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, Utils.getFindJson().getBytes());
        }
        }).start();

        selectDevice();
    }

    private void selectDevice() {
        finddevicedialog.setTitle(getResources().getString(R.string.select_device));
        finddevicedialog.setAdapter(fadapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                final JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("action", "get_update");
                    jsonObject.put("MAC", lamps.get(which).getMacAdress());
                    tempLamp = lamps.get(which);
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
        });
        finddevicedialog.show();
    }

    private void logOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AppSettingActivity.this);
        builder.setTitle(getResources().getString(R.string.log_out));
        builder.setMessage(getResources().getString(R.string.delete_signin_info));
        builder.setNegativeButton(getResources().getString(R.string.cancel), null);
        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new Delete().from(User.class).execute();
                Toast.makeText(AppSettingActivity.this, R.string.after_start_remove_user_info, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent();
                intent.setClass(AppSettingActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.back :
                this.finish();
                break;
            case R.id.clear_musics :
                new AlertDialog.Builder(AppSettingActivity.this)
                    .setTitle(getResources().getString(R.string.clear))
                    .setMessage(getResources().getString(R.string.clear_all_musics_sure))
                    .setNegativeButton(getResources().getString(R.string.cancel), null)
                        .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new Delete().from(OnlineMusicInfo.class).where("downloaded = ?", true).execute();
                                File[] childFiles = Environment.getExternalStoragePublicDirectory("IMT_MUSIC").listFiles();

                                for (File file1 : childFiles) {
                                    if (file1.isFile()) {
                                        file1.delete();
                                    }
                                }
                            }
                        }).show();
                try {
                    cache_size.setText(convertFileSize(getFileSize(Environment.getExternalStoragePublicDirectory("IMT_MUSIC"))));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.feed_back :
//                startActivity(new Intent(AppSettingActivity.this, FellBack.class));
                FeedbackAgent agent = new FeedbackAgent(this);
                agent.startFeedbackActivity();
                break;
            case R.id.devices_update :
                findDevices();
                break;
            case R.id.app_update :
                break;
            case R.id.about :
                startActivity(new Intent(AppSettingActivity.this, AboutActivity.class));
                break;
            case R.id.logout :
                logOut();
                break;
        }
    }

    class findeviceAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return lamps.size();
        }

        @Override
        public Object getItem(int position) {
            return lamps.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView = new TextView(AppSettingActivity.this);
            textView.setText(lamps.get(position).getMacAdress());
            textView.setHeight(Utils.dip2px(AppSettingActivity.this, 45));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
            textView.setGravity(Gravity.CENTER_VERTICAL);
            textView.setTextColor(Color.BLACK);
            textView.setPadding(Utils.dip2px(AppSettingActivity.this, 10), 0, 0, 0);

            return textView;
        }
    }
    public void onEventMainThread(FindDevicesEvent findDevicesEvent) {
        Log.v("find","find");
        Lamp lamp = new Select()
                .from(Lamp.class)
                .where("MacAddress = ?", findDevicesEvent.getMacAddress())
                .executeSingle();
        if (lamp == null) {
            //不存在 创建
            lamp = new Lamp(findDevicesEvent.getMacAddress(), findDevicesEvent.getMacAddress(), findDevicesEvent.getIp());
            lamp.save();
//            localamps.add(lamp);
        } else {
            //存在 更新ip
            lamp.setIp(findDevicesEvent.getIp());
            lamp.setOnline(true);
            lamp.save();
        }
        lamps = new Select().from(Lamp.class).execute();
        fadapter.notifyDataSetChanged();
    }

    public void onEventMainThread(UpdateState updateState){

    }

    public void onEventMainThread(UpdateEvent updateEvent){
        AlertDialog.Builder builder=new AlertDialog.Builder(AppSettingActivity.this);
        switch (updateEvent.getUpdateInfo()){
            case UpdateEvent.NEWVERSATION:
                AlertDialog.Builder builder1=new AlertDialog.Builder(AppSettingActivity.this);
                builder1.setTitle(getResources().getString(R.string.discover_update));
                TextView textView=new TextView(this);
                textView.setText(updateEvent.getInfo());
                builder1.setView(textView);
                builder1.setNegativeButton(getResources().getString(R.string.cancel), null);
                builder1.setPositiveButton(getResources().getString(R.string.update), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final JSONObject jsonObject=new JSONObject();
                        try {
                            jsonObject.put("action", "update");
                            jsonObject.put("MAC",tempLamp.getMacAdress());
                            jsonObject.put("type","download");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT,jsonObject.toString().getBytes());
                            }
                        }).start();
                    }
                }).show();
                break;
            case UpdateEvent.NOTHING:
                Toast.makeText(AppSettingActivity.this, R.string.undiscover_new_version,Toast.LENGTH_LONG).show();
                Log.v("update","nothing");
                break;
            case UpdateEvent.DOWNLOADINT:

                Toast.makeText(AppSettingActivity.this, R.string.downloading_firmware,Toast.LENGTH_LONG).show();
                Log.v("update","Downloading");
                break;
            case UpdateEvent.BADNET:
                Toast.makeText(AppSettingActivity.this, R.string.network_error,Toast.LENGTH_LONG).show();
                Log.v("update","badnet");
                break;
        }
    }

    class SetAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return null;
        }
    }
}
