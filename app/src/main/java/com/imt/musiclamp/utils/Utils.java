package com.imt.musiclamp.utils;

import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.RelativeLayout;

import com.activeandroid.query.Select;
import com.imt.musiclamp.MyApplication;
import com.imt.musiclamp.R;
import com.imt.musiclamp.model.Lamp;
import com.imt.musiclamp.model.LocalMusicInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Utils {

    //获取专辑封面的Uri
    private static final Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");

    public static List<LocalMusicInfo> getAllMusic(Context context) {
        List<LocalMusicInfo> musicInfos = new ArrayList<>();
        Cursor cursor = context.getContentResolver()
                .query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        for (int i = 0; i < cursor.getCount(); i++) {
            LocalMusicInfo musicInfo = new LocalMusicInfo();
            cursor.moveToNext();
            long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
            String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            String album = cursor.getString((cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
            String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
            boolean isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC)) == 1 ? true : false;
            long albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            if (isMusic) {
                musicInfo.set_ID(id);
                musicInfo.setTitle(title);
                musicInfo.setArtist(artist);
                musicInfo.setAlbum(album);
                musicInfo.setDuration(duration);
                musicInfo.setSize(size);
                musicInfo.setFilePath(filePath);
                musicInfo.setAlbumId(albumId);
                musicInfos.add(musicInfo);
            }

        }
        return musicInfos;
    }

    /**
     * 格式化时间，将毫秒转换为分:秒格式
     *
     * @param time
     * @return
     */
    public static String millis(long time) {
        String min = time / (1000 * 60) + "";
        String sec = time % (1000 * 60) + "";
        if (min.length() < 2) {
            min = "0" + time / (1000 * 60) + "";
        } else {
            min = time / (1000 * 60) + "";
        }
        if (sec.length() == 4) {
            sec = "0" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 3) {
            sec = "00" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 2) {
            sec = "000" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 1) {
            sec = "0000" + (time % (1000 * 60)) + "";
        }
        return min + ":" + sec.trim().substring(0, 2);
    }


    /**
     * 获取默认专辑图片
     *
     * @param context
     * @return
     */
    public static Bitmap getDefaultArtwork(Context context, boolean small) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.RGB_565;
        if (small) {  //返回小图片
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
        }
        return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
    }


    /**
     * 从文件当中获取专辑封面位图
     *
     * @param context
     * @param songid
     * @param albumid
     * @return
     */
    private static Bitmap getArtworkFromFile(Context context, long songid, long albumid) {
        Bitmap bm = null;
        if (albumid < 0 && songid < 0) {
            throw new IllegalArgumentException("Must specify an album or a song id");
        }
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            FileDescriptor fd = null;
            if (albumid < 0) {
                Uri uri = Uri.parse("content://media/external/audio/media/"
                        + songid + "/albumart");
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    fd = pfd.getFileDescriptor();
                }
            } else {
                Uri uri = ContentUris.withAppendedId(albumArtUri, albumid);
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    fd = pfd.getFileDescriptor();
                }
            }
            options.inSampleSize = 1;
            // 只进行大小判断
            options.inJustDecodeBounds = true;
            // 调用此方法得到options得到图片大小
            BitmapFactory.decodeFileDescriptor(fd, null, options);
            // 我们的目标是在800pixel的画面上显示
            // 所以需要调用computeSampleSize得到图片缩放的比例
            options.inSampleSize = 100;
            // 我们得到了缩放的比例，现在开始正式读入Bitmap数据
            options.inJustDecodeBounds = false;
            options.inDither = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            //根据options参数，减少所需要的内存
            bm = BitmapFactory.decodeFileDescriptor(fd, null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bm;
    }

    public static Uri getArtworkUri(long album_id) {
        Uri uri = ContentUris.withAppendedId(albumArtUri, album_id);
        return uri;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static void sendMulticastUDP(int port, byte[] data) {
        try {
            Log.e("Send UDP", ":" + port + " content:" + Utils.byte2hex(data));
            InetAddress inetAddress = InetAddress.getByName("255.255.255.255");
            MulticastSocket multicastSocket = new MulticastSocket();
            DatagramPacket datagramPacket = new DatagramPacket(data, 0, data.length, inetAddress, port);
            multicastSocket.send(datagramPacket);
            multicastSocket.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendUDP(String ip, int port, byte[] data) {
        try {

            Log.e("Send UDP", "ip:" + ip + ":" + port + " content:" + Utils.byte2hex(data));

            InetAddress address = InetAddress.getByName(ip);
            DatagramPacket datagramPacket = new DatagramPacket(data, 0, data.length, address, port);
            DatagramSocket datagramSocket = new DatagramSocket();
            datagramSocket.send(datagramPacket);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isFirst(Context context) {
        final SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        if (preferences.getBoolean("isFirst", true)) {
            editor.putBoolean("isFirst", false);
            editor.commit();
            return true;
        }
        return false;
    }


    public static byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        for (int i = begin; i < begin + count; i++) bs[i - begin] = src[i];
        return bs;
    }

    public static String byte2hex(byte[] buffer) {
        String h = "";
        for (int i = 0; i < buffer.length; i++) {
            String temp = Integer.toHexString(buffer[i] & 0xFF);
            if (temp.length() == 1) {
                temp = "0" + temp;
            }
            h = h + " " + temp;
        }
        return h;
    }

    public static byte[] getMacForPacketData(byte[] pakcetData) {
        byte[] macAddress = new byte[6];
        for (int i = 0; i < 6; i++) {
            macAddress[i] = pakcetData[i + 1];
        }
        return macAddress;
    }

    public static String getSyncJson() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("action", "sync_devices");
            return jsonObject.toString();
        } catch (JSONException e) {

            e.printStackTrace();
        }
        return null;
    }

    public static String getFindJson() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("action", "find_devices");
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getSetLampJson(String macAddress, boolean state) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("action", "light_control");
            jsonObject.put("MAC", macAddress);
            if (state) {
                jsonObject.put("type", "on");
            } else {
                jsonObject.put("type", "off");
            }
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getSetWifiJson(String macAddress, String ssid, String password) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("action", "set_sta");
            jsonObject.put("MAC", macAddress);
            jsonObject.put("ssid", ssid);
            jsonObject.put("password", password);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getSetApJson(String macAddress, String ssid, String password) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("action", "set_ap");
            jsonObject.put("MAC", macAddress);

            //DTW 5.16 0为有效 1为无效恢复默认
            jsonObject.put("default", 1);
            jsonObject.put("ssid", ssid);
            jsonObject.put("password", password);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getSetEqualizer(String macAddress, int band, int level) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("action", "set_equalizer");
            jsonObject.put("band", band);
            jsonObject.put("level", level);

            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();

            return null;
        }
    }

    public static String getSetLampColorJson(String macAddress, int r, int g, int b, int x) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("action", "set_light");
            jsonObject.put("MAC", macAddress);
            jsonObject.put("r", r);
            jsonObject.put("g", g);
            jsonObject.put("b", b);
            jsonObject.put("x", x);
            Log.e("getColor", jsonObject.toString());
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getRestartServiceJson(String macAddress,String action,String service){

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("action", action);
            jsonObject.put("MAC", macAddress);
            jsonObject.put("service", service);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getSetTimingJson(String macAddress, String task, int month, int day, int hour, int minute, int delay) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("action", "set_task");
            jsonObject.put("MAC", macAddress);
            jsonObject.put("task", task);
            jsonObject.put("month", month);
            jsonObject.put("day", day);
            jsonObject.put("hour", hour);
            jsonObject.put("minute", minute);
            jsonObject.put("delay", delay);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getDeleteAllTimingJson(String macAddress, int match) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("action", "delete_task");
            jsonObject.put("MAC", macAddress);
            jsonObject.put("id", -2);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getPlayMusicJson(String macAddress, String category, int songId, String url, String name, String artist, String album) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("action", "play_audio");
            jsonObject.put("category", category);
            jsonObject.put("MAC", macAddress);


            //DTW 5.16
            jsonObject.put("id", "" + songId);

            jsonObject.put("url", url);
            jsonObject.put("name", name);//ming
            jsonObject.put("artist", artist);
            jsonObject.put("album", album);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getSetMusicJson(String macAddress, String type, int position) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("action", "find_devices");
            jsonObject.put("MAC", macAddress);
            jsonObject.put("type", type);
            jsonObject.put("position", position);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getSetVolumeJson(String macAddress, String type, int volume) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("action", "volume");
            jsonObject.put("MAC", macAddress);
            jsonObject.put("type", type);
            jsonObject.put("volume", volume);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getMusicStateJson(String macAddress) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("action", "get_music_control");
            jsonObject.put("MAC", macAddress);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getMusicStopJson(String macAddress) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("action", "music_control");
            jsonObject.put("MAC", macAddress);
            jsonObject.put("type", "stop");
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getMusicPauseJson(String macAddress) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("action", "music_control");
            jsonObject.put("MAC", macAddress);
            jsonObject.put("type", "pause");
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getMusicResumeJson(String macAddress) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("action", "music_control");
            jsonObject.put("MAC", macAddress);
            jsonObject.put("type", "start");
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getMusicSeekJson(String macAddress, int position) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("action", "music_control");
            jsonObject.put("MAC", macAddress);
            jsonObject.put("type", "seek");
            jsonObject.put("position", position);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getAddSceneJson(String macAddress, int r, int g, int b, int x, JSONArray jsonArrayMusicList) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("action", "add_scene");
            jsonObject.put("MAC", macAddress);
            jsonObject.put("r", r);
            jsonObject.put("g", g);
            jsonObject.put("b", b);
            jsonObject.put("x", x);
            jsonObject.put("songList", jsonArrayMusicList);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getCandleModeJson(String macAddress) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("action", "atmosphere");
            jsonObject.put("MAC", macAddress);
            jsonObject.put("type", "candle");
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getNightModeJson(String macAddress){

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("action", "atmosphere");
            jsonObject.put("MAC", macAddress);
            jsonObject.put("type", "night");
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    //wifimanager获取ip使用
    public static String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

    public static int getVersionCode(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);

            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();

            return 0;
        }
    }

    public static String getVersionName(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);

            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();

            return null;
        }
    }
}
