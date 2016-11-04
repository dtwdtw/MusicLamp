package com.imt.musiclamp.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.imt.musiclamp.R;
import com.imt.musiclamp.event.TimePickEvent;
import com.imt.musiclamp.view.TimePickBottomSheet;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentAddScene2 extends Fragment {

    @InjectView(R.id.listView)
    ListView listView;

    private int hour;
    private int minute;
    private boolean isTiming;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fragment_add_scene2, container, false);
        ButterKnife.inject(this, view);
        EventBus.getDefault().register(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_single_choice);
        adapter.add("关闭");
        adapter.add("5分钟");
        adapter.add("10分钟");
        adapter.add("15分钟");
        adapter.add("30分钟");
        adapter.add("自定义");
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(onItemClickListener);
        return view;
    }

    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch (position) {
                case 0:
                    isTiming = false;
                    break;
                case 1:
                    isTiming = true;
                    hour = 0;
                    minute = 5;
                    break;
                case 2:
                    isTiming = true;
                    hour = 0;
                    minute = 10;
                    break;
                case 3:
                    isTiming = true;
                    hour = 0;
                    minute = 15;
                    break;
                case 4:
                    isTiming = true;
                    hour = 0;
                    minute = 30;
                    break;
                case 5:
                    TimePickBottomSheet timePickBottomSheet = new TimePickBottomSheet(getActivity());
                    timePickBottomSheet.show();
                    break;
            }
        }
    };

    public void onEvent(final TimePickEvent timePickEvent) {
        hour = timePickEvent.getHour();
        minute = timePickEvent.getMinute();
        isTiming = true;
    }

    public Bundle getData() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("isTiming", isTiming);
        bundle.putInt("hour", hour);
        bundle.putInt("minute", minute);
        return bundle;
    }
}
