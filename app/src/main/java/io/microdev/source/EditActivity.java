package io.microdev.source;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;

import java.io.File;

import io.microdev.source.core.EditContext;
import io.microdev.source.widget.Editor;

public class EditActivity extends AppCompatActivity {

    private File file;
    private EditContext editContext;

    private Editor editor;
    private PopupMenu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if a file URI was passed
        if (getIntent().getData() != null) {
            // Get file for URI
            file = new File(getIntent().getData().getPath());

            // Set edit context to FILE (edits are tied to a file)
            editContext = EditContext.FILE;
        } else {
            // Set edit context to FREE (edits are not tied to a file)
            editContext = EditContext.FREE;
        }

        // Show layout
        setContentView(R.layout.activity_edit);

        // Set action bar to custom toolbar
        setSupportActionBar((Toolbar) findViewById(R.id.activityEditToolbar));

        // Enable action bar up arrow to behave as home button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Decide action bar title based on edit context
        if (editContext == EditContext.FILE) {
            // Set title to filename
            getSupportActionBar().setTitle(file.getName());
        } else if (editContext == EditContext.FREE) {
            // Clear title
            getSupportActionBar().setTitle("");
        }

        // Create and configure overflow popup menu
        menu = new PopupMenu(this, null);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Get references to layout stuff
        editor = (Editor) findViewById(R.id.activityEditEditor);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu
        getMenuInflater().inflate(R.menu.activity_edit, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        /*
        // If a file is being edited
        if (editContext == EditContext.FILE) {
            // Build title for filename menu item
            SpannableStringBuilder menuFilenameTitleBuilder = new SpannableStringBuilder();
            menuFilenameTitleBuilder.append(file.getName());
            menuFilenameTitleBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, menuFilenameTitleBuilder.length(), 0);
            menuFilenameTitleBuilder.setSpan(new RelativeSizeSpan(1.1f), 0, menuFilenameTitleBuilder.length(), 0);

            // Set title
            menu.findItem(R.id.menuActivityEditFilename).setTitle(menuFilenameTitleBuilder);
        } else if (editContext == EditContext.FREE) {
            // Remove filename menu item
            menu.removeItem(R.id.menuActivityEditFilename);
        }
        */

        return super.onPrepareOptionsMenu(menu);
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
            // Open popup menu
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

    private void displayDialogRename() {
        // Construct a new dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set title and message from XML
        builder.setTitle(R.string.activity_edit_dialog_rename_title);

        // Set ok button
        builder.setPositiveButton(R.string.activity_edit_dialog_rename_button_rename, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                System.out.println("Pressed ok button");
            }

        });

        // Content layout for name input
        FrameLayout contentLayout = new FrameLayout(this);

        // Create a text input for name entry
        EditText editTextName = new EditText(this);
        editTextName.setHint(file.getName());
        editTextName.setText(file.getName());
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

        // Add input to dialog
        builder.setView(contentLayout);

        // Build and show dialog
        final AlertDialog dialog = builder.show();

        // Watch for text changes in name input
        editTextName.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Disable OK button if no name is provided
                if (s.length() > 0) {
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                } else {
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

        });
    }

}
