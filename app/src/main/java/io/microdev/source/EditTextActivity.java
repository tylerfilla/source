package io.microdev.source;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatPopupWindow;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.microdev.source.util.Callback;
import io.microdev.source.util.DimenUtil;
import io.microdev.source.widget.editortext.EditorText;
import io.microdev.source.widget.panview.PanView;

import static io.microdev.source.util.DimenUtil.dpToPx;

public class EditTextActivity extends AppCompatActivity {

    private File file;
    private String filename;

    private Toolbar appBar;
    private PanView panView;
    private EditorText editor;

    private AppCompatPopupWindow popupMoreOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set activity layout
        setContentView(R.layout.activity_edit_text);

        // Check if a file URI was passed
        if (getIntent().getData() != null) {
            // Get file for URI
            file = new File(getIntent().getDataString());

            // Get name of file
            filename = file.getName();
        } else {
            // Nullify file
            file = null;

            // Use default filename
            filename = getString(R.string._default_file_name);
        }

        // Find stuff
        editor = (EditorText) findViewById(R.id.activityEditTextEditor);
        panView = (PanView) findViewById(R.id.activityEditTextPanView);
        appBar = (Toolbar) findViewById(R.id.activityEditTextAppBar);

        // Set action bar to custom app bar
        setSupportActionBar(appBar);

        // Enable action bar up arrow to behave as home button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Establish current filename
        setFilename(filename);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Store filename
        outState.putString("filename", filename);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Retrieve filename
        filename = savedInstanceState.getString("filename");

        // Set filename
        setFilename(filename);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate options menu
        getMenuInflater().inflate(R.menu.activity_edit_text_opts, menu);

        // Create more options popup window
        popupMoreOptions = new AppCompatPopupWindow(this, null, android.support.v7.appcompat.R.attr.popupMenuStyle) {

            @Override
            public void update() {
                // Get popup content view
                ViewGroup contentView = (ViewGroup) getContentView();

                /* Update dynamic content */

                // Get more options popup filename item
                ViewGroup itemFilename = (ViewGroup) contentView.findViewById(R.id.activityEditTextMoreOptsPopupItemFilename);

                // Get filename item text view
                TextView itemFilenameText = (TextView) itemFilename.findViewWithTag("text");

                // Build new content for text view
                SpannableStringBuilder itemFilenameTextContent = new SpannableStringBuilder();
                itemFilenameTextContent.append(filename);
                itemFilenameTextContent.setSpan(new StyleSpan(Typeface.BOLD), 0, itemFilenameTextContent.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                itemFilenameTextContent.setSpan(new RelativeSizeSpan(1.2f), 0, itemFilenameTextContent.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                // Set as item text
                itemFilenameText.setText(itemFilenameTextContent);

                /* Resize window in a Material-ish fashion */

                // Set minimum widths of all first-level children to zero
                for (int i = 0; i < contentView.getChildCount(); i++) {
                    contentView.getChildAt(i).setMinimumWidth(0);
                }

                // Measure content view
                contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

                // Get current width
                int widthCurrent = contentView.getMeasuredWidth();

                // Convert 56dp to pixels for the calculations that follow
                int _56dp = (int) DimenUtil.dpToPx(EditTextActivity.this, 56f);

                // If popup width is not divisible by 56dp
                if (widthCurrent % _56dp != 0) {
                    // Recalculate width as the next multiple of 56dp (as per Material guidelines)
                    int widthNew = widthCurrent + _56dp - (widthCurrent + _56dp) % _56dp;

                    // Update popup width
                    setWidth(widthNew);

                    // Set minimum widths of all first-level children to explicitly match popup
                    for (int i = 0; i < contentView.getChildCount(); i++) {
                        contentView.getChildAt(i).setMinimumWidth(widthNew);
                    }
                }

                // Continue with standard update procedure
                super.update();
            }

        };

        // Inflate more options popup content layout
        popupMoreOptions.setContentView(getLayoutInflater().inflate(R.layout.activity_edit_text_more_opts_popup, null));

        // Height and width wrap content
        popupMoreOptions.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupMoreOptions.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);

        // Overlap the more options button
        popupMoreOptions.setSupportOverlapAnchor(true);

        // Make outside touchable to dismiss popup when touching outside its window
        popupMoreOptions.setOutsideTouchable(true);

        // Initial update
        popupMoreOptions.update();

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Switch against item ID to try to handle it
        switch (item.getItemId()) {
        case android.R.id.home:
            // Home button pressed (configured as action bar up arrow)
            // Finish activity
            finish();
            break;
        case R.id.menuActivityEditTextOptsUndo:
            // Undo button pressed
            // Instruct editor to undo last operation
            editor.undo();
            break;
        case R.id.menuActivityEditTextOptsRedo:
            // Redo button pressed
            // Instruct editor to redo last operation
            editor.redo();
            break;
        case R.id.menuActivityEditTextOptsMoreOptions:
            // More options button pressed
            // Show more options popup anchored to more options button
            popupMoreOptions.showAsDropDown(findViewById(R.id.menuActivityEditTextOptsMoreOptions));
            break;
        default:
            // Delegate to super if not handled
            return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        // Dismiss more options popup if showing, otherwise delegate to super
        if (popupMoreOptions.isShowing()) {
            popupMoreOptions.dismiss();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void finish() {
        // Opt for task removal if device supports it
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask();
        } else {
            super.finish();
        }
    }

    public void onMoreOptsPopupItemSelected(View item) {
        // Dismiss the popup
        popupMoreOptions.dismiss();

        // Switch against item ID to try to handle it
        switch (item.getId()) {
        case R.id.activityEditTextMoreOptsPopupItemFilename:
            // Filename item selected
            // Enter rename prompt sequence
            promptRenameFile();
            break;
        case R.id.activityEditTextMoreOptsPopupItemGoto:
            // Goto item selected
            // TODO: Not implemented
            Toast.makeText(this, "Not implemented", Toast.LENGTH_SHORT).show();
            break;
        case R.id.activityEditTextMoreOptsPopupItemFind:
            // Find item selected
            // Enter find and replace sequence
            promptFindReplace();
            break;
        }
    }

    private String getFilename() {
        return filename;
    }

    private void setFilename(String filename) {
        // Change filename
        this.filename = filename;

        // Is a file set?
        if (file != null) {
            // Rename physical file
            file.renameTo(new File(file.getParentFile(), filename));
        }

        // Set activity title
        setTitle(filename);

        // Set app bar title
        appBar.setTitle(filename);

        // Update task description
        updateTaskDescription();

        // Update more options popup
        if (popupMoreOptions != null) {
            popupMoreOptions.update();
        }
    }

    private void setWordWrap(boolean wordWrap) {
        if (wordWrap) {
            // Limit editor width to screen width
            editor.setMaxWidth(findViewById(android.R.id.content).getWidth());
        } else {
            // Maximize max width
            editor.setMaxWidth(Integer.MAX_VALUE);
        }
    }

    private void updateTaskDescription() {
        // Handle task descriptions only on Lollipop and up
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Resolve app icon
            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

            // Resolve primary color
            int colorPrimary;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                colorPrimary = getResources().getColor(R.color.colorPrimary, getTheme());
            } else {
                // noinspection deprecation
                colorPrimary = getResources().getColor(R.color.colorPrimary);
            }

            // Set task description
            setTaskDescription(new ActivityManager.TaskDescription(getTitle().toString(), icon, colorPrimary));
        }
    }

    private void promptRenameFile() {
        // Display rename dialog
        displayDialogRename(new Callback<String>() {

            @Override
            public void ring(String arg) {
                // Set file name
                setFilename(arg);
            }

        });
    }

    private void displayDialogRename(final Callback<String> callback) {
        // Construct a new dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set title from XML
        builder.setTitle(R.string.dialog_activity_edit_text_rename_title);

        // Content layout for name input
        FrameLayout content = new FrameLayout(this);

        // Create a text input for name entry
        final EditText editTextName = new EditText(this);
        editTextName.setText(filename);
        editTextName.setHint(filename);
        editTextName.setSingleLine();
        editTextName.setSelectAllOnFocus(true);

        // Set margins for name input
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, getResources().getDisplayMetrics());
        layoutParams.rightMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, getResources().getDisplayMetrics());
        layoutParams.topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, getResources().getDisplayMetrics());
        editTextName.setLayoutParams(layoutParams);

        // Add name input to content
        content.addView(editTextName);

        // Add name input to dialog
        builder.setView(content);

        // Set up cancel button
        builder.setNegativeButton(R.string.dialog_activity_edit_text_rename_button_cancel_text, null);

        // Set up OK button
        builder.setPositiveButton(R.string.dialog_activity_edit_text_rename_button_rename_text, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Send new name to caller
                callback.ring(editTextName.getText().toString());
            }

        });

        // Build the dialog
        final AlertDialog dialog = builder.create();

        // Listen for dialog show
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                // Focus name input and show the soft keyboard
                if (editTextName.requestFocus()) {
                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(editTextName, InputMethodManager.SHOW_IMPLICIT);
                }
            }

        });

        // Watch for text changes in name input box
        editTextName.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Disable OK button if name box is empty or unchanged
                if (s.length() > 0 && !s.toString().equals(filename)) {
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                } else {
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

        });

        // Show the dialog
        dialog.show();

        // Initially disable the OK button
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
    }

    private void promptFindReplace() {
        // Display find and replace dialog
        displayDialogFindReplace(new Callback<DialogResultFindReplace>() {

            @Override
            public void ring(final DialogResultFindReplace result) {
                // Find occurrences in editor
                findInEditor(result.getSearch(), 0, false, !result.isEnableMatchCase(), new Callback<Integer>() {

                    @Override
                    public void ring(Integer index) {
                        // If a replacement is being made
                        if (result.isEnableReplace()) {
                            // TODO: Perform replace
                        } else {
                            // TODO: Perform find
                        }
                    }

                });
            }

        });
    }

    @SuppressWarnings("ResourceType")
    private void displayDialogFindReplace(final Callback<DialogResultFindReplace> callback) {
        // Construct a new dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set title from XML
        builder.setTitle(R.string.dialog_activity_edit_text_find_title);

        // Content layout
        RelativeLayout content = new RelativeLayout(this);

        // Set content padding as per Material design
        int contentPaddingLeft = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, getResources().getDisplayMetrics());
        int contentPaddingRight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, getResources().getDisplayMetrics());
        int contentPaddingTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, getResources().getDisplayMetrics());
        int contentPaddingBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, getResources().getDisplayMetrics());
        content.setPadding(contentPaddingLeft, contentPaddingTop, contentPaddingRight, contentPaddingBottom);

        // Text input for search text
        final EditText inputSearch = new EditText(this);
        inputSearch.setId(1);
        inputSearch.setHint(R.string.dialog_activity_edit_text_find_input_search_hint);
        inputSearch.setSingleLine();

        // Set layout parameters for search input
        inputSearch.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Create a text input for replacement text
        final EditText inputReplace = new EditText(this);
        inputReplace.setId(2);
        inputReplace.setHint(R.string.dialog_activity_edit_text_find_input_replace_hint);
        inputReplace.setVisibility(View.GONE);
        inputReplace.setSingleLine();

        // Set layout parameters for replacement input
        RelativeLayout.LayoutParams inputReplaceLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        inputReplaceLayoutParams.topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, getResources().getDisplayMetrics());
        inputReplaceLayoutParams.addRule(RelativeLayout.BELOW, inputSearch.getId());
        inputReplace.setLayoutParams(inputReplaceLayoutParams);

        // Checkbox input to enable case matching
        final CheckBox inputEnableMatchCase = new CheckBox(this);
        inputEnableMatchCase.setId(3);
        inputEnableMatchCase.setText(R.string.dialog_activity_edit_text_find_input_enable_match_case_hint);
        inputEnableMatchCase.setChecked(false);

        // Set layout parameters for enable match case input
        RelativeLayout.LayoutParams inputEnableMatchCaseLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        inputEnableMatchCaseLayoutParams.topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, getResources().getDisplayMetrics());
        inputEnableMatchCaseLayoutParams.addRule(RelativeLayout.BELOW, inputReplace.getId());
        inputEnableMatchCase.setLayoutParams(inputEnableMatchCaseLayoutParams);

        // Checkbox input to enable replacement
        final CheckBox inputEnableReplace = new CheckBox(this);
        inputEnableReplace.setId(4);
        inputEnableReplace.setText(R.string.dialog_activity_edit_text_find_input_enable_replace_hint);
        inputEnableReplace.setChecked(false);

        // Set layout parameters for enable replace input
        RelativeLayout.LayoutParams inputEnableReplaceLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        inputEnableReplaceLayoutParams.topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, getResources().getDisplayMetrics());
        inputEnableReplaceLayoutParams.addRule(RelativeLayout.BELOW, inputEnableMatchCase.getId());
        inputEnableReplace.setLayoutParams(inputEnableReplaceLayoutParams);

        // Add stuff to content layout
        content.addView(inputSearch);
        content.addView(inputReplace);
        content.addView(inputEnableMatchCase);
        content.addView(inputEnableReplace);

        // Add content to dialog
        builder.setView(content);

        // A place for all checkbox states
        final boolean[] checkState = new boolean[2];

        // Set up cancel button
        builder.setNegativeButton(R.string.dialog_activity_edit_text_find_button_cancel_text, null);

        // Set up OK button
        builder.setPositiveButton(R.string.dialog_activity_edit_text_find_button_find_text, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Create a package for result data
                DialogResultFindReplace result = new DialogResultFindReplace();
                result.setSearch(inputSearch.getText().toString());
                result.setReplace(inputReplace.getText().toString());
                result.setEnableMatchCase(checkState[0]);
                result.setEnableReplace(checkState[1]);

                // Send result to caller
                callback.ring(result);
            }

        });

        // Build the dialog
        final AlertDialog dialog = builder.create();

        // Listen for dialog show
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                // Focus search input and show the soft keyboard
                if (inputSearch.requestFocus()) {
                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(inputSearch, InputMethodManager.SHOW_IMPLICIT);
                }
            }

        });

        // Listen for changes on enable match case input
        inputEnableMatchCase.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                // Deposit enable match case state
                checkState[0] = b;
            }

        });

        // Listen for changes on enable replace input
        inputEnableReplace.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                // Deposit enable replace state
                checkState[1] = b;

                // Show/hide replacement input based on enable replace state
                if (b) {
                    // Show replacement input
                    inputReplace.setVisibility(View.VISIBLE);

                    // Rename OK button to "replace"
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(R.string.dialog_activity_edit_text_find_button_replace_text);

                    // Transfer focus to replacement input if search input isn't empty
                    if (inputSearch.length() > 0) {
                        inputReplace.requestFocus();
                    }
                } else {
                    // Hide replacement input
                    inputReplace.setVisibility(View.GONE);

                    // Rename OK button to "find"
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(R.string.dialog_activity_edit_text_find_button_find_text);

                    // Transfew focus to search input
                    inputSearch.requestFocus();
                }

                if (inputSearch.length() > 0 && (!checkState[1] || inputReplace.length() > 0)) {
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                } else {
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                }
            }

        });

        // Watch for text changes in search input
        inputSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && (!checkState[1] || inputReplace.length() > 0)) {
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                } else {
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

        });

        // Watch for text changes in replacement input
        inputReplace.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && inputSearch.length() > 0) {
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                } else {
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

        });

        // Show the dialog
        dialog.show();

        // Initially disable the OK button
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
    }

    private void findInEditor(String search, int start, boolean reverse, boolean ignoreCase, final Callback<Integer> callback) {
        // Get editor text
        String text = editor.getText().toString();

        // Deal with case insensitivity
        if (ignoreCase) {
            text = text.toLowerCase();
            search = search.toLowerCase();
        }

        // Iterate over occurrences
        int index = start;
        while ((index = reverse ? text.lastIndexOf(search, index) : text.indexOf(search, index)) > -1) {
            // Call back with index of occurrence
            callback.ring(index);

            // Start next search from the next character
            index += reverse ? -1 : 1;
        }

        // Call back with -1 to indicate end of occurrences
        callback.ring(-1);
    }

    private static class DialogResultFindReplace {

        private String search;
        private String replace;
        private boolean enableMatchCase;
        private boolean enableReplace;

        public DialogResultFindReplace() {
            search = null;
            replace = null;
        }

        public String getSearch() {
            return search;
        }

        public void setSearch(String search) {
            this.search = search;
        }

        public String getReplace() {
            return replace;
        }

        public void setReplace(String replace) {
            this.replace = replace;
        }

        public boolean isEnableMatchCase() {
            return enableMatchCase;
        }

        public void setEnableMatchCase(boolean enableMatchCase) {
            this.enableMatchCase = enableMatchCase;
        }

        public boolean isEnableReplace() {
            return enableReplace;
        }

        public void setEnableReplace(boolean enableReplace) {
            this.enableReplace = enableReplace;
        }

    }

    private static class PopupMoreOptionsAdapter extends BaseAdapter {

        private Context context;

        private List<Item> list;
        private Set<OnItemActionListener> listenerSet;

        private PopupMoreOptionsAdapter(final EditTextActivity context) {
            this.context = context;

            list = new ArrayList<>();
            listenerSet = new HashSet<>();
        }

        public List<Item> getList() {
            return list;
        }

        public void addOnItemActionListener(OnItemActionListener listener) {
            listenerSet.add(listener);
        }

        public void removeOnItemActionListener(OnItemActionListener listener) {
            listenerSet.remove(listener);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Item getItem(int i) {
            return list.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            return getItem(i).getView();
        }

        public void handleItemClick(int i) {
            getItem(i).onClick();
        }

        private void handleItemAction(Item item) {
            for (OnItemActionListener listener : listenerSet) {
                listener.onItemAction(item);
            }
        }

        public interface Item {

            View getView();

            String getTag();

            void onClick();

        }

        public static class ItemSeparator implements Item {

            private PopupMoreOptionsAdapter adapter;
            private String tag;

            public ItemSeparator(PopupMoreOptionsAdapter adapter, String tag) {
                this.adapter = adapter;
                this.tag = tag;
            }

            @Override
            public View getView() {
                View view = new View(adapter.context);

                view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) DimenUtil.dpToPx(adapter.context, 1f)));
                view.setBackgroundColor(0xffdddddd);

                return view;
            }

            @Override
            public String getTag() {
                return tag;
            }

            @Override
            public void onClick() {
                adapter.handleItemAction(this);

                System.out.println("You deserve a medal.");
            }

        }

        public static class ItemText implements Item {

            private PopupMoreOptionsAdapter adapter;
            private String tag;
            private CharSequence text;

            public ItemText(PopupMoreOptionsAdapter adapter, String tag, CharSequence text) {
                this.adapter = adapter;
                this.tag = tag;
                this.text = text;
            }

            public CharSequence getText() {
                return text;
            }

            public void setText(CharSequence text) {
                this.text = text;
            }

            @Override
            public View getView() {
                TextView textView = new TextView(adapter.context);

                // noinspection deprecation
                textView.setTextAppearance(adapter.context, android.R.style.TextAppearance_DeviceDefault_Widget_PopupMenu);
                textView.setTextSize(16f);
                textView.setPadding((int) dpToPx(adapter.context, 16f), 0, (int) dpToPx(adapter.context, 16f), 0);
                textView.setHeight((int) dpToPx(adapter.context, 48f));
                textView.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                textView.setText(text);

                return textView;
            }

            @Override
            public String getTag() {
                return tag;
            }

            @Override
            public void onClick() {
                adapter.handleItemAction(this);
            }

        }

        public static class ItemSwitch extends ItemText {

            private PopupMoreOptionsAdapter adapter;
            private boolean state;

            private SwitchCompat viewSwitch;

            private ItemSwitch(PopupMoreOptionsAdapter adapter, String tag, CharSequence text, boolean state) {
                super(adapter, tag, text);

                this.adapter = adapter;
                this.state = state;
            }

            public boolean getState() {
                return state;
            }

            public void setState(boolean state) {
                this.state = state;
            }

            @Override
            public View getView() {
                // Root layout for this item
                RelativeLayout view = new RelativeLayout(adapter.context);

                // Configure menu item layout params
                view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                // Get base text view from super
                View viewBase = super.getView();

                // Configure switch layout params
                RelativeLayout.LayoutParams viewBaseLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                viewBase.setLayoutParams(viewBaseLayoutParams);

                // Create a switch control
                viewSwitch = new SwitchCompat(adapter.context);

                // Configure switch layout params
                RelativeLayout.LayoutParams viewSwitchLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                viewSwitchLayoutParams.rightMargin = (int) DimenUtil.dpToPx(adapter.context, 16f);
                viewSwitchLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                viewSwitchLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
                viewSwitch.setLayoutParams(viewSwitchLayoutParams);

                // Let the menu do all the talking
                viewBase.setFocusable(false);
                viewSwitch.setFocusable(false);

                // Add components to root view
                view.addView(viewBase);
                view.addView(viewSwitch);

                // Handle switch state
                viewSwitch.setChecked(state);
                viewSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        state = b;

                        adapter.handleItemAction(ItemSwitch.this);
                    }

                });

                return view;
            }

            @Override
            public void onClick() {
                // Toggle internal state
                state = !state;

                // Toggle external state
                if (viewSwitch != null) {
                    viewSwitch.toggle();
                }

                adapter.handleItemAction(this);
            }

        }

        public interface OnItemActionListener {

            void onItemAction(Item item);

        }

    }

}
