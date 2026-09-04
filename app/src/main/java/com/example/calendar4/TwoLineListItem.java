package com.example.calendar4;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Reusable two-line list row control.
 *
 * Layout (single row): [icon 48dp | two text lines (weight 1) | Edit+Delete 32dp]
 * The icon is on the left, Edit/Delete buttons on the right - same as the
 * contacts list. The icon is set through {@link #setTypeIcon(int)}.
 */
public class TwoLineListItem extends LinearLayout {
    private TextView tvTop;
    private TextView tvBottom;
    private TextView tvMarker;
    private ImageButton btnEdit;
    private ImageButton btnDelete;
    private ImageView ivIcon;

    public TwoLineListItem(Context context) {
        this(context, null);
    }

    public TwoLineListItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TwoLineListItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setPadding(dp(6), dp(6), dp(8), dp(6));

        // ----- left column: icon (type / age) -----
        LinearLayout left = new LinearLayout(context);
        left.setOrientation(VERTICAL);
        left.setGravity(Gravity.CENTER_HORIZONTAL);
        left.setPadding(dp(2), 0, dp(6), 0);

        ivIcon = new ImageView(context);
        ivIcon.setImageResource(R.drawable.ic_person_contact);
        ivIcon.setContentDescription("Актуально");
        left.addView(ivIcon, new LinearLayout.LayoutParams(dp(48), dp(48)));

        addView(left, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        // ----- center: two text lines -----
        LinearLayout textBlock = new LinearLayout(context);
        textBlock.setOrientation(VERTICAL);

        tvTop = new TextView(context);
        //tvTop.setTextColor(0xFF555555); // slightly brighter
        tvTop.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        tvTop.setSingleLine(true);
        tvTop.setEllipsize(TextUtils.TruncateAt.END);

        tvBottom = new TextView(context);
        //tvBottom.setTextColor(0xFF2B2B2B); // slightly darker
        tvBottom.setTextColor(Color.BLUE);
        tvBottom.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15); // size -1
        tvBottom.setSingleLine(true);
        tvBottom.setEllipsize(TextUtils.TruncateAt.END);

        textBlock.addView(tvTop, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        textBlock.addView(tvBottom, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        addView(textBlock, new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));

        // ----- marker of "every fifth" line (blue, "--5--" style, smaller font) -----
        // Font size is 3 sp smaller than the main list text (16sp -> 13sp).
        tvMarker = new TextView(context);
        tvMarker.setTextColor(Color.rgb(10, 60, 160)); // dark blue for contrast on light-blue row
        tvMarker.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        tvMarker.setSingleLine(true);
        tvMarker.setPadding(0, 0, dp(4), 0);
        tvMarker.setVisibility(GONE);
        addView(tvMarker, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        // ----- right column: two stacked icon-buttons -----
        LinearLayout right = new LinearLayout(context);
        right.setOrientation(VERTICAL);
        right.setGravity(Gravity.CENTER_HORIZONTAL);
        right.setPadding(dp(2), 0, dp(6), 0);

        btnEdit = new ImageButton(context);
        btnEdit.setImageResource(R.drawable.ic_edit);
        btnEdit.setContentDescription("Редактировать");
        btnEdit.setPadding(dp(5), dp(5), dp(5), dp(5));
        btnEdit.setBackgroundColor(Color.TRANSPARENT);
        right.addView(btnEdit, new LinearLayout.LayoutParams(dp(32), dp(32)));

        btnDelete = new ImageButton(context);
        btnDelete.setImageResource(R.drawable.ic_delete);
        btnDelete.setContentDescription("Удалить");
        btnDelete.setPadding(dp(5), dp(5), dp(5), dp(5));
        btnDelete.setBackgroundColor(Color.TRANSPARENT);
        right.addView(btnDelete, new LinearLayout.LayoutParams(dp(32), dp(32)));

        addView(right, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    }

    // ---------------------------------------------------------------------
    // Public API
    // ---------------------------------------------------------------------

    public void setTopText(String text) {
        tvTop.setText(text == null ? "" : text);
    }

    public void setBottomText(String text) {
        tvBottom.setText(text == null ? "" : text);
    }

    /**
     * Task 32: marks the "every fifth" line of a list.
     * <p>
     * Pass the 0-based row position from the adapter's getView(): the 5th, 10th,
     * 15th, 20th ... rows become blue and get a "--5--", "--10--", "--15--",
     * "--20--" marker with a font 3 sp smaller than the main list text.
     */
    public void setPosition(int position) {
        if (position < 0) {
            clearFiveLine();
            return;
        }
        int lineNumber = position + 1;
        if (lineNumber % 5 == 0) {
            setFiveLine(lineNumber);
        } else {
            clearFiveLine();
        }
    }

    /** Shows the blue "every fifth" row highlight with the given multiple-of-5 marker. */
    private void setFiveLine(int lineNumber) {
        tvMarker.setVisibility(VISIBLE);
        tvMarker.setText("--" + lineNumber + "--");
        // Light blue row background ("каждая пятая линия синяя")
        setBackgroundColor(Color.rgb(198, 224, 255));
    }

    /** Removes the fifth-line marker/highlight (used for non-fifth rows and recycled views). */
    private void clearFiveLine() {
        tvMarker.setVisibility(GONE);
        tvMarker.setText("");
        setBackgroundColor(Color.TRANSPARENT);
    }

    public void setTypeIcon(int resId) {
        ivIcon.setImageResource(resId);
        ivIcon.setContentDescription("Актуально");
    }

    public void setOnEditClickListener(View.OnClickListener l) {
        btnEdit.setOnClickListener(l);
    }

    public void setOnDeleteClickListener(View.OnClickListener l) {
        btnDelete.setOnClickListener(l);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}