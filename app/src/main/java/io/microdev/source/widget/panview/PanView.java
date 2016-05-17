package io.microdev.source.widget.panview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

import io.microdev.source.R;

public class PanView extends FrameLayout {

    private static final long SCROLL_CHANGE_EXPIRATION = 200000000l;
    private static final long SCROLLBAR_FADE_DELAY = 100000000l;

    private static final boolean DEF_FILL_VIEWPORT_HEIGHT = false;
    private static final boolean DEF_FILL_VIEWPORT_WIDTH = false;

    private static final boolean DEF_SCROLLBAR_HORIZONTAL_ENABLED = true;
    private static final boolean DEF_SCROLLBAR_VERTICAL_ENABLED = true;

    private static final int DEF_SCROLLBAR_HORIZONTAL_COLOR = 0x70000000;
    private static final int DEF_SCROLLBAR_VERTICAL_COLOR = 0x70000000;

    private boolean fillViewportHeight;
    private boolean fillViewportWidth;

    private HorizontalScrollView scrollViewX;
    private ScrollView scrollViewY;

    private ScrollbarLens scrollbarLens;

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
        handleAttrs(attrs, 0, 0);
        configure();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PanView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        initialize();
        handleAttrs(attrs, defStyleAttr, defStyleRes);
        configure();
    }

    private void initialize() {
        scrollViewX = new HorizontalScrollView(getContext()) {

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
                // Awaken scrollbars
                scrollbarLens.awakenScrollBars();

                // Send event to super for scroll behavior
                super.onTouchEvent(event);

                // Offset the touch location to account for horizontal scroll
                event.offsetLocation(getScrollX() - getLeft(), 0f);

                // Send event to vertical scroll view
                scrollViewY.dispatchTouchEvent(event);

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

                // Tell the scrollbar view to redraw
                scrollbarLens.postInvalidate();
            }

        };

        scrollViewY = new ScrollView(getContext()) {

            private long timeLastScrollChange;

            @Override
            public boolean onInterceptTouchEvent(MotionEvent event) {
                // Awaken scrollbars
                scrollbarLens.awakenScrollBars();

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

                // Tell the scrollbar view to redraw
                scrollbarLens.postInvalidate();
            }

        };

        scrollbarLens = new ScrollbarLens(getContext());

        fillViewportHeight = DEF_FILL_VIEWPORT_HEIGHT;
        fillViewportWidth = DEF_FILL_VIEWPORT_WIDTH;
    }

    private void handleAttrs(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        // Get styled attributes array
        TypedArray styledAttrs = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.PanView, defStyleAttr, defStyleRes);

        fillViewportHeight = styledAttrs.getBoolean(R.styleable.PanView_fillViewportHeight, fillViewportHeight);
        fillViewportWidth = styledAttrs.getBoolean(R.styleable.PanView_fillViewportWidth, fillViewportWidth);

        // Recycle styled attributes array
        styledAttrs.recycle();
    }

    private void configure() {
        // Disable native scrollbars
        scrollViewX.setHorizontalScrollBarEnabled(false);
        scrollViewY.setVerticalScrollBarEnabled(false);

        // Fill viewport for each axis
        scrollViewX.setFillViewport(fillViewportWidth);
        scrollViewY.setFillViewport(fillViewportHeight);
    }

    public boolean isFillViewportWidth() {
        return fillViewportWidth;
    }

    public void setFillViewportWidth(boolean fillViewportWidth) {
        this.fillViewportWidth = fillViewportWidth;
    }

    public boolean isFillViewportHeight() {
        return fillViewportHeight;
    }

    public void setFillViewportHeight(boolean fillViewportHeight) {
        this.fillViewportHeight = fillViewportHeight;
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
        scrollViewY.addView(child);
        scrollViewX.addView(scrollViewY);
        addView(scrollViewX);

        // Add scrollbar view
        addView(scrollbarLens);
    }

    private class ScrollbarLens extends ScrollView {

        public ScrollbarLens(Context context) {
            super(context);

            // Expand to whatever we're in
            setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            // Use a transparent background, lest we forfeit our lensiness
            setBackgroundColor(0);

            // Enable both scrollbars
            setHorizontalScrollBarEnabled(true);
            setVerticalScrollBarEnabled(true);
        }

        @Override
        public boolean awakenScrollBars() {
            return super.awakenScrollBars();
        }

        @Override
        protected int computeHorizontalScrollExtent() {
            return getWidth();
        }

        @Override
        protected int computeHorizontalScrollOffset() {
            return scrollViewX.getScrollX();
        }

        @Override
        protected int computeHorizontalScrollRange() {
            return scrollViewY.getChildAt(0).getWidth();
        }

        @Override
        protected int computeVerticalScrollExtent() {
            return getHeight();
        }

        @Override
        protected int computeVerticalScrollOffset() {
            return scrollViewY.getScrollY();
        }

        @Override
        protected int computeVerticalScrollRange() {
            return scrollViewY.getChildAt(0).getHeight();
        }

    }

}
