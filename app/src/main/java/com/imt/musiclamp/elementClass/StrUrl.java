package com.imt.musiclamp.elementClass;

import android.view.View;
import android.widget.AdapterView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by dtw on 15/3/19.
 */
public class StrUrl{
    private final static String IPend="&signature=*#signature&timestamp=*#timestamp";

    private final static String IP = "http://112.74.105.77:8080/";
    public final static String regist = IP + "phone/register.do?phoneNumber=*#phoneNumber"+IPend;
    public final static String changePass = IP + "phone/modifyUserPassword.do?userID=*#userID&oldPassword=*#oldPass&newPassword=*#newPass&isFirstSetPassword=*#isFirst"+IPend;
    public final static String loginIMT = IP + "phone/login.do?phoneNumber=*#phoneNumber&password=*#pass"+IPend;
    public final static String loginThird = IP + "phone/loginWith3rd.do?3rdID=*#userID&headIcon=*#userImg&nickName=*#nickName&sex=*#gender&platformType=*#type"+IPend;

    final static String phone = "*#phoneNumber";
    final static String userID = "*#userID";
    final static String oldPass = "*#oldPass";
    final static String newPass = "*#newPass";
    final static String isFirst = "*#isFirst";
    final static String gender = "*#gender";
    final static String pass = "*#pass";
    final static String userImg = "*#userImg";
    final static String nickName = "*#nickName";
    final static String type = "*#type";

    final static String signature="*#signature";
    final static String timestamp="*#timestamp";

    public static String setPhone(String urlStr, String phoneNum) {
        return addSecret(urlStr,phone,phoneNum);
    }
    public static String setUserID(String urlStr,String userIDR){
        return addSecret(urlStr, userID, userIDR);
    }
    public static String setOldPass(String urlStr,String oldPassR){
        return addSecret(urlStr, oldPass, oldPassR);
    }
    public static String setNewPass(String urlStr,String NewPassR){
        return addSecret(urlStr, newPass, NewPassR);
    }
    public static String setIsFirst(String urlStr,String isFirstR){
        return addSecret(urlStr, isFirst, isFirstR);
    }
    public static String setGender(String urlStr,String genderR){
        return addSecret(urlStr, gender, genderR);
    }
    public static String getRongToken(JSONObject jsonObject){
        try {
            return jsonObject.getString("rongCloudToken");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "error";
    }
    public static String setPass(String urlStr,String passR){
        return addSecret(urlStr, pass, passR);
    }
    public static String setUserImg(String urlStr,String userImgR){
        return addSecret(urlStr, userImg, userImgR);
    }
    public static String setNickName(String urlStr,String nickNameR){
        try {
            return addSecret(urlStr, nickName, URLEncoder.encode(nickNameR, "UTF8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return  null;
    }
    public static String setType(String urlStr,String typeR){
        return addSecret(urlStr, type, typeR);
    }
    public static String getResult(JSONObject jsonObject){
        try {
            return jsonObject.getString("result");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "error";
    }
    public static String getUserID(JSONObject jsonObject){
        try {
            return jsonObject.getString("userID");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "error";
    }
    public static String getUserName(JSONObject jsonObject){
        try {
            return jsonObject.getString("nickName");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "error";
    }
    public static String getUserSex(JSONObject jsonObject){
        try {
            return jsonObject.getString("sex");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "error";
    }
    public static String getIcon(JSONObject jsonObject){
        try {
            return jsonObject.getString("headIconURL");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "error";
    }

    private static String addSecret(String urlStr,String oldStr,String newStr){
        String tempStr=urlStr.replace(oldStr,newStr);
        if(tempStr.contains(signature)){
            String currentTime=String.valueOf(System.currentTimeMillis());
            tempStr=tempStr.replace(signature,Md5Base64Encode.createIMTSignature(currentTime)).replace(timestamp, currentTime);
        }
        return tempStr;
    }

}