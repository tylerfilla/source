package io.microdev.source.widget.editortext;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Editable;
import android.text.Layout;
import android.text.ParcelableSpan;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.widget.EditText;

import java.util.Deque;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingDeque;

import io.microdev.source.R;

import static io.microdev.source.util.DimenUtil.dpToPx;

public class EditorText extends EditText {

    private static final int DEF_COLOR_LINE_HIGHLIGHT = 0x2040c4ff;
    private static final int DEF_COLOR_LINE_NUMBER_COLUMN_BG = 0xffe0e0e0;

    private static final boolean DEF_SHOW_LINE_HIGHLIGHT = true;
    private static final boolean DEF_SHOW_LINE_NUMBERS = true;

    private static final float DEF_LINE_NUMBER_COLUMN_PADDING_LEFT_DP = 8f;
    private static final float DEF_LINE_NUMBER_COLUMN_PADDING_RIGHT_DP = 8f;

    private static final boolean DEF_ENABLE_SYNTAX_HIGHLIGHTING = true;

    private int colorLineHighlight;
    private int colorLineNumberColumnBg;

    private boolean showLineHighlight;
    private boolean showLineNumbers;

    private float lineNumberColumnPaddingLeft;
    private float lineNumberColumnPaddingRight;

    private boolean enableSyntaxHighlighting;

    private Paint paintLineHighlight;
    private Paint paintLineNumberColumnBg;

    private Layout layout;

    private int lineCountCurrent;
    private int lineCountPrev;

    private float lineNumberColumnWidth;

    private UndoProvider undoProvider;

    private SyntaxHighlighter syntaxHighlighter;
    private SyntaxHighlightingUpdateThread syntaxHighlightingUpdateThread;

    private int textChangedInternally;

    public EditorText(Context context) {
        super(context);

        initialize();
        configure();
    }

    public EditorText(Context context, AttributeSet attrs) {
        super(context, attrs);

        initialize();
        handleAttrs(attrs, 0, 0);
        configure();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public EditorText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs);

        initialize();
        handleAttrs(attrs, defStyleAttr, defStyleRes);
        configure();
    }

    private void initialize() {
        colorLineHighlight = DEF_COLOR_LINE_HIGHLIGHT;
        colorLineNumberColumnBg = DEF_COLOR_LINE_NUMBER_COLUMN_BG;

        showLineHighlight = DEF_SHOW_LINE_HIGHLIGHT;
        showLineNumbers = DEF_SHOW_LINE_NUMBERS;

        lineNumberColumnPaddingLeft = dpToPx(getContext(), DEF_LINE_NUMBER_COLUMN_PADDING_LEFT_DP);
        lineNumberColumnPaddingRight = dpToPx(getContext(), DEF_LINE_NUMBER_COLUMN_PADDING_RIGHT_DP);

        enableSyntaxHighlighting = DEF_ENABLE_SYNTAX_HIGHLIGHTING;
    }

    private void handleAttrs(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        // Get styled attributes array
        TypedArray styledAttrs = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.EditorText, defStyleAttr, defStyleRes);

        colorLineHighlight = styledAttrs.getColor(R.styleable.EditorText_colorLineHighlight, colorLineHighlight);
        colorLineNumberColumnBg = styledAttrs.getColor(R.styleable.EditorText_colorLineNumberColumnBg, colorLineNumberColumnBg);

        showLineHighlight = styledAttrs.getBoolean(R.styleable.EditorText_showLineHighlight, showLineHighlight);
        showLineNumbers = styledAttrs.getBoolean(R.styleable.EditorText_showLineNumbers, showLineNumbers);

        lineNumberColumnPaddingLeft = styledAttrs.getDimension(R.styleable.EditorText_lineNumberColumnPaddingLeft, lineNumberColumnPaddingLeft);
        lineNumberColumnPaddingRight = styledAttrs.getDimension(R.styleable.EditorText_lineNumberColumnPaddingLeft, lineNumberColumnPaddingRight);

        enableSyntaxHighlighting = styledAttrs.getBoolean(R.styleable.EditorText_enableSyntaxHighlighting, enableSyntaxHighlighting);

        // Recycle styled attributes array
        styledAttrs.recycle();
    }

    private void configure() {
        // Remove default underline and decor
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setBackground(null);
        } else {
            // noinspection deprecation
            setBackgroundDrawable(null);
        }

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

                // Update line number column width
                updateLineNumberColumnWidth(false);
            }

        });

        // Set up a text changed listener to watch input
        addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // If line count changed
                if (getLineCount() != lineCountCurrent) {
                    // Store new line count
                    lineCountCurrent = getLineCount();
                }

                // Check if an operation internal to the editor changed the text
                if (textChangedInternally > 0) {
                    // Reset the flag for future use
                    textChangedInternally--;
                } else {
                    // Bump the undo provider
                    undoProvider.bump();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Bump the syntax highlighting update thread
                syntaxHighlightingUpdateThread.bump();
            }

        });

        // Create a new undo provider
        undoProvider = new UndoProvider();

        // Establish undo baseline
        undoProvider.reset();

        // Create a and start syntax highlighting update thread
        syntaxHighlightingUpdateThread = new SyntaxHighlightingUpdateThread();
        syntaxHighlightingUpdateThread.start();

        // Give line numbering a little nudge
        lineCountCurrent = 0;
        lineCountPrev = -1;
    }

    public boolean getShowLineHighlight() {
        return showLineHighlight;
    }

    public void setShowLineHighlight(boolean showLineHighlight) {
        this.showLineHighlight = showLineHighlight;
    }

    public boolean getShowLineNumbers() {
        return showLineNumbers;
    }

    public void setShowLineNumbers(boolean showLineNumbers) {
        this.showLineNumbers = showLineNumbers;
        updateLineNumberColumnWidth(true);
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

    public boolean getEnableSyntaxHighlighting() {
        return enableSyntaxHighlighting;
    }

    public void setEnableSyntaxHighlighting(boolean enableSyntaxHighlighting) {
        this.enableSyntaxHighlighting = enableSyntaxHighlighting;
    }

    public void undo() {
        undoProvider.undo(1);
    }

    public void undo(int count) {
        undoProvider.undo(count);
    }

    public void redo() {
        undoProvider.redo(1);
    }

    public void redo(int count) {
        undoProvider.redo(count);
    }

    public SyntaxHighlighter getSyntaxHighlighter() {
        return syntaxHighlighter;
    }

    public void setSyntaxHighlighter(SyntaxHighlighter syntaxHighlighter) {
        this.syntaxHighlighter = syntaxHighlighter;
    }

    private void updateLineNumberColumnWidth(boolean force) {
        // If line count has changed since last layout (or update is forced)
        if (force || lineCountCurrent != lineCountPrev) {
            // Mark count as not changed
            lineCountPrev = lineCountCurrent;
        } else {
            // Skip this update
            return;
        }

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

        // Rest depends on presence of line number column
        if (showLineNumbers) {
            // Calculate new width of line number column
            lineNumberColumnWidth = lineNumberColumnPaddingLeft + layout.getPaint().measureText(String.valueOf(numberedLines)) + lineNumberColumnPaddingRight;

            // Bump left padding for new column width
            setPadding(getPaddingLeft() + (int) lineNumberColumnWidth, getPaddingTop(), getPaddingRight(), getPaddingBottom());
        } else {
            // No column means zero width
            lineNumberColumnWidth = 0;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // If rendering in edit mode
        if (isInEditMode()) {
            canvas.drawText("[ Editor ]", 0f, 0f, getPaint());
            return;
        }

        // Render line numbers if preferred
        if (showLineNumbers) {
            // Draw line number column background
            canvas.drawRect(getLeft(), getTop() + getPaddingTop(), getLeft() + lineNumberColumnWidth, getBottom() - getPaddingBottom(), paintLineNumberColumnBg);

            // Iterate over all lines
            int l = 1;
            for (int i = 0; i < getLineCount(); i++) {
                // Determine if line should be numbered
                if (i == 0 || getText().subSequence(layout.getLineVisibleEnd(i - 1), layout.getLineEnd(i - 1)).toString().contains("\n")) { // FIXME: Undoing during draw pass causes problems
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
                float regionTop = layout.getLineTop(line) + getPaddingTop();
                float regionBottom = layout.getLineBottom(line) + getPaddingTop();

                // Preliminary edges of highlight region horizontal bounds
                float regionLeft = layout.getLineLeft(line) + lineNumberColumnWidth;
                float regionRight = getRight();

                // Scan upwards for wrapped lines
                int scanUp = line - 1;
                while (scanUp >= 0 && !getText().subSequence(layout.getLineVisibleEnd(scanUp), layout.getLineEnd(scanUp)).toString().contains("\n")) {
                    regionTop -= getLineHeight();
                    scanUp--;
                }

                // Scan downwards for wrapped lines
                int scanDown = line;
                while (scanDown < getLineCount() - 1 && !getText().subSequence(layout.getLineVisibleEnd(scanDown), layout.getLineEnd(scanDown)).toString().contains("\n")) {
                    regionBottom += getLineHeight();
                    scanDown++;
                }

                // Draw highlight behind line
                canvas.drawRect(regionLeft, regionTop, regionRight, regionBottom, paintLineHighlight);
            }
        }

        // Continue to render as an EditText
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        // Check if state is ours
        if (state instanceof SavedState) {
            // Make it our own
            SavedState savedState = (SavedState) state;

            // Flag text as internally changed
            textChangedInternally += 2;

            // Pass super state to superclass
            super.onRestoreInstanceState(savedState.getSuperState());

            // Restore undo/redo stacks
            undoProvider.stackUndo.clear();
            for (int i = 0; i < savedState.undoProviderStackUndoArray.length; i++) {
                undoProvider.stackUndo.addLast(savedState.undoProviderStackUndoArray[i]);
            }
            undoProvider.stackRedo.clear();
            for (int i = 0; i < savedState.undoProviderStackRedoArray.length; i++) {
                undoProvider.stackRedo.addLast(savedState.undoProviderStackRedoArray[i]);
            }
        } else {
            // Not for us, pass it on
            super.onRestoreInstanceState(state);
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        // Create a stateful object based on super state
        SavedState savedState = new SavedState(super.onSaveInstanceState());

        // Save undo/redo stacks as arrays of ContentFrame
        savedState.undoProviderStackUndoArray = undoProvider.stackUndo.toArray(new UndoProvider.ContentFrame[undoProvider.stackUndo.size()]);
        savedState.undoProviderStackRedoArray = undoProvider.stackRedo.toArray(new UndoProvider.ContentFrame[undoProvider.stackRedo.size()]);

        return savedState;
    }

    private class SavedState extends BaseSavedState {

        private UndoProvider.ContentFrame[] undoProviderStackUndoArray;
        private UndoProvider.ContentFrame[] undoProviderStackRedoArray;

        public SavedState(Parcel source) {
            super(source);

            // Read undo/redo stack arrays
            undoProviderStackUndoArray = (UndoProvider.ContentFrame[]) source.readParcelableArray(UndoProvider.ContentFrame.class.getClassLoader());
            undoProviderStackRedoArray = (UndoProvider.ContentFrame[]) source.readParcelableArray(UndoProvider.ContentFrame.class.getClassLoader());
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);

            // Write undo/redo stack arrays
            out.writeParcelableArray(undoProviderStackUndoArray, 0);
            out.writeParcelableArray(undoProviderStackRedoArray, 0);
        }

        public final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }

        };

    }

    public class UndoProvider {

        private static final long CHECK_PERIOD = 100l;
        private static final long STORE_THRESHOLD = 500l;

        private Timer timer;

        private Deque<ContentFrame> stackUndo;
        private Deque<ContentFrame> stackRedo;

        private boolean stored;
        private long timeLastBumped;

        private UndoProvider() {
            timer = new Timer();

            stackUndo = new LinkedBlockingDeque<>();
            stackRedo = new LinkedBlockingDeque<>();

            stored = true;
            timeLastBumped = System.nanoTime();

            // Schedule timer to check necessity of an undo store
            timer.scheduleAtFixedRate(new TimerTask() {

                @Override
                public void run() {
                    // If this bump series hasn't been highlighted and it's been long enough
                    if (!stored && System.nanoTime() - timeLastBumped > STORE_THRESHOLD * 1000000) {
                        // Store the bump series
                        storeUndo();
                    }
                }

            }, 0l, CHECK_PERIOD);
        }

        private int getUndoCount() {
            return stackUndo.size();
        }

        private int getRedoCount() {
            return stackRedo.size();
        }

        private boolean getCanUndo() {
            return !stackUndo.isEmpty();
        }

        private boolean getCanRedo() {
            return !stackRedo.isEmpty();
        }

        private void undo(int count) {
            // If current bump series hasn't yet been highlighted
            if (!stored) {
                // Short-circuit the bump series (a bit hacky, but it works)
                storeUndo();
            }

            // Perform count undo ops
            for (int i = 0; i < count; i++) {
                // Do not continue if undo stack only contains baseline
                if (stackUndo.size() == 1) {
                    break;
                }

                // Move topmost frame from undo stack to redo stack
                stackRedo.push(stackUndo.pop());

                // Change state to reflect new topmost frame on undo stack
                applyContentFrame(stackUndo.peek());
            }
        }

        private void redo(int count) {
            // Perform count redo ops
            for (int i = 0; i < count; i++) {
                // Do not continue if redo stack is empty
                if (stackRedo.isEmpty()) {
                    break;
                }

                // Move topmost frame from redo stack to undo stack
                stackUndo.push(stackRedo.pop());

                // Change state to reflect new topmost frame on undo stack
                applyContentFrame(stackUndo.peek());
            }
        }

        private void reset() {
            // Clear both undo and redo stacks
            stackUndo.clear();
            stackRedo.clear();

            // Load current state into undo stack
            stackUndo.push(storeContentFrame());
        }

        private void bump() {
            // Reset for next undo storage
            stored = false;

            // Mark time of bump
            timeLastBumped = System.nanoTime();

            // Clear the redo stack
            stackRedo.clear();
        }

        private void storeUndo() {
            // Mark that content was highlighted for this bump series
            stored = true;

            // Push current content onto undo stack for future undoing
            stackUndo.push(storeContentFrame());
        }

        private void applyContentFrame(final ContentFrame contentFrame) {
            // Apply content on the UI thread
            post(new Runnable() {

                @Override
                public void run() {
                    // Set editor text to that in content frame
                    textChangedInternally++;
                    setText(contentFrame.text);

                    // Restore cursor offset clamped to new text length
                    setSelection(contentFrame.selectionStart, contentFrame.selectionEnd);
                }

            });
        }

        private ContentFrame storeContentFrame() {
            // Create a new content frame to represent current content
            ContentFrame contentFrame = new ContentFrame();

            // Store selection info
            contentFrame.selectionEnd = getSelectionEnd();
            contentFrame.selectionStart = getSelectionStart();

            // Store copy of editor text
            contentFrame.text = getText().subSequence(0, length());

            return contentFrame;
        }

        private class ContentFrame implements Parcelable {

            private int selectionEnd;
            private int selectionStart;

            private CharSequence text;

            private ContentFrame() {
                selectionEnd = 0;
                selectionStart = 0;

                text = null;
            }

            private ContentFrame(Parcel in) {
                // Read selection bounds
                selectionEnd = in.readInt();
                selectionStart = in.readInt();

                // Read whether text is spanned
                boolean isSpanned = in.readInt() == 1;

                // If text is spanned
                if (isSpanned) {
                    // Create builder for spannable string
                    SpannableStringBuilder textBuilder = new SpannableStringBuilder();

                    // Read text
                    textBuilder.append(in.readString());

                    // Read number of spans
                    int numSpans = in.readInt();

                    // Read spans and properties iteratively
                    for (int i = 0; i < numSpans; i++) {
                        // Read span
                        ParcelableSpan span = in.readParcelable(ParcelableSpan.class.getClassLoader());

                        // Read properties
                        int start = in.readInt();
                        int end = in.readInt();
                        int flags = in.readInt();

                        // Apply span
                        textBuilder.setSpan(span, start, end, flags);
                    }

                    // Save text instance
                    text = textBuilder;
                } else {
                    // Read text
                    text = in.readString();
                }
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel out, int flags) {
                // Write selection bounds
                out.writeInt(selectionEnd);
                out.writeInt(selectionStart);

                // Write whether text is spanned
                out.writeInt(text instanceof Spanned ? 1 : 0);

                // Write text
                out.writeString(text.toString());

                // If text is spanned
                if (text instanceof Spanned) {
                    Spannable textSpanned = (Spannable) text;

                    // Get all parcelable spans
                    ParcelableSpan[] spans = textSpanned.getSpans(0, textSpanned.length(), ParcelableSpan.class);

                    // Write number of spans
                    out.writeInt(spans.length);

                    // Write spans and properties iteratively
                    for (ParcelableSpan span : spans) {
                        // Write span
                        out.writeParcelable(span, 0);

                        // Write properties
                        out.writeInt(textSpanned.getSpanStart(span));
                        out.writeInt(textSpanned.getSpanEnd(span));
                        out.writeInt(textSpanned.getSpanFlags(span));
                    }
                }
            }

            public final Creator<ContentFrame> CREATOR = new Creator<ContentFrame>() {

                @Override
                public ContentFrame createFromParcel(Parcel in) {
                    return new ContentFrame(in);
                }

                @Override
                public ContentFrame[] newArray(int size) {
                    return new ContentFrame[size];
                }

            };

        }

    }

    public interface SyntaxHighlighter {

        void highlight(Editable source);

    }

    private class SyntaxHighlightingUpdateThread extends Thread {

        private static final long CHECK_PERIOD = 100l;
        private static final long HIGHLIGHT_THRESHOLD = 500l;

        private boolean highlighted;
        private long timeLastBumped;

        private volatile boolean shouldStop;

        private SyntaxHighlightingUpdateThread() {
            highlighted = true;
            timeLastBumped = System.nanoTime();

            shouldStop = false;
        }

        @Override
        public void run() {
            while (!shouldStop) {
                // If syntax highlighting is enabled
                if (enableSyntaxHighlighting && syntaxHighlighter != null) {
                    // If this bump series hasn't been highlighted and it's been long enough
                    if (!highlighted && System.nanoTime() - timeLastBumped > HIGHLIGHT_THRESHOLD * 1000000) {
                        // Run syntax highlighting
                        syntaxHighlighter.highlight(getText());

                        // Set highlighted flag
                        highlighted = true;
                    }
                }

                try {
                    Thread.sleep(CHECK_PERIOD);
                } catch (InterruptedException e) {
                    shouldStop = true;
                }
            }
        }

        public void setShouldStop(boolean shouldStop) {
            this.shouldStop = shouldStop;
        }

        public void bump() {
            // Reset for next highlight
            highlighted = false;

            // Mark time of bump
            timeLastBumped = System.nanoTime();
        }

    }

}
