package com.imt.musiclamp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import javax.net.ssl.KeyManager;

/**
 * Created by dtw on 15/5/21.
 */
public class ChangeInfoActivity extends Activity{
    EditText changeText;
    RadioGroup gender;
    Button send;
    Bundle bundle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.changeinfolayout);
        bundle=getIntent().getExtras();
        changeText=(EditText)findViewById(R.id.changetext);
        send=(Button)findViewById(R.id.send);
        gender=(RadioGroup)findViewById(R.id.gendergroup);

        switch (bundle.getString("action")){
            case "name":
                changeText.setText(bundle.getString("name"));
                break;
            case "gender":
                changeText.setVisibility(View.INVISIBLE);
                gender.setVisibility(View.VISIBLE);
                if(bundle.getString("gender").equals("female")){
                    gender.check(R.id.female);
                }
                else{
                    gender.check(R.id.male);
                }
                break;
            case "info":
                changeText.setText(bundle.getString("info"));
                break;
        }
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode==4){

            bundle.putString("name",changeText.getText().toString());
            bundle.putString("info",changeText.getText().toString());
            bundle.putString("gender",gender.getCheckedRadioButtonId()==R.id.female?"female":"male");
            Intent intent=new Intent();
            intent.putExtras(bundle);
            setResult(RESULT_OK, intent);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStop() {
        super.onStop();

//        bundle.putString("name",changeText.getText().toString());
//        bundle.putString("info",changeText.getText().toString());
//        bundle.putString("gender",gender.getCheckedRadioButtonId()==R.id.female?"female":"male");
//        Intent intent=new Intent();
//        intent.putExtras(bundle);
//        setResult(RESULT_OK, intent);
    }
}
