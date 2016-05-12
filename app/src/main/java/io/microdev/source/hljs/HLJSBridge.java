package io.microdev.source.hljs;

import android.content.Context;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

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

        // Use interpretive mode (bytecode compilation is out of the question on Android)
        jsContext.setOptimizationLevel(-1);

        // Establish scope
        Scriptable scope = jsContext.initStandardObjects();

        // Create a global object for highlight.js to bind to
        jsContext.evaluateString(scope, "var window = {};", "<window>", 1, null);

        // Create a reader for highlight.js core script
        InputStreamReader readerCore = new InputStreamReader(context.getAssets().open("highlight.js/src/highlight.js"));

        // Evaluate core script
        jsContext.evaluateReader(scope, readerCore, "<hljscore>", 1, null);

        // Close core script
        readerCore.close();

        // Iterate over highlight.js language definition scripts
        for (String langFileName : context.getAssets().list("highlight.js/src/languages")) {
            // Create a reader for this language script
            InputStreamReader readerLang = new InputStreamReader(context.getAssets().open("highlight.js/src/languages/" + langFileName));

            // Evaluate language script to obtain its language function
            Function langFunc = (Function) jsContext.evaluateReader(scope, readerLang, "<hljslang>", 1, null);

            // Close language script
            readerLang.close();

            // Add previously obtained language function to scope under the name "lang"
            ScriptableObject.defineProperty(scope, "lang", langFunc, ScriptableObject.DONTENUM);

            // Register the language function with highlight.js
            jsContext.evaluateString(scope, "window.hljs.registerLanguage('" + langFileName.replaceAll("\\'", "\\\\'") + "', lang);", "<meh>", 1, null);
        }

        // FIXME: TESTING
        jsContext.evaluateString(scope, "window.res = window.hljs.highlightAuto('var test = function() {\\n\\teval(\"\");\\n};');", "<cmd>", 1, null);
        System.out.println(jsContext.evaluateString(scope, "window.res.value", "<cmd>", 1, null));
    }

    public void unload() {
        org.mozilla.javascript.Context.exit();
    }

}
