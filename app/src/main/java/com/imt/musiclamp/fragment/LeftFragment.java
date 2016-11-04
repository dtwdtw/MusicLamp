package com.imt.musiclamp.fragment;


import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.activeandroid.util.Log;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.load.resource.transcode.BitmapBytesTranscoder;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.zxing.WriterException;
import com.imt.musiclamp.AppSettingActivity;
import com.imt.musiclamp.ConversationRong;
import com.imt.musiclamp.DeviceSetActivity;
import com.imt.musiclamp.FriendActivity;
import com.imt.musiclamp.LoginActivity;
import com.imt.musiclamp.MainActivity;
import com.imt.musiclamp.MipcaActivityCapture;
import com.imt.musiclamp.MobActivity;
import com.imt.musiclamp.MyApplication;
import com.imt.musiclamp.MyloveAdd;
import com.imt.musiclamp.R;
import com.imt.musiclamp.SetUserInfoActivity;
import com.imt.musiclamp.element.User;
import com.imt.musiclamp.element.UserInfoEvent;
import com.imt.musiclamp.elementClass.StrUrl;
import com.imt.musiclamp.elementClass.StringURL;
import com.imt.musiclamp.elementClass.UserInfo;
import com.imt.musiclamp.elementClass.UserServer;
import com.imt.musiclamp.elementClass.getImg;
import com.imt.musiclamp.event.FindDevicesEvent;
import com.imt.musiclamp.model.Lamp;
import com.imt.musiclamp.service.RongPush;
import com.imt.musiclamp.utils.Utils;
import com.imt.musiclamp.view.IEditText;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;


import java.io.IOException;
import java.util.List;

import QR.BitmapUtil;
import de.greenrobot.event.EventBus;

/**
 * A simple {@link Fragment} subclass.
 */
public class LeftFragment extends Fragment {

    ImageView qr, userImg, gender;
    TextView myName, mySex, info, ID, exit;
    LinearLayout myFriend, myLove, Scan, Mydevice;
    private final static int SCANNIN_GREQUEST_CODE = 1;
    private final static int SCANNIN_GREQUEST_CODE_L = 50;
    private final static int SETUSERINFO = 111;
    Handler handler = new Handler();
    /**
     * Standard activity result: operation canceled.
     */
    public static final int RESULT_CANCELED = 0;
    /**
     * Standard activity result: operation succeeded.
     */
    public static final int RESULT_OK = -1;


    Bitmap bitmap;
    String userName, userSex, userPhone, userInfo,userHeadUrl;
    private LinearLayout settings;
    Bundle bundle;


    public LeftFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_left, container, false);

        bundle = getActivity().getIntent().getExtras();
        myFriend = (LinearLayout) view.findViewById(R.id.myfriend);
        settings = (LinearLayout) view.findViewById(R.id.settings);
        ID = (TextView) view.findViewById(R.id.userID);
        ID.setText(((MyApplication) getActivity().getApplication()).userID);
        myLove = (LinearLayout) view.findViewById(R.id.mylove);
        Scan = (LinearLayout) view.findViewById(R.id.scan);
        info = (TextView) view.findViewById(R.id.info);
        Mydevice = (LinearLayout) view.findViewById(R.id.device);
        qr = (ImageView) view.findViewById(R.id.imageView4);
        userImg = (ImageView) view.findViewById(R.id.userImg);
        myName = (TextView) view.findViewById(R.id.myName);
        exit = (TextView) view.findViewById(R.id.textView);
        gender = (ImageView) view.findViewById(R.id.gender);


        new Thread() {
            @Override
            public void run() {
                android.util.Log.e("Send UDP broadcast", "Sync");
                Utils.sendMulticastUDP(MyApplication.UDP_SERVER_PORT, Utils.getFindJson().getBytes());
            }
        }.start();

        view.findViewById(R.id.loginbutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
                startActivity(new Intent(getActivity(),LoginActivity.class));
            }
        });
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
            }
        });
        qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkLogin()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setView(Create2QR());
                    builder.show();
                }
            }
        });
        myFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkLogin()) {
                    Intent intent = new Intent(getActivity(), FriendActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("userInfo", "userInfo");
                    bundle.putString("userHeadUrl",userHeadUrl);
                    bundle.putParcelable("userImg", bitmap);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });
        Mydevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), DeviceSetActivity.class);
                startActivity(intent);
            }
        });
        myLove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkLogin()) {
                    new UserServer(handler, new StringURL(StringURL.getFriendList).setUserID(MyApplication.userID).setFriendType("1").toString()) {
                        @Override
                        public void httpBack(JSONObject jsonObject) throws JSONException {
                            if (jsonObject.getJSONArray("userList").length() > 0) {
                                final JSONObject jsonlove = jsonObject.getJSONArray("userList").getJSONObject(0);

                                new getImg(new Handler(),jsonlove.getString("headIconURL")) {
                                    @Override
                                    public void bitMapBack(Bitmap bitmap) {

                                        Bundle bundLove = new Bundle();
                                        try {
                                            bundLove.putBoolean("toBig", true);
                                            bundLove.putString("love", "1");
                                            UserInfo.userBit = LeftFragment.this.bitmap;
                                            UserInfo.bitmap = bitmap;
                                            UserInfo.id = jsonlove.getString("userID");
                                            UserInfo.phone = jsonlove.getString("phoneNumber");
                                            UserInfo.userName = jsonlove.getString("nickName");
                                            UserInfo.gender = jsonlove.getString("sex");
                                            Intent intent = new Intent();
                                            intent.putExtras(bundLove);
                                            intent.setClass(getActivity(), ConversationRong.class);
                                            startActivity(intent);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                };
                            } else {

                                Intent intent = new Intent();
                                intent.setClass(getActivity(), MyloveAdd.class);
                                startActivityForResult(intent, SCANNIN_GREQUEST_CODE_L);
                            }
                        }
                    };
                }
            }
        });
        Scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setClass(getActivity(), MipcaActivityCapture.class);
                startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), AppSettingActivity.class);
                startActivity(intent);
            }
        });
        userImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkLogin()) {
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), SetUserInfoActivity.class);
                    Bundle bundle1 = new Bundle();
                    bundle1.putString("nickname", myName.getText().toString());
                    bundle1.putString("gender", userSex);
                    Log.v("usersex", userSex);
                    bundle1.putString("userInfo", userInfo);
                    bundle1.putString("userHeadUrl", userHeadUrl);
                    intent.putExtras(bundle1);
                    startActivityForResult(intent, SETUSERINFO);
                }
            }
        });


        if (bundle != null) {

            userHeadUrl=bundle.getString("userHeadUrl");
            Log.v("headimg",userInfo);
            if(userHeadUrl!=null) {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), Uri.parse(bundle.getString("userHeadUrl")));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            MyApplication.myHead = bitmap;
            userName = bundle.getString("nickname");
            userSex = bundle.getString("gender");
            userPhone = bundle.getString("userPhone");
            userInfo = bundle.getString("userInfo");


            if (bundle.containsKey("tobig")) {
                bitmap = com.imt.musiclamp.elementClass.UserInfo.bitmap;
                userName = com.imt.musiclamp.elementClass.UserInfo.userName;
                userSex = com.imt.musiclamp.elementClass.UserInfo.gender;
                if (userSex.equals("male")) {
                    gender.setImageResource(R.drawable.sexmale);
                }
                Log.v("usersex", userSex);
            }
            if(userHeadUrl!=null){
                Glide.with(this)
                    .load(userHeadUrl)
                    .centerCrop()
                        .listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        userImg.setImageResource(R.drawable.defalthead);
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
                    .into(userImg);

            }
            if (userSex.equals("male")) {
                gender.setImageResource(R.drawable.sexmale);
            }
            Log.v("usersex", userSex);
            if (userInfo != null && userInfo.length()>0) {
                info.setText(userInfo);
            }
            if (bitmap != null) {
                userImg.setImageBitmap(bitmap);
            }
            if (userPhone != null && userPhone.length() > 0) {

                myName.setText(userPhone);
            }
            if (userName != null && userName.length() > 0) {
                myName.setText(userName);
            }
        }

        if(!checkLogin()) {
            view.findViewById(R.id.loginalready).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.loginbutton).setVisibility(View.VISIBLE);
        }
        return view;
    }

    private boolean checkLogin() {
        if (((MyApplication) getActivity().getApplication()).userID != null) {
            return true;
        }
        Toast.makeText(getActivity(), "无法登陆远程服务器", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SCANNIN_GREQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    String resulty = bundle.getString("result");
                    Toast.makeText(getActivity(), resulty, Toast.LENGTH_SHORT).show();
                    Log.v("result", resulty);

                    if (resulty.contains("userID")) {
//                        Toast.makeText(getActivity(), resulty, Toast.LENGTH_SHORT).show();
                        StringURL stringURL = new StringURL(StringURL.addFriend);
                        String userid = resulty;
                        userid = userid.substring(userid.indexOf("userID") + "userID".length() + 1);

                        Log.v("userid", userid);

                        new UserServer(handler, new StringURL(StringURL.addFriend).setFriendID(userid).setUserID(MyApplication.userID).setFriendType("0").toString()) {

                            @Override
                            public void httpBack(JSONObject jsonObject) {
                                if (StrUrl.getResult(jsonObject).equals("1")) {
                                    Toast.makeText(getActivity(), "请求已发送", Toast.LENGTH_SHORT).show();
                                }
                            }
                        };
                    } else if (bundle.getString("result").contains("deviceID")) {
//                        Toast.makeText(getActivity(), bundle.getString("result"), Toast.LENGTH_SHORT).show();
                        String deviceid = resulty;
                        deviceid = deviceid.substring(deviceid.indexOf("deviceID") + "deviceID".length() + 1);

                        new UserServer(handler, new StringURL(StringURL.addDevice).setDeviceID(deviceid).setUserID(MyApplication.userID).toString()) {

                            @Override
                            public void httpBack(JSONObject jsonObject) {
                                if (StrUrl.getResult(jsonObject).equals("1")) {
                                    Toast.makeText(getActivity(), "请求已发送", Toast.LENGTH_SHORT).show();
                                }
                            }
                        };
                    }
                }
                break;
            case SCANNIN_GREQUEST_CODE_L:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    Toast.makeText(getActivity(), bundle.getString("result"), Toast.LENGTH_SHORT).show();
                    StringURL stringURL = new StringURL(StringURL.addFriend);
                    String userid = bundle.getString("result");
                    userid = userid.substring(userid.indexOf("userID") + "userID".length() + 1);

                    Log.v("userid", userid);

                    new UserServer(handler, new StringURL(StringURL.addFriend).setFriendID(userid).setUserID(MyApplication.userID).setFriendType("1").toString()) {

                        @Override
                        public void httpBack(JSONObject jsonObject) {
                            if (StrUrl.getResult(jsonObject).equals("1")) {
                                Toast.makeText(getActivity(), "请求已发送", Toast.LENGTH_SHORT).show();
                            }
                        }
                    };
                }
            case SETUSERINFO:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    if (bundle != null) {
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), Uri.parse(bundle.getString("userHeadUrl")));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        userName = bundle.getString("nickname");
                        userSex = bundle.getString("gender");
                        userPhone = bundle.getString("userPhone");
                        userInfo = bundle.getString("userInfo");
                        UserInfoEvent userInfoEvent=new UserInfoEvent();
                        userInfoEvent.setUserHead(bundle.getString("userHeadUrl"));
                        userInfoEvent.setUserName(userName);
                        MyApplication.userName=userName;
                        EventBus.getDefault().post(userInfoEvent);
                        if (userInfo != null && userInfo != "") {
                            info.setText(userInfo);
                        }
                        if (bitmap != null) {
                            userImg.setImageBitmap(bitmap);
                        }
                        if (userPhone != null && userPhone.length() > 0) {

                            myName.setText(userPhone);
                        }
                        if (userName != null && userName.length() > 0) {
                            myName.setText(userName);
                        }
                        if (userSex != null) {
                            if (userSex.equals("male")) {
                                gender.setImageResource(R.drawable.sexmale);
                            } else {
                                gender.setImageResource(R.drawable.sexfmale);
                            }
                        }
                    }

                }
        }
    }

    public ImageView Create2QR() {
        ImageView qrImg = new ImageView(getActivity());
//        String uri = "IMTuserID:"+((MyApplication)getActivity().getApplication()).userID;
        String uri = new StringURL(StringURL.QR).setUserID(MyApplication.userID).toString();
//      Bitmap bitmap = BitmapUtil.create2DCoderBitmap(uri, mScreenWidth/2, mScreenWidth/2);
        Bitmap bitmap;
        try {
            bitmap = BitmapUtil.createQRCode(uri, getView().getWidth());

            if (bitmap != null) {
                qrImg.setImageBitmap(bitmap);
            }

        } catch (WriterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return qrImg;
    }
}
