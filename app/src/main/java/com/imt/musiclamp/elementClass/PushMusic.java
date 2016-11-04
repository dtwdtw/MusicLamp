package com.imt.musiclamp.elementClass;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import com.imt.musiclamp.MyApplication;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import io.rong.imlib.RongIMClient;
import io.rong.message.VoiceMessage;

/**
 * Created by mac on 15/4/14.
 */
public class PushMusic {
//    RongIMClient client=null;
//    public PushMusic(Context context, final String deviceID, final String  musicFilePath, final SendStateCallBack sendState){
//        RongIMClient.init(context);
//        try {
//            client= RongIMClient.connect(MyApplication.rongToken, new RongIMClient.ConnectCallback() {
//                @Override
//                public void onSuccess(String s) {
//                    MediaPlayer mediaPlayer=new MediaPlayer();
//                    try {
//                        mediaPlayer.setDataSource(musicFilePath);
//                        Log.v("MusicFilePath", musicFilePath);
//                        mediaPlayer.prepare();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    Uri uri= Uri.fromFile(new File(musicFilePath));
//                    VoiceMessage voiceMessage=VoiceMessage.obtain(uri,mediaPlayer.getDuration()/1000);
//                    client.sendMessage(RongIMClient.ConversationType.PRIVATE, deviceID, voiceMessage, new RongIMClient.SendMessageCallback() {
//                        @Override
//                        public void onSuccess(int i) {
//                            sendState.onSendSuccess();
//                        }
//
//                        @Override
//                        public void onError(int i, ErrorCode errorCode) {
//                            sendState.onSendError();
//                        }
//
//                        @Override
//                        public void onProgress(int i, int i1) {
//
//                        }
//                    });
//                }
//
//                @Override
//                public void onError(ErrorCode errorCode) {
//
//                    client.reconnect(null);
//                }
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//    public interface SendStateCallBack{
//        void onSendSuccess();
//        void onSendError();
//        void onConnectError();
//    }
}
