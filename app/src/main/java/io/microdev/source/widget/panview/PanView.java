package io.microdev.source.widget.panview;

import android.animation.Animator;
import android.animation.ValueAnimator;
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

import java.util.Timer;
import java.util.TimerTask;

import io.microdev.source.R;

import static io.microdev.source.util.DimenUtil.dpToPx;

public class PanView extends FrameLayout {

    private static final long SCROLL_CHANGE_EXPIRATION = 200000000l;
    private static final long SCROLLBAR_FADE_DELAY = 100000000l;

    private static final boolean DEF_FILL_VIEWPORT_HEIGHT = false;
    private static final boolean DEF_FILL_VIEWPORT_WIDTH = false;

    private static final boolean DEF_SCROLLBAR_HORIZONTAL_ENABLED = true;
    private static final boolean DEF_SCROLLBAR_VERTICAL_ENABLED = true;

    private static final int DEF_SCROLLBAR_HORIZONTAL_COLOR = 0x70000000;
    private static final int DEF_SCROLLBAR_VERTICAL_COLOR = 0x70000000;

    private static final float DEF_SCROLLBAR_HORIZONTAL_SIZE_DP = 2f;
    private static final float DEF_SCROLLBAR_VERTICAL_SIZE_DP = 2f;

    private static final float DEF_SCROLLBAR_HORIZONTAL_MARGIN_DP = 2f;
    private static final float DEF_SCROLLBAR_VERTICAL_MARGIN_DP = 2f;

    private boolean fillViewportHeight;
    private boolean fillViewportWidth;

    private boolean scrollbarHorizontalEnabled;
    private boolean scrollbarVerticalEnabled;

    private int scrollbarHorizontalColor;
    private int scrollbarVerticalColor;

    private float scrollbarHorizontalSize;
    private float scrollbarVerticalSize;

    private float scrollbarHorizontalMargin;
    private float scrollbarVerticalMargin;

    private HorizontalScrollView scrollViewX;
    private ScrollView scrollViewY;

    private ScrollbarView scrollbarView;

    private boolean isScrollingX;
    private boolean isScrollingY;

    private long timeLastScrollChangeX;
    private long timeLastScrollChangeY;

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

            @Override
            public boolean onInterceptTouchEvent(MotionEvent event) {
                // If scroll has expired
                if (timeLastScrollChangeX > SCROLL_CHANGE_EXPIRATION) {
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
                timeLastScrollChangeX = System.nanoTime();

                // Tell the scrollbar view to redraw
                scrollbarView.postInvalidate();
            }

        };

        scrollViewY = new ScrollView(getContext()) {

            @Override
            public boolean onInterceptTouchEvent(MotionEvent event) {
                // If scroll has expired
                if (timeLastScrollChangeY > SCROLL_CHANGE_EXPIRATION) {
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
                timeLastScrollChangeY = System.nanoTime();

                // Tell the scrollbar view to redraw
                scrollbarView.postInvalidate();
            }

        };

        scrollbarView = new ScrollbarView(getContext());

        fillViewportHeight = DEF_FILL_VIEWPORT_HEIGHT;
        fillViewportWidth = DEF_FILL_VIEWPORT_WIDTH;

        scrollbarHorizontalEnabled = DEF_SCROLLBAR_HORIZONTAL_ENABLED;
        scrollbarVerticalEnabled = DEF_SCROLLBAR_VERTICAL_ENABLED;

        scrollbarHorizontalColor = DEF_SCROLLBAR_HORIZONTAL_COLOR;
        scrollbarVerticalColor = DEF_SCROLLBAR_VERTICAL_COLOR;

        scrollbarHorizontalSize = dpToPx(getContext(), DEF_SCROLLBAR_HORIZONTAL_SIZE_DP);
        scrollbarVerticalSize = dpToPx(getContext(), DEF_SCROLLBAR_VERTICAL_SIZE_DP);

        scrollbarHorizontalMargin = dpToPx(getContext(), DEF_SCROLLBAR_HORIZONTAL_MARGIN_DP);
        scrollbarVerticalMargin = dpToPx(getContext(), DEF_SCROLLBAR_VERTICAL_MARGIN_DP);
    }

    private void handleAttrs(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        // Get styled attributes array
        TypedArray styledAttrs = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.PanView, defStyleAttr, defStyleRes);

        fillViewportHeight = styledAttrs.getBoolean(R.styleable.PanView_fillViewportHeight, fillViewportHeight);
        fillViewportWidth = styledAttrs.getBoolean(R.styleable.PanView_fillViewportWidth, fillViewportWidth);

        scrollbarHorizontalEnabled = styledAttrs.getBoolean(R.styleable.PanView_scrollbarHorizontalEnabled, scrollbarHorizontalEnabled);
        scrollbarVerticalEnabled = styledAttrs.getBoolean(R.styleable.PanView_scrollbarVerticalEnabled, scrollbarVerticalEnabled);

        scrollbarHorizontalColor = styledAttrs.getColor(R.styleable.PanView_scrollbarHorizontalColor, scrollbarHorizontalColor);
        scrollbarVerticalColor = styledAttrs.getColor(R.styleable.PanView_scrollbarVerticalColor, scrollbarVerticalColor);

        scrollbarHorizontalSize = styledAttrs.getDimension(R.styleable.PanView_scrollbarHorizontalSize, scrollbarHorizontalSize);
        scrollbarVerticalSize = styledAttrs.getDimension(R.styleable.PanView_scrollbarVerticalSize, scrollbarVerticalSize);

        scrollbarHorizontalMargin = styledAttrs.getDimension(R.styleable.PanView_scrollbarHorizontalMargin, scrollbarHorizontalMargin);
        scrollbarVerticalMargin = styledAttrs.getDimension(R.styleable.PanView_scrollbarVerticalMargin, scrollbarVerticalMargin);

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

    public boolean isScrollbarHorizontalEnabled() {
        return scrollbarHorizontalEnabled;
    }

    public void setScrollbarHorizontalEnabled(boolean scrollbarHorizontalEnabled) {
        this.scrollbarHorizontalEnabled = scrollbarHorizontalEnabled;
    }

    public boolean isScrollbarVerticalEnabled() {
        return scrollbarVerticalEnabled;
    }

    public void setScrollbarVerticalEnabled(boolean scrollbarVerticalEnabled) {
        this.scrollbarVerticalEnabled = scrollbarVerticalEnabled;
    }

    public int getScrollbarHorizontalColor() {
        return scrollbarHorizontalColor;
    }

    public void setScrollbarHorizontalColor(int scrollbarHorizontalColor) {
        this.scrollbarHorizontalColor = scrollbarHorizontalColor;
    }

    public int getScrollbarVerticalColor() {
        return scrollbarVerticalColor;
    }

    public void setScrollbarVerticalColor(int scrollbarVerticalColor) {
        this.scrollbarVerticalColor = scrollbarVerticalColor;
    }

    public float getScrollbarHorizontalSize() {
        return scrollbarHorizontalSize;
    }

    public void setScrollbarHorizontalSize(float scrollbarHorizontalSize) {
        this.scrollbarHorizontalSize = scrollbarHorizontalSize;
    }

    public float getScrollbarVerticalSize() {
        return scrollbarVerticalSize;
    }

    public void setScrollbarVerticalSize(float scrollbarVerticalSize) {
        this.scrollbarVerticalSize = scrollbarVerticalSize;
    }

    public float getScrollbarHorizontalMargin() {
        return scrollbarHorizontalMargin;
    }

    public void setScrollbarHorizontalMargin(float scrollbarHorizontalMargin) {
        this.scrollbarHorizontalMargin = scrollbarHorizontalMargin;
    }

    public float getScrollbarVerticalMargin() {
        return scrollbarVerticalMargin;
    }

    public void setScrollbarVerticalMargin(float scrollbarVerticalMargin) {
        this.scrollbarVerticalMargin = scrollbarVerticalMargin;
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

    private class ScrollbarView extends View {

        private Paint paint;

        private long timeLastTouch;

        private float scrollbarFade;
        private Timer scrollbarFadeTimer;

        private ValueAnimator animScrollbarsFadeIn;
        private ValueAnimator animScrollbarsFadeOut;

        public ScrollbarView(Context context) {
            super(context);

            paint = new Paint();

            scrollbarFade = 0f;

            // Expand to whatever we're in
            setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            // Transparent background
            setBackgroundColor(0);

            animScrollbarsFadeIn = ValueAnimator.ofFloat(0f, 1f);
            animScrollbarsFadeIn.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
            animScrollbarsFadeIn.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    // Set fade
                    scrollbarFade = (float) valueAnimator.getAnimatedValue();

                    // Force redraw
                    invalidate();
                }

            });
            animScrollbarsFadeIn.addListener(new Animator.AnimatorListener() {

                @Override
                public void onAnimationStart(Animator animator) {
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    // Schedule fade out
                    postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            animScrollbarsFadeOut.start();
                        }

                    }, 1000l);
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }

            });

            animScrollbarsFadeOut = ValueAnimator.ofFloat(1f, 0f);
            animScrollbarsFadeOut.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
            animScrollbarsFadeOut.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    // Set fade
                    scrollbarFade = (float) valueAnimator.getAnimatedValue();

                    // Force redraw
                    invalidate();
                }

            });

            scrollbarFadeTimer = new Timer();
            scrollbarFadeTimer.scheduleAtFixedRate(new TimerTask() {

                @Override
                public void run() {
                    // Fade scrollbars out if necessary
                    if ((System.nanoTime() - timeLastScrollChangeX >= SCROLLBAR_FADE_DELAY || System.nanoTime() - timeLastScrollChangeY >= SCROLLBAR_FADE_DELAY) && scrollbarFade == 1f) {
                        post(new Runnable() {

                            @Override
                            public void run() {
                                animScrollbarsFadeOut.start();
                            }

                        });
                    }
                }

            }, 0l, 100l);

            setHorizontalScrollBarEnabled(true);
            setVerticalScrollBarEnabled(true);
            setScrollBarStyle(SCROLLBARS_INSIDE_OVERLAY);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            // Fade scrollbars in if necessary
            if (scrollbarFade < 1f) {
                //animScrollbarsFadeIn.start();
            }

            awakenScrollBars(200, true);

            return super.onTouchEvent(event);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            // Get reference to child
            View child = scrollViewY.getChildAt(0);

            // Calculate dimensions of horizontal scrollbar
            float barHorizontalLeft = (float) getWidth() * (float) scrollViewX.getScrollX() / (float) child.getWidth() + scrollbarHorizontalMargin;
            float barHorizontalTop = (float) getBottom() - dpToPx(getContext(), scrollbarHorizontalSize);
            float barHorizontalRight = (float) getWidth() / (float) child.getWidth() * ((float) scrollViewX.getScrollX() + (float) scrollViewX.getWidth()) - scrollbarHorizontalMargin;
            float barHorizontalBottom = (float) getBottom() - dpToPx(getContext(), 1f);

            // Calculate dimensions of vertical scrollbar
            float barVerticalLeft = (float) getRight() - dpToPx(getContext(), scrollbarVerticalSize);
            float barVerticalTop = (float) getHeight() * (float) scrollViewY.getScrollY() / (float) child.getHeight() + scrollbarVerticalMargin;
            float barVerticalRight = (float) getRight() - dpToPx(getContext(), 1f);
            float barVerticalBottom = (float) getHeight() / (float) child.getHeight() * ((float) scrollViewY.getScrollY() + (float) scrollViewY.getHeight()) - scrollbarVerticalMargin;

            // Draw horizontal scrollbar
            paint.setColor(scrollbarHorizontalColor);
            paint.setAlpha((int) (scrollbarFade * paint.getAlpha()));
            //canvas.drawRect(barHorizontalLeft, barHorizontalTop, barHorizontalRight, barHorizontalBottom, paint);

            // Draw vertical scrollbar
            paint.setColor(scrollbarVerticalColor);
            paint.setAlpha((int) (scrollbarFade * paint.getAlpha()));
            //canvas.drawRect(barVerticalLeft, barVerticalTop, barVerticalRight, barVerticalBottom, paint);
        }

    }

}
