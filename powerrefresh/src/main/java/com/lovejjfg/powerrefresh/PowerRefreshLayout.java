package com.lovejjfg.powerrefresh;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

/**
 * Created by Joe on 2017/4/16.
 * Email lovejjfg@gmail.com
 */

public class PowerRefreshLayout extends ViewGroup implements NestedScrollingParent, NestedScrollingChild {
    private static final String TAG = PowerRefreshLayout.class.getSimpleName();
    //操作状态  -1是默认的状态   0刷新   1加载
    private static final int STATE_DEFAULT = -1;
    private static final int STATE_REFRESH = 11;
    private static final int STATE_LOADMORE = 12;
    //头部布局
    public View header;
    //头部content
    public View headerContent;
    //底部布局
    public View footer;
    //头部下拉监听接口
    @Nullable
    public OnHeaderListener mOnHeaderListener;
    //底部上啦监听接口
    @Nullable
    public OnFooterListener mOnFooterListener;
    // 当滚动到内容最底部时Y轴所需要的滑动值
    public int bottomScroll;
    // 最后一个childview的index
    public int lastChildIndex;

    private View mTarget; // the target of the gesture

    // 事件监听接口
    private OnRefreshListener listener;
    // Layout状态
    private RefreshStatus refreshStatus = RefreshStatus.DEFAULT;
    private RefreshStatus loadStatus = RefreshStatus.DEFAULT;
    //阻尼系数
    private float damp = 0.5f;
    //恢复动画的执行时间
    public int SCROLL_TIME = 300;
    //是否刷新完成
    private boolean isRefreshSuccess = false;
    //是否加载完成
    private boolean isLoadSuccess = false;
    //正在加载中
    public boolean isLoading = false;
    //正在刷新中
    public boolean isRefreshing = false;
    //是否自动下拉刷新
    private boolean isAutoRefresh = false;
    //是否自动加载更多
    private boolean isAutoLoad = false;

    private int actionStatus = STATE_DEFAULT;

    //是否可以加载更多
    public boolean isCanLoad = true;
    //是否可以下拉刷新
    public boolean isCanRefresh = true;

    private final NestedScrollingParentHelper mNestedScrollingParentHelper;
    private final NestedScrollingChildHelper mNestedScrollingChildHelper;
    private OnChildScrollUpCallback mChildScrollUpCallback;

    private final int[] mParentScrollConsumed = new int[2];
    private final int[] mParentOffsetInWindow = new int[2];
    private int mTotalUnconsumed;
    private int mTotalUnconsumedLoadMore;
    private boolean mNestedScrollInProgress;
    private int footHeight;
    private int headerHeight;

    public PowerRefreshLayout(Context context) {
        this(context, null);
    }

    public PowerRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
    }


    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes);
        // Dispatch up to the nested parent
        startNestedScroll(axes & ViewCompat.SCROLL_AXIS_VERTICAL);
        mTotalUnconsumed = 0;
        mTotalUnconsumedLoadMore = 0;
        mNestedScrollInProgress = true;
    }


    // NestedScrollingChild

    @Override
    public void setEnabled(boolean enabled) {
        setNestedScrollingEnabled(enabled);
        super.setEnabled(enabled);
    }

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mNestedScrollingChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mNestedScrollingChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mNestedScrollingChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        mNestedScrollingChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mNestedScrollingChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
                                        int dyUnconsumed, int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed,
                dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX,
                                    float velocityY) {
        return dispatchNestedPreFling(velocityX, velocityY);
    }


    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mNestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mNestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }


    @Override
    public int getNestedScrollAxes() {
        return mNestedScrollingParentHelper.getNestedScrollAxes();
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return (nestedScrollAxes & 2) != 0;
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        // If we are in the middle of consuming, a scroll, then we want to move the spinner back up
        // before allowing the list to scroll
        Log.e(TAG, "onNestedPreScroll: dy::" + dy);
        if (header != null && !isRefreshing && actionStatus == STATE_REFRESH && dy > 0 && mTotalUnconsumed > 0) {
//            if (dy > mTotalUnconsumed) {
//                consumed[1] = dy - (int) mTotalUnconsumed;
//                mTotalUnconsumed = 0;
//            } else {
//                mTotalUnconsumed -= dy;
//                consumed[1] = dy;
//            }
//            moveSpinner(mTotalUnconsumed);
//            if(actionStatus==-1)actionStatus=0;
            mTotalUnconsumed -= dy;
            goToRefresh(-dy);
            consumed[1] = dy;
        }

        if (dy < 0 && mTotalUnconsumedLoadMore > 0 && footer != null && actionStatus == STATE_LOADMORE) {
            mTotalUnconsumedLoadMore += dy;
            goToLoad(dy);
            consumed[1] = dy;
        }


        // If a client layout is using a custom start position for the circle
        // view, they mean to hide it again before scrolling the child view
        // If we get back to mTotalUnconsumed == 0 and there is more to go, hide
        // the circle so it isn't exposed if its blocking content is moved


        // Now let our nested parent consume the leftovers
        final int[] parentConsumed = mParentScrollConsumed;
        if (dispatchNestedPreScroll(dx - consumed[0], dy - consumed[1], parentConsumed, null)) {
            consumed[0] += parentConsumed[0];
            consumed[1] += parentConsumed[1];
        }
    }


    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed,
                               int dxUnconsumed, int dyUnconsumed) {
        // Dispatch up to the nested parent first
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                mParentOffsetInWindow);

        final int dy = dyUnconsumed + mParentOffsetInWindow[1];
        Log.e(TAG, "onNestedScroll: dy::" + dy);
        if (header != null && dy < 0 && !isRefreshing && !canChildScrollUp()) {
            mTotalUnconsumed += Math.abs(dy);
            if (actionStatus == STATE_DEFAULT) actionStatus = STATE_REFRESH;
            Log.e(TAG, "onNestedScroll:mTotalUnconsumed:: " + mTotalUnconsumed);
            goToRefresh(Math.abs(dy));
        }

        if ((isAutoLoad || footer != null) && dy > 0 && !isLoading && mTotalUnconsumedLoadMore <= 4 * footHeight) {
            mTotalUnconsumedLoadMore += dy;
            if (actionStatus == STATE_DEFAULT) actionStatus = STATE_LOADMORE;
            goToLoad(dy);
        }

    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return dispatchNestedFling(velocityX, velocityY, consumed);
    }


    @Override
    public void onStopNestedScroll(View target) {
        Log.e(TAG, "onStopNestedScroll: ");
        mNestedScrollingParentHelper.onStopNestedScroll(target);
        mNestedScrollInProgress = false;
        // Finish the spinner for nested scrolling if we ever consumed any
        // unconsumed nested scroll
//        if (mTotalUnconsumed > 0) {
//            finishSpinner(mTotalUnconsumed);
        // 判断本次触摸系列事件结束时,Layout的状态
        switch (refreshStatus) {
            //下拉刷新
            case REFRESH_BEFORE:
                scrollToDefaultStatus(RefreshStatus.REFRESH_CANCEL);
                break;
            case REFRESH_AFTER:
                scrollToRefreshStatus();
                break;
            //上拉加载更多
            case LOAD_BEFORE:
                scrollToDefaultStatus(RefreshStatus.LOAD_CANCEL);
                break;
            case LOAD_AFTER:
                scrolltoLoadStatus();
                break;
            default:
                actionStatus = STATE_DEFAULT;
                break;
        }
        mTotalUnconsumed = 0;
        mTotalUnconsumedLoadMore = 0;
//        }
        // Dispatch up our nested parent
        stopNestedScroll();
    }

    /**
     * 设置头部监听回调
     */
    public void setOnHeaderListener(OnHeaderListener mOnHeaderListener) {
        this.mOnHeaderListener = mOnHeaderListener;
    }

    /**
     * 设置底部监听回调
     */
    public void setOnFooterListener(OnFooterListener mOnFooterListener) {
        this.mOnFooterListener = mOnFooterListener;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //获取最后一个子view的index 等于所有子view的数量-1
        lastChildIndex = getChildCount() - 1;
    }

    /**
     * 添加上拉刷新布局作为header
     *
     * @param header 头布局
     */
    public void addHeader(View header) {
        this.header = header;
        headerContent = header; //=header.findViewById(R.id.pull_to_refresh_text);
//        if(headerContent==null)headerContent=header;
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        addView(header, layoutParams);
    }

    /**
     * 添加下拉加载布局作为footer
     *
     * @param footer 底布局
     */
    public void addFooter(View footer) {
        this.footer = footer;
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        addView(footer, layoutParams);
    }

    /**
     * 测量方法  遍历左右子view进行测量  当子view显示状态为GONE的时候不测量
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mTarget == null) {
            ensureTarget();
        }
        if (mTarget == null) {
            return;
        }
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // 重置(避免重复累加)
        int contentHeight = 0;
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        if (mTarget == null) {
            ensureTarget();
        }
        if (mTarget == null) {
            return;
        }
        View child = mTarget;
        final int childLeft = getPaddingLeft();
        final int childTop = getPaddingTop();
        final int childWidth = width - getPaddingLeft() - getPaddingRight();
        final int childHeight = height - getPaddingTop() - getPaddingBottom();
        child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
        contentHeight += child.getMeasuredHeight();
        // 计算到达内容最底部时ViewGroup的滑动距离
        if (header != null) {
            child = header;
            headerHeight = child.getMeasuredHeight();
            child.layout(0, 0 - headerHeight, child.getMeasuredWidth(), 0);
            // TODO: 2017/4/16  
//            child.layout(0, 0, child.getMeasuredWidth(), headerHeight);
        }
        if (footer != null) {
            child = footer;
            footHeight = child.getMeasuredHeight();
            child.layout(0, contentHeight, child.getMeasuredWidth(), contentHeight + footHeight);
        }
        bottomScroll = contentHeight - getMeasuredHeight();      // 遍历进行子视图的置位工作


    }

    private void ensureTarget() {
        // Don't bother getting the parent height if the parent hasn't been laid
        // out yet.
        if (mTarget == null) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (!child.equals(header) || !child.equals(footer)) {
                    mTarget = child;
                    break;
                }
            }
        }
    }


    /**
     * @return Whether it is possible for the child view of this layout to
     * scroll up. Override this if the child view is a custom view.
     */
    public boolean canChildScrollUp() {
        if (mChildScrollUpCallback != null) {
            return mChildScrollUpCallback.canChildScrollUp(this, mTarget);
        }
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTarget;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return ViewCompat.canScrollVertically(mTarget, -1) || mTarget.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mTarget, -1);
        }
    }

    public void setAutoLoadMore(boolean autoLoad) {
        isAutoLoad = autoLoad;
    }


    /**
     * Classes that wish to override {@link SwipeRefreshLayout#canChildScrollUp()} method
     * behavior should implement this interface.
     */
    public interface OnChildScrollUpCallback {
        /**
         * Callback that will be called when {@link SwipeRefreshLayout#canChildScrollUp()} method
         * is called to allow the implementer to override its behavior.
         *
         * @param parent SwipeRefreshLayout that this callback is overriding.
         * @param child  The child view of Swipe
         * @return Whether it is possible for the child view of parent layout to scroll up.
         */
        boolean canChildScrollUp(PowerRefreshLayout parent, @Nullable View child);
    }

    /**
     * Set a callback to override {@link SwipeRefreshLayout#canChildScrollUp()} method. Non-null
     * callback will return the value provided by the callback and ignore all internal logic.
     *
     * @param callback Callback that should be called when canChildScrollUp() is called.
     */
    public void setOnChildScrollUpCallback(@Nullable OnChildScrollUpCallback callback) {
        mChildScrollUpCallback = callback;
    }


    /**
     * 设置是否支持下拉刷新
     */
    public void setCanRefresh(boolean isCanRefresh) {
        this.isCanRefresh = isCanRefresh;
    }

    /**
     * 设置是否支持加载更多
     */
    public void setCanLoad(boolean isCanLoad) {
        this.isCanLoad = isCanLoad;
    }

    /**
     * 设置是否支持自动刷新
     */
    @SuppressWarnings("unused")
    public void setAutoRefresh(boolean isAutoRefresh) {
        this.isAutoRefresh = isAutoRefresh;
        autoRefresh();
    }


    /**
     * 自动刷新
     */
    public void autoRefresh() {
        if (!isAutoRefresh) return;
        isRefreshing = true;
        measureView(header);
        int end = headerContent.getMeasuredHeight();
        performAnim(0, -end, new AnimListener() {
            @Override
            public void onGoing() {
                updateStatus(RefreshStatus.REFRESH_READY);
            }

            @Override
            public void onEnd() {
                updateStatus(RefreshStatus.REFRESH_DOING);
            }
        });

    }

    /**
     * 测量view
     */
    public void measureView(View v) {
        if (v == null) {
            return;
        }
        int w = MeasureSpec.makeMeasureSpec(0,
                MeasureSpec.UNSPECIFIED);
        int h = MeasureSpec.makeMeasureSpec(0,
                MeasureSpec.UNSPECIFIED);
        v.measure(w, h);
    }

    /**
     * 设置接口回调
     */
    public void setOnRefreshListener(OnRefreshListener listener) {
        this.listener = listener;
    }

    /**
     * 去刷新
     */
    private void goToRefresh(int dy) {
        if (actionStatus == STATE_REFRESH) {
            performScroll(dy);
            if (Math.abs(getScrollY()) > headerContent.getMeasuredHeight()) {
                updateStatus(RefreshStatus.REFRESH_AFTER);
            } else {
                updateStatus(RefreshStatus.REFRESH_BEFORE);
            }
        }
    }

    /**
     * 去加载
     */
    private void goToLoad(int dy) {
//        if (footer == null) {
//            return;
//        }
        if (actionStatus == STATE_LOADMORE) {
            // 进行Y轴上的滑动
            performScroll(-dy);
            if (getScrollY() >= bottomScroll + footHeight) {
                updateStatus(RefreshStatus.LOAD_AFTER);
            } else {
                updateStatus(RefreshStatus.LOAD_BEFORE);
            }
        }
    }


    /**
     * 刷新状态
     */
    private void updateStatus(RefreshStatus status) {
        this.refreshStatus = status;
        int scrollY = getScrollY();
        // 判断本次触摸系列事件结束时,Layout的状态
        switch (status) {
            //默认状态
            case DEFAULT:
                onDefault();
                break;
            //下拉刷新
            case REFRESH_BEFORE:
                if (mOnHeaderListener != null) {
                    mOnHeaderListener.onRefreshBefore(scrollY, headerContent.getMeasuredHeight(), headerHeight);
                }
                break;
            //松手刷新
            case REFRESH_AFTER:
                if (mOnHeaderListener != null) {
                    mOnHeaderListener.onRefreshAfter(scrollY, headerContent.getMeasuredHeight(), headerHeight);
                }
                break;
            //准备刷新
            case REFRESH_READY:
                if (mOnHeaderListener != null) {
                    mOnHeaderListener.onRefreshReady(scrollY, headerContent.getMeasuredHeight(), headerHeight);
                }
                break;
            //刷新中
            case REFRESH_DOING:
                if (mOnHeaderListener != null) {
                    mOnHeaderListener.onRefreshing(scrollY, headerContent.getMeasuredHeight(), headerHeight);
                }
                if (listener != null)
                    listener.onRefresh();
                break;
            //刷新完成
            case REFRESH_COMPLETE:
                if (mOnHeaderListener != null) {
                    mOnHeaderListener.onRefreshComplete(scrollY, headerContent.getMeasuredHeight(), headerHeight, isRefreshSuccess);
                }
                break;
            //取消刷新
            case REFRESH_CANCEL:
                if (mOnHeaderListener != null) {
                    mOnHeaderListener.onRefreshCancel(scrollY, headerContent.getMeasuredHeight(), headerHeight);
                }
                break;
            //上拉加载更多
            case LOAD_BEFORE:
                if (mOnFooterListener != null) {
                    mOnFooterListener.onLoadBefore(scrollY);
                }
                break;
            //松手加载
            case LOAD_AFTER:
                if (mOnFooterListener != null) {
                    mOnFooterListener.onLoadAfter(scrollY);
                }
                break;
            //准备加载
            case LOAD_READY:
                if (mOnFooterListener != null) {
                    mOnFooterListener.onLoadReady(scrollY);
                }
                break;
            //加载中
            case LOAD_DOING:
                if (mOnFooterListener != null) {
                    mOnFooterListener.onLoading(scrollY);
                }
                if (listener != null)
                    listener.onLoadMore();
                break;
            //加载完成
            case LOAD_COMPLETE:
                if (mOnFooterListener != null) {
                    mOnFooterListener.onLoadComplete(scrollY, isLoadSuccess);
                }
                break;
            //取消加载
            case LOAD_CANCEL:
                if (mOnFooterListener != null) {
                    mOnFooterListener.onLoadCancel(scrollY);
                }
                break;
        }
    }

    /**
     * 默认状态
     */
    private void onDefault() {
        isRefreshSuccess = false;
        isLoadSuccess = false;
    }

    /**
     * 滚动到加载状态
     */
    private void scrolltoLoadStatus() {
        isLoading = true;
        int start = getScrollY();
        int end = footHeight + bottomScroll;
        performAnim(start, end, new AnimListener() {
            @Override
            public void onGoing() {
                updateStatus(RefreshStatus.LOAD_READY);
            }

            @Override
            public void onEnd() {
                updateStatus(RefreshStatus.LOAD_DOING);
            }
        });
    }

    /**
     * 滚动到刷新状态
     */
    private void scrollToRefreshStatus() {
        isRefreshing = true;
        int start = getScrollY();
        int end = -headerContent.getMeasuredHeight();
        performAnim(start, end, new AnimListener() {
            @Override
            public void onGoing() {
                updateStatus(RefreshStatus.REFRESH_READY);
            }

            @Override
            public void onEnd() {
                updateStatus(RefreshStatus.REFRESH_DOING);
            }
        });
    }

    /**
     * 滚动到默认状态
     */
    private void scrollToDefaultStatus(final RefreshStatus startStatus) {
        int start = getScrollY();
        int end = 0;
        performAnim(start, end, new AnimListener() {
            @Override
            public void onGoing() {
                updateStatus(startStatus);
            }

            @Override
            public void onEnd() {
                updateStatus(RefreshStatus.DEFAULT);
            }
        });
    }

    /**
     * 停止刷新
     */
    public void stopRefresh(boolean isSuccess) {
        isRefreshSuccess = isSuccess;
        isRefreshing = false;
        scrollToDefaultStatus(RefreshStatus.REFRESH_COMPLETE);
    }

    /**
     * 停止加载更多
     */
    public void stopLoadMore(boolean isSuccess) {
        isLoadSuccess = isSuccess;
        isLoading = false;
        scrollToDefaultStatus(RefreshStatus.LOAD_COMPLETE);
    }

    /**
     * 执行滑动
     */
    public void performScroll(int dy) {
        Log.e(TAG, "onRefreshBefore: " + dy);
        scrollBy(0, (int) (-dy * damp));
        // TODO: 2017/4/16
//        mTarget.setTranslationY(-dy * damp);

    }

    /**
     * 执行动画
     */
    private void performAnim(int start, int end, final AnimListener listener) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.setDuration(SCROLL_TIME).start();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                scrollTo(0, value);
                postInvalidate();
                listener.onGoing();
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                listener.onEnd();
            }
        });
    }

    interface AnimListener {
        void onGoing();

        void onEnd();
    }
}
