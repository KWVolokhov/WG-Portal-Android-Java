package com.example.calendar4;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Task 110: строка списка сообщений СМС-чата.
 *
 * Layout: [иконка типа СМС 48dp | 3 строки текста | кнопки Посмотреть+Удалить 32dp]
 * Верхняя строка - автор (Фамилия Имя) + дата и время сообщения.
 * Ниже - до 2-х строк текста сообщения.
 */
public class MessageListItem extends LinearLayout {

    private TextView tvTop;        // автор + дата/время
    private TextView tvMessage;    // текст сообщения (до 2-х строк)
    private ImageButton btnView;   // "Посмотреть"
    private ImageButton btnDelete; // "Удалить"
    private ImageView ivIcon;      // тип СМС (in/out)

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
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setPadding(dp(6), dp(4), dp(8), dp(2));

        // ----- left: icon (тип СМС) -----
        ivIcon = new ImageView(context);
        ivIcon.setImageResource(R.drawable.ic_sms);
        addView(ivIcon, new LinearLayout.LayoutParams(dp(48), dp(48)));

        // ----- center: top line (author + date) + message (2 lines) -----
        LinearLayout textBlock = new LinearLayout(context);
        textBlock.setOrientation(VERTICAL);

        tvTop = new TextView(context);
        tvTop.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        tvTop.setTextColor(Color.rgb(80, 80, 80));
        tvTop.setSingleLine(true);
        tvTop.setPadding(dp(6), 0, dp(6), 0);

        tvMessage = new TextView(context);
        tvMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        tvMessage.setTextColor(Color.BLACK);
        tvMessage.setMaxLines(2);
        tvMessage.setPadding(dp(6), dp(1), dp(6), 0);

        textBlock.addView(tvTop, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        textBlock.addView(tvMessage, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        addView(textBlock, new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));

        // ----- right: two stacked icon buttons: Посмотреть / Удалить -----
        LinearLayout right = new LinearLayout(context);
        right.setOrientation(VERTICAL);
        right.setGravity(Gravity.CENTER_HORIZONTAL);
        right.setPadding(dp(2), 0, dp(6), 0);

        btnView = new ImageButton(context);
        btnView.setImageResource(R.drawable.ic_view);
        btnView.setContentDescription("Посмотреть");
        btnView.setPadding(dp(5), dp(5), dp(5), dp(5));
        btnView.setBackgroundColor(Color.TRANSPARENT);
        right.addView(btnView, new LinearLayout.LayoutParams(dp(32), dp(32)));

        btnDelete = new ImageButton(context);
        btnDelete.setImageResource(R.drawable.ic_delete);
        btnDelete.setContentDescription("Удалить");
        btnDelete.setPadding(dp(5), dp(5), dp(5), dp(5));
        btnDelete.setBackgroundColor(Color.TRANSPARENT);
        right.addView(btnDelete, new LinearLayout.LayoutParams(dp(32), dp(32)));

        addView(right, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    }

    // ---------------------------------------------------------------------
    // Public API
    // ---------------------------------------------------------------------

    /** Верхняя строка: "Фамилия Имя  dd.MM.yyyy HH:mm:ss". */
    public void setTopText(String text) {
        tvTop.setText(text == null ? "" : text);
    }

    /** Текст сообщения (показывается максимум в 2 строки). */
    public void setMessageText(String text) {
        tvMessage.setText(text == null ? "" : text);
    }

    /** Task 117: цвет текста сообщения (входящее/исходящее - разные цвета). */
    public void setMessageTextColor(int color) {
        tvMessage.setTextColor(color);
    }

    /** Иконка типа СМС (входящее/исходящее). */
    public void setTypeIcon(int resId) {
        ivIcon.setImageResource(resId);
    }

    /** Кнопка "Посмотреть" (просмотр полного СМС). */
    public void setOnViewClickListener(View.OnClickListener l) {
        btnView.setOnClickListener(l);
    }

    /** Кнопка "Удалить". */
    public void setOnDeleteClickListener(View.OnClickListener l) {
        btnDelete.setOnClickListener(l);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
