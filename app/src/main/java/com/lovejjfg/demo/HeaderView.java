package com.lovejjfg.demo;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.lovejjfg.powerrefresh.OnHeaderListener;


/**
 * Created by Joe on 2017/4/16.
 * Email lovejjfg@gmail.com
 */

public class HeaderView extends FrameLayout implements OnHeaderListener {

    //    private final ImageView mHeaderImageView;
//    private final AnimationDrawable mFrameAnimation;
//    private final int mNumberOfFrames;
    private final TouchCircleView mHeader;

    public HeaderView(Context context) {
        super(context);
        float density = context.getResources().getDisplayMetrics().density;
        mHeader = new TouchCircleView(context);

        addView(mHeader, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (density * 130)));

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
    public void onRefreshBefore(int scrollY, int refreshHeight, int headerHeight) {
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
    public void onRefreshAfter(int scrollY, int refreshHeight, int headerHeight) {
//        mHeaderImageView.setBackgroundDrawable(mFrameAnimation.getFrame(mNumberOfFrames - 1));
        mHeader.resetTouch();
    }

    /**
     * 准备刷新
     *
     * @param scrollY
     */
    @Override
    public void onRefreshReady(int scrollY, int refreshHeight, int headerHeight) {

    }

    /**
     * 正在刷新
     *
     * @param scrollY
     */
    @Override
    public void onRefreshing(int scrollY, int refreshHeight, int headerHeight) {
        mHeader.handleOffset(refreshHeight);
    }

    /**
     * 刷新成功
     *
     * @param scrollY
     * @param isRefreshSuccess 刷新的状态  是成功了 还是失败了
     */
    @Override
    public void onRefreshComplete(int scrollY, int refreshHeight, int headerHeight, boolean isRefreshSuccess) {

    }

    /**
     * 取消刷新
     *
     * @param scrollY
     */
    @Override
    public void onRefreshCancel(int scrollY, int refreshHeight, int headerHeight) {

    }
}
