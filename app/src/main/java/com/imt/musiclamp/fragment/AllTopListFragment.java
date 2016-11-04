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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.imt.musiclamp.MyApplication;
import com.imt.musiclamp.OnlinePlayistActivity;
import com.imt.musiclamp.R;
import com.imt.musiclamp.adapter.AllTopListAdapter;
import com.imt.musiclamp.api.APIClient;
import com.imt.musiclamp.model.Toplist;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;

public class AllTopListFragment extends Fragment {


    @InjectView(R.id.listView)
    ListView listView;
    private PtrClassicFrameLayout pullToRefresh;
    private LinearLayout loading;

    private List<Toplist> toplists;
    private AllTopListAdapter adapter;

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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_all_top_list, container, false);
        myApplication = (MyApplication) getActivity().getApplication();
        ButterKnife.inject(this, view);
        loading = (LinearLayout) inflater.inflate(R.layout.list_footer_progress, listView, false);
        initPullToRefresh(view, ptrHandler);
        initListView();
        return view;
    }

    private void initListView() {
        toplists = new ArrayList<>();
        adapter = new AllTopListAdapter(getActivity(), toplists);
        listView.addFooterView(loading);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(onItemClickListener);
        listView.setOnScrollListener(onScrollListener);
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
                    toplists.clear();
                    APIClient.getTopList(getActivity(), 1, allTopListResponseHandler);
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
                    APIClient.getTopList(getActivity(), page, allTopListResponseHandler);
                }
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        }
    };

    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(getActivity(), OnlinePlayistActivity.class);
            intent.putExtra("playlistId", toplists.get(position).getPlaylistId());
            intent.putExtra("title", toplists.get(position).getName());
            startActivity(intent);
            myApplication.setCurrentOnlinePlaylist(toplists.get(position));
        }
    };

    AsyncHttpResponseHandler allTopListResponseHandler = new AsyncHttpResponseHandler() {
        @Override
        public void onSuccess(int i, Header[] headers, byte[] bytes) {
            Log.e("onSuccess", new String(bytes));
//            List<Toplist> temp = new Gson().fromJson(new String(bytes), new TypeToken<List<Toplist>>() {
//            }.getType());
//            for (Toplist toplist : temp) {
//                toplists.add(toplist);
//            }
            try {
                JSONArray jsonArray = new JSONArray(new String(bytes));
                for (int j = 0; j < jsonArray.length(); j++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(j);
                    Toplist toplist = new Toplist();
                    toplist.setPlaylistId(Integer.valueOf(jsonObject.getString("toplistId")));
                    toplist.setName(jsonObject.getString("name"));
                    toplist.setImg(jsonObject.getString("imgUrl"));
                    toplist.setTop1Title(jsonObject.getString("top1Title"));
                    toplist.setTop1Artist(jsonObject.getString("top1Artist"));
                    toplist.setTop2Title(jsonObject.getString("top2Title"));
                    toplist.setTop2Artist(jsonObject.getString("top2Artist"));
                    toplist.setTop3Title(jsonObject.getString("top3Title"));
                    toplist.setTop3Artist(jsonObject.getString("top3Artist"));

                    if(toplist.getName().contains("云音乐")){
                        Log.v("idyunyinyue",toplist.getPlaylistId()+"");
                        Log.v("idyunyinyue",jsonObject.toString());
                    }
                    toplists.add(toplist);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                handler.sendEmptyMessage(1);
            }
        }

        @Override
        public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {

            //这句会抛出nullointerexception

//            Log.e("onFailure", new String(bytes));
        }
    };


}
