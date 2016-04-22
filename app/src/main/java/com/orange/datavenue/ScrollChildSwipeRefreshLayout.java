/**
 * Copyright (C) 2015 Orange
 * <p/>
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE' in this package distribution
 * or at 'http://www.apache.org/licenses/LICENSE-2.0'.
 */
package com.orange.datavenue;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author Stéphane SANDON
 */
public class ScrollChildSwipeRefreshLayout extends SwipeRefreshLayout {

    private static final String TAG_NAME = "SwipeRefresh";

    private View mScrollUpChild;

    /**
     *
     * @param context
     */
    public ScrollChildSwipeRefreshLayout(Context context) {
        super(context);
    }

    /**
     *
     * @param context
     * @param attrs
     */
    public ScrollChildSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     *
     * @return
     */
    @Override
    public boolean canChildScrollUp() {
        if (mScrollUpChild != null) {
            return ViewCompat.canScrollVertically(mScrollUpChild, -1);
        }
        return super.canChildScrollUp();
    }

    /**
     *
     * @param view
     */
    public void setScrollUpChild(View view) {
        mScrollUpChild = view;
    }
}