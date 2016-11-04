package com.imt.musiclamp.adapter;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.imt.musiclamp.MyApplication;
import com.imt.musiclamp.R;
import com.imt.musiclamp.api.APIClient;
import com.imt.musiclamp.model.OnlineMusicInfo;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ToplistAdapter extends BaseAdapter {
    private Context context;
    private List<OnlineMusicInfo> musicInfos;
    private LayoutInflater inflater;
    private MyApplication myApplication;
    private boolean isShowAlbum = false;

    public ToplistAdapter(Context context, List<OnlineMusicInfo> musicInfos, boolean isShowAlbum) {
        this.context = context;
        this.musicInfos = musicInfos;
        this.isShowAlbum = isShowAlbum;

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
        OnlineMusicInfo onlineMusicInfo = musicInfos.get(position);

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
        viewHolder.textViewTitle.setText(onlineMusicInfo.getTitle());
        viewHolder.textViewArtist.setText(onlineMusicInfo.getArtist());
        viewHolder.textViewIndex.setText(String.valueOf(position + 1));
//        APIClient.getMusicInfo(context, String.valueOf(myApplication.getCurrentOnlineMusicInfo().getSongId()), musicInfoResponseHandler);

        if (isShowAlbum) {
            Uri imageUri = Uri.parse(onlineMusicInfo.getArtworkUrl());
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

//    private class MusicInfoResponseHandler extends AsyncHttpResponseHandler {
//
//        @Override
//        public void onSuccess(int i, Header[] headers, byte[] bytes) {
//            String jsonContent = new String(bytes);
//            try {
//                JSONObject jsonObject = new JSONObject(jsonContent);
//                JSONObject jsonSong = jsonObject.getJSONArray("songs").getJSONObject(0);
//                JSONObject jsonAlbum = jsonSong.getJSONObject("album");
//                jsonAlbum.getString("picUrl");
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//
//        @Override
//        public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
//
//        }
//    }

}
