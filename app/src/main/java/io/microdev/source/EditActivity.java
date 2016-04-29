package io.microdev.source;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.ScrollView;

import io.microdev.source.widget.Editor;

public class EditActivity extends AppCompatActivity {

    private ScrollView scroll;
    private Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        scroll = (ScrollView) findViewById(R.id.activityEditScroll);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Switch against item ID to try to handle it
        switch (item.getItemId()) {
        case android.R.id.home:
            // Home button pressed (configured as action bar up arrow)
            // Finish activity
            finish();
            return true;
        }

        // Delegate to super if not handled
        return super.onOptionsItemSelected(item);
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

}
