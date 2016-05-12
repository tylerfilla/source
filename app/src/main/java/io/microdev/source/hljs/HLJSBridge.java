package io.microdev.source.hljs;

import android.content.Context;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.ast.ObjectLiteral;

import java.io.IOException;
import java.io.InputStreamReader;

public class HLJSBridge {

    private static final String ASSET_HLJS_ROOT = "highlight.js-9.3.0";
    private static final String ASSET_HLJS_SCRIPT_CORE = ASSET_HLJS_ROOT + "/src/highlight.js";
    private static final String ASSET_HLJS_DIR_LANGS = ASSET_HLJS_ROOT + "/src/languages";

    private Context context;

    private org.mozilla.javascript.Context jsContext;

    public HLJSBridge(Context context) {
        this.context = context;
    }

    public void load() throws IOException {
        // Enter new JavaScript execution context
        jsContext = org.mozilla.javascript.Context.enter();

        // Use interpretive mode (bytecode compilation is out of the question on Android)
        jsContext.setOptimizationLevel(-1);

        // Establish scope
        ScriptableObject scope = jsContext.initStandardObjects();

        // Create a global object for highlight.js to bind to
        scope.defineProperty("window", jsContext.newObject(scope), ScriptableObject.DONTENUM);

        // Create a reader for highlight.js core script
        InputStreamReader readerCore = new InputStreamReader(context.getAssets().open(ASSET_HLJS_SCRIPT_CORE));

        // Evaluate core script
        jsContext.evaluateReader(scope, readerCore, "<hljscore>", 1, null);

        // Close core script
        readerCore.close();

        // Iterate over highlight.js language definition scripts
        for (String langFileName : context.getAssets().list(ASSET_HLJS_DIR_LANGS)) {
            // Create a reader for this language script
            InputStreamReader readerLang = new InputStreamReader(context.getAssets().open(ASSET_HLJS_DIR_LANGS + "/" + langFileName));

            // Evaluate language script to obtain its language function
            Function langFunc = (Function) jsContext.evaluateReader(scope, readerLang, "<hljslang>", 1, null);

            // Close language script
            readerLang.close();

            // Add previously obtained language function to scope under the name "lang"
            scope.defineProperty("lang", langFunc, ScriptableObject.DONTENUM);

            // Register the language function with highlight.js
            jsContext.evaluateString(scope, "window.hljs.registerLanguage('" + langFileName.replaceAll("\\'", "\\\\'") + "', lang);", "<hljsreg>", 1, null);
        }

        // FIXME: TESTING
        jsContext.evaluateString(scope, "window.res = window.hljs.highlightAuto('var test = function() {\\n\\teval(\"\");\\n};');", "<cmd>", 1, null);
        System.out.println(jsContext.evaluateString(scope, "window.res.value", "<cmd>", 1, null));
    }

    public void unload() {
        org.mozilla.javascript.Context.exit();
    }

}
