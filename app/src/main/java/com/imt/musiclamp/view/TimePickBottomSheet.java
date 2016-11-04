package com.imt.musiclamp.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.imt.musiclamp.R;
import com.imt.musiclamp.element.StartTimerEvent;

import antistatic.spinnerwheel.AbstractWheel;
import antistatic.spinnerwheel.adapters.NumericWheelAdapter;
import de.greenrobot.event.EventBus;


public class TimePickBottomSheet extends Dialog implements DialogInterface {

    private Context context;
    private AbstractWheel wheelHour;
    private AbstractWheel wheelMinute;

    public TimePickBottomSheet(Context context) {
        super(context, com.cocosw.bottomsheet.R.style.BottomSheet_Dialog);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setCanceledOnTouchOutside(true);
        View sheetView = LayoutInflater.from(context).inflate(R.layout.sheet_time_pick, null, false);
        wheelHour = (AbstractWheel) sheetView.findViewById(R.id.wheel_hour);
        wheelMinute = (AbstractWheel) sheetView.findViewById(R.id.wheel_minute);

        wheelHour.setViewAdapter(new NumericWheelAdapter(context, 0, 23));
        wheelMinute.setViewAdapter(new NumericWheelAdapter(context, 0, 59));

        Time t = new Time();
        t.setToNow();
        wheelHour.setCurrentItem(t.hour);
        wheelMinute.setCurrentItem(t.minute);

        sheetView.findViewById(R.id.button_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                TimePickEvent timePickEvent = new TimePickEvent();
//                timePickEvent.setSet(true);
//                timePickEvent.setHour(wheelHour.getCurrentItem());
//                timePickEvent.setMinute(wheelMinute.getCurrentItem());
//                EventBus.getDefault().post(timePickEvent);

                Time t = new Time();
                t.setToNow();
                int timeend = (wheelHour.getCurrentItem() - t.hour) * 60 + wheelMinute.getCurrentItem() - t.minute;
                if (timeend > 0) {
                    Log.v("hout", t.hour + "");
                    Log.v("Minutes", t.minute + "");
                    StartTimerEvent startTimerEvent = new StartTimerEvent();
                    startTimerEvent.setEndtime(timeend);
                    EventBus.getDefault().post(startTimerEvent);
                } else {
                    Toast.makeText(context, "请设置闹钟时间在24小时之内", Toast.LENGTH_SHORT).show();
                }
                TimePickBottomSheet.this.dismiss();
            }
        });
        sheetView.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickBottomSheet.this.dismiss();
            }
        });

        setContentView(sheetView);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM;

        TypedArray a = getContext().obtainStyledAttributes(new int[]{android.R.attr.layout_width});
        try {
            params.width = a.getLayoutDimension(0, ViewGroup.LayoutParams.MATCH_PARENT);
        } finally {
            a.recycle();
        }

        getWindow().setAttributes(params);
    }

}
