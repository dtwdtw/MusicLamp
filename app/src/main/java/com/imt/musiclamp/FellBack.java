package com.imt.musiclamp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by dtw on 15/5/13.
 */
public class FellBack extends Activity implements View.OnClickListener{

    private ImageView back;
    private EditText feed_back;
    private TextView submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fellbacklayout);

        back = (ImageView) this.findViewById(R.id.back);
        back.setOnClickListener(this);

        feed_back = (EditText) this.findViewById(R.id.feed_back);

        submit = (TextView) this.findViewById(R.id.submit);
        submit.setOnClickListener(this);
    }

    private void submit() {
        String[] reciver = new String[]{"Developer@imt66.com"};
        String[] mySbuject = new String[]{"test"};
        String myCc = "cc";
        String mybody = feed_back.getText().toString()
                +"\n\n版本号v1.0.0 设备:OnePlase\n";
        Intent myIntent = new Intent(Intent.ACTION_SEND );
        myIntent.setType("plain/text");
        myIntent.putExtra(Intent.EXTRA_EMAIL, reciver);
        myIntent.putExtra(android.content.Intent.EXTRA_CC, myCc);
        myIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, mySbuject);
        myIntent.putExtra(android.content.Intent.EXTRA_TEXT, mybody);
        startActivity(Intent.createChooser(myIntent, "mail test"));
        finish();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.back :
                this.finish();
                break;
            case R.id.submit :
                submit();
                break;
        }
    }
}
