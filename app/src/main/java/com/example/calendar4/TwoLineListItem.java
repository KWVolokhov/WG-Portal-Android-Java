package com.example.calendar4;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
 *
 * The row is vertical: the main content line is topped with an optional
 * "every fifth line" horizontal divider (a line with the centered number,
 * e.g. "--5--", "--10--") which replaces the old blue row highlight.
 */
public class TwoLineListItem extends LinearLayout {
    private LinearLayout contentRow;
    private LinearLayout fifthLineDivider;

    private TextView tvTop;
    private TextView tvBottom;
    private TextView tvMarker;
    private ImageButton btnEdit;
    private ImageButton btnDelete;
    private ImageView ivIcon;

    // Color of the horizontal "every fifth" line and its number
    private static final int FIFTH_LINE_COLOR = Color.rgb(26, 204, 173);

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
        setOrientation(VERTICAL);
        setPadding(dp(6), dp(4), dp(8), dp(2));

        // ----- content row: [left icon | two text lines | right buttons] -----
        contentRow = new LinearLayout(context);
        contentRow.setOrientation(HORIZONTAL);
        contentRow.setGravity(Gravity.CENTER_VERTICAL);

        // left column: icon (type / age)
        LinearLayout left = new LinearLayout(context);
        left.setOrientation(VERTICAL);
        left.setGravity(Gravity.CENTER_HORIZONTAL);
        left.setPadding(dp(2), 0, dp(6), 0);

        ivIcon = new ImageView(context);
        ivIcon.setImageResource(R.drawable.ic_person_contact);
        ivIcon.setContentDescription("Актуально");
        left.addView(ivIcon, new LinearLayout.LayoutParams(dp(48), dp(48)));

        contentRow.addView(left, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        // center: two text lines
        LinearLayout textBlock = new LinearLayout(context);
        textBlock.setOrientation(VERTICAL);

        tvTop = new TextView(context);
        //tvTop.setTextColor(0xFF555555); // slightly brighter
		int mainFontColor = tvTop.getCurrentTextColor();
		ColorUtils.colorToHSL(mainFontColor, hsl);//Запихаем основной цвет в массив
        tvTop.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        tvTop.setSingleLine(true);
        tvTop.setEllipsize(TextUtils.TruncateAt.END);

        tvBottom = new TextView(context);
        //tvBottom.setTextColor(0xFF2B2B2B); // slightly darker
		float[] hsl = new float[3];
		hsl[2] = hsl[2] * 0.7f;
		int minorFontColor = ColorUtils.HSLToColor(hsl);
        tvBottom.setTextColor(minorFontColor);
        tvBottom.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15); // size -1
        tvBottom.setSingleLine(true);
        tvBottom.setEllipsize(TextUtils.TruncateAt.END);

        textBlock.addView(tvTop, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        textBlock.addView(tvBottom, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        contentRow.addView(textBlock, new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));

        // right column: two stacked icon-buttons
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

        contentRow.addView(right, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        addView(contentRow, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        // ----- divider for every fifth line: ----5---- (line - number - line) -----
        // Visible only under the 5th, 10th, 15th ... item of the list.
        fifthLineDivider = new LinearLayout(context);
        fifthLineDivider.setOrientation(HORIZONTAL);
        fifthLineDivider.setGravity(Gravity.CENTER_VERTICAL);
        fifthLineDivider.setPadding(0, dp(2), 0, 0);
        fifthLineDivider.setVisibility(GONE);

        View lineLeft = new View(context);
        lineLeft.setBackgroundColor(FIFTH_LINE_COLOR);

        tvMarker = new TextView(context);
        tvMarker.setTextColor(FIFTH_LINE_COLOR);
        // Font is 3 sp smaller than the main list text (16sp -> 13sp)
        tvMarker.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        tvMarker.setSingleLine(true);
        tvMarker.setPadding(dp(6), 0, dp(6), 0);

        View lineRight = new View(context);
        lineRight.setBackgroundColor(FIFTH_LINE_COLOR);

        fifthLineDivider.addView(lineLeft, new LinearLayout.LayoutParams(0, dp(2), 1f));
        fifthLineDivider.addView(tvMarker, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        fifthLineDivider.addView(lineRight, new LinearLayout.LayoutParams(0, dp(2), 1f));

        addView(fifthLineDivider, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
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
     * Task 32/33: marks the "every fifth" line of a list.
     * <p>
     * Pass the 0-based row position from the adapter's getView(): under the 5th,
     * 10th, 15th, 20th ... rows a horizontal line is drawn with a centered
     * "--5--", "--10--", "--15--", "--20--" number whose font is 3 sp smaller
     * than the main list text. The row itself is NOT highlighted any more.
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

    /** Shows the "every fifth" horizontal divider with the given multiple-of-5 marker. */
    private void setFiveLine(int lineNumber) {
        tvMarker.setVisibility(VISIBLE);
        tvMarker.setText(String.valueOf(lineNumber));
        fifthLineDivider.setVisibility(VISIBLE);
    }

    /** Removes the "every fifth" divider (used for non-fifth rows and recycled views). */
    private void clearFiveLine() {
        if (fifthLineDivider != null) fifthLineDivider.setVisibility(GONE);
        if (tvMarker != null) tvMarker.setText("");
        setBackgroundColor(Color.TRANSPARENT);
    }

    public void setTypeIcon(int resId) {
        ivIcon.setImageResource(resId);
        ivIcon.setContentDescription("Актуально");
    }

    /** Sets the type/state icon from an already built drawable (e.g. status icons). */
    public void setTypeIcon(Drawable drawable) {
        if (drawable != null) {
            ivIcon.setImageDrawable(drawable);
        }
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