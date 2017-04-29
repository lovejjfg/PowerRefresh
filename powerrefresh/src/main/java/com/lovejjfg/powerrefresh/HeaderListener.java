package com.lovejjfg.powerrefresh;

/**
 * Created by Joe on 2017/4/16.
 * Email lovejjfg@gmail.com
 */

public interface HeaderListener {


    void onRefreshBefore(int scrollY, int headerHeight);


    void onRefreshAfter(int scrollY, int headerHeight);


    void onRefreshReady(int scrollY, int headerHeight);


    void onRefreshing(int scrollY, int headerHeight);

    void onRefreshComplete(int scrollY, int headerHeight, boolean isRefreshSuccess);


    void onRefreshCancel(int scrollY, int headerHeight);

    int getRefreshHeight();
}
