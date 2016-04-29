package io.microdev.source.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Editable;
import android.text.Layout;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewTreeObserver;
import android.widget.EditText;

import io.microdev.source.R;

public class Editor extends EditText {

    private static final int DEF_COLOR_LINE_HIGHLIGHT = 0x2040c4ff;
    private static final int DEF_COLOR_LINE_NUMBER_COLUMN_BG = 0xffe0e0e0;

    private static final boolean DEF_SHOW_LINE_HIGHLIGHT = true;
    private static final boolean DEF_SHOW_LINE_NUMBERS = true;

    private static final float DEF_LINE_NUMBER_COLUMN_PADDING_LEFT = 32f;
    private static final float DEF_LINE_NUMBER_COLUMN_PADDING_RIGHT = 32f;

    private int colorLineHighlight;
    private int colorLineNumberColumnBg;

    private boolean showLineHighlight;
    private boolean showLineNumbers;

    private float lineNumberColumnPaddingLeft;
    private float lineNumberColumnPaddingRight;

    private Paint paintLineHighlight;
    private Paint paintLineNumberColumnBg;

    private int paddingBumpLeft;
    private boolean paddingRedirect;

    private Layout layout;
    private int currentLineCount;
    private float lineNumberColumnWidth;

    public Editor(Context context) {
        super(context);

        // Set default attrs
        setDefaultAttrs();

        // Common initializer
        initialize();
    }

    public Editor(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Set default attrs
        setDefaultAttrs();

        // Get styled attributes array
        TypedArray styleAttrs = context.getTheme().obtainStyledAttributes(attrs, R.styleable.Editor, 0, 0);

        /* Read XML attrs */

        colorLineHighlight = styleAttrs.getColor(R.styleable.Editor_colorLineHighlight, colorLineHighlight);
        colorLineNumberColumnBg = styleAttrs.getColor(R.styleable.Editor_colorLineNumberColumnBg, colorLineNumberColumnBg);

        showLineHighlight = styleAttrs.getBoolean(R.styleable.Editor_showLineHighlight, showLineHighlight);
        showLineNumbers = styleAttrs.getBoolean(R.styleable.Editor_showLineNumbers, showLineNumbers);

        lineNumberColumnPaddingLeft = styleAttrs.getDimension(R.styleable.Editor_lineNumberColumnPaddingLeft, lineNumberColumnPaddingLeft);
        lineNumberColumnPaddingRight = styleAttrs.getDimension(R.styleable.Editor_lineNumberColumnPaddingLeft, lineNumberColumnPaddingRight);

        // Recycle styled attributes array
        styleAttrs.recycle();

        // Common initializer
        initialize();
    }

    private void initialize() {
        // Remove default underline and decor
        setBackgroundDrawable(null);

        // Start from top and, depending on system RTL setting, left or right
        setGravity(Gravity.TOP | Gravity.START);

        // Allocate paints for custom drawing
        paintLineHighlight = new Paint();
        paintLineNumberColumnBg = new Paint();

        // Set up paint for line highlighting
        paintLineHighlight.setColor(colorLineHighlight);

        // Set up paint for line number column background
        paintLineNumberColumnBg.setColor(colorLineNumberColumnBg);

        // Set up a global layout listener to watch for layouts
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                // Get current layout
                layout = getLayout();
            }

        });

        // Set up a text changed listener to watch input
        addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // If line count changed
                if (getLineCount() != currentLineCount) {
                    // Store new line count
                    currentLineCount = getLineCount();

                    // Update line number column width
                    updateLineNumberColumnWidth();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }

        });

        // Establish initial column width
        post(new Runnable() {

            @Override
            public void run() {
                updateLineNumberColumnWidth();
            }

        });
    }

    private void setDefaultAttrs() {
        // Colors
        colorLineHighlight = DEF_COLOR_LINE_HIGHLIGHT;
        colorLineNumberColumnBg = DEF_COLOR_LINE_NUMBER_COLUMN_BG;

        // Show flags
        showLineHighlight = DEF_SHOW_LINE_HIGHLIGHT;
        showLineNumbers = DEF_SHOW_LINE_NUMBERS;

        // Line number column padding
        lineNumberColumnPaddingLeft = DEF_LINE_NUMBER_COLUMN_PADDING_LEFT;
        lineNumberColumnPaddingRight = DEF_LINE_NUMBER_COLUMN_PADDING_RIGHT;
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

    public int getColorLineNumberColumnBg() {
        return colorLineNumberColumnBg;
    }

    public void setColorLineNumberColumnBg(int colorLineNumberColumnBg) {
        this.colorLineNumberColumnBg = colorLineNumberColumnBg;
    }

    public float getLineNumberColumnPaddingLeft() {
        return lineNumberColumnPaddingLeft;
    }

    public void setLineNumberColumnPaddingLeft(float lineNumberColumnPaddingLeft) {
        this.lineNumberColumnPaddingLeft = lineNumberColumnPaddingLeft;
    }

    public float getLineNumberColumnPaddingRight() {
        return lineNumberColumnPaddingRight;
    }

    public void setLineNumberColumnPaddingRight(float lineNumberColumnPaddingRight) {
        this.lineNumberColumnPaddingRight = lineNumberColumnPaddingRight;
    }

    private void updateLineNumberColumnWidth() {
        // Iterate over all lines and count non-soft-wrap lines
        int numberedLines = 0;
        for (int i = 0; i < getLineCount(); i++) {
            // Determine if line should be numbered
            if (i == 0 || getText().subSequence(layout.getLineVisibleEnd(i - 1), layout.getLineEnd(i - 1)).toString().contains("\n")) {
                numberedLines++;
            }
        }

        // Subtract old column width from left padding
        setPadding(getPaddingLeft() - (int) lineNumberColumnWidth, getPaddingTop(), getPaddingRight(), getPaddingBottom());

        // Calculate starting width of line number column
        lineNumberColumnWidth = lineNumberColumnPaddingLeft + layout.getPaint().measureText(String.valueOf(numberedLines)) + lineNumberColumnPaddingRight;

        // Bump left padding for new line number column
        setPadding(getPaddingLeft() + (int) lineNumberColumnWidth, getPaddingTop(), getPaddingRight(), getPaddingBottom());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Render line numbers if preferred
        if (showLineNumbers) {
            // Draw line number column background
            canvas.drawRect(getLeft(), getTop() + getPaddingTop(), getLeft() + lineNumberColumnWidth, getBottom() - getPaddingBottom(), paintLineNumberColumnBg);

            // Iterate over all lines
            int l = 1;
            for (int i = 0; i < getLineCount(); i++) {
                // Determine if line should be numbered
                if (i == 0 || getText().subSequence(layout.getLineVisibleEnd(i - 1), layout.getLineEnd(i - 1)).toString().contains("\n")) {
                    // Get Y coordinates of line's vertical bounds
                    float lineTop = layout.getLineTop(i) + getPaddingTop();
                    float lineBottom = layout.getLineBottom(i) + getPaddingTop();

                    // Draw line number text
                    canvas.drawText(String.valueOf(l), lineNumberColumnWidth - lineNumberColumnPaddingRight - layout.getPaint().measureText(String.valueOf(l)), layout.getLineBaseline(i) + getPaddingTop(), layout.getPaint());

                    // Increment user-facing line number
                    l++;
                }
            }
        }

        // Render line highlighting if preferred
        if (showLineHighlight) {
            // Get bounds of current selection in char offsets
            int selStart = Selection.getSelectionStart(getText());
            int selEnd = Selection.getSelectionEnd(getText());

            // If there's no text selection, but the cursor is placed in the editor
            if (selStart > -1 && selStart == selEnd) {
                // Get line number for selection
                int line = layout.getLineForOffset(selStart);

                // Preliminary edges of highlight region vertical bounds
                float hrTop = layout.getLineTop(line) + getPaddingTop();
                float hrBottom = layout.getLineBottom(line) + getPaddingTop();

                // Preliminary edges of highlight region horizontal bounds
                float hrLeft = layout.getLineLeft(line) + lineNumberColumnWidth;
                float hrRight = getRight();

                // Scan upwards for wrapped lines
                int scanUp = line - 1;
                while (scanUp >= 0 && !getText().subSequence(layout.getLineVisibleEnd(scanUp), layout.getLineEnd(scanUp)).toString().contains("\n")) {
                    hrTop -= getLineHeight();
                    scanUp--;
                }

                // Scan downwards for wrapped lines
                int scanDown = line;
                while (scanDown < getLineCount() - 1 && !getText().subSequence(layout.getLineVisibleEnd(scanDown), layout.getLineEnd(scanDown)).toString().contains("\n")) {
                    hrBottom += getLineHeight();
                    scanDown++;
                }

                // Draw highlight behind line
                canvas.drawRect(hrLeft, hrTop, hrRight, hrBottom, paintLineHighlight);
            }
        }

        // Continue to render as an EditText
        super.onDraw(canvas);
    }

}
