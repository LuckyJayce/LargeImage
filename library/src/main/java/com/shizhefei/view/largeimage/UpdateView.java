/*
Copyright 2015 shizhefei（LuckyJayce）
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
   http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.shizhefei.view.largeimage;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

public abstract class UpdateView extends View {

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public UpdateView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public UpdateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public UpdateView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UpdateView(Context context) {
        super(context);
    }

    boolean mRequestedVisible = false;
    boolean mWindowVisibility = false;
    boolean mViewVisibility = false;
    private boolean mGlobalListenersAdded;
    final WindowManager.LayoutParams mLayout = new WindowManager.LayoutParams();

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        mWindowVisibility = visibility == VISIBLE;
        mRequestedVisible = mWindowVisibility && mViewVisibility;
        // updateWindow(false, false);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        mViewVisibility = visibility == VISIBLE;
        boolean newRequestedVisible = mWindowVisibility && mViewVisibility;
        if (newRequestedVisible != mRequestedVisible) {
            // our base class (View) invalidates the layout only when
            // we go from/to the GONE state. However, SurfaceView needs
            // to request a re-layout when the visibility changes at all.
            // This is needed because the transparent region is computed
            // as part of the layout phase, and it changes (obviously) when
            // the visibility changes.
            requestLayout();
        }
        mRequestedVisible = newRequestedVisible;
        // updateWindow(false, false);
    }

    final ViewTreeObserver.OnScrollChangedListener mScrollChangedListener = new ViewTreeObserver.OnScrollChangedListener() {

        @Override
        public void onScrollChanged() {
            updateWindow(false, false);
        }
    };

    private final ViewTreeObserver.OnPreDrawListener mDrawListener = new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
            // reposition ourselves where the surface is
            // mHaveFrame = getWidth() > 0 && getHeight() > 0;
            // updateWindow(false, false);
            return true;
        }
    };

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // mParent.requestTransparentRegion(this);
        // mSession = getWindowSession();
        mLayout.token = getWindowToken();
        mLayout.setTitle("SurfaceView");
        mViewVisibility = getVisibility() == VISIBLE;

        if (!mGlobalListenersAdded) {
            ViewTreeObserver observer = getViewTreeObserver();
            observer.addOnScrollChangedListener(mScrollChangedListener);
            observer.addOnPreDrawListener(mDrawListener);
            mGlobalListenersAdded = true;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mGlobalListenersAdded) {
            ViewTreeObserver observer = getViewTreeObserver();
            observer.removeOnScrollChangedListener(mScrollChangedListener);
            observer.removeOnPreDrawListener(mDrawListener);
            mGlobalListenersAdded = false;
        }
        mRequestedVisible = false;
        updateWindow(false, false);
        mLayout.token = null;
        super.onDetachedFromWindow();
    }

    int[] mLocation = new int[2];
    boolean mVisible = false;
    int mLeft = -1;
    int mTop = -1;

    private boolean lock;

    protected void lock() {
        lock = true;
    }

    protected void unLock() {
        lock = false;
    }

    int[] tempLocationInWindow = new int[2];
    Rect tempVisibilityRect = new Rect();
    long time;

    private void updateWindow(boolean force, boolean redrawNeeded) {
        if (lock) {
            return;
        }
        long c = SystemClock.uptimeMillis();
        if (c - time < 16) {
            return;
        }
        time = c;
        getLocationInWindow(tempLocationInWindow);
        final boolean visibleChanged = mVisible != mRequestedVisible;
        if (force || visibleChanged || tempLocationInWindow[0] != mLocation[0] || tempLocationInWindow[1] != mLocation[1] || redrawNeeded) {
            this.mLocation[0] = tempLocationInWindow[0];
            this.mLocation[1] = tempLocationInWindow[1];
            getVisibilityRect(tempVisibilityRect);
            if (!mVisibilityRect.equals(tempVisibilityRect)) {
                if (!(mVisibilityRect.isEmpty() && tempVisibilityRect.isEmpty())) {
                    this.mVisibilityRect.set(tempVisibilityRect);
                    onUpdateWindow(mVisibilityRect);
                }
            }
        }
    }

    private Rect mVisibilityRect = new Rect();

    protected abstract void onUpdateWindow(Rect visibilityRect);

    int[] location = new int[2];

    protected void getVisibilityRect(Rect visibilityRect) {
        getGlobalVisibleRect(visibilityRect);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        if (visibilityRect.left < 0) {
            visibilityRect.left = 0;
        } else if (visibilityRect.left > screenWidth) {
            visibilityRect.left = screenWidth;
        }
        if (visibilityRect.right < 0) {
            visibilityRect.right = 0;
        } else if (visibilityRect.right > screenWidth) {
            visibilityRect.right = screenWidth;
        }
        if (visibilityRect.top < 0) {
            visibilityRect.top = 0;
        } else if (visibilityRect.top > screenHeight) {
            visibilityRect.top = screenHeight;
        }
        if (visibilityRect.bottom < 0) {
            visibilityRect.bottom = 0;
        } else if (visibilityRect.bottom > screenHeight) {
            visibilityRect.bottom = screenHeight;
        }

//        getLocalVisibleRect(visibilityRect);
//
//        Log.d("pppp", "index: " + index + " getGlobalVisibleRect:" + visibilityRect);

        getLocationInWindow(location);

        visibilityRect.left = visibilityRect.left - location[0];
        visibilityRect.right = visibilityRect.right - location[0];
        visibilityRect.top = visibilityRect.top - location[1];
        visibilityRect.bottom = visibilityRect.bottom - location[1];
    }

    protected int index;

    public void setIndex(int index) {
        this.index = index;
    }
}
