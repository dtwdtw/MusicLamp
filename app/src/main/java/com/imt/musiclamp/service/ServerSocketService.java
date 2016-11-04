package com.imt.musiclamp.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.imt.musiclamp.MyApplication;
import com.imt.musiclamp.element.StartTimerEvent;
import com.imt.musiclamp.element.TimeEndEvent;
import com.imt.musiclamp.element.TimerEvent;
import com.imt.musiclamp.event.FindDevicesEvent;
import com.imt.musiclamp.event.OnHitEvent;
import com.imt.musiclamp.event.PlayCompleEvent;
import com.imt.musiclamp.event.PlayProgressEvent;
import com.imt.musiclamp.event.ProgressThreadEvent;
import com.imt.musiclamp.event.UpdateEvent;
import com.imt.musiclamp.event.UpdateState;
import com.imt.musiclamp.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Random;

import de.greenrobot.event.EventBus;

public class ServerSocketService extends Service {

    private static final int TCP_SERVER_PORT = 8888;
    private static final int UDP_SERVER_PORT = 6005;

    private boolean UDP_SERVER_ENABLE = true;
    private boolean TCP_SERVER_ENABLE = true;

    private InetAddress localAddress;

    private MyApplication myApplication;
    private WifiManager wifiManager;

    static CountDownTimer countDownTimer;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    public void onEventMainThread(final StartTimerEvent startTimerEvent){
        if(countDownTimer!=null){
            countDownTimer.cancel();
        }
        countDownTimer=new CountDownTimer(startTimerEvent.getEndtime() * 60 * 1000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                TimerEvent timerEvent=new TimerEvent();
                timerEvent.setEndsecond(millisUntilFinished);
                EventBus.getDefault().post(timerEvent);
            }

            @Override
            public void onFinish() {
                EventBus.getDefault().post(new TimeEndEvent());
            }
        }.start();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = (MyApplication) getApplication();
        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        EventBus.getDefault().register(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new UDPServerThread().start();

        return super.onStartCommand(intent, flags, startId);
    }

    /*

    socket server 第一次发送指令后会接收不到 再次开的时候会 address already in used

    因为receive 阻塞住了 ENABLE 设置为 false 也等不到下一次

    第二次开会再次打开

    明天试一下 打开之前 判断一下端口是否被占用

    第一次还是接收不到

     */

    DatagramSocket datagramSocket;

    class UDPServerThread extends Thread {

        @Override
        public void run() {
            try {
                byte[] data = new byte[1024];
                if (datagramSocket == null || datagramSocket.isClosed()) {
                    datagramSocket = new DatagramSocket(UDP_SERVER_PORT);
                    datagramSocket.setBroadcast(true);
                }
                while (UDP_SERVER_ENABLE) {
                    DatagramPacket datagramPacket = new DatagramPacket(data, data.length);
                    Log.d("UDP", "Watting for UDP broadcast");
                    datagramSocket.receive(datagramPacket);

                    new UDPHandleThread(datagramPacket).start();
                }
                datagramSocket.close();
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class UDPHandleThread extends Thread {

        private DatagramPacket datagramPacket;

        public UDPHandleThread(DatagramPacket datagramPacket) {
            this.datagramPacket = datagramPacket;
        }

        private String intToIp(int i)  {
            return (i & 0xFF)+ "." + ((i >> 8 ) & 0xFF) + "." + ((i >> 16 ) & 0xFF) +"."+((i >> 24 ) & 0xFF );
        }

        @Override
        public void run() {

            String ip = datagramPacket.getAddress().getHostAddress();
            byte[] data = Utils.subBytes(datagramPacket.getData(), 0, datagramPacket.getLength());
            Log.d("UDP", "Receive message ~" + new String(data) + "~ from " + ip + "threadId:" + Thread.currentThread().getId());
            try {
                JSONObject jsonObject = new JSONObject(new String(data));
                if("Song has been interupt".equals(jsonObject.getString("action"))){
                    WifiManager wifimanage=(WifiManager)getApplication().getSystemService(Context.WIFI_SERVICE);//获取WifiManager

                    if(!wifimanage.isWifiEnabled())  {
                        wifimanage.setWifiEnabled(true);
                    }
                    WifiInfo wifiinfo= wifimanage.getConnectionInfo();
                    String iptemp=intToIp(wifiinfo.getIpAddress());
                    if(iptemp.equals(jsonObject.getString("ip"))) {
                        EventBus.getDefault().post(new OnHitEvent());
                    }
                }
                if ("udp_device_info".equals(jsonObject.getString("action"))) {
                    FindDevicesEvent event = new FindDevicesEvent();
                    event.setIp(ip);
                    event.setMacAddress(jsonObject.getString("MAC"));
                    event.setPlayer(jsonObject.getString("player"));
                    event.setVolume(jsonObject.getInt("volume"));

                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.context);
                    preferences.edit().putInt("volume", jsonObject.getInt("volume")).commit();

                    event.setState(jsonObject.getString("state"));
                    event.setDate(jsonObject.getString("date"));
                    event.setNetwork(jsonObject.getString("network"));
                    event.setFlow(jsonObject.getBoolean("isFlow"));
                    event.setR(jsonObject.getInt("r"));
                    event.setG(jsonObject.getInt("g"));
                    event.setB(jsonObject.getInt("b"));
                    event.setW(jsonObject.getInt("w"));
                    EventBus.getDefault().post(event);
                }
                if ("music_player_state".equals(jsonObject.getString("action"))) {
                    if ("playing".equals(jsonObject.getString("state"))) {
                        if (!myApplication.isLocalDevicesPlaying()) {
                            PlayProgressEvent playProgressEvent = new PlayProgressEvent();
                            playProgressEvent.setLocalDevices(false);
                            playProgressEvent.setDuration(jsonObject.getInt("duration"));
                            playProgressEvent.setCurrentPosition(jsonObject.getInt("value"));
                            EventBus.getDefault().post(playProgressEvent);
                        }
                    } else if ("next_track".equals(jsonObject.getString("state"))) {
                        if (MyApplication.PLAY_MODE_CYCLE == myApplication.getCurrentPlayMode()) {
                            if (myApplication.isLocalMusicPlaying()) {
                                myApplication.setCurrentLocalPosition(myApplication.getCurrentLocalPosition() + 1);
                            } else {
                                myApplication.setCurrentOnlinePosition(myApplication.getCurrentOnlinePosition() + 1);
                            }
                        } else if (MyApplication.PLAY_MODE_RANDOM == myApplication.getCurrentPlayMode()) {
                            if (myApplication.isLocalMusicPlaying()) {
                                myApplication.setCurrentLocalPosition(new Random().nextInt(myApplication.getLocalMusicInfos().size()));
                            } else {
                                myApplication.setCurrentOnlinePosition(new Random().nextInt(myApplication.getOnlineMusicInfos().size()));
                            }
                        }

                        //comple事件是通知界面更新
                        PlayCompleEvent playCompleEvent = new PlayCompleEvent();
                        EventBus.getDefault().post(playCompleEvent);

                        if (myApplication.isLocalMusicPlaying()){
                            new Thread() {
                                @Override
                                public void run() {
                                    pauseRemoteDevices();
                                    startProgressThread();
                                    String ip = Utils.intToIp(wifiManager.getConnectionInfo().getIpAddress());
                                    Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, Utils.getPlayMusicJson(
                                            myApplication.getCurrentPlayingLamp().getMacAdress(),
                                            "local",
                                            (int) myApplication.getCurrentLocalMusicInfo().get_ID(),
                                            "http://" + ip + ":8080" + myApplication.getCurrentLocalMusicInfo().getFilePath().replace(Environment.getExternalStorageDirectory().getPath(), ""),
                                            myApplication.getCurrentLocalMusicInfo().getTitle(),
                                            myApplication.getCurrentLocalMusicInfo().getArtist(),
                                            myApplication.getCurrentLocalMusicInfo().getAlbum()
                                    ).getBytes());
                                }
                            }.start();
                        }else {
                            new Thread() {
                                @Override
                                public void run() {
                                    pauseRemoteDevices();
                                    startProgressThread();
                                    Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, Utils.getPlayMusicJson(
                                            myApplication.getCurrentPlayingLamp().getMacAdress(),
                                            "online",
                                            (int) myApplication.getCurrentOnlineMusicInfo().getSongId(),
                                            myApplication.getCurrentOnlineMusicInfo().getUrl(),
                                            myApplication.getCurrentOnlineMusicInfo().getTitle(),
                                            myApplication.getCurrentOnlineMusicInfo().getArtist(),
                                            myApplication.getCurrentOnlineMusicInfo().getAlbum()
                                    ).getBytes());

                                }
                            }.start();
                        }
                    }
                }
                if ("update_info".equals(jsonObject.getString("action"))) {
                    UpdateEvent updateEvent = new UpdateEvent();
                    updateEvent.setUpdateInfo(jsonObject.getString("info"));
                    EventBus.getDefault().post(updateEvent);
                }
                if ("update_progress".equals("action")) {
                    UpdateState updateState = new UpdateState();
                    updateState.setState(jsonObject.getInt("progress"));
                    EventBus.getDefault().post(updateState);

                }
            } catch (JSONException e) {
//                e.printStackTrace();
            }

        }
    }

    class TCPServerThread extends Thread {

        @Override
        public void run() {
            try {
                ServerSocket serverSocket = new ServerSocket(TCP_SERVER_PORT);
                while (TCP_SERVER_ENABLE) {
                    new TCPHandleThread(serverSocket.accept()).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class TCPHandleThread extends Thread {

        Socket socket;

        public TCPHandleThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                handleSocket();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void handleSocket() throws IOException {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String data;
            while ((data = bufferedReader.readLine()) != null) {
                stringBuilder.append(data);
            }
            bufferedReader.close();
            socket.close();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("service destory", "~");
        UDP_SERVER_ENABLE = false;
        datagramSocket.close();
    }

    private void pauseRemoteDevices() {
        try {
            Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, Utils.getMusicPauseJson(
                    myApplication.getCurrentPlayingLamp().getMacAdress()
            ).getBytes());
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    private void resumeRemoteDevices() {
        Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, Utils.getMusicResumeJson(
                myApplication.getCurrentPlayingLamp().getMacAdress()
        ).getBytes());
    }

    private void startProgressThread() {
        ProgressThreadEvent event = new ProgressThreadEvent();
        event.setStart(true);
        EventBus.getDefault().post(event);
    }

    private void stopProgressThread() {
        ProgressThreadEvent event = new ProgressThreadEvent();
        event.setStart(false);
        EventBus.getDefault().post(event);
    }

}
