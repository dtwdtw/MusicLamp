package com.imt.musiclamp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.imt.musiclamp.MyApplication;
import com.imt.musiclamp.R;
import com.imt.musiclamp.model.OnlineMusicInfo;

import java.util.List;

public class OnlinePlayingListAdapter extends BaseAdapter {

    private Context context;
    private List<OnlineMusicInfo> musicInfos;
    private LayoutInflater inflater;
    private MyApplication myApplication;

    public OnlinePlayingListAdapter(Context context, MyApplication myApplication) {
        this.context = context;
        this.musicInfos = myApplication.getOnlineMusicInfos();
        this.myApplication = myApplication;
        inflater = LayoutInflater.from(context);
    }


    @Override
    public int getCount() {
        return musicInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return musicInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        OnlineMusicInfo musicInfo = musicInfos.get(position);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_list_playing_list, null, false);
            viewHolder = new ViewHolder();
            viewHolder.textViewArtist = (TextView) convertView.findViewById(R.id.textView_artis_and_ablum);
            viewHolder.textViewTitle = (TextView) convertView.findViewById(R.id.textView_title);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.textViewTitle.setText(musicInfo.getTitle());
        viewHolder.textViewArtist.setText(String.format("%s - %s", musicInfo.getArtist(), musicInfo.getAlbum()));
        if (position == myApplication.getCurrentLocalPosition()) {
            viewHolder.textViewTitle.setTextColor(context.getResources().getColor(R.color.style_color_primary));
            viewHolder.textViewArtist.setTextColor(context.getResources().getColor(R.color.style_color_primary));
        } else {
            viewHolder.textViewTitle.setTextColor(Color.BLACK);
            viewHolder.textViewArtist.setTextColor(Color.BLACK);
        }

        return convertView;
    }

    static class ViewHolder {
        TextView textViewTitle;
        TextView textViewArtist;
    }
}
