package com.imt.musiclamp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.imt.musiclamp.MyApplication;
import com.imt.musiclamp.R;
import com.imt.musiclamp.fragment.AllTopListFragment;
import com.imt.musiclamp.model.OnlineMusicInfo;
import com.imt.musiclamp.model.Toplist;

import java.util.List;

public class AllTopListAdapter extends BaseAdapter {

    private Context context;
    private List<Toplist> toplists;
    private LayoutInflater inflater;
    private MyApplication myApplication;

    public AllTopListAdapter(Context context, List<Toplist> toplists) {
        this.context = context;
        this.toplists = toplists;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return toplists.size();
    }

    @Override
    public Object getItem(int position) {
        return toplists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        Toplist toplist = toplists.get(position);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_all_top_list, null, false);
            viewHolder = new ViewHolder();
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
            viewHolder.textViewTop1 = (TextView) convertView.findViewById(R.id.textView_top1);
            viewHolder.textViewTop2 = (TextView) convertView.findViewById(R.id.textView_top2);
            viewHolder.textViewTop3 = (TextView) convertView.findViewById(R.id.textView_top3);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Glide.with(context)
                .load(toplist.getImg())
                .centerCrop()
                .crossFade()
                .into(viewHolder.imageView);
        viewHolder.textViewTop1.setText(String.format("1 %s - %s", toplist.getTop1Title(), toplist.getTop1Artist()));
        viewHolder.textViewTop2.setText(String.format("2 %s - %s", toplist.getTop2Title(), toplist.getTop2Artist()));
        viewHolder.textViewTop3.setText(String.format("3 %s - %s", toplist.getTop3Title(), toplist.getTop3Artist()));
        return convertView;
    }

    static class ViewHolder {
        ImageView imageView;
        TextView textViewTop1;
        TextView textViewTop2;
        TextView textViewTop3;
    }
}
