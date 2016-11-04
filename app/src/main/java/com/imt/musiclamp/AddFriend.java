package com.imt.musiclamp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.imt.musiclamp.elementClass.Friend;
import com.imt.musiclamp.elementClass.StrUrl;
import com.imt.musiclamp.elementClass.StringURL;
import com.imt.musiclamp.elementClass.UserServer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dtw on 15/3/24.
 */
public class AddFriend extends Activity{
    ImageButton back;
    Handler handler=new Handler();
    Bitmap bitmap;
    List<Friend> listFriendAdd;
    ListView addFriend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addfriendlist);
        back=(ImageButton)findViewById(R.id.back);
        listFriendAdd=new ArrayList<>();
        addFriend=(ListView)findViewById(R.id.addFriend);
        bitmap= BitmapFactory.decodeResource(getResources(),R.drawable.big);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        new UserServer(handler, new StringURL(StringURL.requestFriend).setUserID(((MyApplication) getApplication()).userID).toString()) {
            @Override
            public void httpBack(JSONObject jsonObject) {
                try{
                    JSONArray jsonArray=jsonObject.getJSONArray("userList");
                    for(int i=0;i<jsonArray.length();i++) {
                        Friend friend = new Friend(AddFriend.this, bitmap, jsonArray.getJSONObject(i).getString("nickName"), jsonArray.getJSONObject(i).getString("sex"), "Translate the word!", jsonArray.getJSONObject(i).getString("userID"));
                        friend.getAddButton().setVisibility(View.VISIBLE);
                        listFriendAdd.add(friend);
                        addFriend.setAdapter(new adapter());
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        };
        addFriend.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //等待添加
                //显示用户信息
                Intent intent=new Intent();
                Bundle bundle=new Bundle();
                intent.setClass(AddFriend.this,FriendInfo.class);
                startActivity(intent);
            }
        });
    }

    class adapter extends BaseAdapter {

        @Override
        public int getCount() {
            return listFriendAdd.size();
        }

        @Override
        public Object getItem(int position) {
            return listFriendAdd.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Friend friend = listFriendAdd.get(position);
            friend.getAddButton().setOnClickListener(new addClick(position));
            return listFriendAdd.get(position).getView();
        }
    }

    class addClick implements View.OnClickListener {
        int position;

        public addClick(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            new UserServer(AddFriend.this, handler, new StringURL(StringURL.conformAdd).setFriendID(listFriendAdd.get(position).getFriendID()).setUserID(((MyApplication) getApplication()).userID).toString()) {

                @Override
                public void httpBack(JSONObject jsonObject) {
                    if (StrUrl.getResult(jsonObject).equals("1")) {
                        Toast.makeText(AddFriend.this, "添加成功", Toast.LENGTH_SHORT).show();
                        listFriendAdd.get(position).getAddButton().setEnabled(false);
                    }
                }
            };
        }
    }
}
