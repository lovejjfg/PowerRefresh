package com.lovejjfg.powerrefresh;

/**
 * Created by Joe on 2017/4/16.
 * Email lovejjfg@gmail.com
 */

public interface FooterListener {

    void onLoadBefore(int scrollY);

    void onLoadAfter(int scrollY);

    void onLoadReady(int scrollY);

    void onLoading(int scrollY);

    void onLoadComplete(int scrollY, boolean isLoadSuccess);

    void onLoadCancel(int scrollY);
}
