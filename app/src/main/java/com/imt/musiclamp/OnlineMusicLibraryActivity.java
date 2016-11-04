package com.imt.musiclamp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;
import com.imt.musiclamp.fragment.AllTopListFragment;
import com.imt.musiclamp.fragment.PlaylistFragment;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class OnlineMusicLibraryActivity extends FragmentActivity {

    @InjectView(R.id.viewPager)
    ViewPager viewPager;

    @InjectView(R.id.tab)
    PagerSlidingTabStrip tabStrip;

    private MyPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_music_library);
        ButterKnife.inject(this);

        adapter = new MyPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        tabStrip.setViewPager(viewPager);
        tabStrip.setTextColor(0xaaffffff);
    }


    //viewpager的适配器
    class MyPagerAdapter extends FragmentPagerAdapter {

        private final Fragment[] fragments = new Fragment[]{
                new PlaylistFragment(),
                new AllTopListFragment()};

        private final String[] TITLES = { getResources().getString(R.string.featured), getResources().getString(R.string.top_charts)};

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        @Override
        public int getCount() {
            return fragments.length;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
        }
    }

    @OnClick({R.id.imageView_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageView_back:
                finish();
                break;
        }
    }

}
