package io.microdev.source.widget.paninterface;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

public class PanInterface extends FrameLayout {

    private HorizontalScrollView scrollH;
    private ScrollView scrollV;

    public PanInterface(Context context) {
        super(context);
        initialize();
    }

    public PanInterface(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PanInterface(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    private void initialize() {
        scrollH = new HorizontalScrollView(getContext());
        scrollV = new ScrollView(getContext());

        // Disable native scrollbars
        scrollH.setHorizontalScrollBarEnabled(false);
        scrollV.setVerticalScrollBarEnabled(false);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        // If attachment was to vertical scroll view
        if (getParent() == scrollV) {
            // Continuing would cause weird things
            return;
        }

        // Get layout parameters of this view before splicing in scroll views
        ViewGroup.LayoutParams layoutParams = getLayoutParams();

        // Splice scroll views between this view and its parent
        ViewGroup parentGroup = (ViewGroup) getParent();
        parentGroup.removeView(this);
        parentGroup.addView(scrollH);
        scrollH.addView(scrollV);
        scrollV.addView(this);

        // Lay out horizontal scroll view just as this view used to be
        scrollH.setLayoutParams(layoutParams);
    }

}
