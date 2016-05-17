package io.microdev.source;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.database.DataSetObserver;
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
import android.text.Html;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
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
    private String fileName;

    private Toolbar appbar;
    private EditorText editor;

    private ListPopupWindow popupMoreOptions;
    private PopupMoreOptionsAdapter popupMoreOptionsAdapter;

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
            fileName = file.getName();
        } else {
            // Nullify file
            file = null;

            // Use default filename
            fileName = getString(R.string._default_file_name);
        }

        // Find editor view
        editor = (EditorText) findViewById(R.id.activityEditEditor);

        // Find app bar
        appbar = (Toolbar) findViewById(R.id.activityEditToolbar);

        // Set action bar to custom app bar
        setSupportActionBar(appbar);

        // Enable action bar up arrow to behave as home button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Establish current filename
        setFileName(fileName);
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

        // Create and set popup adapter
        popupMoreOptionsAdapter = new PopupMoreOptionsAdapter(this);
        popupMoreOptions.setAdapter(popupMoreOptionsAdapter);

        // FIXME: Generalize width computation
        popupMoreOptions.setWidth((int) DimenUtil.dpToPx(this, 288f));

        // Offset popup from corner
        popupMoreOptions.setHorizontalOffset((int) -DimenUtil.dpToPx(this, 8f));
        popupMoreOptions.setVerticalOffset(-appbar.getHeight() + (int) DimenUtil.dpToPx(this, 8f));

        // Show above keyboard, if applicable
        popupMoreOptions.setInputMethodMode(ListPopupWindow.INPUT_METHOD_NOT_NEEDED);

        // Listen for clicks
        popupMoreOptions.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                PopupMoreOptionsAdapter adapter = (PopupMoreOptionsAdapter) adapterView.getAdapter();

                // Send click notice to adapter
                adapter.onItemClick(i);

                // Dismiss popup
                popupMoreOptions.dismiss();

                // Get tag of clicked item
                String itemTag = adapter.getItem(i).getTag();

                if ("filename".equals(itemTag)) {
                    promptRenameFile();
                }
            }

        });

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
            popupMoreOptions.setAnchorView(findViewById(R.id.menuActivityEditMoreOptions));
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

    private String getFileName() {
        return fileName;
    }

    private void setFileName(String fileName) {
        // Change filename
        this.fileName = fileName;

        // Is a file set?
        if (file != null) {
            // Rename physical file
            file.renameTo(new File(file.getParentFile(), fileName));
        }

        // Set app bar title
        appbar.setTitle(fileName);

        // If more options popup list view has been constructed
        if (popupMoreOptionsAdapter != null) {
            // Notify it of a dataset change (due to new fileName)
            popupMoreOptionsAdapter.notifyDataSetChanged();
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

    private void promptRenameFile() {
        // Display rename dialog
        displayDialogRename(new Callback<String>() {

            @Override
            public void ring(String arg) {
                // Set file name
                setFileName(arg);
            }

        });
    }

    private void displayDialogRename(final Callback<String> callback) {
        // Construct a new dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set title from XML
        builder.setTitle(R.string.dialog_activity_edit_rename_title);

        // Content layout for name input
        FrameLayout contentLayout = new FrameLayout(this);

        // Create a text input for name entry
        final EditText editTextName = new EditText(this);
        editTextName.setText(fileName);
        editTextName.setHint(fileName);
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
                if (s.length() > 0 && !s.toString().equals(fileName)) {
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

    private class PopupMoreOptionsAdapter extends BaseAdapter {

        private Context context;

        private List<Item> list;

        private PopupMoreOptionsAdapter(Context context) {
            this.context = context;

            list = new ArrayList<>();

            // FIXME: externalize these somehow

            final ItemText itemFileName = new ItemText("filename", Html.fromHtml("<b>" + getFileName() + "</b>"));

            list.add(itemFileName);
            list.add(new ItemSeparator(null));
            list.add(new ItemText("find", "Find and replace..."));
            list.add(new ItemText("goto", "Go to..."));
            list.add(new ItemSeparator(null));
            list.add(new ItemSwitch("word_wrap", "Word wrap", false));
            list.add(new ItemSwitch("syntax_highlighting", "Syntax highlighting", true));

            // Observe changes in the underlying data
            registerDataSetObserver(new DataSetObserver() {

                @Override
                public void onChanged() {
                    super.onChanged();

                    // Update filename
                    itemFileName.setText(Html.fromHtml("<b>" + getFileName() + "</b>"));
                }

            });
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

        public void onItemClick(int i) {
            getItem(i).onClick();
        }

        private abstract class Item {

            abstract View getView();

            abstract String getTag();

            abstract void onClick();

        }

        private class ItemSeparator extends Item {

            private String tag;

            public ItemSeparator(String tag) {
                this.tag = tag;
            }

            @Override
            public View getView() {
                View view = new View(context);

                view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) DimenUtil.dpToPx(context, 1f)));
                view.setBackgroundColor(0xffdddddd);

                return view;
            }

            @Override
            public String getTag() {
                return tag;
            }

            @Override
            public void onClick() {
                System.out.println("You deserve a medal...");
            }

        }

        private class ItemText extends Item {

            private String tag;
            private CharSequence text;

            public ItemText(String tag, CharSequence text) {
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
                TextView textView = new TextView(context);

                // noinspection deprecation
                textView.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault_Widget_PopupMenu);
                textView.setTextSize(16f);
                textView.setPadding((int) dpToPx(context, 16f), 0, (int) dpToPx(context, 16f), 0);
                textView.setHeight((int) dpToPx(context, 48f));
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
            }

        }

        private class ItemSwitch extends ItemText {

            private boolean state;

            private SwitchCompat viewSwitch;

            private ItemSwitch(String tag, CharSequence text, boolean state) {
                super(tag, text);

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
                RelativeLayout view = new RelativeLayout(context);

                // Configure menu item layout params
                view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                // Get base text view from super
                View viewBase = super.getView();

                // Configure switch layout params
                RelativeLayout.LayoutParams viewBaseLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                viewBase.setLayoutParams(viewBaseLayoutParams);

                // Create a switch control
                viewSwitch = new SwitchCompat(context);

                // Configure switch layout params
                RelativeLayout.LayoutParams viewSwitchLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                viewSwitchLayoutParams.rightMargin = (int) DimenUtil.dpToPx(context, 16f);
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
            }

        }

    }

}
