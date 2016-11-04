package com.imt.musiclamp;

import android.content.Intent;
import android.os.Bundle;

import static cn.smssdk.framework.utils.R.getStringRes;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.imt.musiclamp.SQLite.CreateSQLite;
import com.imt.musiclamp.elementClass.StrUrl;
import com.imt.musiclamp.elementClass.UserServer;
import com.imt.musiclamp.model.User;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * int i = getIntent().getFlags();
 * i = 0 : 注册账号(sign up)
 * i = 1 : 找回密码(find password)
 * i = 2 : 修改密码(change password)
 */
public class MobActivity extends Activity implements OnClickListener {

    private TextView sensmsButton,verificationButton, title;
    private EditText phonEditText,verEditText,pass1;
    private ImageView back;
    // 填写从短信SDK应用后台注册得到的APPKEY
    private static String APPKEY = "614a1373c394";//463db7238681  27fe7909f8e8
    // 填写从短信SDK应用后台注册得到的APPSECRET
    private static String APPSECRET = "9af29c8735dd995b3e46862f9a0b34a0";//
    public String phString;                                       //3684fd4f0e16d68f69645af1c7f8f5bd

    private int type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mob);

        type = getIntent().getFlags();

        title = (TextView) findViewById(R.id.title);

        sensmsButton=(TextView) findViewById(R.id.button1);
        verificationButton=(TextView) findViewById(R.id.button2);

        phonEditText=(EditText) findViewById(R.id.editText1);
        verEditText=(EditText) findViewById(R.id.editText2);
        sensmsButton.setOnClickListener(this);
        verificationButton.setOnClickListener(this);
        pass1=(EditText)findViewById(R.id.passWord1);

        back = (ImageView) this.findViewById(R.id.back);
        back.setOnClickListener(this);

        if(0 == type) {
            title.setText(R.string.welcome_to_signup);
            verificationButton.setText(R.string.sign_up);
        } else if(1 == type) {
            title.setText(R.string.find_password);
            verificationButton.setText(R.string.submit);
        } else if(2 == type) {
            title.setText(R.string.change_password);
            verificationButton.setText(R.string.change);
        }

        verEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode==KeyEvent.ACTION_DOWN){
                    sensmsButton.callOnClick();
                    pass1.callOnClick();
                }
                return false;
            }
        });
        pass1.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode==KeyEvent.ACTION_DOWN){
                    verificationButton.callOnClick();
                    InputMethodManager inputmanger = (InputMethodManager) getSystemService(MobActivity.this.INPUT_METHOD_SERVICE);
                    inputmanger.hideSoftInputFromWindow(pass1.getWindowToken(), 0);
                }
                return false;
            }
        });

        //System.loadLibrary("OSbase");
        SMSSDK.initSDK(this,APPKEY,APPSECRET);
        EventHandler eh=new EventHandler(){

            @Override
            public void afterEvent(int event, int result, Object data) {

                Message msg = new Message();
                msg.arg1 = event;
                msg.arg2 = result;
                msg.obj = data;
                handler.sendMessage(msg);
            }

        };
        SMSSDK.registerEventHandler(eh);
    }
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.button1://获取验证码
                if(!TextUtils.isEmpty(phonEditText.getText().toString())){
                    SMSSDK.getVerificationCode("86",phonEditText.getText().toString());
                    phString=phonEditText.getText().toString();
                }else {
                    Toast.makeText(this, R.string.phone_number_not_empty, Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.button2://校验验证码
                if(!TextUtils.isEmpty(verEditText.getText().toString())&&pass1.getText().toString().length()>5){
                    SMSSDK.submitVerificationCode("86", phString, verEditText.getText().toString());
                }
                else if(pass1.getText().toString().length()<6){
                    Toast.makeText(this, R.string.password_not_meet_specification, Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(this, R.string.verification_code_not_empty, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.back :
                this.finish();
                break;
            default:
                break;
        }
    }
    Handler handler=new Handler(){

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            int event = msg.arg1;
            int result = msg.arg2;
            Object data = msg.obj;
            Log.e("event", "event="+event);
            if (result == SMSSDK.RESULT_COMPLETE) {
                //短信注册成功后，返回MainActivity,然后提示新好友
                if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {//提交验证码成功
//                    Toast.makeText(getApplicationContext(), "提交验证码成功", Toast.LENGTH_SHORT).show();
                    Toast.makeText(MobActivity.this, R.string.verification_code_has_been_submitted_successfully,Toast.LENGTH_SHORT).show();
                    if(pass1.getText().toString().length()<1){
                        Toast.makeText(MobActivity.this, R.string.password_not_empty,Toast.LENGTH_SHORT).show();
                    }
                    else{
                        new UserServer(MobActivity.this, handler, StrUrl.setPhone(StrUrl.regist, phonEditText.getText().toString())){

                            @Override
                            public void httpBack(JSONObject jsonObject) {
                                ((MyApplication)getApplication()).userID=StrUrl.getUserID(jsonObject);
                                if(StrUrl.getResult(jsonObject).equals("1")){
//                                    insertUser("IMT",phonEditText.getText().toString(),pass1.getText().toString());

                                    String urltemp=StrUrl.changePass;
                                    urltemp=StrUrl.setPhone(urltemp,phonEditText.getText().toString());
                                    urltemp=StrUrl.setNewPass(urltemp,pass1.getText().toString());
                                    urltemp=StrUrl.setIsFirst(urltemp,"1");
                                    urltemp=StrUrl.setUserID(urltemp,((MyApplication)getApplication()).userID);
                                    urltemp=StrUrl.setOldPass(urltemp,"");
                                    Log.v("",urltemp);
                                    new UserServer(MobActivity.this,handler,urltemp){

                                        @Override
                                        public void httpBack(JSONObject jsonObject) {
                                            if(StrUrl.getResult(jsonObject).equals("1")){
                                                User user=new User();
                                                user.setName(phonEditText.getText().toString());
                                                user.setPassword(pass1.getText().toString());
                                                user.setType("IMT");
                                                user.save();

                                                Toast.makeText(MobActivity.this, R.string.signup_sucessfully,Toast.LENGTH_SHORT).show();
                                                Intent resultIntent = new Intent();
                                                Bundle bundle = new Bundle();
                                                bundle.putString("userPhone",phonEditText.getText().toString() );
                                                bundle.putBoolean("ischange", false);
                                                bundle.putString("gender","");
                                                resultIntent.putExtras(bundle);
                                                MobActivity.this.setResult(RESULT_OK, resultIntent);
                                                finish();
                                            }
                                            else{
                                                Toast.makeText(MobActivity.this,"Error",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    };
                                }
                                else if(StrUrl.getResult(jsonObject).equals("0")){
                                    String urltemp=StrUrl.changePass;
                                    urltemp=StrUrl.setPhone(urltemp, phonEditText.getText().toString());
                                    urltemp=StrUrl.setNewPass(urltemp, pass1.getText().toString());
                                    urltemp=StrUrl.setIsFirst(urltemp, "1");
                                    urltemp=StrUrl.setUserID(urltemp, ((MyApplication) getApplication()).userID);
                                    urltemp=StrUrl.setOldPass(urltemp, "");
                                    final Bundle bundle = new Bundle();
                                    bundle.putString("userPhone",phonEditText.getText().toString() );
                                    try {
                                        bundle.putString("nickname",jsonObject.getString("nickName"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    bundle.putBoolean("ischange", true);
                                    try {
                                        bundle.putString("gender",jsonObject.getString("sex"));
                                        bundle.putString("userHeadUrl",jsonObject.getString("headIconURL"));
                                        bundle.putString("userInfo",jsonObject.getString("sign"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    Log.v("",urltemp);
                                    new UserServer(MobActivity.this,handler,urltemp){

                                        @Override
                                        public void httpBack(JSONObject jsonObject) {
                                            if(StrUrl.getResult(jsonObject).equals("1")){
                                                User user=new User();
                                                user.setName(phonEditText.getText().toString());
                                                user.setPassword(pass1.getText().toString());
                                                user.setType("IMT");
                                                user.save();

                                                Toast.makeText(MobActivity.this, R.string.password_has_been_updated_successfully,Toast.LENGTH_SHORT).show();
                                                Intent resultIntent = new Intent();
                                                resultIntent.putExtras(bundle);
                                                MobActivity.this.setResult(RESULT_OK, resultIntent);
                                                finish();
                                            }
                                            else{
                                                Toast.makeText(MobActivity.this,"Error",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    };
                                }
                                else{
                                    Toast.makeText(MobActivity.this,"Error",Toast.LENGTH_SHORT).show();
                                }
                            }
                        };
                    }


                } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE){
                    Toast.makeText(getApplicationContext(), R.string.verification_code_has_been_sent, Toast.LENGTH_SHORT).show();
                }else if (event ==SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES){//返回支持发送验证码的国家列表
                    Toast.makeText(getApplicationContext(), R.string.get_countries_successfully, Toast.LENGTH_SHORT).show();
                }
            } else {
                ((Throwable) data).printStackTrace();
                int resId = getStringRes(MobActivity.this, "smssdk_network_error");
                Toast.makeText(MobActivity.this, R.string.verification_code_error, Toast.LENGTH_SHORT).show();
                if (resId > 0) {
                    Toast.makeText(MobActivity.this, resId, Toast.LENGTH_SHORT).show();
                }
            }

        }

    };
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        SMSSDK.unregisterAllEventHandler();
    }

    private void insertUser(String type,String key,String pwd){
        new CreateSQLite(this).insertUser(type, key, pwd);
    }
}