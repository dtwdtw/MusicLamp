package com.imt.musiclamp.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.imt.musiclamp.MyApplication;
import com.imt.musiclamp.OnlinePlayistActivity;
import com.imt.musiclamp.R;
import com.imt.musiclamp.adapter.PlaylistAdapter;
import com.imt.musiclamp.api.APIClient;
import com.imt.musiclamp.model.OnlinePlaylist;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import in.srain.cube.views.GridViewWithHeaderAndFooter;
import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;

public class PlaylistFragment extends Fragment {

    private GridViewWithHeaderAndFooter gridView;
    private LinearLayout loading;
    private PtrClassicFrameLayout pullToRefresh;

    private List<OnlinePlaylist> playlists;
    private PlaylistAdapter adapter;

    private int page = 1;

    private MyApplication myApplication;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            loading.setVisibility(View.GONE);
            pullToRefresh.refreshComplete();
            adapter.notifyDataSetChanged();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);
        myApplication = (MyApplication) getActivity().getApplication();
        ButterKnife.inject(this, view);
        loading = (LinearLayout) inflater.inflate(R.layout.list_footer_progress, gridView, false);
        initPullToRefresh(view, ptrHandler);
        initGridView(view);
        return view;
    }

    private void initGridView(View view) {
        gridView = (GridViewWithHeaderAndFooter) view.findViewById(R.id.gridView);
        gridView.addFooterView(loading);
        gridView.setOnScrollListener(onScrollListener);
        gridView.setOnItemClickListener(onItemClickListener);
//        gridView.setCacheColorHint(0xdd000000);

        playlists = new ArrayList<>();
        adapter = new PlaylistAdapter(getActivity(), playlists);
        gridView.setAdapter(adapter);
    }

    private void initPullToRefresh(View view, PtrHandler ptrHandler) {
        pullToRefresh = (PtrClassicFrameLayout) view.findViewById(R.id.pull_to_refresh);
        pullToRefresh.setLastUpdateTimeRelateObject(this);
        pullToRefresh.setPtrHandler(ptrHandler);
        // the following are default settings
        pullToRefresh.setResistance(1.7f);
        pullToRefresh.setRatioOfHeaderHeightToRefresh(1.2f);
        pullToRefresh.setDurationToClose(200);
        pullToRefresh.setDurationToCloseHeader(1000);
        // default is false
        pullToRefresh.setPullToRefresh(false);
        // default is true
        pullToRefresh.setKeepHeaderWhenRefresh(true);
        pullToRefresh.postDelayed(new Runnable() {
            @Override
            public void run() {
                pullToRefresh.autoRefresh();
            }
        }, 100);
    }

    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(getActivity(), OnlinePlayistActivity.class);
            intent.putExtra("playlistId", playlists.get(position).getPlaylistId());
            intent.putExtra("title", playlists.get(position).getName());
            startActivity(intent);
            myApplication.setCurrentOnlinePlaylist(playlists.get(position));
        }
    };

    private PtrHandler ptrHandler = new PtrHandler() {
        @Override
        public boolean checkCanDoRefresh(PtrFrameLayout ptrFrameLayout, View view, View view2) {
            return PtrDefaultHandler.checkContentCanBePulledDown(ptrFrameLayout, view, view2);
        }

        @Override
        public void onRefreshBegin(PtrFrameLayout ptrFrameLayout) {
            pullToRefresh.postDelayed(new Runnable() {
                @Override
                public void run() {
                    playlists.clear();
                    APIClient.getAllPlayList(getActivity(), 1, playlistResponseHandler);
                }
            }, 1000);
        }
    };

    private AbsListView.OnScrollListener onScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                // 判断滚动到底部
                if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
                    page += 1;
                    loading.setVisibility(View.VISIBLE);
                    APIClient.getAllPlayList(getActivity(), page, playlistResponseHandler);
                }
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        }
    };

    AsyncHttpResponseHandler playlistResponseHandler = new AsyncHttpResponseHandler() {
        @Override
        public void onSuccess(int i, Header[] headers, byte[] bytes) {
            Log.e("onSuccess", new String(bytes));
            List<OnlinePlaylist> temp = new Gson().fromJson(new String(bytes), new TypeToken<List<OnlinePlaylist>>() {
            }.getType());
            for (OnlinePlaylist playlist : temp) {
                playlists.add(playlist);
            }
            handler.sendEmptyMessage(1);
        }

        @Override
        public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {

        }
    };


}
