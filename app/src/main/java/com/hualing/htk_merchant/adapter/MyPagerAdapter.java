package com.hualing.htk_merchant.adapter;

import android.app.Dialog;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.hualing.htk_merchant.R;
import com.hualing.htk_merchant.activities.MainActivity;
import com.hualing.htk_merchant.global.TheApplication;
import com.hualing.htk_merchant.util.JPushUtil;
import com.hualing.htk_merchant.widget.pull2refresh.PullToRefreshLayout;
import com.hualing.htk_merchant.widget.pull2refresh.pullableview.PullableListView;

import java.util.ArrayList;
import java.util.List;

public class MyPagerAdapter extends PagerAdapter {

    //要切换的View
    private View view1;
    private View view2;
    private View view3;
    //view集合
    private List<View> views;
    private PullableListView mListView1,mListView2,mListView3;
    private PullToRefreshLayout mRefresher1,mRefresher2,mRefresher3;
    private static final int REFRESHER1_TYPE=1;
    private static final int REFRESHER2_TYPE=2;
    private static final int REFRESHER3_TYPE=3;

    public NewOrderAdapter getmAdapter1() {
        return mAdapter1;
    }

    public void setmAdapter1(NewOrderAdapter mAdapter1) {
        this.mAdapter1 = mAdapter1;
    }

    private NewOrderAdapter mAdapter1;
    private DeliveryAdapter mAdapter2;

    public DeliveryAdapter getmAdapter2() {
        return mAdapter2;
    }

    public void setmAdapter2(DeliveryAdapter mAdapter2) {
        this.mAdapter2 = mAdapter2;
    }

    public FinishedAdapter getmAdapter3() {
        return mAdapter3;
    }

    public void setmAdapter3(FinishedAdapter mAdapter3) {
        this.mAdapter3 = mAdapter3;
    }

    private FinishedAdapter mAdapter3;

    public MyPagerAdapter(MainActivity mainActivity,Button mDot1,Button mDot2,Button mDot3){
        views = new ArrayList<>();

        view1 = View.inflate(mainActivity, R.layout.banner_layout_one_pager,null);
        mListView1 = view1.findViewById(R.id.listView);
        mRefresher1 = view1.findViewById(R.id.refresher);

        mRefresher1.setOnRefreshListener(new MyListener(REFRESHER1_TYPE));
        mAdapter1 = new NewOrderAdapter(mainActivity,mDot1);
        mAdapter1.setNewData();
        mListView1.setAdapter(mAdapter1);
        views.add(view1);
        //mRefresher1.autoRefresh();

        view2 = View.inflate(mainActivity,R.layout.banner_layout_two_pager,null);
        mListView2 = view2.findViewById(R.id.listView);
        mRefresher2 = view2.findViewById(R.id.refresher);

        mRefresher2.setOnRefreshListener(new MyListener(REFRESHER2_TYPE));
        mAdapter2 = new DeliveryAdapter(mainActivity,mDot2);
        mAdapter2.setNewData();
        mListView2.setAdapter(mAdapter2);
        views.add(view2);
        //mRefresher2.autoRefresh();

        view3 = View.inflate(mainActivity,R.layout.banner_layout_three_pager,null);
        mListView3 = view3.findViewById(R.id.listView);
        mRefresher3 = view3.findViewById(R.id.refresher);

        mRefresher3.setOnRefreshListener(new MyListener(REFRESHER3_TYPE));
        mAdapter3 = new FinishedAdapter(mainActivity,mDot3);
        mAdapter3.setNewData();
        mListView3.setAdapter(mAdapter3);
        views.add(view3);
        //mRefresher3.autoRefresh();
        JPushUtil.initAdapter(mAdapter1,mAdapter2,mAdapter3);
    }

    public void setNewData(){
        mAdapter1.setNewData();
        mAdapter2.setNewData();
        mAdapter3.setNewData();
    }

    class MyListener implements PullToRefreshLayout.OnRefreshListener {
        private int type;

        public MyListener(int type){
            this.type=type;
        }

        @Override
        public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
            switch (type){
                case REFRESHER1_TYPE:
                    mAdapter1.setNewData();
                    mRefresher1.refreshFinish(PullToRefreshLayout.SUCCEED);
                    break;
                case REFRESHER2_TYPE:
                    mAdapter2.setNewData();
                    mRefresher2.refreshFinish(PullToRefreshLayout.SUCCEED);
                    break;
                case REFRESHER3_TYPE:
                    mAdapter3.setNewData();
                    mRefresher3.refreshFinish(PullToRefreshLayout.SUCCEED);
                    break;
            }
        }

        @Override
        public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {
            //不要上拉加载
            switch (type) {
                case REFRESHER1_TYPE:
                    mAdapter1.setNewData();
                    mRefresher1.loadmoreFinish(PullToRefreshLayout.SUCCEED);
                    break;
                case REFRESHER2_TYPE:
                    mAdapter2.setNewData();
                    mRefresher2.loadmoreFinish(PullToRefreshLayout.SUCCEED);
                    break;
                case REFRESHER3_TYPE:
                    mAdapter3.setNewData();
                    mRefresher3.loadmoreFinish(PullToRefreshLayout.SUCCEED);
                    break;
            }
        }
    }

    @Override
    public int getCount() {
        return views.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View v = views.get(position);
        container.addView(v);
        return v;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(views.get(position));
    }
}
