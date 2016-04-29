package io.microdev.source.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Editable;
import android.text.Layout;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewTreeObserver;
import android.widget.EditText;

import java.io.File;

import io.microdev.source.R;

public class Editor extends EditText {

    private static final int DEFAULT_ATTR_COLOR_LINE_HIGHLIGHT = Color.BLACK;

    private static final boolean DEFAULT_ATTR_SHOW_LINE_HIGHLIGHT = true;
    private static final boolean DEFAULT_ATTR_SHOW_LINE_NUMBERS = true;

    private static final float DEFAULT_LINE_NUMBER_COLUMN_PADDING_LEFT = 0f;
    private static final float DEFAULT_LINE_NUMBER_COLUMN_PADDING_RIGHT = 0f;

    private int colorLineHighlight;

    private boolean showLineHighlight;
    private boolean showLineNumbers;

    private float lineNumberColumnPaddingLeft;
    private float lineNumberColumnPaddingRight;

    private File file;

    private Paint paintLineHighlight;
    private Paint paintLineNumbersBg;

    private Layout layout;

    private int currentLineCount;

    private float lineNumberColumnWidth;

    public Editor(Context context) {
        super(context);

        // Set default attrs
        colorLineHighlight = DEFAULT_ATTR_COLOR_LINE_HIGHLIGHT;
        showLineHighlight = DEFAULT_ATTR_SHOW_LINE_HIGHLIGHT;
        showLineNumbers = DEFAULT_ATTR_SHOW_LINE_NUMBERS;
        lineNumberColumnPaddingLeft = DEFAULT_LINE_NUMBER_COLUMN_PADDING_LEFT;
        lineNumberColumnPaddingRight = DEFAULT_LINE_NUMBER_COLUMN_PADDING_RIGHT;

        // Common init
        initialize();
    }

    public Editor(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Get styled attributes array
        TypedArray styleAttrs = context.getTheme().obtainStyledAttributes(attrs, R.styleable.Editor, 0, 0);

        // Read XML attrs
        colorLineHighlight = styleAttrs.getColor(R.styleable.Editor_colorLineHighlight, DEFAULT_ATTR_COLOR_LINE_HIGHLIGHT);
        showLineHighlight = styleAttrs.getBoolean(R.styleable.Editor_showLineHighlight, DEFAULT_ATTR_SHOW_LINE_HIGHLIGHT);
        showLineNumbers = styleAttrs.getBoolean(R.styleable.Editor_showLineNumbers, DEFAULT_ATTR_SHOW_LINE_NUMBERS);
        lineNumberColumnPaddingLeft = styleAttrs.getDimension(R.styleable.Editor_lineNumberColumnPaddingLeft, DEFAULT_LINE_NUMBER_COLUMN_PADDING_LEFT);
        lineNumberColumnPaddingRight = styleAttrs.getDimension(R.styleable.Editor_lineNumberColumnPaddingLeft, DEFAULT_LINE_NUMBER_COLUMN_PADDING_RIGHT);

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
        paintLineNumbersBg = new Paint();

        // Set up paint for line highlighting
        paintLineHighlight.setColor(colorLineHighlight);

        // Set up paint for line number column background
        paintLineNumbersBg.setColor(0xffe0e0e0);

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

    private void updateLineNumberColumnWidth() {
        // Iterate over all lines and count non-soft-wrap lines
        int numberedLines = 0;
        for (int i = 0; i < getLineCount(); i++) {
            // Determine if line should be numbered
            if (i == 0 || getText().subSequence(layout.getLineVisibleEnd(i - 1), layout.getLineEnd(i - 1)).toString().contains("\n")) {
                numberedLines++;
            }
        }

        // Calculate starting width of line number column
        lineNumberColumnWidth = lineNumberColumnPaddingLeft + layout.getPaint().measureText(String.valueOf(numberedLines)) + lineNumberColumnPaddingRight;

        // FIXME: Commandeer the left padding to account for line number column (more custom way?)
        setPadding((int) lineNumberColumnWidth, getPaddingTop(), getPaddingRight(), getPaddingBottom());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Render line numbers if preferred
        if (showLineNumbers) {
            // Draw line number column background
            canvas.drawRect(0f, 0f, lineNumberColumnWidth, getBottom(), paintLineNumbersBg);

            // Iterate over all lines
            int l = 1;
            for (int i = 0; i < getLineCount(); i++) {
                // Determine if line should be numbered
                if (i == 0 || getText().subSequence(layout.getLineVisibleEnd(i - 1), layout.getLineEnd(i - 1)).toString().contains("\n")) {
                    // Get Y coordinates of line's vertical bounds
                    float lineTop = layout.getLineTop(i) + getPaddingTop();
                    float lineBottom = layout.getLineBottom(i) + getPaddingTop();

                    // Draw line number text
                    canvas.drawText(String.valueOf(l), lineNumberColumnWidth - lineNumberColumnPaddingRight - layout.getPaint().measureText(String.valueOf(l)), layout.getLineBaseline(i), layout.getPaint());

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
                float hrLeft = layout.getLineLeft(line) + getPaddingLeft();
                float hrRight = getRight() - getPaddingRight();

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
