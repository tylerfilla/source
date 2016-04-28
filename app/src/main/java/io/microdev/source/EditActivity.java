package io.microdev.source;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import io.microdev.source.widget.Editor;

public class EditActivity extends AppCompatActivity {

    private Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Show layout
        setContentView(R.layout.activity_edit);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Get reference to editor
        editor = (Editor) findViewById(R.id.activityEditEditor);
    }

}
