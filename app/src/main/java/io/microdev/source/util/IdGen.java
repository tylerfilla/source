package io.microdev.source.util;

import android.support.annotation.IdRes;

public class IdGen {

    private static int next = 0;

    @IdRes
    public static int next() {
        return next++;
    }

}
