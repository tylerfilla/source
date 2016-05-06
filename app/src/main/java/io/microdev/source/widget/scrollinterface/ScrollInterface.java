package io.microdev.source.widget.scrollinterface;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import io.microdev.source.R;

public class ScrollInterface extends View {

    private int resScrollH;
    private int resScrollV;

    private View scrollH;
    private View scrollV;

    public ScrollInterface(Context context) {
        super(context);

        resScrollH = -1;
        resScrollV = -1;

        scrollH = null;
        scrollV = null;
    }

    public ScrollInterface(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Get styled attributes array
        TypedArray styledAttrs = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ScrollInterface, 0, 0);

        // Get scroll view resource IDs
        resScrollH = styledAttrs.getResourceId(R.styleable.ScrollInterface_scrollH, -1);
        resScrollV = styledAttrs.getResourceId(R.styleable.ScrollInterface_scrollV, -1);

        // Recycle styled attributes array
        styledAttrs.recycle();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ScrollInterface(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        // Get styled attributes array
        TypedArray styledAttrs = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ScrollInterface, defStyleAttr, defStyleRes);

        // Get scroll view resource IDs
        resScrollH = styledAttrs.getResourceId(R.styleable.ScrollInterface_scrollH, -1);
        resScrollV = styledAttrs.getResourceId(R.styleable.ScrollInterface_scrollV, -1);

        // Recycle styled attributes array
        styledAttrs.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // We must have access to both scroll views
        if (!getViews()) {
            return;
        }

        // Get scroll amounts
        int scrollX = scrollH.getScrollX();
        int scrollY = scrollV.getScrollY();

        System.out.println(scrollX + "," + scrollY);

        // TODO: Draw scrollbars
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // We must have access to both scroll views
        if (!getViews()) {
            System.out.println("onTouchEvent cannot get views");
            return false;
        }

        // If dragging on scroll interface
        if (ev.getAction() == MotionEvent.ACTION_DOWN || ev.getAction() == MotionEvent.ACTION_MOVE) {
            // Send copies of event to scroll views
            scrollH.onTouchEvent(MotionEvent.obtain(ev));
            scrollV.onTouchEvent(MotionEvent.obtain(ev));

            // Continue to receive events about this drag gesture thing
            return true;
        }

        // Pass all other events through
        return false;
    }

    private boolean getViews() {
        // If resource IDs failed to resolve (should never happen)
        if (resScrollH == -1 || resScrollV == -1) {
            // Internal error; nothing can be done about this
            return false;
        }

        // If resource IDs resolved, but scroll views weren't found (should always happen only once)
        if (scrollH == null || scrollV == null) {
            // Try to find the scroll views
            scrollH = getRootView().findViewById(resScrollH);
            scrollV = getRootView().findViewById(resScrollV);

            // Return true if both scroll views were found
            return (scrollH != null && scrollV != null);
        }

        // Everything is normal
        return true;
    }

}
