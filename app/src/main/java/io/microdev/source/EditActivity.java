package io.microdev.source;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.microdev.source.util.Callback;
import io.microdev.source.util.DimenUtil;
import io.microdev.source.widget.editortext.EditorText;

import static io.microdev.source.util.DimenUtil.dpToPx;

public class EditActivity extends AppCompatActivity {

    private File file;

    private Toolbar toolbar;
    private EditorText editor;

    //private PopupWindow popupMoreOptions;
    private ListPopupWindow popupMoreOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set activity layout
        setContentView(R.layout.activity_edit);

        // Check if a file URI was passed
        if (getIntent().getData() != null) {
            // Get file for URI
            file = new File(getIntent().getDataString());
        }

        // Find editor view
        editor = (EditorText) findViewById(R.id.activityEditEditor);

        // Find toolbar
        toolbar = (Toolbar) findViewById(R.id.activityEditToolbar);

        // Set action bar to custom toolbar
        setSupportActionBar(toolbar);

        // Enable action bar up arrow to behave as home button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Decide action bar title based on file presence
        if (file == null) {
            // Default title
            toolbar.setTitle(getString(R.string._default_document_name));
        } else {
            // Set title to file name
            toolbar.setTitle(file.getName());
        }

        // Handle task descriptions on Lollipop+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Resolve app icon
            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

            // Resolve primary color
            int colorPrimary;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                colorPrimary = getResources().getColor(R.color.colorPrimary, getTheme());
            } else {
                //noinspection deprecation
                colorPrimary = getResources().getColor(R.color.colorPrimary);
            }

            // Set task description
            setTaskDescription(new ActivityManager.TaskDescription(getSupportActionBar().getTitle().toString(), icon, colorPrimary));
        }
    }

    @Override
    protected void onPause() {
        // Dismiss more options popup
        popupMoreOptions.dismiss();

        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu
        getMenuInflater().inflate(R.menu.activity_edit, menu);

        // Create more options popup
        popupMoreOptions = new ListPopupWindow(this);

        // Set popup adapter
        popupMoreOptions.setAdapter(new PopupMoreOptionsAdapter(this));

        // FIXME: Generalize width computation
        popupMoreOptions.setWidth((int) DimenUtil.dpToPx(this, 288f));

        // Offset popup from corner
        popupMoreOptions.setHorizontalOffset((int) -DimenUtil.dpToPx(this, 8f));
        popupMoreOptions.setVerticalOffset(-toolbar.getHeight() + (int) DimenUtil.dpToPx(this, 8f));

        // Show above keyboard, if applicable
        popupMoreOptions.setInputMethodMode(ListPopupWindow.INPUT_METHOD_NOT_NEEDED);

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
        case R.id.menuActivityEditUndo:
            // Undo button pressed
            // Instruct editor to undo last operation
            editor.undo();
            break;
        case R.id.menuActivityEditRedo:
            // Redo button pressed
            // Instruct editor to redo last operation
            editor.redo();
            break;
        case R.id.menuActivityEditMoreOptions:
            // More options button pressed
            // Show more options popup
            View buttonMoreOptions = findViewById(R.id.menuActivityEditMoreOptions);
            popupMoreOptions.setAnchorView(buttonMoreOptions);
            buttonMoreOptions.setOnTouchListener(popupMoreOptions.createDragToOpenListener(buttonMoreOptions));
            popupMoreOptions.show();
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

    private void displayDialogRename(final Callback<String> callback) {
        // Construct a new dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set title from XML
        builder.setTitle(R.string.dialog_activity_edit_rename_title);

        // Content layout for name input
        FrameLayout contentLayout = new FrameLayout(this);

        // Get name of file before change
        final String nameBefore = file == null ? getString(R.string._default_document_name) : file.getName();

        // Create a text input for name entry
        final EditText editTextName = new EditText(this);
        editTextName.setText(nameBefore);
        editTextName.setHint(nameBefore);
        editTextName.setSingleLine();
        editTextName.selectAll();

        // Set margins for name input
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, getResources().getDisplayMetrics());
        layoutParams.rightMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, getResources().getDisplayMetrics());
        layoutParams.topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, getResources().getDisplayMetrics());
        editTextName.setLayoutParams(layoutParams);

        // Add name input to content layout
        contentLayout.addView(editTextName);

        // Add name input to dialog
        builder.setView(contentLayout);

        // Set up cancel button
        builder.setNegativeButton(R.string.dialog_activity_edit_rename_button_cancel, null);

        // Set up OK button
        builder.setPositiveButton(R.string.dialog_activity_edit_rename_button_rename, new DialogInterface.OnClickListener() {

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
                // Show the soft keyboard
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).showSoftInput(editTextName, InputMethodManager.SHOW_IMPLICIT);
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
                if (s.length() > 0 && !s.toString().equals(nameBefore)) {
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

    private static class PopupMoreOptionsAdapter extends BaseAdapter {

        private Context context;

        private List<Item> list;

        private PopupMoreOptionsAdapter(Context context) {
            this.context = context;

            list = new ArrayList<>();

            // FIXME: externalize these somehow
            list.add(new ItemText("Name of file"));
            list.add(new ItemSeparator());
            list.add(new ItemText("Go to..."));
            list.add(new ItemText("Find and replace..."));
            list.add(new ItemSeparator());
            list.add(new ItemSwitch("Word wrap", false));
            list.add(new ItemSwitch("Syntax highlighting", true));
            list.add(new ItemSeparator());
            list.add(new ItemText("How"));
            list.add(new ItemText("well"));
            list.add(new ItemText("does"));
            list.add(new ItemText("this"));
            list.add(new ItemText("scroll?"));
            list.add(new ItemText("scroll?"));
            list.add(new ItemText("scroll?"));
            list.add(new ItemText("scroll?"));
            list.add(new ItemText("scroll?"));
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
            return getItem(i).hashCode();
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            return getItem(i).getView();
        }

        private interface Item {

            View getView();

        }

        private class ItemSeparator implements Item {

            @Override
            public View getView() {
                View view = new View(context);

                view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) DimenUtil.dpToPx(context, 1f)));
                view.setBackgroundColor(0xffdddddd);

                return view;
            }

        }

        private class ItemText implements Item {

            private CharSequence text;

            private ItemText(CharSequence text) {
                this.text = text;
            }

            @Override
            public View getView() {
                TextView textView = new TextView(context);

                textView.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault_Widget_PopupMenu);
                textView.setTextSize(16f);
                textView.setPadding((int) dpToPx(context, 16f), 0, (int) dpToPx(context, 16f), 0);
                textView.setHeight((int) dpToPx(context, 48f));
                textView.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                textView.setText(text);

                return textView;
            }

        }

        private class ItemSwitch extends ItemText {

            private boolean state;

            private ItemSwitch(CharSequence text, boolean state) {
                super(text);

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
                RelativeLayout view = new RelativeLayout(context);

                view.setBackgroundDrawable(context.getResources().getDrawable(android.R.drawable.menuitem_background));

                view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                View viewBase = super.getView();

                viewBase.setFocusable(false);

                final SwitchCompat viewSwitch = new SwitchCompat(context);

                RelativeLayout.LayoutParams viewSwitchLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                viewSwitchLayoutParams.rightMargin = (int) DimenUtil.dpToPx(context, 16f);
                viewSwitchLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                viewSwitchLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
                viewSwitch.setLayoutParams(viewSwitchLayoutParams);

                view.addView(viewBase);
                view.addView(viewSwitch);

                view.setOnTouchListener(new View.OnTouchListener() {

                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        // Forward motion to switch
                        viewSwitch.onTouchEvent(event);

                        return true;
                    }

                });

                viewSwitch.setChecked(state);
                viewSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        state = b;
                    }

                });

                return view;
            }

        }

    }

}
