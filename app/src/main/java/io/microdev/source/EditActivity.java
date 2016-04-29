package io.microdev.source;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

}
