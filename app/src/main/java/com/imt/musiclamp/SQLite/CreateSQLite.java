package com.imt.musiclamp.SQLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by dtw on 15/4/6.
 */
public class CreateSQLite extends SQLiteOpenHelper {
    SQLiteDatabase db = null;
    public CreateSQLite(Context context){
        super(context, "userDB", null, 1);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table user(type varchar(20) not null , loginKey varchar(20) not null , password varchar(60) , gender varchar(10) , thID varchar(100) );";
        db.execSQL(sql);
        db = this.getReadableDatabase();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insertUser(String type ,String key,String pwd){
        ContentValues cv = new ContentValues();//实例化一个ContentValues用来装载待插入的数据
        cv.put("type",type);
        cv.put("loginKey",key);
        cv.put("password",pwd); //添加密码
        db.insert("user",null,cv);//执行插入操作
    }
    public void insertTHUser(String type,String name,String gender,String thID){
        ContentValues cv = new ContentValues();//实例化一个ContentValues用来装载待插入的数据
        cv.put("type",type);
        cv.put("loginKey",name);
        cv.put("gender",gender); //添加密码+
        cv.put("thID",thID);
        db.insert("user",null,cv);//执行插入操作
    }
    public void updateUser(String type ,String key,String pwd){
        ContentValues cv = new ContentValues();//实例化ContentValues
        cv.put("password","iHatePopMusic");//添加要更改的字段及内容
        String whereClause = "loginKey=?";//修改条件
        String[] whereArgs = {"Jack Johnson"};//修改条件的参数
        db.update("user",cv,whereClause,whereArgs);//执行修改
    }
    public Cursor getUser(){
        return db.query("user",null,null,null,null,null,null);
    }
}
