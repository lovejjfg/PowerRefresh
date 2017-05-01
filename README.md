## PowerReresh

This library support **nested scroll** for refresh and load more,so you can use it with `CoordinatorLayout` and `AppBarLayout` and so no..

![screen](https://raw.githubusercontent.com/lovejjfg/screenshort/master/power_refresh.gif)

### Add Header

	   CircleHeaderView header = new CircleHeaderView(getContext());
        mRefreshLayout.addHeader(header);
### Add footer

	FootView footView = new FootView(getContext());
	mRefreshLayout.addFooter(header);

### Callback

you should make `Header` impl `HeaderListener`,so you can know the refresh state.and to `Footer` with `FooterListener`.


    mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
        @Override
        public void onRefresh() {
			//doRefresh
        }

        @Override
        public void onLoadMore() {
          //doLoadmore
        }
    });



### Layout


	<com.lovejjfg.powerrefresh.PowerRefreshLayout
    android:id="@+id/refresh_layout"
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    >
	    <android.support.v7.widget.RecyclerView
	        android:id="@+id/recycler"
	        xmlns:android="http://schemas.android.com/apk/res/android"
	        xmlns:tools="http://schemas.android.com/tools"
	        android:layout_width="match_parent"
	        android:layout_height="match_parent"
	        tools:context="com.lovejjfg.demo.MainActivity">
	    </android.support.v7.widget.RecyclerView>

	</com.lovejjfg.powerrefresh.PowerRefreshLayout>

### Tips 

* If you want to refresh when first init call  `setAutoRefresh(true)` on MainThread.
* If you don't want to add any Footer,but loadmore, you should call `setAutoRefresh(true)` ,and at last, you should call `setLoadEnable(mAdapter.getList().size() < 50)` to make RefreshLayout  konw whether is there any more nor not.

* Refresh finish,you should call `stopRefresh(boolean isSuccess)` to make RefreshLayout  konw that refresh is over,then the header will disappeared as soon as possible. **if you want header disappeare delay, you can call** `stopRefresh(boolean isSuccess, long delay)`

* which height should go to start refresh ? If height is not specified,the default is the header's height. you can return the height by impl `HeaderListener`:

	    @Override
	    public int getRefreshHeight() {
	        return (int) (getMeasuredHeight()*0.8f);
	    }

* If there is only one header or footer in you project ,dont forget to extend PowerRefreshLoayout and add header or footer once.

