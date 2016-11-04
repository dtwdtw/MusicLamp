package com.imt.musiclamp.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.imt.musiclamp.ConversationRong;
import com.imt.musiclamp.FriendActivity;
import com.imt.musiclamp.MyApplication;
import com.imt.musiclamp.R;
import com.imt.musiclamp.element.StartTimerEvent;
import com.imt.musiclamp.element.TimeEndEvent;
import com.imt.musiclamp.element.TimerEvent;
import com.imt.musiclamp.elementClass.MessageModle;
import com.imt.musiclamp.elementClass.StringURL;
import com.imt.musiclamp.elementClass.UserInfo;
import com.imt.musiclamp.elementClass.UserServer;
import com.imt.musiclamp.elementClass.getImg;
import com.imt.musiclamp.model.MessageModlesave;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import io.rong.imlib.RongIMClient;
import io.rong.message.ContactNotificationMessage;
import io.rong.message.ImageMessage;
import io.rong.message.RichContentMessage;
import io.rong.message.TextMessage;
import io.rong.message.VoiceMessage;

import static io.rong.imlib.RongIMClient.ConnectionStatusListener.ConnectionStatus.*;

/**
 * Created by mac on 15/4/20.
 */
public class RongPush extends Service {
    RongIMClient client;
    Handler handler = new Handler();

    @Override
    public IBinder onBind(Intent intent) {
//        try {
//            Log.v("token", MyApplication.rongToken);
//            client = RongIMClient.connect(MyApplication.rongToken, new RongIMClient.ConnectCallback() {
//                @Override
//                public void onSuccess(String s) {
//                    Log.v("success", "success");
//                    client.setOnReceiveMessageListener(new ResiveListener());
//                }
//
//                @Override
//                public void onError(ErrorCode errorCode) {
//                    client.reconnect(null);
//                    Log.v("failed", "failed");
//                }
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.v("connect error", "connect error");
//
//        }
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        RongIMClient.init(this);
        try {
            Log.v("token", MyApplication.rongToken);
            client = RongIMClient.connect(MyApplication.rongToken, new RongIMClient.ConnectCallback() {
                @Override
                public void onSuccess(String s) {
                    Log.v("success", "success");
                    client.setOnReceiveMessageListener(new ResiveListener());
//                    client.disconnect(false);

                    client.setConnectionStatusListener(new RongIMClient.ConnectionStatusListener() {
                        @Override
                        public void onChanged(ConnectionStatus connectionStatus) {
                            if (connectionStatus == DISCONNECTED) {
                                Log.v("connectionstatus", "连接断开");
                            } else if (connectionStatus == KICKED_OFFLINE_BY_OTHER_CLIENT) {

                                Log.v("connectionstatus", "用户账户在其他设备登录，本机会被踢掉线。");
                            } else if (connectionStatus == NETWORK_UNAVAILABLE) {
                                Log.v("connectionstatus", "网络不可用。");

                            }
                        }
                    });
                }

                @Override
                public void onError(ErrorCode errorCode) {
                    client.reconnect(null);
//                    client.disconnect(false);
                    Log.v("failed", "failed");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.v("connect error", "connect error");

        }
    }

    public void onEvent(final MessageModle messageModle) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (client == null) {
                    try {
                        Log.v("token", MyApplication.rongToken);
                        client = RongIMClient.connect(MyApplication.rongToken, new RongIMClient.ConnectCallback() {
                            @Override
                            public void onSuccess(String s) {
                                Log.v("success", "success");
                                client.setOnReceiveMessageListener(new ResiveListener());
                            }

                            @Override
                            public void onError(ErrorCode errorCode) {
                                Log.v("failed", "failed");
                            }
                        });
//                        client.disconnect(false);
                    } catch (Exception e) {
                        client.reconnect(null);
//                        client.disconnect(false);
                        e.printStackTrace();
                        Log.v("connect error", "connect error");

                    }
                }
                Log.v("friendID",messageModle.getFriendID());
                if(client==null){
                    Log.v("clientnull","clientnull");
                }
                if(messageModle.getMessage().getContent()==null){
                    Log.v("messagenull","messagenull");
                }
//                Log.v("title",((RichContentMessage)messageModle.getMessage().getContent()).getTitle());

                try {
                    client.sendMessage(RongIMClient.ConversationType.PRIVATE, messageModle.getFriendID(), messageModle.getMessage().getContent(), null);
                }catch(Exception e){
                    try {
                        client = RongIMClient.connect(MyApplication.rongToken, null);
//                        client.disconnect(false);
                    }catch(Exception e2){
                        e2.printStackTrace();
                        client.reconnect(null);
//                        client.disconnect(false);
                    }
                }
                MessageModlesave messageModlesave = new MessageModlesave();
                messageModlesave.setMessage(messageModle.getMessage());
                messageModlesave.setFriendID(messageModle.getMessage().getTargetId());

                if (MyApplication.modlesaveList.containsKey(messageModle.getFriendID())) {
                    MyApplication.modlesaveList.get(messageModle.getFriendID()).add(messageModlesave);
                    Log.v("listsizecon", MyApplication.modlesaveList.get(messageModle.getFriendID()).size() + "");
                } else {
                    List<MessageModlesave> modlesaveList = new ArrayList<>();
                    modlesaveList.add(messageModlesave);
                    MyApplication.modlesaveList.put(messageModle.getFriendID(), modlesaveList);
                    Log.v("listsize", MyApplication.modlesaveList.get(messageModle.getFriendID()).size() + "");
                }

                Log.v("sended", "successful");
                Log.v("togetID", messageModle.getFriendID());
            }
        }).start();
    }

    class ResiveListener implements RongIMClient.OnReceiveMessageListener {

        @Override
        public void onReceived(final RongIMClient.Message message, int i) {
            if(message.getContent() instanceof ContactNotificationMessage){


                ContactNotificationMessage contactNotificationMessage=(ContactNotificationMessage)message.getContent();
                NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                int icon = R.drawable.icon;
                CharSequence tickerText = "好友ID："+contactNotificationMessage.getSourceUserId();

                Log.v("received","message");

                Notification objNotification = new Notification(icon,
                        tickerText,
                        System.currentTimeMillis());
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putBoolean("toBig", true);
                UserInfo.phone = null;
                UserInfo.userName = null;
                UserInfo.gender = null;
                UserInfo.id = contactNotificationMessage.getSourceUserId();
                UserInfo.bitmap = null;
                UserInfo.userBit = null;
                intent.putExtras(bundle);
                intent.setClass(RongPush.this, ConversationRong.class);
                PendingIntent objPendingIntent = PendingIntent.getActivity(RongPush.this,
                        0,
                        intent,
                        0);

//                objNotification.setLatestEventInfo(RongPush.this,
//                        "新的好友请求",
//                        UserInfo.userName,
//                        objPendingIntent);
                objNotification=new NotificationCompat.Builder(RongPush.this)
                        .setContentTitle("新的好友请求")
                        .setContentText(UserInfo.userName)
                        .setSmallIcon(icon)
                        .build();
                objNotification.ledARGB = 0xff5500dd;
                objNotification.defaults = Notification.DEFAULT_ALL;

                // 添加入通知管理器中
                nm.notify(0,
                        objNotification);
            }
            else {


                new UserServer(RongPush.this, handler, new StringURL(StringURL.getUserInfoByID).setUserID(message.getSenderUserId()).toString()) {

                    @Override
                    public void httpBack(final JSONObject jsonObject) throws JSONException {

                        new getImg(new Handler(),jsonObject.getString("headIconURL")) {
                            @Override
                            public void bitMapBack(Bitmap bitmap) {

                                NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                int icon = R.drawable.small;
                                CharSequence tickerText = "您有新的消息";
                                Notification objNotification = new Notification(icon,
                                        tickerText,
                                        System.currentTimeMillis());

                                try {
                                    Intent intent = new Intent();
                                    Bundle bundle = new Bundle();
                                    bundle.putBoolean("toBig", true);
                                    UserInfo.phone = jsonObject.getString("phoneNumber");
                                    UserInfo.userName = jsonObject.getString("nickName");
                                    UserInfo.gender = jsonObject.getString("sex");
                                    UserInfo.id = jsonObject.getString("userID");
                                    UserInfo.bitmap = bitmap;
                                    UserInfo.userBit = MyApplication.myHead;
                                    intent.putExtras(bundle);
                                    intent.setClass(RongPush.this, ConversationRong.class);

                                    MessageModlesave messageModlesave = new MessageModlesave();
                                    messageModlesave.setMessage(message);
                                    messageModlesave.setFriendID(message.getSenderUserId());
                                    messageModlesave.setBitmap(bitmap);
                                    messageModlesave.setUserBit(MyApplication.myHead);
                                    messageModlesave.setGender(UserInfo.gender);
                                    messageModlesave.setPhone(UserInfo.phone);
                                    messageModlesave.setUserName(UserInfo.userName);
                                    if (MyApplication.modlesaveList.containsKey(message.getSenderUserId())) {
                                        MyApplication.modlesaveList.get(message.getSenderUserId()).add(messageModlesave);
                                        Log.v("listsizecon", MyApplication.modlesaveList.get(message.getSenderUserId()).size() + "");
                                    } else {
                                        List<MessageModlesave> modlesaveList = new ArrayList<>();
                                        modlesaveList.add(messageModlesave);
                                        MyApplication.modlesaveList.put(message.getSenderUserId(), modlesaveList);
                                        Log.v("listsize", MyApplication.modlesaveList.get(message.getSenderUserId()).size() + "");
                                    }

                                    Log.v("lovekk", "lovekk");
                                    // 如果有振动或者全部提示方式，必须在 AndroidManifest.xml 加入振动权限
                                    PendingIntent objPendingIntent = PendingIntent.getActivity(RongPush.this,
                                            0,
                                            intent,
                                            0);

//                                    objNotification.setLatestEventInfo(RongPush.this,
//                                            "YOU HAVE A NEW MESSAGE!",
//                                            UserInfo.userName,
//                                            objPendingIntent);
                                    objNotification=new NotificationCompat.Builder(RongPush.this)
                                            .setContentTitle("YOU HAVE A NEW MESSAGE!")
                                            .setContentText(UserInfo.userName)
                                            .setSmallIcon(icon)
                                            .build();
                                    objNotification.ledARGB = 0xff5500dd;
                                    objNotification.defaults = Notification.DEFAULT_ALL;

                                    // 添加入通知管理器中
                                    nm.notify(0,
                                            objNotification);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                EventBus.getDefault().post(message);
                            }
                        };

                    }
                };
            }

//            Log.v("messagetype", getMessageType(message));
        }
    }

    public String getMessageType(RongIMClient.Message message) {
        String temptype = null;
        if (message.getContent() instanceof TextMessage) {//文本消息
//          temp=(((TextMessage) message.getContent()).getContent());
            temptype = "text";
            Log.d("messagetype", "onSent-TextMessage:" + ((TextMessage) message.getContent()).getContent());
        } else if (message.getContent() instanceof ImageMessage) {//图片消息
            ImageMessage imageMessage = (ImageMessage) message.getContent();
            temptype = ("pic");
            Log.d("messagetype", "onSent-ImageMessage:" + imageMessage.getRemoteUri());
        } else if (message.getContent() instanceof VoiceMessage) {//语音消息
            VoiceMessage voiceMessage = (VoiceMessage) message.getContent();
            temptype = ("voice");
            Log.d("messagetype", "onSent-voiceMessage:" + "VoiceMessage");
        } else if (message.getContent() instanceof RichContentMessage) {//图文消息
            RichContentMessage richContentMessage = (RichContentMessage) message.getContent();
            temptype = ("rich");
            Log.d("messagetype", "onSent-RichContentMessage:" + richContentMessage.getContent());
        } else {
            temptype = "other";
            Log.d("messagetype", "onSent-其他消息，自己来判断处理");
        }
        return temptype;
    }
}
