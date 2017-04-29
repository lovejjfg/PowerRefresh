package com.lovejjfg.demo;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.lovejjfg.powerrefresh.HeaderListener;


/**
 * Created by Joe on 2017/4/16.
 * Email lovejjfg@gmail.com
 */

public class CircleHeaderView extends FrameLayout implements HeaderListener {

    public TouchCircleView getmHeader() {
        return mHeader;
    }

    //    private final ImageView mHeaderImageView;
//    private final AnimationDrawable mFrameAnimation;
//    private final int mNumberOfFrames;
    private final TouchCircleView mHeader;

    public CircleHeaderView(Context context) {
        super(context);
        float density = context.getResources().getDisplayMetrics().density;
        mHeader = new TouchCircleView(context);

        addView(mHeader, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (density * 150)));

//        LayoutInflater.from(context).inflate(R.layout.layout_header_loading, this, true);
//        mHeaderImageView = (ImageView) findViewById(R.id.pull_to_refresh_image);
//
//        mFrameAnimation = (AnimationDrawable) mHeaderImageView.getBackground();
//        mNumberOfFrames = mFrameAnimation.getNumberOfFrames();
    }

    /**
     * 下拉刷新
     *
     * @param scrollY
     */
    @Override
    public void onRefreshBefore(int scrollY, int headerHeight) {
        Log.e("TAG", "onRefreshBefore: " + scrollY);
        mHeader.handleOffset(-scrollY);
//        mHeaderImageView.setBackgroundDrawable(mFrameAnimation.getFrame(current));

    }

    /**
     * 松开刷新
     *
     * @param scrollY
     */
    @Override
    public void onRefreshAfter(int scrollY, int headerHeight) {
//        mHeaderImageView.setBackgroundDrawable(mFrameAnimation.getFrame(mNumberOfFrames - 1));
        mHeader.setRefresh(true);
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

    }

    /**
     * 刷新成功
     *
     * @param scrollY
     * @param isRefreshSuccess 刷新的状态  是成功了 还是失败了
     */
    @Override
    public void onRefreshComplete(int scrollY, int headerHeight, boolean isRefreshSuccess) {
        if (isRefreshSuccess) {
            mHeader.setRefreshSuccess();

        } else {
            mHeader.setRefreshError();
        }

    }

    /**
     * 取消刷新
     *
     * @param scrollY
     */
    @Override
    public void onRefreshCancel(int scrollY, int headerHeight) {

    }

    @Override
    public int getRefreshHeight() {
        return (int) (getMeasuredHeight()*0.8f);
    }

    public void refreshCompleted() {
        mHeader.setRefreshSuccess();

    }
}
