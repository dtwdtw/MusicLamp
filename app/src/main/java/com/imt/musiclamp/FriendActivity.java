package com.imt.musiclamp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.imt.musiclamp.elementClass.Friend;
import com.imt.musiclamp.elementClass.StrUrl;
import com.imt.musiclamp.elementClass.StringURL;
import com.imt.musiclamp.elementClass.UserInfo;
import com.imt.musiclamp.elementClass.UserServer;
import com.imt.musiclamp.elementClass.getImg;
import com.imt.musiclamp.event.FindDevicesEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.security.Guard;
import java.util.ArrayList;
import java.util.List;

import io.rong.message.RichContentMessage;

/**
 * Created by dtw on 15/3/16.
 */
public class FriendActivity extends Activity {
    ListView friendList;

    public static List<Friend> listFriend;
    private adapter friendsAdapter;

    List<Friend> listPeople;
    Handler handler = new Handler();
    Bitmap bitmapuser;
    ImageButton back, add;
    int position = 0;
    EditText editText;
    AlertDialog.Builder builder1;
    List<String> friendheadurl=new ArrayList<>();
    String userHeadurl=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.friendactive);
        back = (ImageButton) findViewById(R.id.back);
        add = (ImageButton) findViewById(R.id.add);
        Bundle bundle = getIntent().getExtras();
        bitmapuser = bundle.getParcelable("userImg");

        listFriend = new ArrayList<>();
        listPeople = new ArrayList<>();
        friendList = (ListView) findViewById(R.id.friendList);
//        TextView textView = new TextView(this);
//        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
//        textView.setText("新的好友:)");
//        textView.setTextColor(0xccffffff);
        View headview = View.inflate(this, R.layout.frienditem, null);
        ((TextView) headview.findViewById(R.id.friendName)).setText(getResources().getString(R.string.new_friend));
        friendList.addHeaderView(headview);
        builder1 = new AlertDialog.Builder(this);

//        Friend friend = new Friend(this, bitmap, "hello", "男", "love the word", "10");
//        friend.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.small));
//        listFriend.add(friend);
        getFriend();

        friendsAdapter = new adapter();
        friendList.setAdapter(friendsAdapter);
        friendList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
//                    bundle.putBoolean("toBig", true);
                    if (listFriend.get(position - 1).getPhoneNum() != null) {
                        UserInfo.phone = listFriend.get(position - 1).getPhoneNum();
                        bundle.putString("userPhone",listFriend.get(position - 1).getPhoneNum());
                    }
//                    UserInfo.userName = listFriend.get(position - 1).getName();
//                    UserInfo.gender = listFriend.get(position - 1).getSex();
//                    UserInfo.id = listFriend.get(position - 1).getFriendID();
//                    UserInfo.bitmap = listFriend.get(position - 1).getBitmap();
//                    UserInfo.userBit = bitmapuser;

//                    bundle.putString("userPhone", textPhone.getText().toString());
////                                        bundle.putParcelable("userImg",userPic);
                    bundle.putString("userID", MyApplication.userID);
                    bundle.putString("nickname", listFriend.get(position - 1).getName());
                    bundle.putString("gender", listFriend.get(position - 1).getSex());
                    bundle.putString("friendID",listFriend.get(position - 1).getFriendID());
                    bundle.putString("friendInfo", listFriend.get(position-1).getInfo());
                    bundle.putString("userHeadUrl", userHeadurl);
                    bundle.putString("friendHeadUrl",friendheadurl.get(position-1));
                    intent.putExtra("position", position);
                    intent.setClass(FriendActivity.this, ConversationRong.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent();
                    intent.setClass(FriendActivity.this, AddFriend.class);
                    startActivity(intent);
                }
            }
        });
        friendList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(FriendActivity.this);
                builder.setNeutralButton(getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new UserServer(handler, new StringURL(StringURL.deleteFriend).setUserID(((MyApplication) getApplication()).userID).setFriendID(listFriend.get(position - 1).getFriendID()).toString()) {
                            @Override
                            public void httpBack(JSONObject jsonObject) {
                                if (StrUrl.getResult(jsonObject).equals("1")) {
                                    Toast.makeText(FriendActivity.this, R.string.user_deleted_successfully, Toast.LENGTH_SHORT).show();
                                    getFriend();
                                }
                            }
                        };
                    }
                });
                Button button = new Button(FriendActivity.this);
                button.setText(getResources().getString(R.string.set_permissions_friends));
                button.setBackgroundColor(0x00000000);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(FriendActivity.this);
                        ListView listView = new ListView(FriendActivity.this);
                        listView.setAdapter(new ArrayAdapter<String>(FriendActivity.this, android.R.layout.simple_list_item_1, new String[]{getResources().getString(R.string.reading),
                                getResources().getString(R.string.take_video)}));
                        builder1.setView(listView);
                        builder1.show();
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        });
                    }
                });
                builder.setView(button);
                builder.show();
                return true;
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(FriendActivity.this);
                builder.setTitle(getResources().getString(R.string.enter_friend_id));
                editText = new EditText(FriendActivity.this);
                builder.setView(editText);
                builder.setNegativeButton(getResources().getString(R.string.cancel), null);
                builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        flash();
                    }
                }).show();
            }
        });
    }

    private void flash() {
        new UserServer(handler, new StringURL(StringURL.searchUser).setUserID(((MyApplication) getApplication()).userID).setFriendID(editText.getText().toString()).setPosition(position + "").setCount("10").toString()) {

            @Override
            public void httpBack(JSONObject jsonObject) {
                if (StrUrl.getResult(jsonObject).equals("1")) {
                    listPeople.clear();
                    try {
                        final JSONArray jsonArray = jsonObject.getJSONArray("userList");
                        if (jsonArray.length() < 1) {
                            Toast.makeText(FriendActivity.this, "没有找到好友", Toast.LENGTH_SHORT).show();
                            position -= 10;
                            if(position<0){
                                position=0;
                            }
                            flash();
                        }
                        for (int i = 0; i < jsonArray.length(); i++) {
                            final int count = jsonArray.length();
                            final int finalI = i;



                            String sex="female";
                            try {
                                sex = jsonArray.getJSONObject(finalI).getString("sex");
                            }catch (JSONException e){
                                sex="female";
                            }
                            String sign="Translate the word!";
                            try{
                                sign=jsonArray.getJSONObject(finalI).getString("sign");
                            }catch(JSONException e){
                                sign="Translate the word!";
                            }
                            final Friend friend = new Friend(FriendActivity.this, null, jsonArray.getJSONObject(finalI).getString("nickName"), sex, sign, jsonArray.getJSONObject(finalI).getString("userID"));
                            Glide.with(FriendActivity.this)
                                    .load(jsonArray.getJSONObject(finalI).getString("headIconURL"))
                                    .centerCrop()
                                    .listener(new RequestListener<String, GlideDrawable>() {
                                        @Override
                                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    ((ImageView) friend.getView().findViewById(R.id.friendHead)).setImageResource(R.drawable.defalthead);
                                                }
                                            });
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                            return false;
                                        }
                                    })
                                    .crossFade()
                                    .into((ImageView) friend.getView().findViewById(R.id.friendHead));
                            listPeople.add(friend);

                            if (finalI > count-2 ) {
                                ListView listView = new ListView(FriendActivity.this);
//            listView.setBackgroundResource(R.drawable.index_bg);
                                listView.setAdapter(new peAdapter());
                                builder1.setTitle(getResources().getString(R.string.find_following_friends)).setView(listView);
                                builder1.setNeutralButton(getResources().getString(R.string.ok), null);
                                builder1.setNegativeButton(getResources().getString(R.string.previous_page), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (position > 9) {
                                            position -= 10;
                                            if(position<0){
                                                position=0;
                                            }
                                            flash();
                                        } else {
                                            Toast.makeText(FriendActivity.this, "已经是第一页", Toast.LENGTH_SHORT).show();
                                            flash();
                                        }
                                    }
                                });
                                builder1.setPositiveButton(getResources().getString(R.string.next_page), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (listPeople.size() > 9) {
                                            position += 10;
                                            flash();
                                        } else {
                                            Toast.makeText(FriendActivity.this, R.string.last_page, Toast.LENGTH_SHORT).show();
                                            flash();
                                        }
                                    }
                                });
                                builder1.show();
                                break;
                            }

                        }

//                                        listPeople.setAdapter(new adapter());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        };
    }

    private void getFriend() {

        new UserServer(handler, new StringURL(StringURL.getFriendList).setUserID(MyApplication.userID).toString()) {
            @Override
            public void httpBack(JSONObject jsonObject) {
                listFriend.clear();
                try {
                    final JSONArray jsonArray = jsonObject.getJSONArray("userList");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        final int finalI = i;
//                        new getImg(new Handler(), jsonArray.getJSONObject(finalI).getString("headIconURL")) {
//                            @Override
//                            public void bitMapBack(Bitmap bitmap) {
//                                if (bitmap == null)
//                                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.defalthead);
//                                Friend friend = null;
//                                try {
//                                    friend = new Friend(FriendActivity.this, bitmap, jsonArray.getJSONObject(finalI).getString("nickName"), jsonArray.getJSONObject(finalI).getString("sex"), "Translate the word!", jsonArray.getJSONObject(finalI).getString("userID"));
//                                    if (jsonArray.getJSONObject(finalI).getString("phoneNumber") != null) {
//                                        friend.setPhoneNum(jsonArray.getJSONObject(finalI).getString("phoneNumber"));
//                                    }
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
//                                listFriend.add(friend);
//                                handler.post(new upList());
//                            }
//                        };
                        String sex="female";
                        try {
                            sex = jsonArray.getJSONObject(finalI).getString("sex");
                        }catch (JSONException e){
                            sex="female";
                        }
                        String sign="Translate the word!";
                        try{
                            sign=jsonArray.getJSONObject(finalI).getString("sign");
                        }catch(JSONException e){
                            sign="Translate the word!";
                        }
                        final Friend friend = new Friend(FriendActivity.this, null, jsonArray.getJSONObject(finalI).getString("nickName"), sex, sign, jsonArray.getJSONObject(finalI).getString("userID"));
                        if (jsonArray.getJSONObject(finalI).getString("phoneNumber") != null) {
                            friend.setPhoneNum(jsonArray.getJSONObject(finalI).getString("phoneNumber"));
                        }
                        Glide.with(FriendActivity.this)
                                .load(jsonArray.getJSONObject(finalI).getString("headIconURL"))
                                .centerCrop()
                                .listener(new RequestListener<String, GlideDrawable>() {
                                    @Override
                                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                ((ImageView) friend.getView().findViewById(R.id.friendHead)).setImageResource(R.drawable.defalthead);
                                            }
                                        });
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                        return false;
                                    }
                                })
                                .crossFade()
                                .into((ImageView) friend.getView().findViewById(R.id.friendHead));
                        listFriend.add(friend);
                        friendheadurl.add(jsonArray.getJSONObject(finalI).getString("headIconURL"));
                        handler.post(new upList());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    class upList implements Runnable {

        @Override
        public void run() {
            friendList.setAdapter(new adapter());
        }
    }

    class adapter extends BaseAdapter {

        @Override
        public int getCount() {
            return listFriend.size();
        }

        @Override
        public Object getItem(int position) {
            return listFriend.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return listFriend.get(position).getView();
        }
    }

    class peAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return listPeople.size();
        }

        @Override
        public Object getItem(int position) {
            return listPeople.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            listPeople.get(position).getAddButton().setOnClickListener(new addClick(position));
            View view = listPeople.get(position).getView();
            ((TextView) view.findViewById(R.id.friendName)).setTextColor(0xff000000);
            listPeople.get(position).getAddButton().setVisibility(View.VISIBLE);
            return view;
        }
    }

    class addClick implements View.OnClickListener {
        int position;

        public addClick(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            new UserServer(handler, new StringURL(StringURL.addFriend).setFriendID(listPeople.get(position).getFriendID()).setUserID(((MyApplication) getApplication()).userID).toString()) {

                @Override
                public void httpBack(JSONObject jsonObject) {
                    if (StrUrl.getResult(jsonObject).equals("1")) {
                        Toast.makeText(FriendActivity.this, R.string.request_has_been_sent, Toast.LENGTH_SHORT).show();
                        handler.post(new upButtonVisible(position));
                    }
                }
            };
        }
    }

    class upButtonVisible implements Runnable {
        int position;

        public upButtonVisible(int position) {
            this.position = position;
        }

        @Override
        public void run() {
            listPeople.get(position).getAddButton().setEnabled(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(null != friendsAdapter)
            friendsAdapter.notifyDataSetChanged();
    }
}