package io.microdev.source.widget.editorbinary;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.EditText;

import io.microdev.source.R;

public class EditorBinary extends EditText {

    public EditorBinary(Context context) {
        super(context);

        initialize();
        configure();
    }

    public EditorBinary(Context context, AttributeSet attrs) {
        super(context, attrs);

        initialize();
        handleAttrs(attrs, 0, 0);
        configure();
    }

    public EditorBinary(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initialize();
        handleAttrs(attrs, defStyleAttr, 0);
        configure();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public EditorBinary(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        initialize();
        handleAttrs(attrs, defStyleAttr, defStyleRes);
        configure();
    }

    public void initialize() {
    }

    public void handleAttrs(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        // Get styled attributes array
        TypedArray styledAttrs = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.EditorText, defStyleAttr, defStyleRes);

        // TODO: Do attr handling stuff here

        // Recycle styled attributes array
        styledAttrs.recycle();
    }

    public void configure() {
    }

}
