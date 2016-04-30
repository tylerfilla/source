package io.microdev.source;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import java.io.File;

import io.microdev.source.core.EditContext;
import io.microdev.source.widget.Editor;

public class EditActivity extends AppCompatActivity {

    private Editor editor;

    private File file;
    private EditContext editContext;

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

        // Get action bar
        ActionBar actionBar = getSupportActionBar();

        // Enable up arrow to behave as home button
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Get references to layout stuff
        editor = (Editor) findViewById(R.id.activityEditEditor);

        // Listen for changes to editor content
        editor.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu
        getMenuInflater().inflate(R.menu.activity_edit, menu);

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
        case R.id.menuActivityEditFilename:
            // Filename menu item pressed
            // Begin rename process
            displayDialogRename();
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
        builder.setMessage(R.string.activity_edit_dialog_rename_message);

        // Set cancel button
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                System.out.println("Pressed cancel button");
            }

        });

        // Set ok button
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                System.out.println("Pressed ok button");
            }

        });

        // Create a text input for name entry
        EditText editTextName = new EditText(this);
        editTextName.setHint(file.getName());
        builder.setView(editTextName);

        // Build and show dialog
        builder.show();
    }

}
