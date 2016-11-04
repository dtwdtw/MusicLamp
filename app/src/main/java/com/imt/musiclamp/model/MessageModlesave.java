package com.imt.musiclamp.model;

import android.graphics.Bitmap;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import io.rong.imlib.RongIMClient;

/**
 * Created by mac on 15/4/21.
 */
@Table(name = "message")
public class MessageModlesave extends Model {
    @Column(name = "friendID")
    String friendID;
    @Column(name = "IMmessage")
    RongIMClient.Message IMmessage;
    @Column(name = "phone")
    String phone;
    @Column(name = "userName")
    String userName;
    @Column(name = "gender")
    String gender;
    @Column(name = "bitmap")
    Bitmap bitmap;
    @Column(name = "userBit")
    Bitmap userBit;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }


    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Bitmap getUserBit() {
        return userBit;
    }

    public void setUserBit(Bitmap userBit) {
        this.userBit = userBit;
    }

    public String getFriendID() {
        return friendID;
    }

    public void setFriendID(String friendID) {
        this.friendID = friendID;
    }

    public RongIMClient.Message getMessage() {
        return IMmessage;
    }

    public void setMessage(RongIMClient.Message message) {
        this.IMmessage = message;
    }

}

