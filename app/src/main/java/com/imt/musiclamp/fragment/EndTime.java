package com.imt.musiclamp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.imt.musiclamp.R;
import com.imt.musiclamp.model.Lamp;
import com.imt.musiclamp.showTv;
import com.imt.musiclamp.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by dtw on 15/3/3.
 */
public class EndTime extends LazyFragment {


    @InjectView(R.id.timeList)
    ListView lv;

    @InjectView(R.id.endT)
    TextView tv;

    List<String> list;
    ViewHolder viewHolder = null;


//    android.os.Handler h=new android.os.Handler();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        list = new ArrayList<String>();
        list.add("关闭");
        list.add("10分钟");
        list.add("30分钟");
        list.add("60分钟");
        list.add("自定义");


        View v = inflater.inflate(R.layout.endtime, null);
        ButterKnife.inject(this, v);
//        lv = (ListView) getActivity().findViewById(R.id.timeList);

        lazyLoad();

        showTv.tv = this.tv;
        return v;
    }

    @Override
    protected void lazyLoad() {

        lv.setAdapter(new timeEnd());
        lv.setOnItemClickListener(new itemClick());
    }

    class itemClick implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch (list.get(position)) {
                case "自定义":
//                    Intent intent = new Intent();
//                    intent.setClass(getActivity(), AlarmActivity.class);
//                    startActivity(intent);
                    break;
                case "关闭":
                    closeLamp((byte) 00);
                    break;
                case "10分钟":
                    closeLamp((byte) 10);
                    break;
                case "30分钟":
                    closeLamp((byte) 30);
                    break;
                case "60分钟":
                    closeLamp((byte) 60);
                    break;
            }
        }

        public void closeLamp(final byte minuteByte) {
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
//                        Utils.sendUDP(ip, 8999, Utils.getTimetoTurnOffByte(macAddress, minuteByte));
                    }

                }
            }.start();
            new showTv().startTime((int) minuteByte);
        }
    }

    class timeEnd extends BaseAdapter {

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());

            if (view == null) {
                view = inflater.inflate(R.layout.endlayout, null, false);
                viewHolder = new ViewHolder();
                viewHolder.textView = (TextView) view.findViewById(R.id.textView_name);
//                viewHolder.radioButton = (RadioButton) view.findViewById(R.id.radioButton);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            viewHolder.textView.setText(list.get(position));

//            viewHolder.radioButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    ((CheckBox)v).setChecked(true);
//                    notifyDataSetChanged();
//                }
//            });

            return view;
        }
    }

    class ViewHolder {
        TextView textView;
//        RadioButton radioButton;
    }
}
