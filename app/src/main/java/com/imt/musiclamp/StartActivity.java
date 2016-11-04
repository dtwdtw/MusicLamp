package com.imt.musiclamp;

import android.app.Activity;
import android.media.Image;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.activeandroid.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dtw on 15/5/21.
 */
public class StartActivity extends Activity implements View.OnClickListener, ViewPager.OnPageChangeListener {
    private ViewPager vp;

    private ViewPagerAdapter vpAdapter;

    private List<View> views;

    boolean lastpage = false;
    LinearLayout ll;

    //引导图片资源

    private static final int[] pics = {R.drawable.start1,

            R.drawable.start2, R.drawable.start3,

            R.drawable.start4, R.drawable.start5};


    //底部小店图片

    private ImageView[] dots;


    //记录当前选中位置

    private int currentIndex;


    @Override

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.startlayout);


        views = new ArrayList<View>();


        LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,

                LinearLayout.LayoutParams.WRAP_CONTENT);


        //初始化引导图片列表

        for (int i = 0; i < pics.length; i++) {

            ImageView iv = new ImageView(this);

            iv.setLayoutParams(mParams);

            iv.setImageResource(pics[i]);

            views.add(iv);

        }

        vp = (ViewPager) findViewById(R.id.viewPager);

        //初始化Adapter

        vpAdapter = new ViewPagerAdapter(views);

        vp.setAdapter(vpAdapter);

        //绑定回调

        vp.setOnPageChangeListener(this);


        //初始化底部小点

        initDots();


    }


    private void initDots() {

        ll = (LinearLayout) findViewById(R.id.dot_dot);


        dots = new ImageView[pics.length ];


        //循环取得小点图片

        for (int i = 0; i < pics.length ; i++) {

            //得到一个LinearLayout下面的每一个子元素

            dots[i] = (ImageView) ll.getChildAt(i);

            dots[i].setEnabled(true);//都设为灰色

            dots[i].setOnClickListener(this);

            dots[i].setTag(i);//设置位置tag，方便取出与当前位置对应

        }


        currentIndex = 0;

        dots[currentIndex].setEnabled(false);//设置为白色，即选中状态

    }


    private void setCurView(int position)

    {

        if (position < 0 || position >= pics.length) {

            return;

        }


        vp.setCurrentItem(position);

    }


    private void setCurDot(int positon)

    {

        if (positon < 0 || positon > pics.length - 1 || currentIndex == positon) {

            return;

        }


        dots[positon].setEnabled(false);

        dots[currentIndex].setEnabled(true);


        currentIndex = positon;


    }


    //当滑动状态改变时调用

    @Override

    public void onPageScrollStateChanged(int arg0) {

        // TODO Auto-generated method stub


    }


    //当当前页面被滑动时调用

    @Override

    public void onPageScrolled(int arg0, float arg1, int arg2) {

        // TODO Auto-generated method stub


    }


    //当新的页面被选中时调用

    @Override

    public void onPageSelected(int arg0) {

        //设置底部小点选中状态

        setCurDot(arg0);
        if (arg0 > 3) {
            lastpage = true;
            ll.setVisibility(View.INVISIBLE);
        } else
            ll.setVisibility(View.VISIBLE);
        lastpage = false;
    }


    @Override

    public void onClick(View v) {

        int position = (Integer) v.getTag();

        setCurView(position);

        setCurDot(position);

    }

    public class ViewPagerAdapter extends PagerAdapter {


        //界面列表

        private List<View> views;


        public ViewPagerAdapter(List<View> views) {

            this.views = views;

        }


        //销毁arg1位置的界面

        @Override

        public void destroyItem(View arg0, int arg1, Object arg2) {

            ((ViewPager) arg0).removeView(views.get(arg1));

        }


        @Override

        public void finishUpdate(View arg0) {

            // TODO Auto-generated method stub


        }


        //获得当前界面数

        @Override

        public int getCount() {

            if (views != null)

            {

                return views.size();

            }


            return 0;

        }


        //初始化arg1位置的界面

        @Override

        public Object instantiateItem(View arg0, int arg1) {


            ((ViewPager) arg0).addView(views.get(arg1), 0);

            if (arg1 > 3) {
                views.get(arg1).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
            }
            return views.get(arg1);

        }


        //判断是否由对象生成界面

        @Override

        public boolean isViewFromObject(View arg0, Object arg1) {

            return (arg0 == arg1);

        }


        @Override

        public void restoreState(Parcelable arg0, ClassLoader arg1) {

            // TODO Auto-generated method stub


        }


        @Override

        public Parcelable saveState() {

            // TODO Auto-generated method stub

            return null;

        }


        @Override

        public void startUpdate(View arg0) {

            // TODO Auto-generated method stub


        }


    }
}
