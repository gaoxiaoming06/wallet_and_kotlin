package com.xlqianbao.and.view.refresh;


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.handmark.pulltorefresh.library.PullToRefreshListView;

/**
 * 解决viewpager跟refresh_view的事件冲突
 */
public class RefreshProgressLayoutForListView extends RefreshProgressLayout {

    private PullToRefreshListView mPullToRefreshListView;

    public RefreshProgressLayoutForListView(Context context) {
        super(context);
    }

    public RefreshProgressLayoutForListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setChildListView(PullToRefreshListView view) {
        this.mPullToRefreshListView = view;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int firstVisiblePosition = mPullToRefreshListView == null ? 0 : mPullToRefreshListView.getRefreshableView().getFirstVisiblePosition();
        if (firstVisiblePosition == 0) {
            return super.onInterceptTouchEvent(ev);
        } else {
            return false;
        }

    }
}


