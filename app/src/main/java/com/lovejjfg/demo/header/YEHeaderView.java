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

package com.lovejjfg.demo.header;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.lovejjfg.demo.R;
import com.lovejjfg.powerrefresh.HeaderListener;

/**
 * Created by Joe on 2017/4/29.
 * Email lovejjfg@gmail.com
 */


public class YEHeaderView extends FrameLayout implements HeaderListener {

    private final ImageView mHeaderImageView;
    private final AnimationDrawable mFrameAnimation;
    private final int mNumberOfFrames;
    public TextView mRefreshTv;

    public YEHeaderView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.layout_header_loading, this, true);
        mRefreshTv = (TextView) findViewById(R.id.pull_to_refresh_text);
        mHeaderImageView = (ImageView) findViewById(R.id.pull_to_refresh_image);
        mHeaderImageView.setBackgroundResource(R.drawable.pull_refresh_ld);

        mFrameAnimation = (AnimationDrawable) mHeaderImageView.getBackground();
        mNumberOfFrames = mFrameAnimation.getNumberOfFrames();
    }

    /**
     * 下拉刷新
     *
     * @param scrollY
     */
    @Override
    public void onRefreshBefore(int scrollY, int headerHeight) {
        mRefreshTv.setText(R.string.pull_refresh_label);
        Log.e("TAG", "onRefreshBefore: " + scrollY);
        if (Math.abs(scrollY) < headerHeight * 0.5f) {
            return;
        }
        float v = Math.abs(Math.abs(scrollY + headerHeight * 0.5f) * 1.0f / headerHeight);
        if (v > 1) {
            v = 1;
        }
        int current = (int) (v * 44);
        Log.e("TAG", "frame: " + current);
        mHeaderImageView.setBackgroundDrawable(mFrameAnimation.getFrame(current));

    }

    /**
     * 松开刷新
     *
     * @param scrollY
     */
    @Override
    public void onRefreshAfter(int scrollY, int headerHeight) {
        mHeaderImageView.setBackgroundDrawable(mFrameAnimation.getFrame(mNumberOfFrames - 1));
        mRefreshTv.setText(R.string.pull_release_label);

    }

    /**
     * 准备刷新
     *
     * @param scrollY
     */
    @Override
    public void onRefreshReady(int scrollY, int headerHeight) {

    }

    /**
     * 正在刷新
     *
     * @param scrollY
     */
    @Override
    public void onRefreshing(int scrollY, int headerHeight) {
//        mRefreshTv.setText("正在刷新");
        mHeaderImageView.setBackgroundDrawable(mFrameAnimation);
        ((AnimationDrawable) mHeaderImageView.getBackground()).start();
    }

    /**
     * 刷新成功
     *
     * @param scrollY
     * @param isRefreshSuccess 刷新的状态  是成功了 还是失败了
     */
    @Override
    public void onRefreshComplete(int scrollY, int headerHeight, boolean isRefreshSuccess) {
        mRefreshTv.setText(isRefreshSuccess ? "刷新成功" : "刷新失败");
        Drawable background = mHeaderImageView.getBackground();
        if (background instanceof AnimationDrawable) {
            ((AnimationDrawable) background).stop();
        }
    }

    /**
     * 取消刷新
     *
     * @param scrollY
     */
    @Override
    public void onRefreshCancel(int scrollY, int headerHeight) {
//        mRefreshTv.setText("取消刷新");
    }

    @Override
    public int getRefreshHeight() {
        return 0;
    }
}
