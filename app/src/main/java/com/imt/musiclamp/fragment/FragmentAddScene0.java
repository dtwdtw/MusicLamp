package com.imt.musiclamp.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.imt.musiclamp.MainActivity;
import com.imt.musiclamp.MyApplication;
import com.imt.musiclamp.R;
import com.imt.musiclamp.adapter.SceneAddMusicAdapter;
import com.imt.musiclamp.model.OnlineMusicInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentAddScene0 extends Fragment {

    @InjectView(R.id.textView_playlist)
    TextView textViewPlaylist;
    @InjectView(R.id.listView)
    ListView listView;

    private SceneAddMusicAdapter adapter;

    private MyApplication myApplication;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fragment_add_scene0, container, false);
        myApplication = (MyApplication) getActivity().getApplication();
        ButterKnife.inject(this, view);
        List<OnlineMusicInfo> likemusic=new ArrayList<>();
        likemusic=new Select().from(OnlineMusicInfo.class)
                .where("like = ?", true)
                .execute();
        if(likemusic.size() > 0){
            adapter = new SceneAddMusicAdapter(getActivity(), likemusic);
            listView.setAdapter(adapter);
            textViewPlaylist.setText(getResources().getString(R.string.red_heart_songs_list));
        }
        else if (myApplication.getOnlineMusicInfos().size() > 0) {
            adapter = new SceneAddMusicAdapter(getActivity(), myApplication.getOnlineMusicInfos());
            listView.setAdapter(adapter);
            textViewPlaylist.setText(myApplication.getCurrentOnlinePlaylist().getName());
        } else {
            Toast.makeText(getActivity(), R.string.update_red_heart_first,Toast.LENGTH_LONG).show();

            Intent intent = new Intent();
            intent.setClass(getActivity(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            getActivity().finish();

        }

        return view;
    }

    public JSONArray getMusicList() {
        return adapter.getCheckList();
    }

}
