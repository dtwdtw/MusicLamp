package com.imt.musiclamp.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.imt.musiclamp.MyApplication;
import com.imt.musiclamp.R;
import com.imt.musiclamp.model.LocalMusicInfo;
import com.imt.musiclamp.utils.Utils;

import java.util.List;

public class MusicListAdapter extends BaseAdapter {

    private Context context;
    private List<LocalMusicInfo> musicInfos;
    private LayoutInflater inflater;
    private MyApplication myApplication;

    public MusicListAdapter(Context context, List<LocalMusicInfo> musicInfos) {
        this.context = context;
        this.musicInfos = musicInfos;

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
        LocalMusicInfo musicInfo = musicInfos.get(position);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_list_music, null, false);
            viewHolder = new ViewHolder();
            viewHolder.textViewArtist = (TextView) convertView.findViewById(R.id.textView_artis_and_ablum);
            viewHolder.textViewTitle = (TextView) convertView.findViewById(R.id.textView_title);
            viewHolder.textViewIndex = (TextView) convertView.findViewById(R.id.textView_index);
            viewHolder.imageViewArtWork = (ImageView) convertView.findViewById(R.id.imageView_artwork);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.textViewTitle.setText(musicInfo.getTitle());
        viewHolder.textViewArtist.setText(String.format("%s - %s", musicInfo.getArtist(), musicInfo.getAlbum()));
        viewHolder.textViewIndex.setText(String.valueOf(position + 1));

        if(musicInfo.isDownloaded()){
            Glide.with(context)
                    .load(musicInfo.getAlbumUrl())
                    .centerCrop()
                    .placeholder(R.drawable.ic_audiotrack)
                    .crossFade()
                    .into(viewHolder.imageViewArtWork);
        }else {
            Uri imageUri = Utils.getArtworkUri(musicInfo.getAlbumId());
            if (imageUri != null) {
                Glide.with(context)
                        .load(imageUri)
                        .centerCrop()
                        .placeholder(R.drawable.ic_audiotrack)
                        .crossFade()
                        .into(viewHolder.imageViewArtWork);
            }
        }

        return convertView;
    }

    static class ViewHolder {
        ImageView imageViewArtWork;
        TextView textViewTitle;
        TextView textViewArtist;
        TextView textViewIndex;
        TextView textViewTime;

    }
}
