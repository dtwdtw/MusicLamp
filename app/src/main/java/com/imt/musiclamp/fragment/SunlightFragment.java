package com.imt.musiclamp.fragment;


import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.imt.musiclamp.musicLight.AudioProcess;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.imt.musiclamp.MyApplication;
import com.imt.musiclamp.R;
import com.imt.musiclamp.event.ColorSceneEvent;
import com.imt.musiclamp.event.SceneEvent;
import com.imt.musiclamp.event.SunSceneEvent;
import com.imt.musiclamp.model.Lamp;
import com.imt.musiclamp.utils.Utils;

import java.util.List;

import antistatic.spinnerwheel.AbstractWheel;
import antistatic.spinnerwheel.OnWheelChangedListener;
import antistatic.spinnerwheel.adapters.NumericWheelAdapter;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class SunlightFragment extends Fragment {

    private static Bitmap imageOriginal;
    private static Matrix matrix;
    private int wheelHeight, wheelWidth;

    @InjectView(R.id.imageView_wheel)
    ImageView imageViewColorWheel;

    @InjectView(R.id.spinnerwheel)
    AbstractWheel wheelView;

    @InjectView(R.id.imageView_color)
    ImageView imageViewColor;

    @InjectView(R.id.imageView_switch)
    ImageView imageViewSwitch;

    @InjectView(R.id.imageView_curl)
    ImageView imageViewCurl;

    @InjectView(R.id.imageView_switch_bg)
    ImageView imageViewSwitchBg;

    @InjectView(R.id.imageView_wheel_bg)
    ImageView imageViewWheelBg;

    @InjectView(R.id.textView_rgb)
    TextView textViewRBG;

    @InjectView(R.id.linearLayout_tab0)
    LinearLayout layoutTab0;


    @InjectView(R.id.linearLayout_tab1)
    LinearLayout layoutTab1;


    @InjectView(R.id.deviceLamp)
    TextView textDevice;

    private int r;
    private int g;
    private int b;
    private int x;
    //亮度 中间wheel的取值 默认为1
    private double bright = 1;

    private int wheelValue = 0;

    boolean lampState = true;

    MyApplication myApplication;
    static  int frequency = 8000;//分辨率
    static final int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    static final int audioEncodeing = AudioFormat.ENCODING_PCM_16BIT;
    static final int yMax = 50;//Y轴缩小比例最大值
    static final int yMin = 1;//Y轴缩小比例最小值
    int minBufferSize;//采集数据需要的缓冲区大小
    AudioRecord audioRecord;//录音
    AudioProcess audioProcess = AudioProcess.creatAudioProcess();//处理


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sun_light, null);




        myApplication = (MyApplication) getActivity().getApplication();

        ButterKnife.inject(this, view);

        // load the image only once
        if (imageOriginal == null) {
            imageOriginal = BitmapFactory.decodeResource(getResources(), R.drawable.sun_light_wheel);
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

        wheelView.setViewAdapter(new NumericWheelAdapter(getActivity(), 1, 100));
        wheelView.setCurrentItem(50);
        wheelView.addChangingListener(onWheelChangedListener);

        EventBus.getDefault().register(this);

        textDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LampListFragment mdf = new LampListFragment();
                android.app.FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
                ft.setTransition(FragmentTransaction.TRANSIT_UNSET);
                mdf.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                mdf.show(ft,"ik");
            }
        });

        return view;
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

                //初始化 第一次进入也获取颜色
                getColor(true);
            }
        }
    };

    public void onEvent(SceneEvent sceneEvent) {
        Log.e("sceneEvent", sceneEvent.getName() + "sun");
        String eventName=null;
        eventName=sceneEvent.getFragment();
        if ("sun".equals(sceneEvent.getFragment())) {
            audioProcess.stop();
            Log.e("sceneEvent", "sun");
            float[] values;
            values = sceneEvent.getValues();
            matrix.setValues(values);
            imageViewColorWheel.setImageMatrix(matrix);
            wheelView.setCurrentItem(0);
            wheelView.scroll(sceneEvent.getWheelValue(), 1000);
            getColor(true);
        } else if ("color".equals(sceneEvent.getFragment())) {
            audioProcess.stop();
            Log.e("sceneEvent", "color");
            switchToColor();
            ColorSceneEvent colorSceneEvent = new ColorSceneEvent();
            colorSceneEvent.setName(sceneEvent.getName());
            colorSceneEvent.setValues(sceneEvent.getValues());
            colorSceneEvent.setWheelValue(sceneEvent.getWheelValue());
            colorSceneEvent.setFragment(sceneEvent.getFragment());
            new EventBus().post(colorSceneEvent);
        }else if("music".equals(eventName)){
            Log.e("Event","MusicEvent");

//            new Thread() {
//                @Override
//                public void run() {
//                    while ("music".equals(eventName)) {

            try {
                //录音
                minBufferSize = AudioRecord.getMinBufferSize(frequency,
                        channelConfiguration,
                        audioEncodeing);
                //minBufferSize = 2 * minBufferSize;
                audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,frequency,
                        channelConfiguration,
                        audioEncodeing,
                        minBufferSize);
//                            audioProcess.baseLine = sfv.getHeight()-100;
                audioProcess.frequence = frequency;
                //启动绘图
                audioProcess.start(audioRecord, minBufferSize);
                //停止绘图；
//                audioProcess.stop();
            } catch (Exception e) {
                // TODO: handle exception
            }

//                    }
//                }
//            }.start();
        }
    }

    public void onEvent(SunSceneEvent sunSceneEvent) {
        Log.e("sceneEvent", sunSceneEvent.getName());
        if (sunSceneEvent.isReadOnly()) {
            setRotateDialer(sunSceneEvent.getDegress());
        } else {
            float[] values;
            values = sunSceneEvent.getValues();
            matrix.setValues(values);
            imageViewColorWheel.setImageMatrix(matrix);
        }
        wheelView.setCurrentItem(0);
        wheelView.scroll(sunSceneEvent.getWheelValue(), 1000);
        getColor(true);
    }


    OnWheelChangedListener onWheelChangedListener = new OnWheelChangedListener() {
        @Override
        public void onChanged(AbstractWheel wheel, int oldValue, final int newValue) {
            wheelValue = newValue;
            bright = newValue * 0.01;
            getColor(false);
        }
    };

    @OnClick({R.id.imageView_left_menu, R.id.imageView_right_menu, R.id.imageView_color_picker, R.id.imageView_brightness_picker, R.id.imageView_switch})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageView_left_menu:
                ((SlidingFragmentActivity) getActivity()).toggle();
                break;
            case R.id.imageView_right_menu:
                ((SlidingFragmentActivity) getActivity()).getSlidingMenu().showSecondaryMenu(true);
                break;
            case R.id.imageView_color_picker:
                switchToColor();
                break;
            case R.id.imageView_switch:

                List<Lamp> lamps = new Select()
                        .from(Lamp.class)
                        .where("selected = ?", true)
                        .execute();

                for (final Lamp lamp1 : lamps) {

                    final String ip = lamp1.getIp();
                    final byte[] macAddress = lamp1.getMacAddressByte();
                    if (lampState) {
                        new Thread() {
                            @Override
                            public void run() {
//                                Utils.sendUDP(ip, 8999, Utils.getTurnOffByte(macAddress));
                            }
                        }.start();
                    } else {
                        new Thread() {
                            @Override
                            public void run() {
//                                Utils.sendUDP(ip, 8999, Utils.getTurnOnByte(macAddress));
                            }
                        }.start();
                    }
                }

                if (lampState) {
                    imageViewColor.setVisibility(View.INVISIBLE);
                    layoutTab0.setVisibility(View.INVISIBLE);
                    layoutTab1.setVisibility(View.INVISIBLE);
                    imageViewColorWheel.setVisibility(View.INVISIBLE);
                    wheelView.setVisibility(View.INVISIBLE);
                    imageViewCurl.setVisibility(View.INVISIBLE);
                    imageViewWheelBg.setVisibility(View.INVISIBLE);
                    imageViewSwitchBg.setVisibility(View.INVISIBLE);
                } else {
                    imageViewSwitch.setImageResource(R.drawable.ic_action_io_on);
                    imageViewColor.setVisibility(View.VISIBLE);
                    layoutTab0.setVisibility(View.VISIBLE);
                    layoutTab1.setVisibility(View.VISIBLE);
                    imageViewColorWheel.setVisibility(View.VISIBLE);
                    wheelView.setVisibility(View.VISIBLE);
                    imageViewCurl.setVisibility(View.VISIBLE);
                    imageViewWheelBg.setVisibility(View.VISIBLE);
                    imageViewSwitchBg.setVisibility(View.VISIBLE);
                }

                lampState = !lampState;

                if (lamps.size() == 0) {
                    Toast.makeText(getActivity(), R.string.dont_select_any_lamp, Toast.LENGTH_SHORT).show();
                }

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

        final int r = (int) (this.r * bright);
        final int g = (int) (this.g * bright);
        final int b = (int) (this.b * bright);
        final int x = (int) (bright * 255);

        new Thread() {
            @Override
            public void run() {
                List<Lamp> lamps = new Select()
                        .from(Lamp.class)
                        .where("selected = ?", true)
                        .execute();

                for (final Lamp lamp1 : lamps) {
                    final String ip = lamp1.getIp();
                    final byte[] macAddress = lamp1.getMacAddressByte();
//                    Utils.sendUDP(ip, 8999, Utils.getSetLightByte(macAddress, (byte) r, (byte) g, (byte) b, (byte) x));
                }
            }
        }.start();

        imageViewColor.setBackgroundColor(Color.rgb(r, g, b));
        textViewRBG.setText(String.format("R:%s,G:%s,B:%s", r, g, b));
    }

    public float[] getMatrixValues() {
        float[] values = new float[9];
        matrix.getValues(values);
        return values;
    }

    private void switchToColor() {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
        ColorPickerFragment colorPickerFragment = (ColorPickerFragment) getActivity().getSupportFragmentManager().findFragmentByTag("color");

        if (!colorPickerFragment.isAdded()) {
            transaction.hide(this).add(R.id.content_frame, colorPickerFragment, "color");
        } else {
            Log.e("colorPickerFragment", "isAdd");
            transaction.hide(this).show(colorPickerFragment);
        }
        transaction.commit();
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
}
