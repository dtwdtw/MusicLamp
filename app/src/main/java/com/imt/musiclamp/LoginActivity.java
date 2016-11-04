package com.imt.musiclamp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.text.Selection;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.imt.musiclamp.SQLite.CreateSQLite;
import com.imt.musiclamp.elementClass.Constants;
import com.imt.musiclamp.elementClass.StrUrl;
import com.imt.musiclamp.elementClass.UserServer;
import com.imt.musiclamp.model.User;
import com.imt.musiclamp.service.RongPush;
import com.imt.musiclamp.view.IEditText;
import com.tencent.connect.UserInfo;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by dtw on 15/3/13.
 */

public class LoginActivity extends ActionBarActivity implements View.OnClickListener{

    private TextView skip;
    private Context context;
    TextView regist;
    RelativeLayout weinxin, qq;
    final int MOBCOMPLITE = 11;
    Tencent mTencent;
    String bitmap;
    Handler handler;
//    TextView jumpout;
    public static IWXAPI api;
    String userID = null;

    UserServer userServer;


    IEditText textPhone;
    IEditText textPwd;
    final int MOBBACK = 10;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        context = LoginActivity.this;

        textPhone = (IEditText) findViewById(R.id.textPhone);
        textPwd = (IEditText) findViewById(R.id.textPwd);
//        textPwd.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if(keyCode==KeyEvent.ACTION_DOWN) {
//                    InputMethodManager inputmanger = (InputMethodManager) getSystemService(LoginActivity.this.INPUT_METHOD_SERVICE);
//                    inputmanger.hideSoftInputFromWindow(textPwd.getWindowToken(), 0);
//                    findViewById(R.id.buttonOK).callOnClick();
//                }
//
//                return false;
//            }
//        });

        skip = (TextView) this.findViewById(R.id.skip);
        skip.setOnClickListener(this);

        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (0 == textPhone.getText().toString().length()) {
                    Toast.makeText(context, R.string.phone_number_not_empty, Toast.LENGTH_SHORT).show();
                    textPhone.requestFocus();
                } else if (11 != textPhone.getText().toString().length()) {
                    Toast.makeText(context, R.string.phone_number_error, Toast.LENGTH_SHORT).show();
                    textPhone.requestFocus();
                    Selection.setSelection(textPhone.getText(), textPhone.getText().toString().length());
                } else if (0 == textPwd.getText().toString().length()) {
                    Toast.makeText(context, R.string.password_not_empty, Toast.LENGTH_SHORT).show();
                    textPwd.requestFocus();
                } else if (6 > textPwd.getText().toString().length()) {
                    Toast.makeText(context, R.string.password_less_than_6, Toast.LENGTH_SHORT).show();
                    textPwd.requestFocus();
                    Selection.setSelection(textPhone.getText(), textPhone.getText().toString().length());
                } else {
                    String addphone = StrUrl.setPhone(StrUrl.loginIMT, textPhone.getText().toString());
                    String addpass = StrUrl.setPass(addphone, textPwd.getText().toString());
                    User user = new User();
                    user.setName(textPhone.getText().toString());
                    user.setType("IMT");
                    user.setPassword(textPwd.getText().toString());
                    user.save();
                    new UserServer(LoginActivity.this, handler, addpass) {
                        String userName;
                        String userID;
                        String userSex;
                        String sign;

                        @Override
                        public void httpBack(JSONObject jsonObject) {
                            try {
                                if (jsonObject.getString("result").equals("1")) {
                                    ((MyApplication) getApplication()).userID = StrUrl.getUserID(jsonObject);
                                    MyApplication.rongToken = StrUrl.getRongToken(jsonObject);

                                    userName = StrUrl.getUserName(jsonObject);
                                    userID = StrUrl.getUserID(jsonObject);
                                    userSex = StrUrl.getUserSex(jsonObject);
                                    sign = jsonObject.getString("sign");
                                    Log.v("usersign", sign);
                                    final Intent intent = new Intent();
                                    final Bundle bundle = new Bundle();
                                    bundle.putString("userPhone", textPhone.getText().toString());
                                    bundle.putString("userID", userID);
                                    bundle.putString("nickname", userName);
                                    bundle.putString("gender", userSex);
                                    bundle.putString("userInfo", sign);
                                    bundle.putString("userHeadUrl", jsonObject.getString("headIconURL"));
                                    MyApplication.headUri = jsonObject.getString("headIconURL");
                                    MyApplication.userName = userName;
                                    intent.putExtras(bundle);
                                    intent.setClass(LoginActivity.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    LoginActivity.this.startService(new Intent(LoginActivity.this, RongPush.class));
                                    startActivity(intent);
                                    finish();

                                } else {
                                    Toast.makeText(LoginActivity.this, "登陆失败", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                }
            }
        });
        findViewById(R.id.forgetPwd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, MobActivity.class);
                intent.setFlags(1);
                startActivityForResult(intent, MOBBACK);
            }
        });
//        bitmap=BitmapFactory.decodeResource(getResources(),R.drawable.test);
//        bitmap=BitmapBlur.blurBitmap(bitmap,this);
//        BitmapDrawable bitmapDrawable=new BitmapDrawable(getResources(),bitmap);
//        getWindow().setBackgroundDrawable(bitmapDrawable);

//        jumpout = (TextView) findViewById(R.id.jumpout);
        weinxin = (RelativeLayout) findViewById(R.id.wechat_layout);
        regist = (TextView) findViewById(R.id.regist);
        qq = (RelativeLayout) findViewById(R.id.qq_layout);
        mTencent = Tencent.createInstance("1104359946", this);
        handler = new Handler();


//        long time=System.currentTimeMillis();
//        Bitmap bit=BitmapFactory.decodeResource(getResources(),R.drawable.mm);
//        bit=ThumbnailUtils.extractThumbnail(bit,60,80);
//        bit=BitmapBlur.blurBitmap(bit,LoginActivity.this);
//        bit=ThumbnailUtils.extractThumbnail(bit,840,1280);
//        LoginActivity.this.getWindow().setBackgroundDrawable(new BitmapDrawable(getResources(),bit));
//        LoginActivity.this.getWindow().getDecorView().getBackground().setColorFilter(0x440000ff, PorterDuff.Mode.DARKEN);
//        Log.v("time",System.currentTimeMillis()-time+"");
        EventBus.getDefault().register(this);
        List<User> listUser = null;
        try {
            listUser = new Select().from(User.class).execute();
        } catch (Exception e) {
        }

        if (listUser != null && listUser.size() > 0 && MyApplication.firstLogin) {
            final String userPhone = listUser.get(listUser.size() - 1).getName();
            String userPwd = listUser.get(listUser.size() - 1).getPassword();
            String addphone = StrUrl.setPhone(StrUrl.loginIMT, userPhone);
            String addpass = StrUrl.setPass(addphone, userPwd);

            Log.v("out", addpass);
            new UserServer(handler, addpass) {
                String userName;
                String userID;
                String userSex;
                String userInfo;
                String userPicurl;

                @Override
                public void httpBack(JSONObject jsonObject) {
                    try {
                        if (jsonObject.getString("result").equals("1")) {
//                            Toast.makeText(LoginActivity.this, "登陆成功", Toast.LENGTH_SHORT).show();
                            ((MyApplication) getApplication()).userID = StrUrl.getUserID(jsonObject);
                            MyApplication.rongToken = StrUrl.getRongToken(jsonObject);
                            Log.v("token", StrUrl.getRongToken(jsonObject));


                            userName = StrUrl.getUserName(jsonObject);
                            userID = StrUrl.getUserID(jsonObject);
                            userSex = StrUrl.getUserSex(jsonObject);
                            userInfo = jsonObject.getString("sign");
                            userPicurl = jsonObject.getString("headIconURL");
                            Intent intent = new Intent();
                            Bundle bundle = new Bundle();
                            bundle.putString("userPhone", userPhone);
//                                        bundle.putParcelable("userImg",userP®ic);
                            bundle.putString("userID", userID);
                            bundle.putString("nickname", userName);
                            bundle.putString("gender", userSex);
                            bundle.putString("userInfo", userInfo);
                            bundle.putString("userHeadUrl", userPicurl);
                            intent.putExtras(bundle);
                            MyApplication.headUri = userPicurl;
                            MyApplication.userName = userName;
                            intent.setClass(LoginActivity.this, MainActivity.class);
                            LoginActivity.this.startService(new Intent(LoginActivity.this, RongPush.class));
                            startActivity(intent);
                            Log.v("userhead", userPicurl);
                            finish();


                        } else {
                            Toast.makeText(LoginActivity.this, "登陆失败", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(LoginActivity.this, "登陆失败", Toast.LENGTH_SHORT).show();
                    }
                }
            };
        }

//        jumpout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.setClass(LoginActivity.this, MainActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
//            }
//        });

        weinxin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                api = WXAPIFactory.createWXAPI(LoginActivity.this, Constants.APP_ID, false);
                api.registerApp(Constants.APP_ID);
                SendAuth.Req req = new SendAuth.Req();
                req.scope = "snsapi_userinfo";
                req.state = "imt_musiclamp";
                api.sendReq(req);
                Log.v("weixinclick", "weixinclick");

            }
        });

        regist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, MobActivity.class);
                intent.setFlags(0);
                startActivityForResult(intent, MOBBACK);
            }
        });

//        loginB.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.setClass(LoginActivity.this, login.class);
//                startActivity(intent);
//            }
//        });
        qq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTencent.login(LoginActivity.this, "all", new listener());
            }
        });
    }

    private void insertUser(String type, String key, String pwd) {
        new CreateSQLite(this).insertUser(type, key, pwd);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.skip :
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            break;
        }
    }

    class listener implements IUiListener {

        @Override
        public void onComplete(Object o) {
            JSONTokener jsonTokener = new JSONTokener(o.toString());
            JSONObject jsonObject = null;
            try {
                jsonObject = (JSONObject) jsonTokener.nextValue();
                Log.i("only", jsonObject.getString("pay_token"));
                userID = jsonObject.getString("openid");
                UserInfo info = new UserInfo(LoginActivity.this, mTencent.getQQToken());
                info.getUserInfo(this);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                final JSONObject jsonObject2 = jsonObject;
                final String userID2 = userID;
                Log.i("all", o.toString());
                Log.i("json", jsonObject.getString("nickname"));
                Log.i("pic", jsonObject.getString("figureurl_qq_1"));
                new Thread(new Runnable() {
                    String userName,
                            userSex;

                    @Override
                    public void run() {

                        URL url = null; //path图片的网络地址
                        try {
                            url = new URL(jsonObject2.getString("figureurl_qq_2"));

                            MyApplication.headUri = jsonObject2.getString("figureurl_qq_2");
                            userName = jsonObject2.getString("nickname");
                            MyApplication.userName = userName;
                            userSex = jsonObject2.getString("gender").contains("男") ? "male" : "female";
                            Log.v("gender", jsonObject2.getString("gender"));
//                            User user=new User();
//                            user.setName(userName);
//                            user.setThID(userID);
//                            user.setGender(userSex);

//                            user.setType("QQ");
//                            user.save();
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
//                        HttpURLConnection httpURLConnection = null;
//                        try {
//                            httpURLConnection = (HttpURLConnection) url.openConnection();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }

                        bitmap = url.toString();
                        String thirdUrl = StrUrl.loginThird;
                        thirdUrl = StrUrl.setGender(thirdUrl, userSex);
                        thirdUrl = StrUrl.setNickName(thirdUrl, userName);
                        thirdUrl = StrUrl.setUserID(thirdUrl, userID2);
                        thirdUrl = StrUrl.setUserImg(thirdUrl, url.toString());
                        thirdUrl = StrUrl.setType(thirdUrl, "QQ");
                        Log.v("", thirdUrl);

                        new UserServer(handler, thirdUrl) {
                            @Override
                            public void httpBack(JSONObject jsonObject) {
                                if (StrUrl.getResult(jsonObject).equals("1")) {
                                    ((MyApplication) getApplication()).userID = StrUrl.getUserID(jsonObject);
                                    MyApplication.rongToken = StrUrl.getRongToken(jsonObject);
                                    LoginActivity.this.startService(new Intent(LoginActivity.this, RongPush.class));
                                    String userInfo = "";
                                    try {
                                        bitmap=jsonObject.getString("headIconURL");
                                        MyApplication.headUri=bitmap;
                                        userName = jsonObject.getString("nickName");
                                        userSex = jsonObject.getString("sex");
                                        userInfo = jsonObject.getString("sign");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    handler.post(new upImg(bitmap, MyApplication.userID, userName, userSex, userInfo));
                                    Log.v("thirdLogin", jsonObject.toString());
                                }
                            }
                        };
                    }
                }).start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        @Override
        public void onError(UiError uiError) {

        }

        @Override
        public void onCancel() {

        }
    }

    class upImg implements Runnable {
        String bitmap;
        String userID, userName, userSex, userInfo;

        public upImg(String bitmap, String userID, String userName, String userSex, String userInfo) {
            this.bitmap = bitmap;
            this.userID = userID;
            this.userName = userName;
            this.userSex = userSex;
            this.userInfo = userInfo;
        }

        @Override
        public void run() {
//            userPic.setImageBitmap(bitmap);//加载到ImageView上
            Toast.makeText(LoginActivity.this, "QQ登陆成功", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString("userHeadUrl", bitmap);
            bundle.putString("nickname", userName);
            bundle.putString("gender", userSex);
            bundle.putString("userInfo", userInfo);
            MyApplication.userName=userName;
            intent.putExtras(bundle);
            intent.setClass(LoginActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }


    public void onEvent(String userInfo) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putBoolean("tobig", true);
        intent.putExtras(bundle);
        intent.setClass(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        Log.v("hello", "catch");
//        handler.post(new upImg((Bitmap)event.getParcelable("userImg"),event.getString("userID"),event.getString("nickname"),event.getString("gender")));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case MOBBACK:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    String userPhone = bundle.getString("userPhone");
                    Toast.makeText(this, userPhone, Toast.LENGTH_SHORT).show();
                    if (!bundle.getBoolean("ischange")) {
                        Intent intent = new Intent();
                        intent.putExtras(bundle);
                        intent.setClass(LoginActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();

                    } else {
                        Intent intent = new Intent();
                        intent.putExtras(bundle);
                        intent.setClass(LoginActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                }
                break;
//            case MOBCOMPLITE:
//                if (resultCode == RESULT_OK) {
//
//                    final Bundle bundle = data.getExtras();
////                    Toast.makeText(this, bundle.getString("userPhone"), Toast.LENGTH_SHORT).show();
//
//                    userServer = new UserServer(this, handler, StrUrl.setPhone(StrUrl.regist, bundle.getString("userPhone"))) {
//                        @Override
//                        public void httpBack(JSONObject jsonObject) {
//                            try {
//                                if (jsonObject.getString("result").equals("1")) {
//                                    Toast.makeText(LoginActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
//                                    ((MyApplication) getApplication()).userID = jsonObject.getString("userID");
//                                    MyApplication.rongToken=jsonObject.getString("rongCloudToken");
//                                    Intent intent = new Intent();
//                                    intent.putExtras(bundle);
//                                    intent.setClass(LoginActivity.this, MainActivity.class);
//                                    startActivity(intent);
//                                    finish();
//                                } else {
//                                    Toast.makeText(LoginActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
//                                }
//
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    };

//                    Intent intent = new Intent();
//                    intent.putExtras(bundle);
//                    intent.setClass(LoginActivity.this, MainActivity.class);
//                    startActivity(intent);
//                    finish();
//                }
        }
    }
}
