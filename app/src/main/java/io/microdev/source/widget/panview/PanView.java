package io.microdev.source.widget.panview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

import io.microdev.source.R;
import io.microdev.source.util.DimenUtil;

public class PanView extends FrameLayout {

    private static final long SCROLL_CHANGE_EXPIRATION = 200000000l;

    private boolean fillViewportHeight;
    private boolean fillViewportWidth;

    private boolean showScrollbarHorizontal;
    private boolean showScrollbarVertical;

    private HorizontalScrollView scrollViewX;
    private ScrollView scrollViewY;

    private ScrollbarView scrollbarView;

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
                scrollbarView.postInvalidate();
            }

        };

        scrollViewY = new ScrollView(getContext()) {

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

                // Tell the scrollbar view to redraw
                scrollbarView.postInvalidate();
            }

        };

        scrollbarView = new ScrollbarView(getContext());

        fillViewportHeight = false;
        fillViewportWidth = false;

        showScrollbarHorizontal = true;
        showScrollbarVertical = true;
    }

    private void handleAttrs(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        // Get styled attributes array
        TypedArray styledAttrs = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.PanView, defStyleAttr, defStyleRes);

        fillViewportHeight = styledAttrs.getBoolean(R.styleable.PanView_fillViewportHeight, fillViewportHeight);
        fillViewportWidth = styledAttrs.getBoolean(R.styleable.PanView_fillViewportWidth, fillViewportWidth);

        showScrollbarHorizontal = styledAttrs.getBoolean(R.styleable.PanView_showScrollbarHorizontal, showScrollbarHorizontal);
        showScrollbarVertical = styledAttrs.getBoolean(R.styleable.PanView_showScrollbarVertical, showScrollbarVertical);

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

    public boolean isShowScrollbarHorizontal() {
        return showScrollbarHorizontal;
    }

    public void setShowScrollbarHorizontal(boolean showScrollbarHorizontal) {
        this.showScrollbarHorizontal = showScrollbarHorizontal;
    }

    public boolean isShowScrollbarVertical() {
        return showScrollbarVertical;
    }

    public void setShowScrollbarVertical(boolean showScrollbarVertical) {
        this.showScrollbarVertical = showScrollbarVertical;
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
        addView(scrollbarView);
    }

    private class ScrollbarView extends FrameLayout {

        public ScrollbarView(Context context) {
            super(context);

            // Expand to whatever we're in
            setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            // Transparent background
            setBackgroundColor(0);
        }

        @Override
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            // Get reference to child
            View child = scrollViewY.getChildAt(0);

            // Calculate dimensions of horizontal scrollbar
            float barHorizontalLeft = (float) getWidth() * (float) scrollViewX.getScrollX() / (float) child.getWidth();
            float barHorizontalTop = (float) getBottom() - DimenUtil.dpToPx(getContext(), 3f);
            float barHorizontalRight = (float) getWidth() / (float) child.getWidth() * ((float) scrollViewX.getScrollX() + (float) scrollViewX.getWidth());
            float barHorizontalBottom = (float) getBottom() - DimenUtil.dpToPx(getContext(), 1f);

            // Calculate dimensions of vertical scrollbar
            float barVerticalLeft = (float) getRight() - DimenUtil.dpToPx(getContext(), 3f);
            float barVerticalTop = (float) getHeight() * (float) scrollViewY.getScrollY() / (float) child.getHeight();
            float barVerticalRight = (float) getRight() - DimenUtil.dpToPx(getContext(), 1f);
            float barVerticalBottom = (float) getHeight() / (float) child.getHeight() * ((float) scrollViewY.getScrollY() + (float) scrollViewY.getHeight());

            // FIXME: Do something with this paint
            Paint p = new Paint();
            p.setColor(0);
            p.setAlpha(127);

            // Draw scrollbars
            canvas.drawRect(barHorizontalLeft, barHorizontalTop, barHorizontalRight, barHorizontalBottom, p);
            canvas.drawRect(barVerticalLeft, barVerticalTop, barVerticalRight, barVerticalBottom, p);
        }

    }

}
