package com.imt.musiclamp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

/**
 * Created by Denting on 15/6/17.
 */
public class WelcomeActivity extends Activity implements Animation.AnimationListener{

    private ImageView image;

    private Animation animation;

    private Context context;

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);

        image = (ImageView) this.findViewById(R.id.image);

        context = WelcomeActivity.this;
        intent = new Intent();

        init();
    }

    private void init() {
        animation = new AlphaAnimation(0.3f, 1.0f);
        animation.setDuration(1000);

        startAnimation();
    }

    private void startAnimation() {
        image.setAnimation(animation);
        animation.setAnimationListener(this);
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        intent.setClass(context, LoginActivity.class);
        this.startActivity(intent);
        this.finish();
    }

    @Override
    public void finish() {
        super.finish();

        overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}
