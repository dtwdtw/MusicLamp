package com.imt.musiclamp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.imt.musiclamp.elementClass.Friend;
import com.imt.musiclamp.elementClass.StrUrl;
import com.imt.musiclamp.elementClass.StringURL;
import com.imt.musiclamp.elementClass.UserServer;
import com.imt.musiclamp.elementClass.getImg;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mac on 15/4/17.
 */
public class MyloveAdd extends Activity {
    int SCANNIN_GREQUEST_LOVE = 120;
    Handler handler = new Handler();
    EditText editText;
    List<Friend> listPeople = new ArrayList<>();
    int position = 0;
    AlertDialog.Builder builder1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myloveadd);
        editText=(EditText)findViewById(R.id.userID);
        builder1=new AlertDialog.Builder(this);
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putBoolean("myLove", true);

                intent.putExtras(bundle);
                intent.setClass(MyloveAdd.this, MipcaActivityCapture.class);
                startActivityForResult(intent, SCANNIN_GREQUEST_LOVE);
            }
        });
        Bundle bundle = getIntent().getExtras();
        findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new UserServer(handler, new StringURL(StringURL.addFriend).setFriendID(editText.getText().toString()).setUserID(((MyApplication) getApplication()).userID).setFriendType("1").toString()) {

                    @Override
                    public void httpBack(JSONObject jsonObject) {
                        if (StrUrl.getResult(jsonObject).equals("1")) {
                            Toast.makeText(MyloveAdd.this, "请求已发送", Toast.LENGTH_SHORT).show();
                            finish();

                        }
                    }
                };
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
                        for (int i = 0; i < jsonArray.length(); i++) {
                            final int count = jsonArray.length();
                            final int finalI = i;
                            new getImg(new Handler(),jsonArray.getJSONObject(finalI).getString("headIconURL")) {
                                @Override
                                public void bitMapBack(Bitmap bitmap) {
                                    try {
                                        Friend friend = new Friend(MyloveAdd.this, bitmap, jsonArray.getJSONObject(finalI).getString("nickName"), jsonArray.getJSONObject(finalI).getString("sex"), "Translate the word!", jsonArray.getJSONObject(finalI).getString("userID"));
                                        listPeople.add(friend);
                                        if (finalI > count - 2) {
                                            handler.post(new upPeople());
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            };
                        }
//                                        listPeople.setAdapter(new adapter());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
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
            listPeople.get(position).getAddButton().setVisibility(View.VISIBLE);
            listPeople.get(position).getAddButton().setOnClickListener(new addClick(position));
            return listPeople.get(position).getView();
        }
    }

    class addClick implements View.OnClickListener {
        int position;

        public addClick(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            new UserServer(handler, new StringURL(StringURL.addFriend).setFriendID(listPeople.get(position).getFriendID()).setUserID(((MyApplication) getApplication()).userID).setFriendType("1").toString()) {

                @Override
                public void httpBack(JSONObject jsonObject) {
                    if (StrUrl.getResult(jsonObject).equals("1")) {
                        Toast.makeText(MyloveAdd.this, "请求已发送", Toast.LENGTH_SHORT).show();
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

    class upPeople implements Runnable {

        @Override
        public void run() {
            ListView listView = new ListView(MyloveAdd.this);
            listView.setAdapter(new peAdapter());
            builder1.setTitle(getResources().getString(R.string.find_following_friends)).setView(listView);
            builder1.setNeutralButton(getResources().getString(R.string.ok), null);
            builder1.setNegativeButton(getResources().getString(R.string.previous_page), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (position > 9) {
                        position -= 10;
                        flash();
                    } else {
                        Toast.makeText(MyloveAdd.this, R.string.last_page, Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(MyloveAdd.this, R.string.last_page, Toast.LENGTH_SHORT).show();
                        flash();
                    }
                }
            });
            builder1.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCANNIN_GREQUEST_LOVE && resultCode == RESULT_OK) {
            final Bundle bundle = data.getExtras();
            Toast.makeText(MyloveAdd.this, bundle.getString("result"), Toast.LENGTH_SHORT).show();
            final Intent resultIntent = new Intent();
            new UserServer(handler, new StringURL(StringURL.addFriend).setFriendID(bundle.getString("result").substring(bundle.getString("result").indexOf("userID")).replace("userID=", "")).setUserID(MyApplication.userID).setFriendType("1").toString()) {

                @Override
                public void httpBack(JSONObject jsonObject) {
                    if (StrUrl.getResult(jsonObject).equals("1")) {
                        Toast.makeText(MyloveAdd.this, R.string.request_has_been_sent, Toast.LENGTH_SHORT).show();
                        Bundle bundle1 = new Bundle();
                        bundle1.putString("result", bundle.getString("result"));
                        resultIntent.putExtras(bundle);
                        MyloveAdd.this.setResult(RESULT_OK, resultIntent);
                        finish();
                    }
                }
            };
        }
    }

}
