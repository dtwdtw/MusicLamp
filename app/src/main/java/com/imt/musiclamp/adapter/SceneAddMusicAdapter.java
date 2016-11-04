package com.imt.musiclamp.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.imt.musiclamp.MyApplication;
import com.imt.musiclamp.R;
import com.imt.musiclamp.model.OnlineMusicInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class SceneAddMusicAdapter extends BaseAdapter {
    private Context context;
    private List<OnlineMusicInfo> musicInfos;
    private LayoutInflater inflater;
    private MyApplication myApplication;
    private boolean[] checks;

    public SceneAddMusicAdapter(Context context, List<OnlineMusicInfo> musicInfos) {
        this.context = context;
        this.musicInfos = musicInfos;
        checks = new boolean[musicInfos.size()];
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        OnlineMusicInfo onlineMusicInfo = musicInfos.get(position);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_scene_list_music, null, false);
            viewHolder = new ViewHolder();
            viewHolder.textViewTitle = (TextView) convertView.findViewById(R.id.textView_title);
            viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.textViewTitle.setText(onlineMusicInfo.getTitle());
        final ViewHolder finalViewHolder = viewHolder;
        viewHolder.textViewTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(musicInfos.get(position).isSceneCheck()){
                    checks[position]=false;
                    musicInfos.get(position).setSceneCheck(false);
                    finalViewHolder.checkBox.setChecked(false);
                }
                else{

                    checks[position]=true;
                    musicInfos.get(position).setSceneCheck(true);
                    finalViewHolder.checkBox.setChecked(true);
                }
            }
        });
        viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checks[position] = isChecked;
                musicInfos.get(position).setSceneCheck(isChecked);
            }
        });
        return convertView;
    }

    static class ViewHolder {
        TextView textViewTitle;
        CheckBox checkBox;
    }


    public JSONArray getCheckList() {
        JSONArray jsonArray = new JSONArray();
        for (OnlineMusicInfo musicInfo : musicInfos) {
            if (musicInfo.isSceneCheck()) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("songID", musicInfo.getSongId());
                    jsonArray.put(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return jsonArray;
    }

    public boolean[] getChecks() {
        return checks;
    }

    public void setChecks(boolean[] checks) {
        this.checks = checks;
    }
}
