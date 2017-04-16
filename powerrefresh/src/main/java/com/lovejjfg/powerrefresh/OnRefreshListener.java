package com.lovejjfg.powerrefresh;

/**
 * Created by Joe on 2017/4/16.
 * Email lovejjfg@gmail.com
 */

public interface OnRefreshListener {

    /**
     * 刷新回调
     */
    void onRefresh();

    /**
     * 加载更多回调
     */
    void onLoadMore();
}
