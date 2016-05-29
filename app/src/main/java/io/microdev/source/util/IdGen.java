package io.microdev.source.util;

import android.support.annotation.IdRes;

public class IdGen {

    private static int id;

    @IdRes
    public static int next() {
        // Return current ID and increment for next use
        return id++;
    }

    static {
        // Start at 0
        id = 0;
    }

}
