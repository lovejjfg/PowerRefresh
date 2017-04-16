package com.lovejjfg.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.lovejjfg.powerrecycle.PowerAdapter;
import com.lovejjfg.powerrefresh.OnRefreshListener;
import com.lovejjfg.powerrefresh.PowerRefreshLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnRefreshListener {

    private PowerRefreshLayout mRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recycler);
        mRefreshLayout = (PowerRefreshLayout) findViewById(R.id.refresh_layout);
        mRefreshLayout.setOnRefreshListener(this);
        HeaderView header = new HeaderView(this);
        FootView footView = new FootView(this);
        mRefreshLayout.addHeader(header);
        mRefreshLayout.addFooter(footView);
        mRefreshLayout.setOnHeaderListener(header);
//        mRefreshLayout.setAutoLoadMore(true);
//        mRefreshLayout.setOnFooterListener(footView);
//        mRecyclerView.setLayoutFrozen();
//        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        MyAdapter mAdapter = new MyAdapter();
        mRecyclerView.setAdapter(mAdapter);
        List<String> mlist = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            mlist.add("nice" + i);
        }
        mAdapter.setList(mlist);
    }

    @Override
    public void onRefresh() {
        mRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRefreshLayout.stopRefresh(true);
            }
        }, 1000);
    }

    @Override
    public void onLoadMore() {
        Log.e("TAG", "onLoadMore::::: ");
        mRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRefreshLayout.stopLoadMore(true);
                Log.e("TAG", "run: 可以加载更多了");
            }
        }, 10000);
    }

    static class MyAdapter extends PowerAdapter<String> {
        @Override
        public RecyclerView.ViewHolder onViewHolderCreate(ViewGroup parent, int viewType) {
            float density = parent.getContext().getResources().getDisplayMetrics().density;
           TextView mFoot = new TextView(parent.getContext());
            mFoot.setBackgroundColor(Color.parseColor("#333333"));
            double random = Math.random();
            int height = (int) (30 * random);
            mFoot.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (density * (40 + height))));
            mFoot.setText("加载更多！！");
            mFoot.setGravity(Gravity.CENTER);
            return new MyHolder(mFoot);
        }

        @Override
        public void onViewHolderBind(RecyclerView.ViewHolder holder, int position) {
            ((MyHolder) holder).onBind("This is the " + position);
        }
    }

    static class MyHolder extends RecyclerView.ViewHolder {

        private final TextView mText;

        public MyHolder(View itemView) {
            super(itemView);
            mText = (TextView) itemView;
        }

        public void onBind(String s) {
            mText.setText(s);
        }
    }


}
