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

package com.lovejjfg.powerrefresh;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;

@SuppressWarnings("unused")
public class PowerRefreshLayout extends ViewGroup implements NestedScrollingParent, NestedScrollingChild {
    private static final String TAG = PowerRefreshLayout.class.getSimpleName();
    private static final int STATE_DEFAULT = -1;
    private static final int STATE_REFRESH = 11;
    private static final int STATE_LOADMORE = 12;
    public View header;
    public View footer;
    @Nullable
    public HeaderListener mHeaderListener;
    @Nullable
    public FooterListener mFooterListener;
    public int bottomScroll;
    private View mTarget; // the target of the gesture
    private OnRefreshListener listener;
    private RefreshStatus refreshStatus = RefreshStatus.DEFAULT;
    private static final float DRAG_RATE = 0.5f;
    private static final int INVALID_POINTER = -1;
    public int ANIMATION_DURATION = 300;
    private boolean isRefreshSuccess = false;
    private boolean isLoadSuccess = false;
    public boolean isLoading = false;
    public boolean isRefreshing = false;
    private boolean isAutoRefresh = false;
    private boolean isAutoLoad = false;
    private int currentStatus = STATE_DEFAULT;
    private boolean loadEnable = true;
    private boolean refreshEnable = true;

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
    private boolean mIsBeingDragged;
    private float mTouchSlop;
    private int mActivePointerId;
    private float mInitialDownY;
    private float mInitialMotionY;
    private Runnable refreshAction;

    public PowerRefreshLayout(Context context) {
        this(context, null);
    }

    public PowerRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        ensureTarget();
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
        if (refreshEnable && header != null && !isRefreshing && currentStatus == STATE_REFRESH && dy > 0 && mTotalUnconsumed > 0) {
            mTotalUnconsumed -= dy;

            if (mTotalUnconsumed <= 0) {//over
                mTotalUnconsumed = 0;
                dy = (int) (-getScrollY() / DRAG_RATE);
            }
            goToRefresh(-dy);
            consumed[1] = dy;
        }

        if (loadEnable && !isLoading && footer != null && dy < 0 && getScrollY() >= bottomScroll && mTotalUnconsumedLoadMore > 0 && currentStatus == STATE_LOADMORE) {
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
        if (refreshEnable && header != null && dy < 0 && !isRefreshing && !canChildScrollUp()) {
            mTotalUnconsumed += Math.abs(dy);
            if (currentStatus == STATE_DEFAULT || mTotalUnconsumed != 0)
                currentStatus = STATE_REFRESH;
            goToRefresh(Math.abs(dy));
        }

        if (loadEnable && (isAutoLoad || footer != null) && getScrollY() >= bottomScroll && dy > 0 && !isLoading && mTotalUnconsumedLoadMore <= 4 * footHeight) {
            mTotalUnconsumedLoadMore += dy;
            if (currentStatus == STATE_DEFAULT || mTotalUnconsumedLoadMore != 0)
                currentStatus = STATE_LOADMORE;
            goToLoad(dy);
        }

    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return dispatchNestedFling(velocityX, velocityY, consumed);
    }


    @Override
    public void onStopNestedScroll(View target) {
        mNestedScrollingParentHelper.onStopNestedScroll(target);
        mNestedScrollInProgress = false;
        // Finish the spinner for nested scrolling if we ever consumed any
        // unconsumed nested scroll
//        if (mTotalUnconsumed > 0) {
//            finishSpinner(mTotalUnconsumed);
        resetScroll();

//        }
        // Dispatch up our nested parent
        stopNestedScroll();
    }

    private void resetScroll() {
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
                scrollToLoadStatus();
                break;
            default:
                currentStatus = STATE_DEFAULT;
                break;
        }
        mTotalUnconsumed = 0;
        mTotalUnconsumedLoadMore = 0;
    }

    /**
     * Call this method to add {@link HeaderListener} ,It's better to make the {@link #header} to impl.
     */
    public void setOnHeaderListener(HeaderListener mHeaderListener) {
        this.mHeaderListener = mHeaderListener;
    }

    /**
     * Call this method to add {@link FooterListener} ,It's better to make the {@link #footer} to impl.
     */
    public void setOnFooterListener(FooterListener mFooterListener) {
        this.mFooterListener = mFooterListener;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ensureTarget();
    }

    /**
     * add the refresh view.
     */
    public void addHeader(@NonNull View header) {
        this.header = header;
        if (header instanceof HeaderListener) {
            mHeaderListener = (HeaderListener) header;
        }
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        addView(header, layoutParams);
    }

    /**
     * add the footer view.
     */
    public void addFooter(@NonNull View footer) {
        this.footer = footer;
        if (footer instanceof FooterListener) {
            mFooterListener = (FooterListener) footer;
        }
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        addView(footer, layoutParams);
    }

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
        int contentHeight = 0;
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int childLeft = getPaddingLeft();
        int childRight = getPaddingLeft();
        int childTop = getPaddingTop();
        int childWidth = width - childLeft - childRight;
        int childHeight;
        View child;
        if (header != null) {
            child = header;
            headerHeight = child.getMeasuredHeight();
            child.layout(childLeft, childTop - headerHeight, childLeft + childWidth, childTop);
        }
        //make sure there is the target!
        if (mTarget == null) {
            ensureTarget();
        }
        if (mTarget == null) {
            return;
        }
        child = mTarget;
        childLeft = getPaddingLeft();
        childTop = getPaddingTop();
        childWidth = width - getPaddingLeft() - getPaddingRight();
        childHeight = height - getPaddingTop() - getPaddingBottom();
        child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
        contentHeight += child.getMeasuredHeight();

        if (footer != null) {
            child = footer;
            footHeight = child.getMeasuredHeight();
            child.layout(0, contentHeight, child.getMeasuredWidth(), contentHeight + footHeight);
        }
        bottomScroll = contentHeight - getMeasuredHeight();


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

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        ensureTarget();

        final int action = MotionEventCompat.getActionMasked(ev);
        int pointerIndex;


        if (!isEnabled() || isRefreshing || isLoading || canChildScrollUp()
                || mNestedScrollInProgress) {
            // Fail fast if we're not in a state where a swipe is possible
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = ev.getPointerId(0);
                mIsBeingDragged = false;

                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                mInitialDownY = ev.getY(pointerIndex);
                break;

            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == INVALID_POINTER) {
                    return false;
                }

                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                final float y = ev.getY(pointerIndex);
                startDragging(y);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                break;
        }

        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        int pointerIndex;


        if (!isEnabled() || canChildScrollUp()
                || isLoading || isRefreshing || mNestedScrollInProgress) {
            // Fail fast if we're not in a state where a swipe is possible
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = ev.getPointerId(0);
                mIsBeingDragged = false;
                break;

            case MotionEvent.ACTION_MOVE: {
                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }

                final float y = ev.getY(pointerIndex);
                startDragging(y);
                //in this case ,just can refresh.
                if (mIsBeingDragged) {
                    final float overscrollTop = (y - mInitialMotionY);
                    if (overscrollTop < 0 && getScrollY() > 0) {
                        ev.setAction(MotionEvent.ACTION_CANCEL);
                        return false;
                    }
                    currentStatus = STATE_REFRESH;
                    goToRefresh((int) overscrollTop);
                }
                mInitialMotionY = y;
                break;
            }
            case MotionEventCompat.ACTION_POINTER_DOWN: {
                pointerIndex = MotionEventCompat.getActionIndex(ev);
                if (pointerIndex < 0) {
                    return false;
                }
                mActivePointerId = ev.getPointerId(pointerIndex);
                break;
            }

//            case MotionEventCompat.ACTION_POINTER_UP:
//                onSecondaryPointerUp(ev);
//                break;

            case MotionEvent.ACTION_UP: {
                pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }

                if (mIsBeingDragged) {
                    mIsBeingDragged = false;
                    resetScroll();
                }
                mActivePointerId = INVALID_POINTER;
                return false;
            }
            case MotionEvent.ACTION_CANCEL:
                resetScroll();
                return false;
        }

        return true;
    }


    @SuppressLint("NewApi")
    private void startDragging(float y) {
        final float yDiff = y - mInitialDownY;
        if (yDiff > mTouchSlop && !mIsBeingDragged) {
            mInitialMotionY = mInitialDownY + mTouchSlop;
            mIsBeingDragged = true;
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
    @SuppressWarnings("WeakerAccess")
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


    public void setRefreshEnable(boolean isCanRefresh) {
        this.refreshEnable = isCanRefresh;
    }

    public void setLoadEnable(boolean isCanLoad) {
        this.loadEnable = isCanLoad;
    }

    /**
     * call this method to auto refresh when first init.
     *
     * @param isAutoRefresh true go to refresh ,false otherwise.
     */
    public void setAutoRefresh(boolean isAutoRefresh) {
        this.isAutoRefresh = isAutoRefresh;
        autoRefresh();
    }

    private void autoRefresh() {
        if (!isAutoRefresh) return;
        isRefreshing = true;
        measureView(header);
        int refreshHeight = 0;
        if (mHeaderListener != null) {
            refreshHeight = mHeaderListener.getRefreshHeight();
        }
        int end = refreshHeight != 0 ? refreshHeight : headerHeight;
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

    public void autoLoad() {
        if (!isAutoLoad) return;
        isAutoLoad = true;
        int end = footHeight;
        performAnim(bottomScroll, bottomScroll + end, new AnimListener() {
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


    public void measureView(View v) {
        if (v == null) {
            return;
        }
        int w = MeasureSpec.makeMeasureSpec(0,
                MeasureSpec.UNSPECIFIED);
        int h = MeasureSpec.makeMeasureSpec(0,
                MeasureSpec.UNSPECIFIED);
        v.measure(w, h);
        if (v instanceof HeaderListener && headerHeight == 0) {
            headerHeight = v.getMeasuredHeight();
        }
    }

    /**
     * Set the listener to be notified when a refresh is triggered via the swipe
     * gesture.
     */
    public void setOnRefreshListener(OnRefreshListener listener) {
        this.listener = listener;
    }

    private void goToRefresh(int dy) {
        int scrollY = getScrollY();
        if (currentStatus == STATE_REFRESH) {
            performScroll(dy);
            int end = mHeaderListener != null && mHeaderListener.getRefreshHeight() != 0 ? mHeaderListener.getRefreshHeight() : headerHeight;
            if (Math.abs(scrollY) > end) {
                updateStatus(RefreshStatus.REFRESH_AFTER);
            } else {
                updateStatus(RefreshStatus.REFRESH_BEFORE);
            }
        }
    }

    private void goToLoad(int dy) {
        if (footer == null && !isAutoLoad) {
            return;
        }
        if (currentStatus == STATE_LOADMORE) {
            performScroll(-dy);
            if (getScrollY() >= bottomScroll + footHeight) {
                updateStatus(RefreshStatus.LOAD_AFTER);
            } else {
                updateStatus(RefreshStatus.LOAD_BEFORE);
            }
        }
    }


    private void updateStatus(RefreshStatus status) {
        this.refreshStatus = status;
        int scrollY = getScrollY();
        switch (status) {
            case DEFAULT:
                onDefault();
                break;
            case REFRESH_BEFORE:
                if (mHeaderListener != null) {
                    mHeaderListener.onRefreshBefore(scrollY, headerHeight);
                }
                break;
            case REFRESH_AFTER:
                if (mHeaderListener != null) {
                    mHeaderListener.onRefreshAfter(scrollY, headerHeight);
                }
                break;
            case REFRESH_READY:
                if (mHeaderListener != null) {
                    mHeaderListener.onRefreshReady(scrollY, headerHeight);
                }
                break;
            case REFRESH_DOING:
                if (mHeaderListener != null) {
                    mHeaderListener.onRefreshing(scrollY, headerHeight);
                }
                if (listener != null)
                    listener.onRefresh();
                break;
            case REFRESH_COMPLETE:
                if (mHeaderListener != null) {
                    mHeaderListener.onRefreshComplete(scrollY, headerHeight, isRefreshSuccess);
                }
                break;
            case REFRESH_CANCEL:
                if (mHeaderListener != null) {
                    mHeaderListener.onRefreshCancel(scrollY, headerHeight);
                }
                break;
            case LOAD_BEFORE:
                if (mFooterListener != null) {
                    mFooterListener.onLoadBefore(scrollY);
                }
                break;
            case LOAD_AFTER:
                if (mFooterListener != null) {
                    mFooterListener.onLoadAfter(scrollY);
                }
                break;
            case LOAD_READY:
                if (mFooterListener != null) {
                    mFooterListener.onLoadReady(scrollY);
                }
                break;
            case LOAD_DOING:
                if (mFooterListener != null) {
                    mFooterListener.onLoading(scrollY);
                }
                if (listener != null)
                    listener.onLoadMore();
                break;
            case LOAD_COMPLETE:
                if (mFooterListener != null) {
                    mFooterListener.onLoadComplete(scrollY, isLoadSuccess);
                }
                break;
            case LOAD_CANCEL:
                if (mFooterListener != null) {
                    mFooterListener.onLoadCancel(scrollY);
                }
                break;
        }
    }

    private void onDefault() {
        isRefreshSuccess = false;
        isLoadSuccess = false;
    }

    private void scrollToLoadStatus() {
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

    private void scrollToRefreshStatus() {
        isRefreshing = true;
        int start = getScrollY();
        int end = mHeaderListener != null && mHeaderListener.getRefreshHeight() != 0 ? -mHeaderListener.getRefreshHeight() : -headerHeight;
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
     * call this method to make {@link #PowerRefreshLayout} know the refresh is over.
     * you can also see {@link #stopRefresh(boolean, long)}
     *
     * @param isSuccess true refresh successful,false otherwise.
     */
    public void stopRefresh(boolean isSuccess) {
        stopRefresh(isSuccess, 0);
    }

    /**
     * call this method to make {@link #PowerRefreshLayout} know this refresh is over.
     *
     * @param delay     The delay (in milliseconds) until head disappeared.
     * @param isSuccess True refresh successful,false otherwise.
     */
    public void stopRefresh(boolean isSuccess, long delay) {
        isRefreshSuccess = isSuccess;
        updateStatus(RefreshStatus.REFRESH_COMPLETE);
        if (refreshAction == null) {
            refreshAction = new Runnable() {
                @Override
                public void run() {
                    scrollToDefaultStatus(RefreshStatus.REFRESH_COMPLETE);
                    isRefreshing = false;
                }
            };
        }
        postDelayed(refreshAction, delay);
    }

    Runnable loadAction = new Runnable() {
        @Override
        public void run() {
            scrollToDefaultStatus(RefreshStatus.LOAD_COMPLETE);
            isLoading = false;
        }
    };

    public void stopLoadMore(boolean isSuccess) {
        stopLoadMore(isSuccess, 0);
    }

    public void stopLoadMore(boolean isSuccess, long delay) {
        isLoadSuccess = isSuccess;
        updateStatus(RefreshStatus.LOAD_COMPLETE);
        postDelayed(loadAction, delay);
    }

    public void performScroll(int dy) {
        float ddy = -dy * DRAG_RATE;
        scrollBy(0, (int) (ddy));
    }

    private void performAnim(int start, int end, final AnimListener listener) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.setDuration(ANIMATION_DURATION).start();
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
