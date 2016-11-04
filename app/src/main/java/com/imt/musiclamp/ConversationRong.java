package com.imt.musiclamp;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Path;
import android.media.Image;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.text.format.Time;
import android.util.JsonToken;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.annotation.Column;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.bumptech.glide.Glide;
import com.imt.musiclamp.elementClass.Device;
import com.imt.musiclamp.elementClass.GetDevices;
import com.imt.musiclamp.elementClass.MessageModle;
import com.imt.musiclamp.elementClass.StringURL;
import com.imt.musiclamp.elementClass.UserInfo;
import com.imt.musiclamp.elementClass.UserServer;
import com.imt.musiclamp.elementClass.getImg;
import com.imt.musiclamp.event.PauseEvent;
import com.imt.musiclamp.event.PlayEvent;
import com.imt.musiclamp.model.MessageModlesave;
import com.imt.musiclamp.model.OnlineMusicInfo;
import com.imt.musiclamp.utils.Utils;
import com.sea_monster.core.resource.ResourceManager;
import com.sea_monster.core.resource.model.Resource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import de.greenrobot.event.EventBus;
import io.rong.imlib.RongIMClient;
import io.rong.message.ImageMessage;
import io.rong.message.RichContentMessage;
import io.rong.message.TextMessage;
import io.rong.message.VoiceMessage;

/**
 * Created by dtw on 15/3/16.
 */
public class ConversationRong extends Activity {
    Button sendVoice, sendMessage, shareMusic;
    ImageView friendInfo;
    ImageButton back;
    EditText text;
    int saveNum = 0;
    List<RongIMClient.Message> listMessage = new ArrayList<>();
    MediaRecorder recorder;
    String PATH_NAME;
    ListView converView;
    Bitmap userBit, friendBit;
    MediaPlayer mediaPlayer;
    //    MediaPlayer myPlay;
    TextView textView;
    long timeL;
    String name;
    String friendID;
    String gender;
    Time t = new Time();
    Bundle bundle;
    Handler handler = new Handler();
    String friendInfostr, friendHeadUrl;
    //    RongIMClient client = null;
    VoiceMessage voiceMessage = null;
    List<View> listconversation = new ArrayList<>();
//    List<String> pathlist = new ArrayList<>();
//    List<File> fileList = new ArrayList<>();
//    List<Integer> integerList = new ArrayList<>();
//    HashMap<Integer, Bitmap> richImg = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversation);
        listconversation.clear();
        textView = (TextView) findViewById(R.id.tab);
//        myPlay=new MediaPlayer();
        recorder = new MediaRecorder();
        mediaPlayer = new MediaPlayer();
        bundle = getIntent().getExtras();
        if (bundle == null || bundle.containsKey("toBig")) {
            userBit = UserInfo.userBit;
            friendBit = UserInfo.bitmap;
            name = UserInfo.userName;
            friendID = UserInfo.id;
            gender = UserInfo.gender;
        } else {
//            userBit = bundle.getParcelable("userImg");
//            friendBit = bundle.getParcelable("friendImg");

//            bundle.putString("userHeadUrl", userHeadurl);


            name = bundle.getString("nickname");
            friendHeadUrl = bundle.getString("friendHeadUrl");
            friendInfostr = bundle.getString("friendInfo");
            gender = bundle.getString("gender");
            friendID = bundle.getString("friendID");


        }
        EventBus.getDefault().register(this);
        textView.setText(name);
        converView = (ListView) findViewById(R.id.converList);
        sendVoice = (Button) findViewById(R.id.sendS);
        sendMessage = (Button) findViewById(R.id.sendM);
        friendInfo = (ImageView) findViewById(R.id.info);
        back = (ImageButton) findViewById(R.id.back);
        text = (EditText) findViewById(R.id.text);
        shareMusic = (Button) findViewById(R.id.music);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);//得到系统服务
        manager.cancel(0);
        if (MyApplication.modlesaveList.containsKey(friendID)) {
            for (int i = 0; i < MyApplication.modlesaveList.get(friendID).size(); i++) {
                listMessage.add(MyApplication.modlesaveList.get(friendID).get(i).getMessage());

            }
//            MyApplication.modlesaveList.remove(friendID);
            converView.setAdapter(new adapter());
            converView.setSelection(listMessage.size() - 1);

        }
//        try {
//            RongIMClient.init(this);
//            client = RongIMClient.connect(MyApplication.rongToken, new RongIMClient.ConnectCallback() {
//                @Override
//                public void onSuccess(String s) {
//
//                    client.setOnReceiveMessageListener(new ResiveListener());
////                    getHistoryMessage();
////                    getLatestMessage();
//                }
//
//                @Override
//                public void onError(ErrorCode errorCode) {
//
//                }
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        shareMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Cursor cursor;
//                AlertDialog.Builder builder = new AlertDialog.Builder(ConversationRong.this);
//                ListView listFile = new ListView(ConversationRong.this);
//                builder.setTitle("选择一个音频文件");
//
//                ContentResolver cr = ConversationRong.this.getContentResolver();
//                if (cr != null) {
//                    cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
//                            null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
//                    if (cursor.moveToFirst()) {
//                        do {
//                            File file = new File(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
//                            pathlist.add(file.getName());
//                            fileList.add(file);
//                            integerList.add(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
//                        }
//                        while (cursor.moveToNext());
//                    }
//                }
//                listFile.setAdapter(new ArrayAdapter(ConversationRong.this, android.R.layout.simple_expandable_list_item_1, pathlist));
//                builder.setView(listFile);
//                builder.show();
//                listFile.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
//                        final VoiceMessage voiceMessage = VoiceMessage.obtain(Uri.fromFile(fileList.get(position)), integerList.get(position) / 1000);
//                        final MediaPlayer mediaPlayer = new MediaPlayer();
////                        try {
////                            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
////                                @Override
////                                public void onPrepared(MediaPlayer mp) {
////                                    mediaPlayer.start();
////                                }
////                            });
////                            mediaPlayer.setDataSource(fileList.get(position).getPath());
////                            mediaPlayer.prepare();
////                        } catch (IOException e) {
////                            e.printStackTrace();
////                        }
//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//
//                                client.sendMessage(RongIMClient.ConversationType.PRIVATE, friendID, voiceMessage, null);
//                            }
//                        }).start();
//                    }
//                });


                final List<OnlineMusicInfo> myLikeMusicInfos;
                final AlertDialog.Builder builder = new AlertDialog.Builder(ConversationRong.this);
                builder.setTitle("请选择歌曲");
//                try {
                myLikeMusicInfos = new Select().from(OnlineMusicInfo.class)
                        .where("like = ?", true)
                        .execute();

                Log.v("musiccount", myLikeMusicInfos.size() + "");

                ListView musicList = new ListView(ConversationRong.this);
                musicList.setAdapter(new BaseAdapter() {
                    @Override
                    public int getCount() {
                        return myLikeMusicInfos.size();
                    }

                    @Override
                    public Object getItem(int position) {
                        return myLikeMusicInfos.get(position);
                    }

                    @Override
                    public long getItemId(int position) {
                        return position;
                    }

                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View musicshare = View.inflate(ConversationRong.this, R.layout.musicshareview, null);
                        TextView musicinfo = (TextView) musicshare.findViewById(R.id.musicinfo);
                        ImageView musicIcon = (ImageView) musicshare.findViewById(R.id.musicIcon);
                        musicinfo.setText(myLikeMusicInfos.get(position).getTitle() + "\n" + myLikeMusicInfos.get(position).getArtist());
                        return musicshare;
                    }
                });
                builder.setView(musicList);
                final AlertDialog alertdialog = builder.show();


                musicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        RongIMClient.Message message = new RongIMClient.Message();
//                            RichContentMessage richContentMessage=new RichContentMessage(myLikeMusicInfos.get(position).getTitle(),myLikeMusicInfos.get(position).getArtist(),"");
//                        RichContentMessage richContentMessage = new RichContentMessage();
//                            richContentMessage.setTitle(myLikeMusicInfos.get(position).getTitle());
//                            richContentMessage.setContent(myLikeMusicInfos.get(position).getArtist());
//                            richContentMessage.setUrl("");
//                            richContentMessage.setExtra(Utils.getPlayMusicJson("", "", myLikeMusicInfos.get(position).getSongId(), myLikeMusicInfos.get(position).getUrl(), myLikeMusicInfos.get(position).getTitle(), myLikeMusicInfos.get(position).getArtist(), myLikeMusicInfos.get(position).getAlbum()));

//                        richContentMessage.setTitle("hello");
//                        richContentMessage.setContent("lvoe");
//                        richContentMessage.setUrl("http://www.baidu.com");
//                        richContentMessage.setExtra("hello");

                        TextMessage textMessage = new TextMessage("imt_music");
                        textMessage.setExtra(Utils.getPlayMusicJson("", "", myLikeMusicInfos.get(position).getSongId(), myLikeMusicInfos.get(position).getUrl(), myLikeMusicInfos.get(position).getTitle(), myLikeMusicInfos.get(position).getArtist(), myLikeMusicInfos.get(position).getArtworkUrl()));


                        MessageModle messageModle = new MessageModle();
                        messageModle.setFriendID(friendID);
                        message.setContent(textMessage);
                        message.setMessageDirection(RongIMClient.MessageDirection.SEND);
                        messageModle.setMessage(message);
                        EventBus.getDefault().post(messageModle);


                        listMessage.add(message);
                        converView.setAdapter(new adapter());
                        converView.setSelection(listMessage.size() - 1);
                        alertdialog.dismiss();
                    }
                });
//                }catch(Exception e){
//                    Toast.makeText(ConversationRong.this,"请添我红心歌单",Toast.LENGTH_LONG).show();
//                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                EventBus.getDefault().unregister(this);
                finish();
            }
        });
        this.setTitle(name);
        findViewById(R.id.change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager im = (InputMethodManager) getSystemService(ConversationActivity.INPUT_METHOD_SERVICE);
                if (sendVoice.getVisibility() == View.VISIBLE) {
                    ((Button) findViewById(R.id.change)).setText(getResources().getString(R.string.text));
                    sendVoice.setVisibility(View.INVISIBLE);
                    text.setVisibility(View.VISIBLE);
                    sendMessage.setVisibility(View.VISIBLE);
                    im.showSoftInput(text, InputMethodManager.SHOW_FORCED);
                } else {
                    ((Button) findViewById(R.id.change)).setText(getResources().getString(R.string.voice));
                    sendVoice.setVisibility(View.VISIBLE);
                    text.setVisibility(View.INVISIBLE);
                    sendMessage.setVisibility(View.INVISIBLE);

                    im.hideSoftInputFromWindow(text.getWindowToken(), 0);
                }
            }
        });
        friendInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
//                Bundle bundle1 = new Bundle();
//                bundle1.putBoolean("toBig", true);
                if (bundle.containsKey("love")) {
                    bundle.putString("love", "1");
                }
                intent.putExtras(bundle);
                intent.setClass(ConversationRong.this, FriendInfo.class);
                intent.putExtra("position", getIntent().getIntExtra("position", 0));
                startActivity(intent);
            }
        });
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RongIMClient.Message message = new RongIMClient.Message();
                TextMessage textMessage = new TextMessage(text.getText().toString());
                message.setContent(textMessage);
                message.setMessageDirection(RongIMClient.MessageDirection.SEND);
//                msg.add(text.getText().toString());
//                client.sendMessage(RongIMClient.ConversationType.PRIVATE, friendID, textMessage, null);
                MessageModle messageModle = new MessageModle();
                messageModle.setFriendID(friendID);
                messageModle.setMessage(message);
                EventBus.getDefault().post(messageModle);
                listMessage.add(message);
                converView.setAdapter(new adapter());
                text.setText("");
                converView.setSelection(listMessage.size() - 1);


            }
        });
        sendVoice.setOnTouchListener(new View.OnTouchListener()

        {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    sendVoice.setText("松开发送");
                    Log.v("down", "down");
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    PATH_NAME = ConversationRong.this.getExternalCacheDir().toString() + "/" + (listMessage.size()) + "temp.amr";
                    recorder = new MediaRecorder();
                    recorder.setOutputFile(PATH_NAME);
                    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    try {
                        recorder.prepare();
                        recorder.start();   // Recording is now started
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    t.setToNow();
                    timeL = t.toMillis(true);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    Log.v("up", "up");
                    sendVoice.setText("按下录音");
                    try {
                        recorder.stop();
                        recorder.reset();
                    } catch (Exception e) {
                        Toast.makeText(ConversationRong.this, R.string.send_failure, Toast.LENGTH_LONG).show();
                        return false;
                    }
//                    recorder.release(); // Now
                    t.setToNow();
                    timeL = t.toMillis(true) - timeL;
                    timeL = timeL / 1000;
                    if (timeL < 1) {
                        Toast.makeText(ConversationRong.this, R.string.recording_time_too_short, Toast.LENGTH_LONG).show();
                    } else {
                        try {
                            mediaPlayer.setDataSource(PATH_NAME);
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
//                    FileInputStream fileInputStream = null;
//                    try {
//                        fileInputStream = new FileInputStream(PATH_NAME);
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    }
//                    byte[] bytes = new byte[0];
//                    try {
//                        bytes = new byte[fileInputStream.available()];
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    try {
//                        fileInputStream.read(bytes);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    Log.v("length", bytes.length + "");
                        Log.v("FriendID", friendID);
//                    voiceMessage = new VoiceMessage(bytes);
//                    voiceMessage.setUri(Uri.fromFile(new File(PATH_NAME)));
//                    voiceMessage.setDuration((int)timeL);
                        Uri uri = Uri.fromFile(new File(PATH_NAME));
                        voiceMessage = VoiceMessage.obtain(uri, (int) timeL);
                        RongIMClient.Message message = new RongIMClient.Message();
                        message.setContent(voiceMessage);
                        message.setMessageDirection(RongIMClient.MessageDirection.SEND);
                        listMessage.add(message);
                        converView.setAdapter(new adapter());
                        converView.setSelection(listMessage.size() - 1);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                RongIMClient.Message message1 = new RongIMClient.Message();
                                message1.setContent(voiceMessage);
                                message1.setMessageDirection(RongIMClient.MessageDirection.SEND);
                                MessageModle messageModle = new MessageModle();
                                messageModle.setFriendID(friendID);
                                messageModle.setMessage(message1);
                                EventBus.getDefault().post(messageModle);
                            }
                        }).start();
//                    Uri uri = Uri.fromFile(new File(PATH_NAME));
//                    final VoiceMessage voiceMessage = VoiceMessage.obtain(uri, (int) timeL);

//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                    }
                }
                return false;
            }
        });

        converView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,final int position, long id) {

                if (getMessageType(listMessage.get(position)).equals("voice")) {
                    final List<String> deviceList = new ArrayList<String>();
                    final AlertDialog.Builder builder = new AlertDialog.Builder(ConversationRong.this);
                    new UserServer(ConversationRong.this, handler, new StringURL(StringURL.getDivece).setUserID(MyApplication.userID).toString()) {

                        @Override
                        public void httpBack(JSONObject jsonObject) throws JSONException {
                            JSONArray jsonArray = jsonObject.getJSONArray("deviceList");
                            deviceList.add(getResources().getString(R.string.select_device));
                            for (int i = 0; i < jsonArray.length(); i++) {
                                deviceList.add(jsonArray.getJSONObject(i).getString("UUID"));
                            }
                            builder.setAdapter(new ArrayAdapter(ConversationRong.this, android.R.layout.simple_list_item_1, deviceList), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, final int which) {
                                    JSONTokener jsonTokener1 = new JSONTokener(((TextMessage) listMessage.get(position).getContent()).getExtra());
                                    try {
                                        JSONObject jsonObject1 = (JSONObject) jsonTokener1.nextValue();
                                        jsonObject1.put("MAC", deviceList.get(which));
                                        RongIMClient.Message message = new RongIMClient.Message();
                                        TextMessage textMessage = new TextMessage(jsonObject1.toString());
                                        textMessage.setExtra(jsonObject1.toString());
                                        message.setContent(textMessage);
                                        message.setMessageDirection(RongIMClient.MessageDirection.SEND);
                                        MessageModle messageModle = new MessageModle();
                                        messageModle.setFriendID(deviceList.get(which));
                                        messageModle.setMessage(message);
                                        EventBus.getDefault().post(messageModle);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            builder.show();
                            Log.v("showdivece", "showdevice");
                        }
                    };
                } else if (getMessageType(listMessage.get(position)).equals("text")) {
                    final List<String> deviceList = new ArrayList<String>();
                    final AlertDialog.Builder builder = new AlertDialog.Builder(ConversationRong.this);
                    new UserServer(ConversationRong.this, handler, new StringURL(StringURL.getDivece).setUserID(MyApplication.userID).toString()) {

                        @Override
                        public void httpBack(JSONObject jsonObject) throws JSONException {
                            JSONArray jsonArray = jsonObject.getJSONArray("deviceList");
                            deviceList.add(getResources().getString(R.string.select_device));
                            for (int i = 0; i < jsonArray.length(); i++) {
                                deviceList.add(jsonArray.getJSONObject(i).getString("UUID"));
                            }
                            builder.setAdapter(new ArrayAdapter(ConversationRong.this, android.R.layout.simple_list_item_1, deviceList), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, final int which) {
                                    JSONTokener jsonTokener1 = new JSONTokener(((TextMessage) listMessage.get(position).getContent()).getExtra());
                                    try {
                                        JSONObject jsonObject1 = (JSONObject) jsonTokener1.nextValue();
                                        jsonObject1.put("MAC", deviceList.get(which));
                                        RongIMClient.Message message = new RongIMClient.Message();
                                        TextMessage textMessage = new TextMessage(jsonObject1.toString());
                                        textMessage.setExtra(jsonObject1.toString());
                                        message.setContent(textMessage);
                                        message.setMessageDirection(RongIMClient.MessageDirection.SEND);
                                        MessageModle messageModle = new MessageModle();
                                        messageModle.setFriendID(deviceList.get(which));
                                        messageModle.setMessage(message);
                                        EventBus.getDefault().post(messageModle);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            builder.show();
                            Log.v("showdivece", "showdevice");
                        }
                    };
                }

                return false;
            }
        });

        converView.setOnItemClickListener(new AdapterView.OnItemClickListener()

        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position,
                                    long id) {
                if (getMessageType(listMessage.get(position)).equals("voice")) {
//                    String PATH_NAME = ConversationRong.this.getExternalCacheDir().toString() + "/" + position + "temp.amr";
//                    byte[] bytes=((VoiceMessage) listMessage.get(position).getContent()).encode();
//                    try {
//                        FileOutputStream fileOutputStream = new FileOutputStream(PATH_NAME);
//                        fileOutputStream.write(bytes);
//                        fileOutputStream.flush();
//                        fileOutputStream.close();
//                        mediaPlayer.stop();
//                        mediaPlayer.reset();
//                        mediaPlayer.setDataSource(PATH_NAME);
//                        Log.v("", PATH_NAME);
//                        mediaPlayer.prepare();
//                        mediaPlayer.start();
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }

//                    Glide.with(context)
//                    .load(imageUri)
//                    .centerCrop()
//                    .placeholder(R.drawable.ic_audiotrack)
//                    .crossFade()
//                    .into(viewHolder.imageViewArtWork);

                    new Thread(new Runnable() {

                        @Override
                        public void run() {

                            MediaPlayer mMediaPlayer = new MediaPlayer();
                            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    mp.start();
                                }
                            });

                            try {
                                if ((((VoiceMessage) listMessage.get(position).getContent()).getUri()) != null) {
                                    Resource resource = new Resource(((VoiceMessage) listMessage.get(position).getContent()).getUri());
//                                    mMediaPlayer.setDataSource(ConversationRong.this, Uri.fromFile(ResourceManager.getInstance().getFile(resource)));
                                    mMediaPlayer.setDataSource(ConversationRong.this, ((VoiceMessage) listMessage.get(position).getContent()).getUri());
                                    mMediaPlayer.prepare();
                                } else {
                                    handler.post(new Urinull());

                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                } else if (getMessageType(listMessage.get(position)).equals("text")) {
                    if (((TextMessage) listMessage.get(position).getContent()).getContent().equals("imt_music")) {
                        JSONTokener jsonTokener = new JSONTokener(((TextMessage) listMessage.get(position).getContent()).getExtra());
                        JSONObject jsonObject = null;
                        try {
                            jsonObject = (JSONObject) jsonTokener.nextValue();
//                            if(myPlay.isPlaying()){
//                                myPlay.stop();
//                                myPlay.reset();
//                            }
//                            myPlay.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                                @Override
//                                public void onPrepared(MediaPlayer mp) {
//                                    myPlay.start();
//                                }
//                            });
//                            myPlay.setDataSource(ConversationRong.this,Uri.parse(jsonObject.getString("url")));
//                            myPlay.prepare();
                            MyApplication myApplication = (MyApplication) getApplication();

                            OnlineMusicInfo onlineMusicInfo = new OnlineMusicInfo();
                            PlayEvent playEvent = new PlayEvent();
                            onlineMusicInfo.setTitle(jsonObject.getString("name"));
                            onlineMusicInfo.setArtist(jsonObject.getString("artist"));
                            onlineMusicInfo.setArtworkUrl(jsonObject.getString("album"));
                            onlineMusicInfo.setSongId(jsonObject.getInt("id"));
                            onlineMusicInfo.setUrl(jsonObject.getString("url"));
                            playEvent.setSongId(jsonObject.getInt("id"));
                            playEvent.setLocal(false);
                            List<OnlineMusicInfo> onlineMusicInfos = new ArrayList<OnlineMusicInfo>();
                            onlineMusicInfos.add(onlineMusicInfo);
                            if (myApplication.isPlaying() && myApplication.getCurrentSongId() == onlineMusicInfo.getSongId()) {
                                EventBus.getDefault().post(new PauseEvent());
                                myApplication.setPlaying(false);
                            } else {
                                EventBus.getDefault().post(new PauseEvent());
                                myApplication.setOnlineMusicInfos(onlineMusicInfos);
                                myApplication.setCurrentOnlinePosition(0);
                                myApplication.setCurrentSongId(onlineMusicInfo.getSongId());
                                myApplication.setPlaying(true);
                                myApplication.setLocalDevicesPlaying(true);
                                playEvent.setOnlineMusicInfo(onlineMusicInfo);
                                EventBus.getDefault().post(playEvent);
                            }

//                            JSONObject jsonObject = new JSONObject();
//                            jsonObject.put("action", "play_audio");
//                            jsonObject.put("category", category);
//                            jsonObject.put("MAC", macAddress);
//                            jsonObject.put("id", songId);
//                            jsonObject.put("url", url);
//                            jsonObject.put("name", name);//ming
//                            jsonObject.put("artist", artist);
//                            jsonObject.put("album", album);
//                            return jsonObject.toString();

                            Log.v("musicuri", Uri.parse(jsonObject.getString("url")).toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
//                        catch (MalformedURLException e) {
//                            e.printStackTrace();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
                    }
                }
            }
        });
    }

    class Urinull implements Runnable {

        @Override
        public void run() {
            Toast.makeText(ConversationRong.this, R.string.server_error, Toast.LENGTH_LONG);
        }
    }

//    private void getLatestMessage() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
////                try {
////                    Thread.sleep(1000);
////                } catch (InterruptedException e) {
////                    e.printStackTrace();
////                }
//                List<io.rong.imlib.RongIMClient.Message> list = client.getLatestMessages(RongIMClient.ConversationType.PRIVATE, friendID, client.getTotalUnreadCount());
//                Log.v("list", "list sssss " + list.size());
//                listMessage.addAll(list);
//                Collections.reverse(listMessage);
//                handler.post(new uplist());
//            }
//        }).start();
//    }
//
//    private void getHistoryMessage() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                listMessage.addAll(client.getHistoryMessages(RongIMClient.ConversationType.PRIVATE, friendID, -1, 10));
//                Collections.reverse(listMessage);
//                handler.post(new uplist());
//
//            }
//        }).start();
//    }

    class uplist implements Runnable {

        @Override
        public void run() {
            converView.setAdapter(new adapter());
            converView.setSelection(listMessage.size() - 1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        recorder.release();

    }

    class adapter extends BaseAdapter {

        @Override
        public int getCount() {
            return listMessage.size();
        }

        @Override
        public Object getItem(int position) {
            return listMessage.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View viewME = View.inflate(ConversationRong.this, R.layout.dialogto, null);
            View viewFriend = View.inflate(ConversationRong.this, R.layout.dialogfrom, null);
            View musicMe = View.inflate(ConversationRong.this, R.layout.musicdialogto, null);
            View musicFriend = View.inflate(ConversationRong.this, R.layout.musicdialogfrom, null);

            TextView textViewMe = (TextView) viewME.findViewById(R.id.out);
            TextView textViewFriend = (TextView) viewFriend.findViewById(R.id.out);
            TextView musictextMe = (TextView) musicMe.findViewById(R.id.out);
            TextView musictextFriend = (TextView) musicFriend.findViewById(R.id.out);

            ImageView imageViewMe = (ImageView) viewME.findViewById(R.id.headImg);
            ImageView imageViewFriend = (ImageView) viewFriend.findViewById(R.id.headImg);
            ImageView musiciheadMe = (ImageView) musicMe.findViewById(R.id.headImg);
            ImageView musiciheadFriend = (ImageView) musicFriend.findViewById(R.id.headImg);

            ImageView musicimgMe = (ImageView) musicMe.findViewById(R.id.musicImg);
            ImageView musicimgFriend = (ImageView) musicFriend.findViewById(R.id.musicImg);

            imageViewFriend.setOnClickListener(new headClick());
            musiciheadFriend.setOnClickListener(new headClick());
            if (userBit != null) {
                imageViewMe.setImageBitmap(userBit);
                musiciheadMe.setImageBitmap(userBit);

            }
            if (friendBit != null) {
                imageViewFriend.setImageBitmap(friendBit);
                musiciheadFriend.setImageBitmap(friendBit);

            }

            if (listMessage.get(position).getMessageDirection() == RongIMClient.MessageDirection.RECEIVE) {
                switch (getMessageType(listMessage.get(position))) {
                    case "text":
                        if (((TextMessage) listMessage.get(position).getContent()).getContent().equals("imt_music")) {

                            JSONTokener jsonTokener = new JSONTokener(((TextMessage) listMessage.get(position).getContent()).getExtra());
                            JSONObject jsonObject = null;
                            try {
                                jsonObject = (JSONObject) jsonTokener.nextValue();
                                musictextFriend.setText(jsonObject.getString("name") + "\n" + jsonObject.getString("artist") + "\n" + jsonObject.getString("category"));
                                Glide.with(ConversationRong.this)
                                        .load(jsonObject.getString("album"))
                                        .centerCrop()
                                        .placeholder(R.drawable.ic_audiotrack)
                                        .crossFade()
                                        .into(musicimgFriend);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Log.v("musicmessage", ((TextMessage) listMessage.get(position).getContent()).getExtra());
//                        musicimgMe.setImageBitmap(richImg.get(position));
                            musicFriend.findViewById(R.id.sendtodevice).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    final List<String> deviceList = new ArrayList<String>();
                                    final AlertDialog.Builder builder = new AlertDialog.Builder(ConversationRong.this);
                                    new UserServer(ConversationRong.this, handler, new StringURL(StringURL.getDivece).setUserID(MyApplication.userID).toString()) {

                                        @Override
                                        public void httpBack(JSONObject jsonObject) throws JSONException {
                                            JSONArray jsonArray = jsonObject.getJSONArray("deviceList");
                                            deviceList.add(getResources().getString(R.string.select_device));
                                            for (int i = 0; i < jsonArray.length(); i++) {
                                                deviceList.add(jsonArray.getJSONObject(i).getString("UUID"));
                                            }
                                            builder.setAdapter(new ArrayAdapter(ConversationRong.this, android.R.layout.simple_list_item_1, deviceList), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, final int which) {
                                                    JSONTokener jsonTokener1 = new JSONTokener(((TextMessage) listMessage.get(position).getContent()).getExtra());
                                                    try {
                                                        JSONObject jsonObject1 = (JSONObject) jsonTokener1.nextValue();
                                                        jsonObject1.put("MAC", deviceList.get(which));
                                                        RongIMClient.Message message = new RongIMClient.Message();
                                                        TextMessage textMessage = new TextMessage(jsonObject1.toString());
                                                        textMessage.setExtra(jsonObject1.toString());
                                                        message.setContent(textMessage);
                                                        message.setMessageDirection(RongIMClient.MessageDirection.SEND);
                                                        MessageModle messageModle = new MessageModle();
                                                        messageModle.setFriendID(deviceList.get(which));
                                                        messageModle.setMessage(message);
                                                        EventBus.getDefault().post(messageModle);
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                            builder.show();
                                            Log.v("showdivece", "showdevice");
                                        }
                                    };
                                }
                            });
                            listconversation.add(musicFriend);
                            return musicFriend;
                        } else {
                            textViewFriend.setText(((TextMessage) listMessage.get(position).getContent()).getContent());
                        }
                        break;
                    case "pic":
                        break;
                    case "voice":
                        textViewFriend.setText(((VoiceMessage) listMessage.get(position).getContent()).getDuration() + "s");
                        viewFriend.findViewById(R.id.sendtodevice).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final List<String> deviceList = new ArrayList<String>();
                                final AlertDialog.Builder builder = new AlertDialog.Builder(ConversationRong.this);
                                new UserServer(ConversationRong.this, handler, new StringURL(StringURL.getDivece).setUserID(MyApplication.userID).toString()) {

                                    @Override
                                    public void httpBack(JSONObject jsonObject) throws JSONException {
                                        JSONArray jsonArray = jsonObject.getJSONArray("deviceList");
                                        deviceList.add(getResources().getString(R.string.select_device));
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            deviceList.add(jsonArray.getJSONObject(i).getString("UUID"));
                                        }
                                        builder.setAdapter(new ArrayAdapter(ConversationRong.this, android.R.layout.simple_list_item_1, deviceList), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                VoiceMessage voiceMessage = ((VoiceMessage) listMessage.get(position).getContent());
                                                RongIMClient.Message message = new RongIMClient.Message();
                                                message.setContent(voiceMessage);
                                                message.setMessageDirection(RongIMClient.MessageDirection.SEND);
                                                MessageModle messageModle = new MessageModle();
                                                messageModle.setFriendID(deviceList.get(which));
                                                messageModle.setMessage(message);
                                                EventBus.getDefault().post(messageModle);
                                            }
                                        });
                                        builder.show();
                                        Log.v("showdivece", "showdevice");
                                    }
                                };
                            }
                        });
                        break;
                    case "rich":
                        musictextFriend.setText(((RichContentMessage) listMessage.get(position).getContent()).getTitle() + "\n" + ((RichContentMessage) listMessage.get(position).getContent()).getContent());
//                        musicimgFriend.setImageBitmap(richImg.get(position));
                        Glide.with(ConversationRong.this)
                                .load(((RichContentMessage) listMessage.get(position).getContent()).getUrl())
                                .centerCrop()
                                .placeholder(R.drawable.ic_audiotrack)
                                .crossFade()
                                .into(musicimgFriend);
                        break;
                    case "other":
                        break;
                }
                listconversation.add(viewFriend);
                return viewFriend;
            } else {
                switch (getMessageType(listMessage.get(position))) {
                    case "text":
                        if (((TextMessage) listMessage.get(position).getContent()).getContent().equals("imt_music")) {

                            JSONTokener jsonTokener = new JSONTokener(((TextMessage) listMessage.get(position).getContent()).getExtra());
                            JSONObject jsonObject = null;
                            try {
                                jsonObject = (JSONObject) jsonTokener.nextValue();
                                musictextMe.setText(jsonObject.getString("name") + "\n" + jsonObject.getString("artist") + "\n" + jsonObject.getString("category"));
                                Glide.with(ConversationRong.this)
                                        .load(jsonObject.getString("album"))
                                        .centerCrop()
                                        .placeholder(R.drawable.ic_audiotrack)
                                        .crossFade()
                                        .into(musicimgMe);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Log.v("musicmessage", ((TextMessage) listMessage.get(position).getContent()).getExtra());
//                        musicimgMe.setImageBitmap(richImg.get(position));
                            listconversation.add(musicMe);
                            return musicMe;
                        } else {
                            textViewMe.setText(((TextMessage) listMessage.get(position).getContent()).getContent());
                        }
                        break;
                    case "pic":

                        break;
                    case "voice":
                        textViewMe.setText(((VoiceMessage) listMessage.get(position).getContent()).getDuration() + "s");
                        break;
                    case "rich":
                        musictextMe.setText(((RichContentMessage) listMessage.get(position).getContent()).getTitle() + "\n" + ((RichContentMessage) listMessage.get(position).getContent()).getContent());
//                        musicimgMe.setImageBitmap(richImg.get(position));
                        Glide.with(ConversationRong.this)
                                .load(((RichContentMessage) listMessage.get(position).getContent()).getUrl())
                                .centerCrop()
                                .placeholder(R.drawable.ic_audiotrack)
                                .crossFade()
                                .into(musicimgMe);
                        break;
                    case "other":
                        break;
                }
                listconversation.add(viewME);
                return viewME;
            }
        }
    }

    public String getMessageType(RongIMClient.Message message) {
        String temptype = null;
        if (message.getContent() instanceof TextMessage) {//文本消息
//                temp=(((TextMessage) message.getContent()).getContent());
            temptype = "text";
            Log.d("TAG", "onSent-TextMessage:" + ((TextMessage) message.getContent()).getContent());
        } else if (message.getContent() instanceof ImageMessage) {//图片消息
            ImageMessage imageMessage = (ImageMessage) message.getContent();
            temptype = ("pic");
            Log.d("TAG", "onSent-ImageMessage:" + imageMessage.getRemoteUri());
        } else if (message.getContent() instanceof VoiceMessage) {//语音消息
            VoiceMessage voiceMessage = (VoiceMessage) message.getContent();
            temptype = ("voice");
            Log.d("TAG", "onSent-voiceMessage:" + "VoiceMessage");
        } else if (message.getContent() instanceof RichContentMessage) {//图文消息
            RichContentMessage richContentMessage = (RichContentMessage) message.getContent();
            temptype = ("rich");
            Log.d("TAG", "onSent-RichContentMessage:" + richContentMessage.getContent());
        } else {
            temptype = "other";
            Log.d("TAG", "onSent-其他消息，自己来判断处理");
        }

        return temptype;
    }

    class headClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.putExtras(bundle);
            intent.setClass(ConversationRong.this, FriendInfo.class);
            startActivity(intent);
        }
    }

//    protected static boolean isTopActivity(Activity activity) {
//        String packageName = "com.imt.musiclamp";
//        ActivityManager activityManager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(1);
//        if (tasksInfo.size() > 0) {
//            System.out.println("---------------包名-----------" + tasksInfo.get(0).topActivity.getPackageName());
//            //应用程序位于堆栈的顶层
//            if (packageName.equals(tasksInfo.get(0).topActivity.getPackageName())) {
//                return true;
//            }
//        }
//        return false;
//    }

    public void onEvent(final RongIMClient.Message message) {
//        if (isTopActivity(this)) {
//            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);//得到系统服务
//            manager.cancel(0);
//        }
        if (message.getSenderUserId().equals(friendID)) {

//            MyApplication.modlesaveList.remove(friendID);
//            if (getMessageType(message).equals("rich")) {
//                new getImg(((RichContentMessage) message.getContent()).getUrl()) {
//                    @Override
//                    public void bitMapBack(Bitmap bitmap) {
//
//                        richImg.put(listMessage.size(), bitmap);
//                        listMessage.add(message);
//                        handler.post(new uplist());
//                    }
//                };
//            } else {

            listMessage.add(message);
            handler.post(new uplist());
//            }
        }
    }
//    class ResiveListener implements RongIMClient.OnReceiveMessageListener {
//
//        @Override
//        public void onReceived(final RongIMClient.MessageModle message, int i) {
//            if (getMessageType(message).equals("rich")) {
//                new getImg(((RichContentMessage) message.getContent()).getUrl()) {
//                    @Override
//                    public void bitMapBack(Bitmap bitmap) {
//
//                        richImg.put(listMessage.size(),bitmap );
//                        listMessage.add(message);
//                        handler.post(new uplist());
//                    }
//                };
//            }else{
//
//                listMessage.add(message);
//                handler.post(new uplist());
//            }
//        }
//    }
}
