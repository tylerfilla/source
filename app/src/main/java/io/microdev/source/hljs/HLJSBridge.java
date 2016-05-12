package io.microdev.source.hljs;

import android.content.Context;

import org.mozilla.javascript.Scriptable;

import java.io.IOException;
import java.io.InputStreamReader;

public class HLJSBridge {

    private Context context;

    private org.mozilla.javascript.Context jsContext;

    public HLJSBridge(Context context) {
        this.context = context;
    }

    public void load() throws IOException {
        // Enter new execution context
        jsContext = org.mozilla.javascript.Context.enter();

        // Use interpretive mode
        jsContext.setOptimizationLevel(-1);

        // Establish scope
        Scriptable scope = jsContext.initSafeStandardObjects();

        jsContext.evaluateReader(scope, new InputStreamReader(context.getAssets().open("highlight.js/src/highlight.js")), "<hljs>", 1, null);

        System.out.println(jsContext.evaluateString(scope, "hljs", "<cmd>", 1, null));
    }

    public void unload() {
        org.mozilla.javascript.Context.exit();
    }

}
