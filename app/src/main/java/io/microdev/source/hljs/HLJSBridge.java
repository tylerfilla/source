package io.microdev.source.hljs;

import android.content.Context;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;

import java.io.IOException;
import java.io.InputStreamReader;

public class HLJSBridge {

    private static final String ASSET_HLJS_ROOT = "highlight.js-9.3.0";
    private static final String ASSET_HLJS_SCRIPT_CORE = ASSET_HLJS_ROOT + "/src/highlight.js";
    private static final String ASSET_HLJS_DIR_LANGS = ASSET_HLJS_ROOT + "/src/languages";

    private Context context;

    private boolean loaded;

    private org.mozilla.javascript.Context js;
    private ScriptableObject jsScope;

    public HLJSBridge(Context context) {
        this.context = context;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void load() throws IOException {
        // Enter new JavaScript execution context
        js = org.mozilla.javascript.Context.enter();

        // Use interpretive mode (bytecode compilation is out of the question on Android)
        js.setOptimizationLevel(-1);

        // Create global scope
        jsScope = js.initStandardObjects();

        // Create a global object for highlight.js to bind to (pretending to be a Web browser)
        jsScope.defineProperty("window", js.newObject(jsScope), ScriptableObject.DONTENUM);

        // Create a reader for highlight.js core script
        InputStreamReader readerCore = new InputStreamReader(context.getAssets().open(ASSET_HLJS_SCRIPT_CORE));

        // Evaluate core script
        js.evaluateReader(jsScope, readerCore, "<hljscore>", 1, null);

        // Close core script
        readerCore.close();

        // Iterate over highlight.js language definition scripts and register them
        for (String langFileName : context.getAssets().list(ASSET_HLJS_DIR_LANGS)) {
            // If file doesn't have JavaScript extension, skip it
            if (!langFileName.endsWith(".js")) {
                continue;
            }

            // Create a reader for this language script
            InputStreamReader readerLang = new InputStreamReader(context.getAssets().open(ASSET_HLJS_DIR_LANGS + "/" + langFileName));

            // Evaluate language script to obtain its language function
            Function langFunc = (Function) js.evaluateReader(jsScope, readerLang, "<hljslang>", 1, null);

            // Close language script
            readerLang.close();

            // Add previously obtained language function to scope under the name "lang"
            jsScope.defineProperty("lang", langFunc, ScriptableObject.DONTENUM);

            // Register the language function with highlight.js
            ((Function) ((ScriptableObject) ((ScriptableObject) jsScope.get("window")).get("hljs")).get("registerLanguage")).call(js, jsScope, jsScope, new Object[] { langFileName.substring(0, langFileName.length() - 3), langFunc });
        }

        // Set loaded flag
        loaded = true;
    }

    public void unload() {
        org.mozilla.javascript.Context.exit();

        // Clear loaded flag
        loaded = false;
    }

    public String highlight(String lang, String code) {
        // Send the code off to highlight.js
        ScriptableObject obj = (ScriptableObject) ((Function) ((ScriptableObject) ((ScriptableObject) jsScope.get("window")).get("hljs")).get("highlight")).call(js, jsScope, jsScope, new Object[] { lang, code, true, null });

        // Obtain and return the highlighted result
        return String.valueOf(obj.get("value"));
    }

}
