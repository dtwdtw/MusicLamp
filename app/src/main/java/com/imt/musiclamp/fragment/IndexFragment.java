package com.imt.musiclamp.fragment;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Select;
//import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.imt.musiclamp.CustomPlayistActivity;
import com.imt.musiclamp.DownloadedActivity;
import com.imt.musiclamp.LocalMusicListActivity;
import com.imt.musiclamp.MainActivity;
import com.imt.musiclamp.MyApplication;
import com.imt.musiclamp.MyLikeActivity;
import com.imt.musiclamp.OnlineMusicLibraryActivity;
import com.imt.musiclamp.OnlinePlayistActivity;
import com.imt.musiclamp.R;
import com.imt.musiclamp.RecentlyActivity;
import com.imt.musiclamp.RecomendMusicActivity;
import com.imt.musiclamp.SearchActivity;
import com.imt.musiclamp.adapter.CustomPlaylistAdapter;
import com.imt.musiclamp.element.UserInfoEvent;
import com.imt.musiclamp.event.PlayProgressEvent;
import com.imt.musiclamp.model.CustomPlayList;
import com.imt.musiclamp.model.Toplist;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class IndexFragment extends Fragment {

    @InjectView(R.id.gridView)
    GridView gridView;
    @InjectView(R.id.layout_header)
    LinearLayout layoutHeader;
    @InjectView(R.id.imageView_playlist_add)
    ImageView imageViewPlaylistAdd;
    @InjectView(R.id.imageView_avatar)
    ImageView headImg;
    @InjectView(R.id.textView_name)
    TextView userName;
    @InjectView(R.id.recomendmusic)
    LinearLayout recomendmusic;

    //dialog's view
    private EditText editTextDialogName;

    private List<CustomPlayList> playlists;
    private CustomPlaylistAdapter adapter;

    private static final int UPDATE_CUSTOM_PLAYLIST = 1;


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_CUSTOM_PLAYLIST:
                    playlists = new Select().from(CustomPlayList.class).execute();
                    adapter = new CustomPlaylistAdapter(getActivity(), playlists);
                    gridView.setAdapter(adapter);

                    Log.v("findcustomlist", playlists.size()+"");
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_index, container, false);
        EventBus.getDefault().register(this);
        ButterKnife.inject(this, view);

        playlists = new Select().from(CustomPlayList.class).execute();
        adapter = new CustomPlaylistAdapter(getActivity(), playlists);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(onItemClickListener);
        gridView.setOnItemLongClickListener(onItemLongClickListener);
        if (MyApplication.headUri != null) {
            Glide.with(this)
                    .load(MyApplication.headUri)
                    .centerCrop()
                    .crossFade()
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    headImg.setImageResource(R.drawable.defalthead);
                                }
                            });
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(headImg);
            userName.setText(MyApplication.userName);
            Log.v("url", MyApplication.headUri);
        }

        headImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SlidingMenu sm = ((MainActivity) getActivity()).getSlidingMenu();
                sm.showMenu();
            }
        });
        view.findViewById(R.id.search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SearchActivity.class));
            }
        });
        return view;
    }

    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(getActivity(), CustomPlayistActivity.class);
            intent.putExtra("id", playlists.get(position).getId());
            intent.putExtra("name", playlists.get(position).getName());
            startActivity(intent);
        }
    };


    AdapterView.OnItemLongClickListener onItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
            MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                    .title(R.string.settings)
                    .customView(R.layout.dialog_custom_playlist, false)
                    .positiveText(R.string.edit)
                    .negativeText(R.string.cancel)
                    .neutralText(R.string.delete)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            playlists.get(position).setName(editTextDialogName.getText().toString());
                            playlists.get(position).save();
                            handler.sendEmptyMessage(UPDATE_CUSTOM_PLAYLIST);
                        }

                        @Override
                        public void onNeutral(MaterialDialog dialog) {
                            if (!playlists.get(position).isReadOnly()) {
                                playlists.get(position).delete();
                                handler.sendEmptyMessage(UPDATE_CUSTOM_PLAYLIST);
                            } else {
                                Toast.makeText(getActivity(), "此歌单不可删除", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).build();
            editTextDialogName = (EditText) dialog.getCustomView().findViewById(R.id.editText_name);
            editTextDialogName.setText(playlists.get(position).getName());
            dialog.show();
            return true;
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        handler.sendEmptyMessage(UPDATE_CUSTOM_PLAYLIST);
    }

    @OnClick({R.id.layout_all_music, R.id.layout_music_library, R.id.layout_like, R.id.layout_downloaded, R.id.layout_recently, R.id.recomendmusic,
            R.id.imageView_playlist_add})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.recomendmusic:
                Toplist toplist = null;
                String hotMusicjson="{\"imgUrl\":\"http:\\/\\/p4.music.126.net\\/eSXJexcoihfSe8ERgOdMnQ==\\/2920302885027135.jpg\",\"toplistId\":3778678,\"top1Artist\":\"Wiz Khalifa\",\"top2Artist\":\"Maroon 5\",\"top3Artist\":\"李荣浩\",\"name\":\"云音乐热歌榜\",\"intr\":null,\"top3Title\":\"李白\",\"top2Title\":\"Sugar\",\"top1Title\":\"See You Again (feat. Charlie Puth)\"}";
                try {
                    JSONObject jsonObject=new JSONObject(hotMusicjson);
                    toplist = new Toplist();
                    toplist.setPlaylistId(Integer.valueOf(jsonObject.getString("toplistId")));
                    toplist.setName(jsonObject.getString("name"));
                    toplist.setImg(jsonObject.getString("imgUrl"));
                    toplist.setTop1Title(jsonObject.getString("top1Title"));
                    toplist.setTop1Artist(jsonObject.getString("top1Artist"));
                    toplist.setTop2Title(jsonObject.getString("top2Title"));
                    toplist.setTop2Artist(jsonObject.getString("top2Artist"));
                    toplist.setTop3Title(jsonObject.getString("top3Title"));
                    toplist.setTop3Artist(jsonObject.getString("top3Artist"));
                    MyApplication myApplication=(MyApplication)getActivity().getApplication();
                    myApplication.setCurrentOnlinePlaylist(toplist);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(getActivity(), OnlinePlayistActivity.class);
                intent.putExtra("playlistId", toplist.getPlaylistId());
                intent.putExtra("title", toplist.getName());
                startActivity(intent);
                break;
            case R.id.layout_all_music:
                startActivity(new Intent(getActivity(), LocalMusicListActivity.class));
                break;
            case R.id.layout_music_library:
                startActivity(new Intent(getActivity(), OnlineMusicLibraryActivity.class));
                break;
            case R.id.layout_like:
                startActivity(new Intent(getActivity(), MyLikeActivity.class));
                break;
            case R.id.layout_downloaded:
                startActivity(new Intent(getActivity(), DownloadedActivity.class));
                break;
            case R.id.layout_recently:
                startActivity(new Intent(getActivity(), RecentlyActivity.class));
                break;
            case R.id.imageView_playlist_add:
                MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                        .title("添加歌单")
                        .customView(R.layout.dialog_custom_playlist, false)
                        .positiveText(R.string.add)
                        .negativeText(R.string.cancel)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                CustomPlayList customPlayList = new CustomPlayList();
                                customPlayList.setName(editTextDialogName.getText().toString());
                                customPlayList.setReadOnly(false);
                                customPlayList.save();
                                Log.v("addcustomlist",customPlayList.getName());
                                handler.sendEmptyMessage(UPDATE_CUSTOM_PLAYLIST);
                            }
                        }).build();
                editTextDialogName = (EditText) dialog.getCustomView().findViewById(R.id.editText_name);
                dialog.show();
                break;
        }
    }


    public void onEvent(PlayProgressEvent playProgressEvent) {
        handler.post(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    public void onEventMainThread(UserInfoEvent userInfoEvent) {
        Glide.with(this)
                .load(userInfoEvent.getUserHead())
                .centerCrop()
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                headImg.setImageResource(R.drawable.defalthead);
                            }
                        });
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        return false;
                    }
                })
                .crossFade()
                .into(headImg);
        userName.setText(userInfoEvent.getUserName());
    }

}
