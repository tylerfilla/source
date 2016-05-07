package io.microdev.source.widget.panview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

import io.microdev.source.R;

public class PanView extends FrameLayout {

    private static final long SCROLL_CHANGE_EXPIRATION = 200000l;

    private HorizontalScrollView scrollH;
    private ScrollView scrollV;

    private boolean fillViewportHeight;
    private boolean fillViewportWidth;

    private boolean isScrollingX;
    private boolean isScrollingY;

    public PanView(Context context) {
        super(context);

        initialize();
        configure();
    }

    public PanView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initialize();

        // Get styled attributes array
        TypedArray styledAttrs = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PanView, 0, 0);

        fillViewportHeight = styledAttrs.getBoolean(R.styleable.PanView_fillViewportHeight, false);
        fillViewportWidth = styledAttrs.getBoolean(R.styleable.PanView_fillViewportWidth, false);

        // Recycle styled attributes array
        styledAttrs.recycle();

        configure();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PanView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        initialize();

        // Get styled attributes array
        TypedArray styledAttrs = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PanView, 0, 0);

        fillViewportHeight = styledAttrs.getBoolean(R.styleable.PanView_fillViewportHeight, false);
        fillViewportWidth = styledAttrs.getBoolean(R.styleable.PanView_fillViewportWidth, false);

        // Recycle styled attributes array
        styledAttrs.recycle();

        configure();
    }

    private void initialize() {
        scrollH = new HorizontalScrollView(getContext()) {

            private long timeLastScrollChange;

            @Override
            public boolean onInterceptTouchEvent(MotionEvent event) {
                // If scroll has expired
                if (timeLastScrollChange > SCROLL_CHANGE_EXPIRATION) {
                    isScrollingX = false;
                }

                return true;
            }

            @Override
            public boolean onTouchEvent(MotionEvent event) {
                // Send event to super for scroll behavior
                super.onTouchEvent(event);

                // Offset the touch location to account for horizontal scroll
                event.offsetLocation(getScrollX() - getLeft(), 0f);

                // Send event to vertical scroll view
                scrollV.dispatchTouchEvent(event);

                // Always consider events handled
                return true;
            }

            @Override
            protected void onScrollChanged(int l, int t, int oldl, int oldt) {
                super.onScrollChanged(l, t, oldl, oldt);

                // Set X scroll flag
                isScrollingX = true;

                // Store time of this scroll change
                timeLastScrollChange = System.nanoTime();
            }

        };
        scrollV = new ScrollView(getContext()) {

            private long timeLastScrollChange;

            @Override
            public boolean onInterceptTouchEvent(MotionEvent event) {
                // If scroll has expired
                if (timeLastScrollChange > SCROLL_CHANGE_EXPIRATION) {
                    isScrollingY = false;
                }

                // If scrolling or should start scrolling, intercept the event
                return isScrollingX || isScrollingY || super.onInterceptTouchEvent(event);
            }

            @Override
            protected void onScrollChanged(int l, int t, int oldl, int oldt) {
                super.onScrollChanged(l, t, oldl, oldt);

                // Set Y scroll flag
                isScrollingY = true;

                // Store time of this scroll change
                timeLastScrollChange = System.nanoTime();
            }

        };

        fillViewportHeight = false;
        fillViewportWidth = false;
    }

    private void configure() {
        // Disable native scrollbars
        scrollH.setHorizontalScrollBarEnabled(false);
        scrollV.setVerticalScrollBarEnabled(false);

        // Fill viewport for each axis
        scrollH.setFillViewport(fillViewportWidth);
        scrollV.setFillViewport(fillViewportHeight);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        // Enforce number of children
        if (getChildCount() != 1) {
            if (getChildCount() > 1) {
                throw new IllegalStateException("PanView can only host one direct child");
            }

            return;
        }

        // Splice scroll views between this view and its child
        View child = getChildAt(0);
        removeAllViews();
        scrollV.addView(child);
        scrollH.addView(scrollV);
        addView(scrollH);
    }

}
