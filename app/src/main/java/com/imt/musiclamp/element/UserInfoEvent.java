package com.imt.musiclamp.element;

/**
 * Created by dtw on 15/5/27.
 */
public class UserInfoEvent {
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserHead() {
        return UserHead;
    }

    public void setUserHead(String userHead) {
        UserHead = userHead;
    }

    String userName;
    String UserHead;
}
