package com.imt.musiclamp.wxapi;


import com.imt.musiclamp.LoginActivity;
import com.imt.musiclamp.MainActivity;
import com.imt.musiclamp.MyApplication;
import com.imt.musiclamp.R;
import com.imt.musiclamp.elementClass.Constants;
import com.imt.musiclamp.elementClass.StrUrl;
import com.imt.musiclamp.elementClass.StringURL;
import com.imt.musiclamp.elementClass.UserInfo;
import com.imt.musiclamp.elementClass.UserServer;
import com.imt.musiclamp.service.RongPush;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import de.greenrobot.event.EventBus;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;

    private Button gotoBtn, regBtn, launchBtn, checkBtn;
    Handler handler = new Handler();
    String bitmap;
    String userName;
    String gender;
    String headUri;
    String openID;

    private IWXAPI api;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        LoginActivity.api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

//		setIntent(intent);
//        api.handleIntent(intent, this);
    }


    @Override
    public void onReq(BaseReq baseReq) {

    }

    @Override
    public void onResp(BaseResp baseResp) {
        String result = null;
        Log.v("resp", "resp");
        switch (baseResp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                result = "success";
                new UserServer(WXEntryActivity.this, handler, new StringURL(StringURL.weixinGetToken).setCode(((SendAuth.Resp) baseResp).code).setAppid(Constants.APP_ID).setSecret(Constants.secret).toString()) {
                    @Override
                    public void httpBack(JSONObject jsonObject) throws JSONException {
                        String accessToken = jsonObject.getString("access_token");
                        openID = jsonObject.getString("openid");
                        new UserServer(WXEntryActivity.this, handler, new StringURL(StringURL.weixinGerUInfo).setToken(accessToken).setOpenID(openID).toString()) {
                            @Override
                            public void httpBack(JSONObject jsonObject) throws JSONException {
                                userName = jsonObject.getString("nickname");
                                gender = jsonObject.getString("sex").contains("1") ? "male" : "female";
                                headUri = jsonObject.getString("headimgurl");
                                openID = jsonObject.getString("openid");

                                MyApplication.headUri = jsonObject.getString("headimgurl");
                                MyApplication.userName = jsonObject.getString("nickname");
                                Log.v("headimg", headUri);
                                new Thread(new Runnable() {

                                    @Override
                                    public void run() {
                                        URL url = null; //path图片的网络地址
                                        try {
                                            url = new URL(headUri);
                                        } catch (MalformedURLException e) {
                                            e.printStackTrace();
                                        }

                                        bitmap = url.toString();
                                        String thirdUrl = StrUrl.loginThird;
                                        thirdUrl = StrUrl.setGender(thirdUrl, gender);
                                        thirdUrl = StrUrl.setNickName(thirdUrl, userName);
                                        thirdUrl = StrUrl.setUserID(thirdUrl, openID);
                                        thirdUrl = StrUrl.setUserImg(thirdUrl, url.toString());
                                        thirdUrl = StrUrl.setType(thirdUrl, "wx");
                                        Log.v("", thirdUrl);

                                        new UserServer(WXEntryActivity.this, handler, thirdUrl) {
                                            @Override
                                            public void httpBack(JSONObject jsonObject) {
                                                if (StrUrl.getResult(jsonObject).equals("1")) {
                                                    ((MyApplication) getApplication()).userID = StrUrl.getUserID(jsonObject);
                                                    MyApplication.rongToken = StrUrl.getRongToken(jsonObject);
                                                    WXEntryActivity.this.startService(new Intent(WXEntryActivity.this, RongPush.class));
                                                    String userInfo = "";
                                                    try {
                                                        bitmap=jsonObject.getString("headIconURL");
                                                        MyApplication.headUri=bitmap;
                                                        MyApplication.userID=jsonObject.getString("userID");
                                                        userName = jsonObject.getString("nickName");
                                                        gender = jsonObject.getString("sex");
                                                        userInfo = jsonObject.getString("sign");
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                    handler.post(new upImg(bitmap, MyApplication.userID, userName, gender, userInfo));
                                                }
                                            }
                                        };
                                    }
                                }).start();
                            }
                        };
                    }
                };
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                result = "cancle";
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                result = "deny";
                Toast.makeText(WXEntryActivity.this, "微信拒绝获取用户信息,请选择其他方式登录!", Toast.LENGTH_LONG).show();
                finish();
                break;
            default:
                result = "unknow";
                break;
        }

        Log.v("result", result);
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
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            Toast.makeText(WXEntryActivity.this, "微信登陆成功", Toast.LENGTH_SHORT).show();

            bundle.putString("userHeadUrl", bitmap);
            bundle.putString("nickname", userName);
            bundle.putString("gender", userSex);
            bundle.putString("userInfo", userInfo);

            bundle.putString("userID", userID);
            MyApplication.userID = userID;

            intent.putExtras(bundle);
            intent.setClass(WXEntryActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
//            EventBus.getDefault().post("userInfo");
//            new EventBus().post("userInfo");
            finish();
        }
    }
}