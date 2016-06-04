package io.microdev.source.widget;

import android.content.Context;
import android.support.v7.widget.AppCompatPopupWindow;
import android.view.View;
import android.view.ViewGroup;

import static io.microdev.source.util.DimenUtil.dpToPxI;

public class PseudoPopupMenu extends AppCompatPopupWindow {

    private Context context;

    public PseudoPopupMenu(Context context) {
        super(context, null, android.support.v7.appcompat.R.attr.popupMenuStyle);

        // Because Android is too cool to let me use its copy
        this.context = context;

        // Height and width wrap content
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);

        // Make outside touchable to dismiss popup when touching outside its window
        setOutsideTouchable(true);
    }

    @Override
    public void update() {
        // Get popup content view
        View contentView = getContentView();

        if (contentView instanceof ViewGroup) {
            ViewGroup contentViewGroup = (ViewGroup) contentView;

            // Set minimum widths of all first-level children to zero
            for (int i = 0; i < contentViewGroup.getChildCount(); i++) {
                contentViewGroup.getChildAt(i).setMinimumWidth(0);
            }

            // Measure content view
            contentViewGroup.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

            int widthCurrent = contentViewGroup.getMeasuredWidth();

            // Convert 56dp to pixels for the calculations that follow
            int _56dp = dpToPxI(context, 56f);

            // If popup width is not divisible by 56dp
            if (widthCurrent % _56dp != 0) {
                // Recalculate width as the next multiple of 56dp (as per Material guidelines)
                int widthNew = widthCurrent + _56dp - (widthCurrent + _56dp) % _56dp;

                // Update popup width
                setWidth(widthNew);

                // Set minimum widths of all first-level children to explicitly match popup
                for (int i = 0; i < contentViewGroup.getChildCount(); i++) {
                    contentViewGroup.getChildAt(i).setMinimumWidth(widthNew);
                }
            }
        }

        super.update();
    }

}
