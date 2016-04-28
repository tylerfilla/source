package io.microdev.source.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.text.Selection;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.EditText;

import java.io.File;

import io.microdev.source.R;

public class Editor extends EditText {

    private static final int DEFAULT_ATTR_COLOR_LINE_HIGHLIGHT = Color.BLACK;

    private static final boolean DEFAULT_ATTR_SHOW_LINE_HIGHLIGHT = true;
    private static final boolean DEFAULT_ATTR_SHOW_LINE_NUMBERS = true;

    private int colorLineHighlight;

    private boolean showLineHighlight;
    private boolean showLineNumbers;

    private Paint paintLineHighlight;
    private Paint paintLineNumbersColumn;

    private File file;

    public Editor(Context context) {
        super(context);

        // Default attributes
        colorLineHighlight = DEFAULT_ATTR_COLOR_LINE_HIGHLIGHT;
        showLineHighlight = DEFAULT_ATTR_SHOW_LINE_HIGHLIGHT;
        showLineNumbers = DEFAULT_ATTR_SHOW_LINE_NUMBERS;

        // Common init
        initialize();
    }

    public Editor(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Get styled attributes array
        TypedArray styleAttrs = context.getTheme().obtainStyledAttributes(attrs, R.styleable.Editor, 0, 0);

        // Read attributes from XML
        colorLineHighlight = styleAttrs.getColor(R.styleable.Editor_colorLineHighlight, DEFAULT_ATTR_COLOR_LINE_HIGHLIGHT);
        showLineHighlight = styleAttrs.getBoolean(R.styleable.Editor_showLineHighlight, DEFAULT_ATTR_SHOW_LINE_HIGHLIGHT);
        showLineNumbers = styleAttrs.getBoolean(R.styleable.Editor_showLineNumbers, DEFAULT_ATTR_SHOW_LINE_NUMBERS);

        // Recycle styled attributes array
        styleAttrs.recycle();

        // Common init
        initialize();
    }

    private void initialize() {
        // Remove default underline and decor
        setBackgroundDrawable(null);

        // Start from top and, depending on system RTL setting, left or right
        setGravity(Gravity.TOP | Gravity.START);

        // Allocate paints for custom drawing
        paintLineHighlight = new Paint();
        paintLineNumbersColumn = new Paint();
    }

    public boolean getShowLineHighlight() {
        return showLineHighlight;
    }

    public void setShowLineHighlight(boolean showLineHighlight) {
        this.showLineHighlight = showLineHighlight;
    }

    public int getColorLineHighlight() {
        return colorLineHighlight;
    }

    public void setColorLineHighlight(int colorLineHighlight) {
        this.colorLineHighlight = colorLineHighlight;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Render line numbers if preferred
        if (showLineNumbers) {
            // Iterate over lines
            for (int l = 1; l <= getLineCount(); l++) {
                // Write number
                canvas.drawText(String.valueOf(l), 0, getLayout().getLineTop(l), getPaint());
            }

            // Shift everything to the right from now on
            canvas.translate(50f, 0f);
        }

        // Render line highlighting if preferred
        if (showLineHighlight) {
            // Get bounds of current selection in chars
            int selectionStart = Selection.getSelectionStart(getText());
            int selectionEnd = Selection.getSelectionEnd(getText());

            // If there is a selection
            if (selectionStart > -1) {
                // Calculate bounds of selection in lines
                int lineStart = getLayout().getLineForOffset(selectionStart);
                int lineEnd = getLayout().getLineForOffset(selectionEnd);

                // Number of lines to highlight
                int lineCount = 1;

                // If selection has a different end boundary, it could span multiple lines; count them, too
                if (selectionEnd != selectionStart) {
                    lineCount += lineEnd - lineStart;
                }

                // Calculate vertical offset boundaries for highlight region
                int highlightStart = getLayout().getLineBottom(lineStart) - getPaddingTop();
                int highlightEnd = getLayout().getLineBottom(lineStart + lineCount - 1) + getLineHeight() - getPaddingTop();

                // Set paint color of highlight region
                paintLineHighlight.setColor(colorLineHighlight);

                // Draw the highlight region
                canvas.drawRect(0f, highlightStart, getWidth(), highlightEnd, paintLineHighlight);
            }
        }

        // Continue to render as an EditText
        super.onDraw(canvas);
    }

}
