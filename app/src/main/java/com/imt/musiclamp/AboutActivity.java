package com.imt.musiclamp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.security.AccessControlContext;

/**
 * Created by dtw on 15/5/25.
 */
public class AboutActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aboutlayout);
        findViewById(R.id.about).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AboutActivity.this, IMTInfoActivity.class));

            }
        });
        findViewById(R.id.startpages).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AboutActivity.this,StartActivity.class));
            }
        });
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}
