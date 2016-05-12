package io.microdev.source.hljs;

import android.content.Context;

import org.mozilla.javascript.Scriptable;

import java.io.IOException;

public class HLJSBridge {

    private static final String ASSET_HLJS_ARCHIVE = "highlightjs.zip";

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

        System.out.println(jsContext.evaluateString(scope, "'hello'", "<cmd>", 1, null));
    }

    public void unload() {
        org.mozilla.javascript.Context.exit();
    }

}
