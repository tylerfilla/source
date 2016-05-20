package io.microdev.source.widget.editorbinary;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.EditText;

import java.io.Reader;

import io.microdev.source.R;

public class EditorBinary extends EditText {

    private Reader reader;
    private Swapper swapper;

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
        reader = null;
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

    public Reader getReader() {
        return reader;
    }

    public void setReader(Reader reader) {
        this.reader = reader;
    }

    public Swapper getSwapper() {
        return swapper;
    }

    public void setSwapper(Swapper swapper) {
        this.swapper = swapper;
    }

    public interface Swapper {

        Block in(BlockId blockId);

        BlockId out(Block block);

    }

    public class Block {
    }

    public class BlockId {
    }

}
