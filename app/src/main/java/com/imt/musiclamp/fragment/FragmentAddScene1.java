package com.imt.musiclamp.fragment;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.imt.musiclamp.MyApplication;
import com.imt.musiclamp.R;
import com.imt.musiclamp.event.ColorSceneEvent;
import com.imt.musiclamp.event.SceneEvent;
import com.imt.musiclamp.event.SunSceneEvent;
import com.triggertrap.seekarc.SeekArc;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class FragmentAddScene1 extends Fragment {

    private static Bitmap imageOriginal;
    private static Matrix matrix;
    private int wheelHeight, wheelWidth;

    @InjectView(R.id.imageView_wheel)
    ImageView imageViewColorWheel;

    @InjectView(R.id.seekArc)
    SeekArc seekArc;

    @InjectView(R.id.seekBar_sound)
    SeekBar seekBarSound;

    private int r;
    private int g;
    private int b;
    private int brightness;
    private int volume;
    private boolean isColor = true;

    int rm = 0, gm = 0, bm = 0;
    int max = 30000;

    String eventName = null;
    //亮度 中间wheel的取值 默认为1
    private double bright = 0.5;

    private int wheelValue = 50;

    private Handler handler;

    MyApplication myApplication;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fragment_add_scene1, null);


        Log.e("onCreateView", "color");

        handler = new Handler();
        myApplication = (MyApplication) getActivity().getApplication();

        ButterKnife.inject(this, view);

        // load the image only once
        if (imageOriginal == null) {
            imageOriginal = BitmapFactory.decodeResource(getResources(), R.drawable.color_wheel);
        }

        // initialize the matrix only once
        if (matrix == null) {
            matrix = new Matrix();
        } else {
            // not needed, you can also post the matrix immediately to restore the old state
            matrix.reset();
        }

        imageViewColorWheel.setDrawingCacheEnabled(true);
        imageViewColorWheel.setOnTouchListener(onTouchListener);
        imageViewColorWheel.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);

        seekArc.setOnSeekArcChangeListener(new seekArcListener());
        seekArc.setProgress(50);

        seekBarSound.setOnSeekBarChangeListener(onSeekBarChangeListener);

        EventBus.getDefault().register(this);

        return view;
    }

    class seekArcListener implements SeekArc.OnSeekArcChangeListener {
        @Override
        public void onProgressChanged(SeekArc seekArc, final int progress, boolean fromUser) {
            brightness = 100 - progress;
        }

        @Override
        public void onStartTrackingTouch(SeekArc seekArc) {

        }

        @Override
        public void onStopTrackingTouch(SeekArc seekArc) {

        }
    }

    ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            if (wheelHeight == 0 || wheelWidth == 0) {
                wheelHeight = imageOriginal.getHeight();
                wheelWidth = imageOriginal.getWidth();

                // translate to the image view's center
                float translateX = wheelWidth / 2 - imageOriginal.getWidth() / 2;
                float translateY = wheelHeight / 2 - imageOriginal.getHeight() / 2;
                matrix.postTranslate(translateX, translateY);

                imageViewColorWheel.setImageBitmap(imageOriginal);
                imageViewColorWheel.setImageMatrix(matrix);

                getColor(true);
            }
        }
    };

    public void onEvent(SceneEvent sceneEvent) {
        Log.e("sceneEvent", sceneEvent.getName() + "color");
        eventName = sceneEvent.getFragment();
        if ("color".equals(sceneEvent.getFragment())) {
            Log.e("sceneEvent", "color");
            if (sceneEvent.isReadOnly()) {
                setRotateDialer(sceneEvent.getDegress());
            } else {
                float[] values;
                values = sceneEvent.getValues();
                matrix.setValues(values);
                imageViewColorWheel.setImageMatrix(matrix);
            }
            getColor(true);
        } else if ("sun".equals(sceneEvent.getFragment())) {
            Log.e("sceneEvent", "sun");
            switchToSun();
            SunSceneEvent sunSceneEvent = new SunSceneEvent();
            sunSceneEvent.setName(sceneEvent.getName());
            sunSceneEvent.setValues(sceneEvent.getValues());
            sunSceneEvent.setWheelValue(sceneEvent.getWheelValue());
            sunSceneEvent.setFragment(sceneEvent.getFragment());
            new EventBus().post(sunSceneEvent);
        }
    }

    public void onEvent(final ColorSceneEvent colorSceneEvent) {
        Log.e("sceneEvent", colorSceneEvent.getName());
        if (colorSceneEvent.isReadOnly()) {
            setRotateDialer(colorSceneEvent.getDegress());
        } else {
            float[] values;
            values = colorSceneEvent.getValues();
            matrix.setValues(values);
            imageViewColorWheel.setImageMatrix(matrix);
        }
        getColor(true);
    }

    @OnClick({R.id.imageView_switch_color, R.id.imageView_switch_sunlight})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageView_switch_color:
                imageViewColorWheel.setImageResource(R.drawable.color_wheel);
                ((ImageView)getActivity().findViewById(R.id.imageView_switch_color)).setImageResource(R.drawable.ic_lamp_music_chouse);
                ((ImageView)getActivity().findViewById(R.id.imageView_switch_sunlight)).setImageResource(R.drawable.ic_lamp_sunlight);
                isColor = true;
                break;
            case R.id.imageView_switch_sunlight:
                imageViewColorWheel.setImageResource(R.drawable.sun_light_wheel);
                ((ImageView)getActivity().findViewById(R.id.imageView_switch_color)).setImageResource(R.drawable.ic_lamp_music);
                ((ImageView)getActivity().findViewById(R.id.imageView_switch_sunlight)).setImageResource(R.drawable.ic_lamp_sunlight_check);
                isColor = false;
                break;
        }
    }


    View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        private double startAngle;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startAngle = getAngle(event.getX(), event.getY());
                    break;
                case MotionEvent.ACTION_MOVE:
                    double currentAngle = getAngle(event.getX(), event.getY());
                    rotateDialer((float) (startAngle - currentAngle));
                    startAngle = currentAngle;

                    getColor(true);

                    break;
            }
            return true;
        }
    };

    SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(final SeekBar seekBar, final int progress, boolean fromUser) {
            volume = progress;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(final SeekBar seekBar) {

        }
    };

    private void getColor(boolean getColor) {
        //加if为了区分色盘取色和wheel取色 wheel取色时就部重复在色盘取色了
        if (getColor) {
            int color = imageViewColorWheel.getDrawingCache().getPixel(wheelWidth / 2, 50);//x、y为bitmap所对应的位置
            imageViewColorWheel.setDrawingCacheEnabled(false);
            imageViewColorWheel.setDrawingCacheEnabled(true);
            r = Color.red(color);
            g = Color.green(color);
            b = Color.blue(color);
        }
    }

    private void switchToSun() {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
        SunlightFragment sunlightFragment = (SunlightFragment) getActivity().getSupportFragmentManager().findFragmentByTag("sun");
        if (sunlightFragment == null) {
            sunlightFragment = new SunlightFragment();
        }
        if (!sunlightFragment.isAdded()) {
            transaction.hide(this).add(R.id.content_frame, sunlightFragment, "sun");
        } else {
            Log.e("sunlightFragment", "isAdd");
            transaction.hide(this).show(sunlightFragment);
        }
        transaction.commit();
    }

    public float[] getMatrixValues() {
        float[] values = new float[9];
        matrix.getValues(values);
        return values;
    }

    private void rotateDialer(float degrees) {
        matrix.postRotate(degrees, wheelWidth / 2, wheelHeight / 2);
        imageViewColorWheel.setImageMatrix(matrix);
    }

    private void setRotateDialer(float degrees) {
        matrix.setRotate(0, wheelWidth / 2, wheelHeight / 2);
        matrix.setRotate(degrees, wheelWidth / 2, wheelHeight / 2);
        imageViewColorWheel.setImageMatrix(matrix);
    }

    private double getAngle(double xTouch, double yTouch) {
        double x = xTouch - (wheelWidth / 2d);
        double y = wheelHeight - yTouch - (wheelHeight / 2d);

        switch (getQuadrant(x, y)) {
            case 1:
                return Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
            case 2:
            case 3:
                return 180 - (Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);

            case 4:
                return 360 + Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;

            default:
                // ignore, does not happen
                return 0;
        }
    }

    private static int getQuadrant(double x, double y) {
        if (x >= 0) {
            return y >= 0 ? 1 : 4;
        } else {
            return y >= 0 ? 2 : 3;
        }
    }

    public int getWheelValue() {
        return wheelValue;
    }

    public void setWheelValue(int wheelValue) {
        this.wheelValue = wheelValue;
    }

    public Bundle getPickerData() {
        Bundle bundle = new Bundle();
        bundle.putInt("r", r);
        bundle.putInt("g", g);
        bundle.putInt("b", b);
        bundle.putInt("brightness", brightness);
        bundle.putInt("volume", volume);
        bundle.putBoolean("isColor", isColor);
        return bundle;
    }
}
