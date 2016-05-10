package io.microdev.source;

import android.app.ActivityManager;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import java.io.File;

import io.microdev.source.util.Callback;
import io.microdev.source.widget.editortext.EditorText;

public class EditActivity extends AppCompatActivity {

    private File file;
    private EditorText editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if a file URI was passed
        if (getIntent().getData() != null) {
            // Get file for URI
            file = new File(getIntent().getDataString());
        }

        // Inflate layout
        setContentView(R.layout.activity_edit);

        // Get editor view
        editor = (EditorText) findViewById(R.id.activityEditEditor);

        // Set action bar to custom toolbar
        setSupportActionBar((Toolbar) findViewById(R.id.activityEditToolbar));

        // Enable action bar up arrow to behave as home button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Decide action bar title based on file presence
        if (file == null) {
            // Default title
            getSupportActionBar().setTitle(getString(R.string._default_document_name));
        } else {
            // Set title to file name
            getSupportActionBar().setTitle(file.getName());
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate action bar menu
        getMenuInflater().inflate(R.menu.activity_edit, menu);

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
            return true;
        case R.id.menuActivityEditUndo:
            // Undo button pressed
            // Instruct editor to undo last operation
            editor.undo();
            return true;
        case R.id.menuActivityEditRedo:
            // Redo button pressed
            // Instruct editor to redo last operation
            editor.redo();
            return true;
        case R.id.menuActivityEditMoreOptions:
            // Menu button pressed
            // TODO: Show options popup menu
            displayDialogRename(new Callback<String>() {

                @Override
                public void ring(String obj) {
                }

            });
            return true;
        default:
            // Delegate to super if not handled
            return super.onOptionsItemSelected(item);
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

}
