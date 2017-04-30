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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lovejjfg.demo.R;
import com.lovejjfg.demo.header.YEHeaderView;
import com.lovejjfg.powerrefresh.OnRefreshListener;
import com.lovejjfg.powerrefresh.PowerRefreshLayout;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link ScrollFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScrollFragment extends Fragment {
    private static final String TAG = ScrollFragment.class.getSimpleName();
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ScrollFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment MyFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ScrollFragment newInstance(String param1) {
        ScrollFragment fragment = new ScrollFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.layout_normal, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final PowerRefreshLayout mRefreshLayout = (PowerRefreshLayout) view.findViewById(R.id.refresh_layout);
        final YEHeaderView header = new YEHeaderView(getContext());
        mRefreshLayout.addHeader(header);
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.e(TAG, "onRefresh: ");
                mRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRefreshLayout.stopRefresh(true);

//                        mRefreshLayout.stopRefresh(true);
                    }
                }, 2000);

            }

            @Override
            public void onLoadMore() {
                Log.e(TAG, "onLoadMore: ");
            }
        });
        mRefreshLayout.setOnChildScrollUpCallback(new PowerRefreshLayout.OnChildScrollUpCallback() {
            @Override
            public boolean canChildScrollUp(PowerRefreshLayout parent, @Nullable View child) {
                return child != null && child.getScrollY() == 0;
            }
        });
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
