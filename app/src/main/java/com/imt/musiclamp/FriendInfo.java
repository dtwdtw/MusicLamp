package com.imt.musiclamp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.imt.musiclamp.bitmap.BitmapBlur;
import com.imt.musiclamp.elementClass.StrUrl;
import com.imt.musiclamp.elementClass.StringURL;
import com.imt.musiclamp.elementClass.UserInfo;
import com.imt.musiclamp.elementClass.UserServer;

import org.json.JSONObject;
import org.w3c.dom.Text;

/**
 * Created by dtw on 15/3/30.
 */
public class FriendInfo extends Activity implements View.OnClickListener{

    private ImageView back;
    private TextView user_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friendinfo);
        final Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey("toBig")) {
            Bitmap bitmap = UserInfo.bitmap;
            ((ImageView) findViewById(R.id.friendImg)).setImageBitmap(bitmap);
            ((TextView) findViewById(R.id.name)).setText(UserInfo.userName);
            ((TextView) findViewById(R.id.friendid)).setText(UserInfo.id);
            if (UserInfo.phone != null) {
                ((TextView) findViewById(R.id.phone)).setText(UserInfo.phone);
            }
            ((TextView) findViewById(R.id.gender)).setText(UserInfo.gender.trim());
        } else {
            Bitmap bitmap = bundle.getParcelable("friendHeadUrl");
            ((ImageView) findViewById(R.id.friendImg)).setImageBitmap(bitmap);

            Glide.with(this)
                    .load(bundle.getString("friendHeadUrl"))
                    .centerCrop()
                    .placeholder(R.drawable.defalthead)
                    .crossFade()
                    .into(((ImageView) findViewById(R.id.friendImg)));

            ((TextView) findViewById(R.id.name)).setText(bundle.getString("nickname"));
            ((TextView) findViewById(R.id.friendid)).setText(bundle.getString("friendID"));
            ((TextView)findViewById(R.id.info)).setText(bundle.getString("friendInfo"));
            ((TextView)findViewById(R.id.gender)).setText(bundle.getString("gender"));
            if (bundle.containsKey("userPhone")) {
                ((TextView) findViewById(R.id.phone)).setText(bundle.getString("userPhone"));
            }
        }

        back = (ImageView) this.findViewById(R.id.back);
        back.setOnClickListener(this);

        user_name = (TextView) this.findViewById(R.id.user_name);
        user_name.setText(bundle.getString("nickname"));

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.del).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bundle.getString("friendID") == null) {
                    bundle.putString("friendID", UserInfo.id);
                }
                if (bundle.containsKey("love")) {

                    Log.v("lovedel","lovedel");
                    new UserServer(FriendInfo.this, new Handler(), new StringURL(StringURL.deleteFriend).setUserID(((MyApplication) getApplication()).userID).setFriendID(bundle.getString("friendID")).setFriendType("1").toString()) {
                        @Override
                        public void httpBack(JSONObject jsonObject) {
                            if (StrUrl.getResult(jsonObject).equals("1")) {
                                Toast.makeText(FriendInfo.this, R.string.user_deleted_successfully, Toast.LENGTH_SHORT).show();
                                exit();
                            }
                        }
                    };
                } else {
                    new UserServer(FriendInfo.this, new Handler(), new StringURL(StringURL.deleteFriend).setUserID(((MyApplication) getApplication()).userID).setFriendID(bundle.getString("friendID")).toString()) {
                        @Override
                        public void httpBack(JSONObject jsonObject) {
                            if (StrUrl.getResult(jsonObject).equals("1")) {
                                Toast.makeText(FriendInfo.this, R.string.user_deleted_successfully, Toast.LENGTH_SHORT).show();
                                exit();
                            }
                        }
                    };
                }
            }

        });
    }

    private void exit() {
        FriendActivity.listFriend.remove(getIntent().getIntExtra("position", 0));
        this.finish();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.back :
                this.finish();
                break;
        }
    }
}
