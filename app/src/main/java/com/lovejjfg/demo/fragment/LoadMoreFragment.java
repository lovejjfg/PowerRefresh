/*
 * Copyright (c) 2017.  Joe
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lovejjfg.demo.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.lovejjfg.demo.CircleHeaderView;
import com.lovejjfg.demo.FootView;
import com.lovejjfg.demo.R;
import com.lovejjfg.powerrecycle.PowerAdapter;
import com.lovejjfg.powerrecycle.holder.PowerHolder;
import com.lovejjfg.powerrefresh.OnRefreshListener;
import com.lovejjfg.powerrefresh.PowerRefreshLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joe on 2017/5/1..
 * Email lovejjfg@gmail.com
 */

public class LoadMoreFragment extends Fragment {

    private PowerRefreshLayout mRefreshLayout;
    private MyAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.activity_main, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        RecyclerView mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        mRefreshLayout = (PowerRefreshLayout) view.findViewById(R.id.refresh_layout);
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                mRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRefreshLayout.stopRefresh(true, 300);
                    }
                }, 5000);
            }

            @Override
            public void onLoadMore() {
                mRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRefreshLayout.stopLoadMore(true);
                        List<String> mlist = new ArrayList<>();
                        for (int i = 0; i < 10; i++) {
                            mlist.add("nice" + i);
                        }
                        mAdapter.appendList(mlist);
                        mRefreshLayout.setLoadEnable(mAdapter.getList().size() < 50);
                    }
                }, 5000);
            }
        });
        CircleHeaderView header = new CircleHeaderView(getContext());
        FootView footView = new FootView(getContext());
        mRefreshLayout.addHeader(header);
        mRefreshLayout.addFooter(footView);
        mRefreshLayout.setAutoLoadMore(true);
        mRefreshLayout.setAutoRefresh(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
//        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        mAdapter = new MyAdapter();
        mRecyclerView.setAdapter(mAdapter);
        List<String> mlist = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            mlist.add("nice" + i);
        }
        mAdapter.setList(mlist);
        super.onViewCreated(view, savedInstanceState);
    }

    static class MyHolder extends PowerHolder<String> {

        private final TextView mText;

        public MyHolder(View itemView) {
            super(itemView);
            mText = (TextView) itemView;
        }

        @Override
        public void onBind(String s) {
            mText.setText(s);
        }
    }

    static class MyAdapter extends PowerAdapter<String> {
        @Override
        public PowerHolder<String> onViewHolderCreate(ViewGroup parent, int viewType) {
            float density = parent.getContext().getResources().getDisplayMetrics().density;
            TextView mFoot = new TextView(parent.getContext());
            double random = Math.random();
            int height = (int) (30 * random);
            mFoot.setLayoutParams(
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (density * (40 + height))));
            mFoot.setGravity(Gravity.CENTER);
            return new MyHolder(mFoot);
        }

        @Override
        public void onViewHolderBind(PowerHolder<String> holder, int position) {
            holder.onBind("This is the " + position);
        }
    }
}
