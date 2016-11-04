package com.imt.musiclamp.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.imt.musiclamp.MyApplication;
import com.imt.musiclamp.R;
import com.imt.musiclamp.model.CustomPlayList;
import com.imt.musiclamp.model.OnlinePlaylist;

import java.util.List;

public class CustomPlaylistAdapter extends BaseAdapter {

    private Context context;
    private List<CustomPlayList> playlists;
    private LayoutInflater inflater;
    private MyApplication myApplication;

    public CustomPlaylistAdapter(Context context, List<CustomPlayList> playlists) {
        this.context = context;
        this.playlists = playlists;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return playlists.size();
    }

    @Override
    public Object getItem(int position) {
        return playlists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_playlist, null, false);
            viewHolder = new ViewHolder();
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
            viewHolder.textViewTitle = (TextView) convertView.findViewById(R.id.textView_title);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

//        //最后一位位添加
//        if (position == getCount() - 1) {
//            viewHolder.imageView.setImageResource(R.drawable.ic_add_black);
//        } else {
        CustomPlayList playlist = playlists.get(position);

        Glide.with(context)
                .load(playlist.getImgUrl())
                .centerCrop()
                .crossFade()
                .placeholder(R.drawable.album_default)
                .into(viewHolder.imageView);
        viewHolder.textViewTitle.setText(playlist.getName());
//        }

        return convertView;
    }

    static class ViewHolder {
        ImageView imageView;
        TextView textViewTitle;
    }
}
