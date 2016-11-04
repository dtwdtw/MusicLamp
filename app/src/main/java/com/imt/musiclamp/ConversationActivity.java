package com.imt.musiclamp;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dtw on 15/3/16.
 */
public class ConversationActivity extends Activity{
    Button sendVoice,back;
    int saveNum=0;
    public static List<String> msg = new ArrayList<>();
    MediaRecorder recorder;
    String PATH_NAME;
    ListView converView;
    Bitmap bitmap;
    MediaPlayer mediaPlayer;
    TextView textView;
    long timeL;
    String name;
    Time t=new Time();
    Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversation);
        textView=(TextView) findViewById(R.id.tab);
        recorder = new MediaRecorder();
        mediaPlayer=new MediaPlayer();
        bundle=getIntent().getExtras();
        bitmap=bundle.getParcelable("userImg");
        name=bundle.getString("name");
        textView.setText(name);
        converView=(ListView)findViewById(R.id.converList);
        converView.setAdapter(new adapter());
        sendVoice =(Button)findViewById(R.id.sendS);
        back=(Button)findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        this.setTitle(name);
        sendVoice.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    Log.v("down","down");
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    PATH_NAME=ConversationActivity.this.getExternalCacheDir().toString()+"/"+saveNum+++"temp.arm";
                    recorder.setOutputFile(PATH_NAME);
                    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    try {
                        recorder.prepare();
                        recorder.start();   // Recording is now started
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    t.setToNow();
                    timeL=t.toMillis(true);
                }
                else if(event.getAction()==MotionEvent.ACTION_UP){
                    Log.v("up", "up");
                    recorder.stop();
                    recorder.reset();
                    t.setToNow();
                    timeL=t.toMillis(true)-timeL;
                    timeL=timeL/1000;
                    msg.add("(((  "+timeL+"s");
                    converView.setAdapter(new adapter());
                    converView.setSelection(msg.size()-1);
                    try {
                        mediaPlayer.setDataSource(PATH_NAME);
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });
        converView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                try {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(ConversationActivity.this.getExternalCacheDir().toString()+"/"+position+"temp.arm");
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.v("","jkal;sdfk");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recorder.release();
    }

    class adapter extends BaseAdapter {

        @Override
        public int getCount() {
            return msg.size();
        }

        @Override
        public Object getItem(int position) {
            return msg.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view=View.inflate(ConversationActivity.this,R.layout.dialogto,null);
            TextView textView=(TextView)view.findViewById(R.id.out);
            ImageView imageView=(ImageView)view.findViewById(R.id.headImg);
            imageView.setImageBitmap(bitmap);
            textView.setText(msg.get(position));
            return  view;
        }
    }
    private class headClick implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            Intent intent=new Intent();
            intent.putExtras(bundle);
            intent.setClass(ConversationActivity.this,FriendInfo.class);
            startActivity(intent);
        }
    }
}
