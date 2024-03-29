package io.microdev.source;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gmail.tylerfilla.widget.panview.OnPanChangedListener;
import com.gmail.tylerfilla.widget.panview.OnPanStoppedListener;
import com.gmail.tylerfilla.widget.panview.PanView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.microdev.source.util.Callback;
import io.microdev.source.util.IdGen;
import io.microdev.source.widget.PseudoPopupMenu;
import io.microdev.source.widget.editor.Editor;

import static io.microdev.source.util.DimenUtil.dpToPxI;

public class EditActivity extends AppCompatActivity {

    private File file;
    private String filename;

    private Toolbar appBar;
    private PanView panView;
    private Editor editor;

    private PopupMoreOptions popupMoreOptions;
    private PopupContextFindReplace popupContextFindReplace;

    private boolean withinFindReplace;
    private int findReplaceSelectionStart;
    private int findReplaceSelectionEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set activity layout
        setContentView(R.layout.activity_edit);

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
        appBar = (Toolbar) findViewById(R.id.activity_edit_app_bar);
        panView = (PanView) findViewById(R.id.activity_edit_panview);
        editor = (Editor) findViewById(R.id.activity_edit_editor);

        // Set action bar to custom app bar
        setSupportActionBar(appBar);

        // Enable action bar up arrow
        appBar.setNavigationIcon(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_mtrl_am_alpha);

        // Listen for editor text changes
        editor.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // If within find and replace operation
                if (withinFindReplace) {
                    // Cancel find and replace operation
                    withinFindReplace = false;

                    // Dismiss the find and replace context popup if it is showing
                    if (popupContextFindReplace.isShowing()) {
                        popupContextFindReplace.dismiss();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

        });

        // Create find and replace context menu
        popupContextFindReplace = new PopupContextFindReplace(this);

        // Listen for pan changes
        panView.addOnPanChangedListener(new OnPanChangedListener() {

            @Override
            public void onPanChanged(final int l, final int t, final int oldl, final int oldt) {
                // If within find and replace operation
                if (withinFindReplace) {
                    // Dismiss the find and replace context popup if it is showing
                    if (popupContextFindReplace.isShowing()) {
                        popupContextFindReplace.dismiss();
                    }
                }
            }

        });

        // Listen for pan stops
        panView.addOnPanStoppedListener(new OnPanStoppedListener() {

            @Override
            public void onPanStopped() {
                // If within find and replace operation
                if (withinFindReplace) {
                    // If the find and replace context popup is not showing
                    if (!popupContextFindReplace.isShowing()) {
                        // Check selection bounds
                        if (editor.getSelectionStart() == findReplaceSelectionStart && editor.getSelectionEnd() == findReplaceSelectionEnd) {
                            // Select occurrence text
                            editor.setSelection(findReplaceSelectionStart, findReplaceSelectionEnd);

                            // Show find and replace popup
                            showPopupContextFindReplace();
                        } else {
                            // Cancel find and replace operation
                            withinFindReplace = false;
                        }
                    }
                }
            }

        });

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
        setFilename(savedInstanceState.getString("filename", filename));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate options menu
        getMenuInflater().inflate(R.menu.activity_edit_options, menu);

        // Create more options popup window
        popupMoreOptions = new PopupMoreOptions(this);

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
        case R.id.activity_edit_menu_options_item_undo:
            // Undo button pressed
            // Instruct editor to undo last operation
            editor.undo();
            break;
        case R.id.activity_edit_menu_options_item_redo:
            // Redo button pressed
            // Instruct editor to redo last operation
            editor.redo();
            break;
        case R.id.activity_edit_menu_options_item_more_options:
            // More options button pressed
            // Show more options popup anchored to more options button
            popupMoreOptions.showAsDropDown(findViewById(R.id.activity_edit_menu_options_item_more_options));
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
        case R.id.activity_edit_popup_more_options_item_rename:
            // Filename item selected
            // Enter rename prompt sequence
            promptRenameFile();
            break;
        case R.id.activity_edit_popup_more_options_item_goto:
            // Goto item selected
            // Not implemented
            promptGoto();
            break;
        case R.id.activity_edit_popup_more_options_item_find_replace:
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
        // Get editor layout parameters
        ViewGroup.LayoutParams layoutParams = editor.getLayoutParams();

        // If word wrap should be enabled
        if (wordWrap) {
            // Make editor width match screen width
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        } else {
            // Allow editor to expand horizontally
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        }

        // Update editor layout parameters
        editor.setLayoutParams(layoutParams);
    }

    private void updateTaskDescription() {
        // Handle task descriptions only on Lollipop and up
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Resolve app icon
            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

            // Resolve primary color
            int colorPrimary;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                colorPrimary = getResources().getColor(R.color.app_theme_color_primary, getTheme());
            } else {
                // noinspection deprecation
                colorPrimary = getResources().getColor(R.color.app_theme_color_primary);
            }

            // Set task description
            setTaskDescription(new ActivityManager.TaskDescription(getTitle().toString(), icon, colorPrimary));
        }
    }

    private void promptRenameFile() {
        // Display rename dialog
        displayDialogRename(new Callback<String>() {

            @Override
            public void ring(String filename) {
                // Set filename
                setFilename(filename);
            }

        });
    }

    private void displayDialogRename(final Callback<String> callback) {
        // Construct a new dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set title from XML
        builder.setTitle(R.string.activity_edit_dialog_rename_title);

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
        layoutParams.leftMargin = dpToPxI(this, 24f);
        layoutParams.rightMargin = dpToPxI(this, 24f);
        layoutParams.topMargin = dpToPxI(this, 10f);
        editTextName.setLayoutParams(layoutParams);

        // Add name input to content
        content.addView(editTextName);

        // Add name input to dialog
        builder.setView(content);

        // Set up cancel button
        builder.setNegativeButton(R.string.activity_edit_dialog_rename_button_cancel_text, null);

        // Set up OK button
        builder.setPositiveButton(R.string.activity_edit_dialog_rename_button_rename_text, new DialogInterface.OnClickListener() {

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

    private void promptGoto() {
        // Display goto dialog
        displayDialogGoto(new Callback<GotoDialogResult>() {

            @Override
            public void ring(GotoDialogResult result) {
                System.out.println("Go to offset " + result.getValue());
            }

        });
    }

    private void displayDialogGoto(final Callback<GotoDialogResult> callback) {
        // Construct a new dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set dialog title
        builder.setTitle(R.string.activity_edit_dialog_goto_title);

        // Content layout
        LinearLayout content = new LinearLayout(this);

        // Set padding as per Material design
        content.setPadding(dpToPxI(this, 24f), dpToPxI(this, 10f), dpToPxI(this, 24f), dpToPxI(this, 10f));

        // Set vertical orientation
        content.setOrientation(LinearLayout.VERTICAL);

        // Text input for goto
        final EditText inputGoto = new EditText(this);
        inputGoto.setId(IdGen.next());
        inputGoto.setHint(R.string.activity_edit_dialog_goto_input_goto_hint_type_line);
        inputGoto.setInputType(InputType.TYPE_CLASS_NUMBER);
        inputGoto.setSingleLine();

        // Radio group to choose goto type
        final RadioGroup inputGroupChooseTypeGoto = new RadioGroup(this);
        inputGroupChooseTypeGoto.setId(IdGen.next());
        inputGroupChooseTypeGoto.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams inputGroupChooseTypeGotoLP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        inputGroupChooseTypeGotoLP.topMargin = dpToPxI(this, 5f);
        inputGroupChooseTypeGoto.setLayoutParams(inputGroupChooseTypeGotoLP);

        // Radio button to enable line input
        final RadioButton inputChooseGotoTypeLine = new RadioButton(this);
        inputChooseGotoTypeLine.setId(IdGen.next());
        inputChooseGotoTypeLine.setText(R.string.activity_edit_dialog_goto_input_enable_line_input_text);
        inputChooseGotoTypeLine.setChecked(true);
        inputGroupChooseTypeGoto.addView(inputChooseGotoTypeLine);

        // Radio button to enable offset input
        final RadioButton inputChooseGotoTypeOffset = new RadioButton(this);
        inputChooseGotoTypeOffset.setId(IdGen.next());
        inputChooseGotoTypeOffset.setText(R.string.activity_edit_dialog_goto_input_enable_offset_input_text);
        inputChooseGotoTypeOffset.setChecked(false);
        inputGroupChooseTypeGoto.addView(inputChooseGotoTypeOffset);

        // Add inputs to content
        content.addView(inputGoto);
        content.addView(inputGroupChooseTypeGoto);

        // Add content to dialog
        builder.setView(content);

        // Set up negative button
        builder.setNegativeButton(R.string.activity_edit_dialog_goto_button_negative_text, null);

        // Set up positive button
        builder.setPositiveButton(R.string.activity_edit_dialog_goto_button_positive_text, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                GotoDialogResult result = new GotoDialogResult();

                result.setValue(Integer.parseInt(inputGoto.getText().toString()));

                if (inputGroupChooseTypeGoto.getCheckedRadioButtonId() == inputChooseGotoTypeLine.getId()) {
                    result.setType(GotoDialogResult.GotoType.LINE);
                } else if (inputGroupChooseTypeGoto.getCheckedRadioButtonId() == inputChooseGotoTypeOffset.getId()) {
                    result.setType(GotoDialogResult.GotoType.OFFSET);
                }

                // Send offset to caller
                callback.ring(result);
            }

        });

        // Build the dialog
        final AlertDialog dialog = builder.create();

        // Listen for dialog show
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                // Focus offset input and show the soft keyboard
                if (inputGoto.requestFocus()) {
                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(inputGoto, InputMethodManager.SHOW_IMPLICIT);
                }
            }

        });

        // Listen for goto type changes
        inputGroupChooseTypeGoto.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == inputChooseGotoTypeLine.getId()) {
                    // Show line hint text on goto input
                    inputGoto.setHint(R.string.activity_edit_dialog_goto_input_goto_hint_type_line);
                } else if (checkedId == inputChooseGotoTypeOffset.getId()) {
                    // Show offset hint text on goto input
                    inputGoto.setHint(R.string.activity_edit_dialog_goto_input_goto_hint_type_offset);
                }
            }

        });

        inputGoto.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (inputGoto.length() > 0) {
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

        // Initially disable the goto button
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
    }

    private void promptFindReplace() {
        // Display find and replace dialog
        displayDialogFindReplace(new Callback<FindReplaceDialogResult>() {

            @Override
            public void ring(final FindReplaceDialogResult resultDialog) {
                // Appears to detach keyboard from editor enough to not make scrolling spazzy
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(editor.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                // Find occurrences in editor
                findInEditor(resultDialog.getSearch(), 0, false, !resultDialog.isEnableMatchCase(), new Callback<EditorFindResult>() {

                    @Override
                    public void ring(final EditorFindResult resultFind) {
                        // If reached end of occurrences
                        if (resultFind.getOffset() == -1) {
                            // Cancel find and replace operation
                            withinFindReplace = false;

                            // Dismiss find and replace dialog if it is showing
                            if (popupContextFindReplace.isShowing()) {
                                popupContextFindReplace.dismiss();
                            }

                            // Re-enable editor focus
                            editor.setFocusable(true);

                            // If no occurrences were found
                            if (resultFind.getOccurrenceTotal() == 0) {
                                // Notify the user
                                Snackbar.make(editor, getString(R.string.activity_edit_snackbar_find_replace_result_text_fail, resultDialog.getSearch()), Snackbar.LENGTH_SHORT).show();
                            } else {
                                // No occurrences remain, notify user
                                Snackbar.make(editor, R.string.activity_edit_snackbar_find_replace_result_text_end, Snackbar.LENGTH_SHORT).show();
                            }
                        } else {
                            // Get offset from result
                            int offset = resultFind.getOffset();

                            // Set within find and replace operation
                            withinFindReplace = true;

                            // Set find and replace bounds
                            findReplaceSelectionStart = offset;
                            findReplaceSelectionEnd = offset + resultDialog.getSearch().length();

                            // Get popup content view
                            View popupContentView = popupContextFindReplace.getContentView();

                            // Get contents and stuff
                            View buttonReplace = popupContentView.findViewById(R.id.activity_edit_popup_find_replace_item_replace);
                            View buttonNext = popupContentView.findViewById(R.id.activity_edit_popup_find_replace_item_next);
                            View buttonPrevious = popupContentView.findViewById(R.id.activity_edit_popup_find_replace_item_previous);

                            // Show replace button if replacing, else hide it
                            if (resultDialog.isEnableReplace()) {
                                buttonReplace.setVisibility(View.VISIBLE);
                            } else {
                                buttonReplace.setVisibility(View.GONE);
                            }

                            // Show next button if a valid occurrence follows, else hide it
                            if (resultFind.getNextValidForward() != -1) {
                                buttonNext.setVisibility(View.VISIBLE);
                            } else {
                                buttonNext.setVisibility(View.GONE);
                            }

                            // Show previous button if a valid occurrence precedes, else hide it
                            if (resultFind.getNextValidBackward() != -1) {
                                buttonPrevious.setVisibility(View.VISIBLE);
                            } else {
                                buttonPrevious.setVisibility(View.GONE);
                            }

                            // Listen for replace button clicks
                            buttonReplace.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    // Dismiss popup
                                    popupContextFindReplace.dismiss();

                                    // Replace text
                                    editor.getText().replace(findReplaceSelectionStart, findReplaceSelectionEnd, resultDialog.getReplace());

                                    // Replace occurrence
                                    resultFind.dispatchResponse(new EditorFindResult.Response(EditorFindResult.Response.Type.REPLACE, resultDialog.getReplace().length() - resultDialog.getSearch().length()));
                                }

                            });

                            // Listen for next button clicks
                            buttonNext.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    // Dismiss popup
                                    popupContextFindReplace.dismiss();

                                    // Move to next occurrence
                                    resultFind.dispatchResponse(new EditorFindResult.Response(EditorFindResult.Response.Type.NEXT, null));
                                }

                            });

                            // Listen for previous button clicks
                            buttonPrevious.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    // Dismiss popup
                                    popupContextFindReplace.dismiss();

                                    // Move to previous occurrence
                                    resultFind.dispatchResponse(new EditorFindResult.Response(EditorFindResult.Response.Type.PREVIOUS, null));
                                }

                            });

                            // Re-measure the content view
                            popupContentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

                            // Get editor text layout
                            Layout layout = editor.getLayout();

                            // Calculate number of lines spanned by the selection
                            int selectionLineSpan = 1 + layout.getLineForOffset(findReplaceSelectionEnd) - layout.getLineForOffset(findReplaceSelectionStart);

                            // Interesting stuff about selection bounds
                            int selectionLeft = 0;
                            int selectionRight = 0;
                            int selectionTop = editor.getTop() + layout.getLineTop(layout.getLineForOffset(findReplaceSelectionStart));
                            int selectionBottom = editor.getTop() + layout.getLineBottom(layout.getLineForOffset(findReplaceSelectionEnd));

                            // If selection only spans one line
                            if (selectionLineSpan == 1) {
                                // Get stuff about selection
                                selectionLeft = editor.getLeft() + (int) editor.getLineNumberColumnWidth() + (int) layout.getPrimaryHorizontal(findReplaceSelectionStart);
                                selectionRight = editor.getLeft() + (int) editor.getLineNumberColumnWidth() + (int) layout.getPrimaryHorizontal(findReplaceSelectionEnd);
                            } else {
                                // Get stuff about selection
                                selectionLeft = editor.getLeft() + (int) editor.getLineNumberColumnWidth();
                                selectionRight = editor.getLeft() + (int) editor.getLineNumberColumnWidth() + editor.getWidth();
                            }

                            // Calculate pan coordinates for optimum user interaction
                            int panX = selectionLeft - (panView.getWidth() - (selectionRight - selectionLeft)) / 2;
                            int panY = selectionBottom - panView.getHeight() + Math.max(panView.getHeight() / 2, popupContentView.getMeasuredHeight() + 2 * editor.getLineHeight());

                            // Pan to these coordinates
                            panView.smoothPanTo(panX, panY);

                            editor.postDelayed(new Runnable() {

                                @Override
                                public void run() {
                                    // Select occurrence text
                                    editor.setSelection(findReplaceSelectionStart, findReplaceSelectionEnd);

                                    // Show find and replace popup
                                    showPopupContextFindReplace();
                                }

                            }, getResources().getInteger(android.R.integer.config_mediumAnimTime));
                        }
                    }

                });
            }

        });
    }

    private void displayDialogFindReplace(final Callback<FindReplaceDialogResult> callback) {
        // Construct a new dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set title from XML
        builder.setTitle(R.string.activity_edit_dialog_find_replace_title);

        // Content layout
        RelativeLayout content = new RelativeLayout(this);

        // Set content padding as per Material design
        int contentPaddingLeft = dpToPxI(this, 24f);
        int contentPaddingRight = dpToPxI(this, 24f);
        int contentPaddingTop = dpToPxI(this, 10f);
        int contentPaddingBottom = dpToPxI(this, 10f);
        content.setPadding(contentPaddingLeft, contentPaddingTop, contentPaddingRight, contentPaddingBottom);

        // Text input for search text
        final EditText inputSearch = new EditText(this);
        inputSearch.setId(IdGen.next());
        inputSearch.setHint(R.string.activity_edit_dialog_find_replace_input_search_hint);
        inputSearch.setSingleLine();

        // Set layout parameters for search input
        inputSearch.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Create a text input for replacement text
        final EditText inputReplace = new EditText(this);
        inputReplace.setId(IdGen.next());
        inputReplace.setHint(R.string.activity_edit_dialog_find_replace_input_replace_hint);
        inputReplace.setVisibility(View.GONE);
        inputReplace.setSingleLine();

        // Set layout parameters for replacement input
        RelativeLayout.LayoutParams inputReplaceLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        inputReplaceLayoutParams.topMargin = dpToPxI(this, 5f);
        inputReplaceLayoutParams.addRule(RelativeLayout.BELOW, inputSearch.getId());
        inputReplace.setLayoutParams(inputReplaceLayoutParams);

        // Checkbox input to enable case matching
        final CheckBox inputEnableMatchCase = new CheckBox(this);
        inputEnableMatchCase.setId(IdGen.next());
        inputEnableMatchCase.setText(R.string.activity_edit_dialog_find_replace_input_enable_match_case_hint);
        inputEnableMatchCase.setChecked(false);

        // Set layout parameters for enable match case input
        RelativeLayout.LayoutParams inputEnableMatchCaseLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        inputEnableMatchCaseLayoutParams.topMargin = dpToPxI(this, 10f);
        inputEnableMatchCaseLayoutParams.addRule(RelativeLayout.BELOW, inputReplace.getId());
        inputEnableMatchCase.setLayoutParams(inputEnableMatchCaseLayoutParams);

        // Checkbox input to enable replacement
        final CheckBox inputEnableReplace = new CheckBox(this);
        inputEnableReplace.setId(IdGen.next());
        inputEnableReplace.setText(R.string.activity_edit_dialog_find_replace_input_enable_replace_hint);
        inputEnableReplace.setChecked(false);

        // Set layout parameters for enable replace input
        RelativeLayout.LayoutParams inputEnableReplaceLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        inputEnableReplaceLayoutParams.topMargin = dpToPxI(this, 5f);
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
        builder.setNegativeButton(R.string.activity_edit_dialog_find_replace_button_negative_text, null);

        // Set up OK button
        builder.setPositiveButton(R.string.activity_edit_dialog_find_replace_button_positive_text_find, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Create a package for result data
                FindReplaceDialogResult result = new FindReplaceDialogResult();
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
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(R.string.activity_edit_dialog_find_replace_button_positive_text_replace);

                    // Transfer focus to replacement input if search input isn't empty
                    if (inputSearch.length() > 0) {
                        inputReplace.requestFocus();
                    }
                } else {
                    // Hide replacement input
                    inputReplace.setVisibility(View.GONE);

                    // Rename OK button to "find"
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(R.string.activity_edit_dialog_find_replace_button_positive_text_find);

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

    private void showPopupContextFindReplace() {
        // Get popup content view
        View popupContentView = popupContextFindReplace.getContentView();

        // Get editor layout
        Layout layout = editor.getLayout();

        // Sanity check (we can't really do much if this fails, but it shouldn't kill the app)
        if (layout != null) {
            // Measure popup contents
            popupContentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

            // Calculate number of lines spanned by the selection
            int selectionLineSpan = 1 + layout.getLineForOffset(findReplaceSelectionEnd) - layout.getLineForOffset(findReplaceSelectionStart);

            // Interesting stuff about selection bounds
            int selectionLeft = 0;
            int selectionRight = 0;
            int selectionTop = editor.getTop() + layout.getLineTop(layout.getLineForOffset(findReplaceSelectionStart));
            int selectionBottom = editor.getTop() + layout.getLineBottom(layout.getLineForOffset(findReplaceSelectionEnd));

            // If selection only spans one line
            if (selectionLineSpan == 1) {
                // Get stuff about selection
                selectionLeft = editor.getLeft() + (int) editor.getLineNumberColumnWidth() + (int) layout.getPrimaryHorizontal(findReplaceSelectionStart);
                selectionRight = editor.getLeft() + (int) editor.getLineNumberColumnWidth() + (int) layout.getPrimaryHorizontal(findReplaceSelectionEnd);
            } else {
                // Get stuff about selection
                selectionLeft = editor.getLeft() + (int) editor.getLineNumberColumnWidth();
                selectionRight = editor.getLeft() + (int) editor.getLineNumberColumnWidth() + editor.getWidth();
            }

            // Calculate coordinates for popup
            int popupX = editor.getLeft() + selectionLeft + panView.getLeft() - panView.getPanX();
            int popupY = editor.getTop() + selectionBottom + panView.getTop() - panView.getPanY() + 2 * editor.getLineHeight();

            // If popup exceeds space below selection
            if (popupY + popupContentView.getMeasuredHeight() > panView.getBottom()) {
                // Move popup above text
                popupY = editor.getTop() + selectionTop + panView.getTop() - panView.getPanY() - popupContentView.getMeasuredHeight();
            }

            // Show popup
            if (popupContextFindReplace.isShowing()) {
                popupContextFindReplace.update(popupX, popupY, -1, -1);
            } else {
                popupContextFindReplace.showAtLocation(editor, Gravity.NO_GRAVITY, popupX, popupY);
                popupContextFindReplace.update();
            }
        }
    }

    private void findInEditor(String search, int start, boolean reverse, boolean ignoreCase, final Callback<EditorFindResult> callback) {
        // Get editor text
        String text = editor.getText().toString();

        // Deal with case insensitivity
        if (ignoreCase) {
            text = text.toLowerCase();
            search = search.toLowerCase();
        }

        // List to store occurrence indices
        final List<Integer> occurrenceOffsetList = new ArrayList<>();

        // Iterate over occurrences
        int offset = start;
        while ((offset = reverse ? text.lastIndexOf(search, offset) : text.indexOf(search, offset)) > -1) {
            // Store occurrence index
            occurrenceOffsetList.add(offset);

            // Start next search from the next character
            offset += reverse ? -1 : 1;
        }

        // Notify caller if no occurrences were found with an invalid result
        if (occurrenceOffsetList.isEmpty()) {
            callback.ring(EditorFindResult.buildInvalidReference(occurrenceOffsetList.size()));
            return;
        }

        // Final package to hold iterator across callbacks
        final int[] i = new int[] { 0 };

        // Create result to represent first occurrence
        EditorFindResult resultFirst = new EditorFindResult();

        // Configure first result
        resultFirst.setOffset(occurrenceOffsetList.get(i[0]));
        resultFirst.setOccurrenceCurrent(i[0]);
        resultFirst.setOccurrenceTotal(occurrenceOffsetList.size());

        // If more than one occurrence found
        if (occurrenceOffsetList.size() > 1) {
            // Next valid forward occurrence is adjacent in forward direction
            resultFirst.setNextValidForward(1);
        }

        // Create callback to receive first response
        resultFirst.setResponseCallback(new Callback<EditorFindResult.Response>() {

            @Override
            public void ring(EditorFindResult.Response response) {
                // Next valid occurrences in either direction
                int nextValidForward = -1;
                int nextValidBackward = -1;

                // Search forward for next valid occurrence
                int searchForward = i[0];
                while (true) {
                    // Increment iterator
                    searchForward++;

                    // If we hit upper boundary
                    if (searchForward > occurrenceOffsetList.size() - 1) {
                        nextValidForward = -1;
                        break;
                    }

                    // If this is a valid occurrence
                    if (occurrenceOffsetList.get(searchForward) != -1) {
                        nextValidForward = searchForward;
                        break;
                    }
                }

                // Search backward for next valid occurrence
                int searchBackward = i[0];
                while (true) {
                    // Decrement iterator
                    searchBackward--;

                    // If we hit lower boundary
                    if (searchBackward < 0) {
                        nextValidBackward = -1;
                        break;
                    }

                    // If this is a valid occurrence
                    if (occurrenceOffsetList.get(searchBackward) != -1) {
                        nextValidBackward = searchBackward;
                        break;
                    }
                }

                // Switch against response
                switch (response.getType()) {
                case REPLACE:
                    // Mark this offset as invalid (replaced) without affecting other occurrences
                    occurrenceOffsetList.set(i[0], -1);

                    // Shift following valid occurrences by difference due to replacement
                    for (int j = i[0]; j < occurrenceOffsetList.size(); j++) {
                        // Skip current
                        if (j == i[0]) {
                            continue;
                        }

                        // For replacements, the payload is the difference between new and old lengths
                        if (occurrenceOffsetList.get(j) != -1) {
                            occurrenceOffsetList.set(j, occurrenceOffsetList.get(j) + (int) response.getPayload());
                        }
                    }

                    // Move to next valid occurrence (try forward, then backward)
                    if (nextValidForward != -1) {
                        i[0] = nextValidForward;
                    } else if (nextValidBackward != -1) {
                        i[0] = nextValidBackward;
                    } else {
                        // Notify caller of end of occurrences
                        callback.ring(EditorFindResult.buildInvalidReference(occurrenceOffsetList.size()));
                        return;
                    }
                    break;
                case NEXT:
                    // Move forwards to next valid occurrence
                    i[0] = nextValidForward;
                    break;
                case PREVIOUS:
                    // Move backwards to next valid occurrence
                    i[0] = nextValidBackward;
                    break;
                }

                // Search forward again for next valid occurrence
                searchForward = i[0];
                while (true) {
                    // Increment iterator
                    searchForward++;

                    // If we hit upper boundary
                    if (searchForward > occurrenceOffsetList.size() - 1) {
                        nextValidForward = -1;
                        break;
                    }

                    // If this is a valid occurrence
                    if (occurrenceOffsetList.get(searchForward) != -1) {
                        nextValidForward = searchForward;
                        break;
                    }
                }

                // Search backward again for next valid occurrence
                searchBackward = i[0];
                while (true) {
                    // Decrement iterator
                    searchBackward--;

                    // If we hit lower boundary
                    if (searchBackward < 0) {
                        nextValidBackward = -1;
                        break;
                    }

                    // If this is a valid occurrence
                    if (occurrenceOffsetList.get(searchBackward) != -1) {
                        nextValidBackward = searchBackward;
                        break;
                    }
                }

                // Prepare next result
                EditorFindResult resultNext = new EditorFindResult();

                // Configure result
                resultNext.setOffset(occurrenceOffsetList.get(i[0]));
                resultNext.setOccurrenceCurrent(i[0]);
                resultNext.setOccurrenceTotal(occurrenceOffsetList.size());
                resultNext.setNextValidBackward(nextValidBackward);
                resultNext.setNextValidForward(nextValidForward);

                // Iterate over occurrences
                for (int i = 0; i < occurrenceOffsetList.size(); i++) {
                    // Flag each occurrence as either valid or invalid
                    resultNext.setOccurrenceValid(i, occurrenceOffsetList.get(i) != -1);
                }

                // Use this same callback for next result, enabling recursive iteration
                resultNext.setResponseCallback(this);

                // Call back with next result
                callback.ring(resultNext);
            }

        });

        // Call back with first result
        callback.ring(resultFirst);
    }

    private static class EditorFindResult {

        private int offset;
        private int occurrenceCurrent;
        private int occurrenceTotal;

        private int nextValidBackward;
        private int nextValidForward;

        private Map<Integer, Boolean> validityMap;

        private Callback<Response> responseCallback;

        public EditorFindResult() {
            offset = -1;
            occurrenceCurrent = -1;
            occurrenceTotal = 0;

            nextValidBackward = -1;
            nextValidForward = -1;

            validityMap = new HashMap<>();
        }

        public int getOffset() {
            return offset;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        public int getOccurrenceCurrent() {
            return occurrenceCurrent;
        }

        public void setOccurrenceCurrent(int occurrenceCurrent) {
            this.occurrenceCurrent = occurrenceCurrent;
        }

        public int getOccurrenceTotal() {
            return occurrenceTotal;
        }

        public void setOccurrenceTotal(int occurrenceTotal) {
            this.occurrenceTotal = occurrenceTotal;
        }

        public int getNextValidBackward() {
            return nextValidBackward;
        }

        public void setNextValidBackward(int nextValidBackward) {
            this.nextValidBackward = nextValidBackward;
        }

        public int getNextValidForward() {
            return nextValidForward;
        }

        public void setNextValidForward(int nextValidForward) {
            this.nextValidForward = nextValidForward;
        }

        public boolean getOccurenceValid(int index) {
            if (validityMap.containsKey(index)) {
                return validityMap.get(index);
            }

            return true;
        }

        public void setOccurrenceValid(int index, boolean valid) {
            validityMap.put(index, valid);
        }

        public Callback<Response> getResponseCallback() {
            return responseCallback;
        }

        public void setResponseCallback(Callback<Response> responseCallback) {
            this.responseCallback = responseCallback;
        }

        public void dispatchResponse(Response response) {
            responseCallback.ring(response);
        }

        public static EditorFindResult buildInvalidReference(int occurrenceTotal) {
            EditorFindResult result = new EditorFindResult();

            result.setOffset(-1);
            result.setOccurrenceTotal(occurrenceTotal);

            return result;
        }

        public static class Response {

            private Type type;
            private Object payload;

            public Response(Type type, Object payload) {
                this.type = type;
                this.payload = payload;
            }

            public Type getType() {
                return type;
            }

            public Object getPayload() {
                return payload;
            }

            public enum Type {

                REPLACE,
                NEXT,
                PREVIOUS

            }

        }

    }

    private static class GotoDialogResult {

        private int value;
        private GotoType type;

        public GotoDialogResult() {
            value = -1;
            type = null;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public GotoType getType() {
            return type;
        }

        public void setType(GotoType type) {
            this.type = type;
        }

        public enum GotoType {

            LINE,
            OFFSET

        }

    }

    private static class FindReplaceDialogResult {

        private String search;
        private String replace;
        private boolean enableMatchCase;
        private boolean enableReplace;

        public FindReplaceDialogResult() {
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

    private static class PopupMoreOptions extends PseudoPopupMenu {

        private EditActivity editActivity;

        public PopupMoreOptions(EditActivity activity) {
            super(activity);

            this.editActivity = activity;

            // Inflate more options popup content layout
            setContentView(activity.getLayoutInflater().inflate(R.layout.activity_edit_popup_more_options, null));

            // Overlap the more options button
            setSupportOverlapAnchor(true);

            // Initial update
            update();
        }

        @Override
        public void update() {
            // Get popup content view
            ViewGroup contentView = (ViewGroup) getContentView();

            // Get more options popup filename item
            ViewGroup itemFilename = (ViewGroup) contentView.findViewById(R.id.activity_edit_popup_more_options_item_rename);

            // Get filename item text view
            TextView itemFilenameText = (TextView) itemFilename.findViewWithTag("text");

            // Build new content for text view
            SpannableStringBuilder itemFilenameTextContent = new SpannableStringBuilder();
            itemFilenameTextContent.append(editActivity.filename);
            itemFilenameTextContent.setSpan(new StyleSpan(Typeface.BOLD), 0, itemFilenameTextContent.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            itemFilenameTextContent.setSpan(new RelativeSizeSpan(1.2f), 0, itemFilenameTextContent.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Set as item text
            itemFilenameText.setText(itemFilenameTextContent);

            super.update();
        }

    }

    private static class PopupContextFindReplace extends PseudoPopupMenu {

        private EditActivity activity;

        public PopupContextFindReplace(EditActivity activity) {
            super(activity);

            this.activity = activity;

            // Inflate find and replace context menu
            setContentView(activity.getLayoutInflater().inflate(R.layout.activity_edit_popup_find_replace, null));

            // Initial update
            update();
        }

    }

}
