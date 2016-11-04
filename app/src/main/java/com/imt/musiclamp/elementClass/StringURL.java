package com.imt.musiclamp.elementClass;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by dtw on 15/3/23.
 */
public class StringURL {
    private final static String IP = "http://112.74.105.77:8080/phone/";
    private final static String IPend="&signature=*#signature&timestamp=*#timestamp";
    public final static String regist = IP + "register.do?phoneNumber=*#phoneNumber";
    public final static String changePass = IP + "modifyUserPassword.do?userID=*#userID&oldPassword=*#oldPass&newPassword=*#newPass&isFirstSetPassword=*#isFirst";
    public final static String changeUserInfo = IP + "modifyUserInfo.do?userID=*#userID&sex=*#gender&nickName=*#nickName";
    public final static String loginIMT = IP + "login.do?phoneNumber=*#phoneNumber&password=*#pass";
    public final static String loginThird = IP + "loginWith3rd.do?3rdID=*#userID&headIcon=*#userImg&nickName=*#nickName&sex=*#gender&platformType=*#type";
    public final static String getUserInfoByID = IP + "getUserInfoByID.do?userID=*#userID";
    public final static String conformAdd=IP+"confirmFriend.do?userID=*#userID&friendID=*#friendID&ftype=*#fType";
    public final static String addFriend=IP+"addFriend.do?userID=*#userID&friendID=*#friendID&ftype=*#fType";
    public final static String getFriendList=IP+"getFriendsByUserID.do?userID=*#userID&ftype=*#fType";
    public final static String requestFriend=IP+"getRequestFriendsByUserID.do?userID=*#userID&ftype=*#fType";
    public final static String deleteFriend=IP+"delFriend.do?userID=*#userID&friendID=*#friendID&ftype=*#fType";
    public final static String searchUser=IP+"searchUser.do?keyWord=*#friendID&userID=*#userID&sex=&start=*#start&count=*#count";
    public final static String weixinGetToken="https://api.weixin.qq.com/sns/oauth2/access_token?appid=*#APPID&secret=*#SECRET&code=*#CODE&grant_type=authorization_code";
    public final static String weixinGerUInfo="https://api.weixin.qq.com/sns/userinfo?access_token=*#ACCESS_TOKEN&openid=*#OPENID";
    public final static String addDevice=IP+ "bindDevice.do?UUID=*#diveceID&userID=*#userID";
    public final static String getDivece=IP+"getDeviceListByUserID.do?userID=*#userID";
    public final static String QR=IP+"qrcode.do?userID=*#userID";
    public final static String upSign=IP+"upSign.do?userID=*#userID&sign=*#sign";
    public final static String upHeadURL=IP+"upHeadjpg.do?userID=*#userID&headjpg=*#headImg";

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
    final static String friendID="*#friendID";
    final static String start="*#start";
    final static String count="*#count";
    final static String friendType="*#fType";
    final static String appid="*#APPID";
    final static String secret="*#SECRET";
    final static  String code="*#CODE";
    final static String token="*#ACCESS_TOKEN";
    final static String openid="*#OPENID";
    final static String diveceid="*#diveceID";
    final static String signature="*#signature";
    final static String timestamp="*#timestamp";
    final static String sign="*#sign";
    final static String headUrl="*#headImg";


    String url;
    public StringURL setHeadUrl(String headUrlR){
        return new StringURL(url.replace(headUrl,headUrlR));
    }
    public StringURL setSing(String signR) {
        try {
            return new StringURL(url.replace(sign, URLEncoder.encode(signR, "utf-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
    public StringURL(String url){
        this.url=url;
    }
    public StringURL setDeviceID(String diveceidR){
        return new StringURL(url.replace(diveceid,diveceidR));
    }
    public StringURL setFriendType(String friendTypeR){
        return new StringURL(url.replace(friendType,friendTypeR));
    }

    public  StringURL setPhone(String phoneNum) {
        return new StringURL(url.replace(phone, phoneNum));
    }
    public  StringURL setUserID(String userIDR){
        return new StringURL(url.replace(userID,userIDR));
    }
    public  StringURL setOldPass(String oldPassR){
        return new StringURL(url.replace(oldPass,oldPassR));
    }
    public  StringURL setNewPass(String NewPassR){
        return new StringURL(url.replace(newPass,NewPassR));
    }
    public  StringURL setIsFirst(String isFirstR){
        return new StringURL(url.replace(isFirst,isFirstR));
    }
    public  StringURL setGender(String genderR){
        try {
            return new StringURL(url.replace(gender,URLEncoder.encode(genderR,"utf-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
    public  StringURL setPass(String passR){
        return new StringURL(url.replace(pass,passR));
    }
    public  StringURL setUserImg(String userImgR){
        return new StringURL(url.replace(userImg,userImgR));
    }
    public  StringURL setNickName(String nickNameR){
        try {
            return new StringURL(url.replace(nickName,URLEncoder.encode(nickNameR,"utf-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
    public  StringURL setType(String typeR){
        return new StringURL(url.replace(type,typeR));
    }
    public StringURL setFriendID(String friendIDR){
        return new StringURL(url.replace(friendID,friendIDR));
    }
    public StringURL setPosition(String startR){
        return new StringURL(url.replace(start,startR));
    }
    public StringURL setCount(String countR){
        return new StringURL(url.replace(count,countR));
    }
    public StringURL setAppid(String appidR){
        return  new StringURL(url.replace(appid,appidR));
    }
    public StringURL setSecret(String secretR){
        return  new StringURL(url.replace(secret,secretR));
    }
    public StringURL setCode(String codeR){
        return new StringURL(url.replace(code,codeR));
    }
    public StringURL setToken(String tokenR){
        return new StringURL(url.replace(token,tokenR));
    }
    public StringURL setOpenID(String openidR){
        return new StringURL(url.replace(openid,openidR));
    }


    public String toString(){
        if(url.contains("*#fType")){
            url=url.replace("*#fType","0");
        }
        if(url.contains("qrcode.do")){
            return url;
        }
        String currentTime=String.valueOf(System.currentTimeMillis());
        url=url+IPend;
        url=url.replace(signature,Md5Base64Encode.createIMTSignature(currentTime)).replace(timestamp, currentTime);
        return url;
    }
}
