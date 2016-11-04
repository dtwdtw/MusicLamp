package com.imt.musiclamp.elementClass;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.imt.musiclamp.MyApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.rong.imlib.RongIMClient;
import io.rong.message.VoiceMessage;

/**
 * Created by mac on 15/4/14.
 */
public abstract class GetDevices{
    public abstract void deviceBack(List<Device> deviceList);
    public GetDevices(Handler handler,String userID){
        new UserServer(handler,new StringURL(StringURL.getDivece).setUserID(MyApplication.userID).toString()) {
            @Override
            public void httpBack(JSONObject jsonObject) throws JSONException {
                List<Device> list=new ArrayList<>();
                JSONArray jsonArray=jsonObject.getJSONArray("deviceList");
                for(int i=0;i<jsonArray.length();i++){
                    Device device=new Device();
                    device.deviceName=jsonArray.getJSONObject(i).getString("deviceName");
                    device.tID=jsonArray.getJSONObject(i).getString("tid");
                    device.UUID=jsonArray.getJSONObject(i).getString("UUID");
                    list.add(device);
                }
                deviceBack(list);
            }
        };
    }

}
