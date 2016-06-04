package io.microdev.source.util;

import android.content.Context;

public class DimenUtil {

    public static float dpToPx(Context context, float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public static float pxToDp(Context context, float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static int dpToPxI(Context context, float dp) {
        return (int) dpToPx(context, dp);
    }

    public static int pxToDpI(Context context, float px) {
        return (int) pxToDp(context, px);
    }

}
