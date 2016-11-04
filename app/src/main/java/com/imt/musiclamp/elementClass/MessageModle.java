package com.imt.musiclamp.elementClass;

import io.rong.imlib.RongIMClient;

/**
 * Created by mac on 15/4/20.
 */
public class MessageModle {

    RongIMClient.Message message;
    String friendID;

    public RongIMClient.Message getMessage() {
        return message;
    }

    public void setMessage(RongIMClient.Message message) {
        this.message = message;
    }

    public String getFriendID() {
        return friendID;
    }

    public void setFriendID(String friendID) {
        this.friendID = friendID;
    }
}
