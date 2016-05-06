package io.microdev.source.widget.twodimscrollview;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class TwoDimScrollView extends FrameLayout {

    public TwoDimScrollView(Context context) {
        super(context);
        initialize();
    }

    public TwoDimScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TwoDimScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    private void initialize() {
    }

}
