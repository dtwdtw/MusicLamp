package com.imt.musiclamp.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by dtw on 15/4/6.
 */
@Table(name = "User")
public class User extends Model {

    @Column(name = "Type")
    String type;
    @Column(name = "Name")
    String name;
    @Column(name = "Password")
    String password;
    @Column(name = "ThID")
    String thID;
    @Column(name = "Gender")
    String gender;

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getThID() {
        return thID;
    }

    public void setThID(String thID) {
        this.thID = thID;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
