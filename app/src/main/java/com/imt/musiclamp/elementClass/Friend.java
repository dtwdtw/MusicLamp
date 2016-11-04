package com.imt.musiclamp.elementClass;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.imt.musiclamp.MyApplication;
import com.imt.musiclamp.R;

/**
 * Created by dtw on 15/3/16.
 */
public class Friend{
    ImageView imageView;
    TextView name,sex,info;
    Button add;
    View view;
    String friendID,phoneNum;
    Context context;
    Bitmap bitmap;
    private void load(){

        imageView= (ImageView) view.findViewById(R.id.friendHead);
        name=(TextView)view.findViewById(R.id.friendName);
        sex=(TextView)view.findViewById(R.id.friendSex);
        info=(TextView)view.findViewById(R.id.friendInfo);
        add=(Button)view.findViewById(R.id.add);
    }
    public Friend(Context context) {
        view=View.inflate(context, R.layout.frienditem,null);
        this.context=context;
        load();
    }

    public Friend(Context context,Bitmap bitmap,String name,String sex,String info,String userID){
        view=View.inflate(context, R.layout.frienditem,null);
        this.context=context;
        load();
        setImage(bitmap);
        setName(name);
        setSex(sex);
        setInfo(info);
        setFriendID(userID);
    }

    public void setImage(Bitmap bitmap){
        imageView.setImageBitmap(bitmap);
        this.bitmap=bitmap;
    }
    public String getPhoneNum(){return this.phoneNum;}
    public Bitmap getBitmap(){
        return this.bitmap;
    }
    public void setName(String name){
        this.name.setText(name);
    }
    public String getName(){
        return this.name.getText().toString();
    }
    public void setSex(String sex){
        this.sex.setText(sex);
    }
    public String getSex(){return this.sex.getText().toString();}
    public void setInfo(String info){
        this.info.setText(info);
    }
    public String getInfo(){
        return this.info.getText().toString();
    }
    public void setFriendID(String friendID){this.friendID=friendID;}
    public void setPhoneNum(String phoneNum){this.phoneNum=phoneNum;}
    public String getFriendID(){
        return friendID;
    }
    public Button getAddButton(){
        return add;
    }
    public View getView(){
        return view;
    }
    private int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
