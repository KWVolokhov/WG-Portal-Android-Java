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

import androidx.core.graphics.ColorUtils;

/**
 * Task 125: универсальный элемент списка (слит из TwoLineListItem и MessageListItem).
 *
 * Layout (одна строка): [иконка 48dp | текст 1..N строк | кнопки 32dp].
 * Верхняя строка - главный текст (16sp), нижняя - второстепенный (15sp), по умолчанию 1 строка,
 * расширяется до нескольких (2/3/4) через setOnEditNewIcon / setMessageMaxLines.
 * Кнопка справа - по умолчанию "Редактировать" (ic_edit), может стать просмотром (ic_view),
 * вторая кнопка - "Удалить" (ic_delete).
 * Каждая 5-я строка списка отделяется горизонтальной чертой с номером в стиле "--5--".
 */
public class MessageListItem extends LinearLayout {
    private LinearLayout contentRow;
    private LinearLayout fifthLineDivider;

    private TextView tvTop;
    private TextView tvMid;
    private TextView tvBottom;
    private TextView tvMarker;
    private ImageButton btnEdit;
    private ImageButton btnDelete;
    private ImageView ivIcon;

    // Цвет горизонтальной черты "каждая 5-я" и её номера
    private static final int FIFTH_LINE_COLOR = Color.rgb(26, 204, 173);

    public MessageListItem(Context context) {
        this(context, null);
    }

    public MessageListItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MessageListItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setOrientation(VERTICAL);
        setPadding(dp(6), dp(4), dp(8), dp(2));

        // ----- строка содержимого: [иконка | текст | кнопки] -----
        contentRow = new LinearLayout(context);
        contentRow.setOrientation(HORIZONTAL);
        contentRow.setGravity(Gravity.CENTER_VERTICAL);

        // слева: иконка типа/состояния
        LinearLayout left = new LinearLayout(context);
        left.setOrientation(VERTICAL);
        left.setGravity(Gravity.CENTER_HORIZONTAL);
        left.setPadding(dp(2), 0, dp(6), 0);

        ivIcon = new ImageView(context);
        ivIcon.setImageResource(R.drawable.ic_type_project);
        ivIcon.setContentDescription("Актуально");
        left.addView(ivIcon, new LinearLayout.LayoutParams(dp(48), dp(48)));
        contentRow.addView(left, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        // по центру: 1..N текстовых строк
        LinearLayout textBlock = new LinearLayout(context);
        textBlock.setOrientation(VERTICAL);

        tvTop = new TextView(context);
        int mainFontColor = tvTop.getCurrentTextColor();
        tvTop.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        tvTop.setSingleLine(true);
        tvTop.setEllipsize(TextUtils.TruncateAt.END);

        tvBottom = new TextView(context);
        float[] hsl = new float[3];
        ColorUtils.colorToHSL(mainFontColor, hsl);
        hsl[2] = hsl[2] * 0.5f;
        tvBottom.setTextColor(ColorUtils.HSLToColor(hsl));
        tvBottom.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        tvBottom.setSingleLine(true);
        tvBottom.setEllipsize(TextUtils.TruncateAt.END);

        textBlock.addView(tvTop, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        // Task 129: средняя строка (дата со временем в Истории); скрыта, пока не задана
        tvMid = new TextView(context);
        tvMid.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        tvMid.setSingleLine(true);
        tvMid.setEllipsize(TextUtils.TruncateAt.END);
        tvMid.setVisibility(GONE);
        textBlock.addView(tvMid, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        textBlock.addView(tvBottom, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        contentRow.addView(textBlock, new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        // справа: кнопки (по умолчанию Редактировать + Удалить)
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

        contentRow.addView(right, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        addView(contentRow, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        // ----- разделитель каждой 5-й строки: ----5---- -----
        fifthLineDivider = new LinearLayout(context);
        fifthLineDivider.setOrientation(HORIZONTAL);
        fifthLineDivider.setGravity(Gravity.CENTER_VERTICAL);
        fifthLineDivider.setPadding(0, dp(2), 0, 0);
        fifthLineDivider.setVisibility(GONE);

        View lineLeft = new View(context);
        lineLeft.setBackgroundColor(FIFTH_LINE_COLOR);

        tvMarker = new TextView(context);
        tvMarker.setTextColor(FIFTH_LINE_COLOR);
        tvMarker.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        tvMarker.setSingleLine(true);
        tvMarker.setPadding(dp(6), 0, dp(6), 0);

        View lineRight = new View(context);
        lineRight.setBackgroundColor(FIFTH_LINE_COLOR);

        fifthLineDivider.addView(lineLeft, new LinearLayout.LayoutParams(0, dp(2), 1f));
        fifthLineDivider.addView(tvMarker, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        fifthLineDivider.addView(lineRight, new LinearLayout.LayoutParams(0, dp(2), 1f));
        addView(fifthLineDivider, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
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

    /** Task 129: средняя строка (3 строки в строке списка); скрыта, пока текст пустой. */
    public void setMiddleText(String text) {
        boolean empty = text == null || text.trim().isEmpty();
        tvMid.setText(empty ? "" : text);
        tvMid.setVisibility(empty ? GONE : VISIBLE);
    }

    /** Синоним setBottomText (текст сообщения СМС). */
    public void setMessageText(String text) {
        setBottomText(text);
    }

    /** Цвет нижней строки (например цвет сообщений входящих/исходящих в чате). */
    public void setMessageTextColor(int color) {
        tvBottom.setTextColor(color);
    }

    /** Нижняя строка в несколько строк (всего 2/3/4 строки вместе с верхней). */
    public void setMessageMaxLines(int lines) {
        tvBottom.setSingleLine(false);
        tvBottom.setMaxLines(lines);
    }

    /** Разделитель "--5--" под 5-й, 10-й, 15-й ... строками (0-based позиция). */
    public void setPosition(int position) {
        if (position < 0) {
            clearFiveLine();
            return;
        }
        int lineNumber = position + 1;
        if (lineNumber % 5 == 0) setFiveLine(lineNumber);
        else clearFiveLine();
    }

    public void setTypeIcon(int resId) {
        ivIcon.setImageResource(resId);
        ivIcon.setContentDescription("Актуально");
    }

    /** Иконка типа/состояния из готового drawable (например статусные иконки). */
    public void setTypeIcon(Drawable drawable) {
        if (drawable != null) ivIcon.setImageDrawable(drawable);
        ivIcon.setContentDescription("Актуально");
    }

    public void setOnEditClickListener(View.OnClickListener l) {
        btnEdit.setOnClickListener(l);
    }

    /** Кнопка редактирования превращается в просмотр (иконка+описание), низ - до numSecondRows строк. */
    public void setOnEditNewIcon(String desc, int iconNew, int numSecondRows) {
        btnEdit.setImageResource(iconNew);
        btnEdit.setContentDescription(desc);
        setMessageMaxLines(numSecondRows);
    }

    public void setOnDeleteClickListener(View.OnClickListener l) {
        btnDelete.setOnClickListener(l);
    }

    private void setFiveLine(int lineNumber) {
        tvMarker.setVisibility(VISIBLE);
        tvMarker.setText(String.valueOf(lineNumber));
        fifthLineDivider.setVisibility(VISIBLE);
    }

    private void clearFiveLine() {
        if (fifthLineDivider != null) fifthLineDivider.setVisibility(GONE);
        if (tvMarker != null) tvMarker.setText("");
        setBackgroundColor(Color.TRANSPARENT);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
