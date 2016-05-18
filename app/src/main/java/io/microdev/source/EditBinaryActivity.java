package io.microdev.source;

import android.app.ActivityManager;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.microdev.source.util.DimenUtil;
import io.microdev.source.widget.editorbinary.EditorBinary;
import io.microdev.source.widget.panview.PanView;

import static io.microdev.source.util.DimenUtil.dpToPx;

public class EditBinaryActivity extends AppCompatActivity {

    private File file;
    private String filename;

    private Toolbar appBar;
    private PanView panView;
    private EditorBinary editor;

    private ListPopupWindow popupMoreOptions;
    private PopupMoreOptionsAdapter popupMoreOptionsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set activity layout
        //setContentView(R.layout.activity_edit_binary);

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
        editor = (EditorBinary) findViewById(R.id.activityEditEditor);
        panView = (PanView) findViewById(R.id.activityEditPanView);
        appBar = (Toolbar) findViewById(R.id.activityEditAppBar);

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu
        getMenuInflater().inflate(R.menu.activity_edit_text_opts, menu);

        // Create more options popup
        popupMoreOptions = new ListPopupWindow(this);

        // Create and set popup adapter
        popupMoreOptionsAdapter = new PopupMoreOptionsAdapter(this);
        popupMoreOptions.setAdapter(popupMoreOptionsAdapter);

        // FIXME: Generalize width computation
        popupMoreOptions.setWidth((int) DimenUtil.dpToPx(this, 288f));

        // Offset popup from corner
        popupMoreOptions.setHorizontalOffset((int) -DimenUtil.dpToPx(this, 8f));
        popupMoreOptions.setVerticalOffset(-appBar.getHeight() + (int) DimenUtil.dpToPx(this, 8f));

        // Show above keyboard, if applicable
        popupMoreOptions.setInputMethodMode(ListPopupWindow.INPUT_METHOD_NOT_NEEDED);

        // Listen for clicks
        popupMoreOptions.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                PopupMoreOptionsAdapter adapter = (PopupMoreOptionsAdapter) adapterView.getAdapter();

                // Send click notice to adapter
                adapter.handleItemClick(i);

                // Dismiss popup
                popupMoreOptions.dismiss();
            }

        });

        // Listen for item actions
        popupMoreOptionsAdapter.addOnItemActionListener(new PopupMoreOptionsAdapter.OnItemActionListener() {

            @Override
            public void onItemAction(PopupMoreOptionsAdapter.Item item) {
                // Perform corresponding action
                if ("filename".equals(item.getTag())) {
                    //promptRenameFile();
                } else if ("find".equals(item.getTag())) {
                    //promptFindReplace();
                }
            }

        });

        // Get adapter list
        final List<PopupMoreOptionsAdapter.Item> popupMoreOptionsAdapterList = popupMoreOptionsAdapter.getList();

        // Observe dataset changes
        popupMoreOptionsAdapter.registerDataSetObserver(new DataSetObserver() {

            @Override
            public void onChanged() {
                super.onChanged();

                // Iterate over items to update them
                for (PopupMoreOptionsAdapter.Item item : popupMoreOptionsAdapterList) {
                    if ("filename".equals(item.getTag())) {
                        SpannableStringBuilder itemFileNameText = new SpannableStringBuilder();

                        // Get filename
                        itemFileNameText.append(getFilename());

                        // Embolden and enlarge by 15%
                        itemFileNameText.setSpan(new StyleSpan(Typeface.BOLD), 0, itemFileNameText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        itemFileNameText.setSpan(new RelativeSizeSpan(1.15f), 0, itemFileNameText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        ((PopupMoreOptionsAdapter.ItemText) item).setText(itemFileNameText);
                    }
                }
            }

        });

        // Create "menu" items for popup
        popupMoreOptionsAdapterList.add(new PopupMoreOptionsAdapter.ItemText(popupMoreOptionsAdapter, "filename", getFilename()));
        popupMoreOptionsAdapterList.add(new PopupMoreOptionsAdapter.ItemSeparator(popupMoreOptionsAdapter, null));
        popupMoreOptionsAdapterList.add(new PopupMoreOptionsAdapter.ItemText(popupMoreOptionsAdapter, "find", getString(R.string.popup_activity_edit_binary_more_options_find)));

        // Initial data
        popupMoreOptionsAdapter.notifyDataSetChanged();

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
            //editor.undo();
            break;
        case R.id.menuActivityEditRedo:
            // Redo button pressed
            // Instruct editor to redo last operation
            //editor.redo();
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

        // Handle task descriptions on Lollipop+
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
            setTaskDescription(new ActivityManager.TaskDescription(getSupportActionBar().getTitle().toString(), icon, colorPrimary));
        }

        // If more options popup adapter has been constructed
        if (popupMoreOptionsAdapter != null) {
            // Notify it of a dataset change (due to new filename)
            popupMoreOptionsAdapter.notifyDataSetChanged();
        }
    }

    private static class PopupMoreOptionsAdapter extends BaseAdapter {

        private Context context;

        private List<Item> list;
        private Set<OnItemActionListener> listenerSet;

        private PopupMoreOptionsAdapter(final EditBinaryActivity context) {
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

            private EditBinaryActivity.PopupMoreOptionsAdapter adapter;
            private String tag;

            public ItemSeparator(EditBinaryActivity.PopupMoreOptionsAdapter adapter, String tag) {
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

            private EditBinaryActivity.PopupMoreOptionsAdapter adapter;
            private String tag;
            private CharSequence text;

            public ItemText(EditBinaryActivity.PopupMoreOptionsAdapter adapter, String tag, CharSequence text) {
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

            private EditBinaryActivity.PopupMoreOptionsAdapter adapter;
            private boolean state;

            private SwitchCompat viewSwitch;

            private ItemSwitch(EditBinaryActivity.PopupMoreOptionsAdapter adapter, String tag, CharSequence text, boolean state) {
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
