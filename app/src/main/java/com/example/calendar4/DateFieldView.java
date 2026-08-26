package com.example.calendar4;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Reusable date input control (analogue of the phone control, but for dates).
 *
 * Layout: a single horizontal line containing a masked display field
 * "__.__.____" (dd.MM.yyyy) on the left and a "Выбор" (pick) button on the right.
 * The display field is non-editable; the date is chosen via a modal dialog with
 * the {@link RussianCalendarView}. The chosen date is stored internally as a
 * java.util.Date and accessible through {@link #setDate(Date)} / {@link #getDate()}.
 */
public class DateFieldView extends LinearLayout {

    private static final String EMPTY_MASK = "__.__.____";

    private EditText etDate;
    private Date date; // nullable

    public DateFieldView(Context context) {
        this(context, null);
    }

    public DateFieldView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DateFieldView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        // ----- masked (non-editable) display EditText -----
        etDate = new EditText(context);
        etDate.setInputType(InputType.TYPE_NULL);
        etDate.setSingleLine(true);
        etDate.setFocusable(false);
        etDate.setClickable(false);
        etDate.setText(formatDisplay(null)); // show the empty mask
        etDate.setSelectAllOnFocus(false);

        LinearLayout.LayoutParams lpDate = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
        addView(etDate, lpDate);

        // ----- pick button (ImageButton with app icon / calendar picker, 32dp) -----
        ImageButton btnPick = new ImageButton(context);
        btnPick.setImageResource(R.drawable.ic_pick_date);
        btnPick.setBackgroundResource(android.R.color.transparent);
        int sizePx = (int) (getResources().getDisplayMetrics().density * 32);
        LinearLayout.LayoutParams lpBtn = new LinearLayout.LayoutParams(sizePx, sizePx);
        addView(btnPick, lpBtn);

        btnPick.setOnClickListener(v -> showPicker());
    }

    /** Opens a modal dialog with the {@link RussianCalendarView} and stores the selected date. */
    private void showPicker() {
        RussianCalendarView calendarView = new RussianCalendarView(getContext());

        if (date != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            calendarView.activeDate = date;   // highlight the currently set date
            calendarView.setCurrentMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH));
        } else {
            calendarView.goToToday();
        }
        // No per-date details Toast inside the picker (we show our own date Toast on tap)
        calendarView.setDetailsEnabled(false);

        // Give the calendar an explicit width so the 7-column GridView lays out correctly
        int widthPx = (int) (getResources().getDisplayMetrics().density * 320);
        calendarView.setLayoutParams(new ViewGroup.LayoutParams(widthPx, ViewGroup.LayoutParams.WRAP_CONTENT));

        float density = getResources().getDisplayMetrics().density;
        int sizePx = (int) (density * 32);

        // ----- custom header: title + OK( green check) / Cancel (gray cross) ImageButtons -----
        LinearLayout header = new LinearLayout(getContext());
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        // Vertical padding reduced ~3x vs default AlertDialog title
        int titleVPx = (int) (density * 8);
        header.setPadding((int) (density * 16), titleVPx, (int) (density * 16), titleVPx);

        TextView title = new TextView(getContext());
        title.setText("Выбор даты");
        title.setTextSize(12); // same size as day digit "1"
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(title, new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));

        ImageButton btnOk = new ImageButton(getContext());
        btnOk.setImageResource(R.drawable.ic_ok_green);
        btnOk.setBackgroundResource(android.R.color.transparent);
        header.addView(btnOk, new LinearLayout.LayoutParams(sizePx, sizePx));

        ImageButton btnCancel = new ImageButton(getContext());
        btnCancel.setImageResource(R.drawable.ic_cancel_gray);
        btnCancel.setBackgroundResource(android.R.color.transparent);
        header.addView(btnCancel, new LinearLayout.LayoutParams(sizePx, sizePx));

        LinearLayout container = new LinearLayout(getContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.addView(header);
        container.addView(calendarView, calendarView.getLayoutParams());

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(container);
        final AlertDialog dialog = builder.create();

        String[] monthStr = {"Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
                "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"};

        // Tapping a date only shows a Toast (like MainActivity) - no selection.
        calendarView.setOnDateSelectedListener(selected -> {
            Calendar cal = Calendar.getInstance();
            cal.setTime(selected);
            String dateStr = cal.get(Calendar.DAY_OF_MONTH) + " "
                    + monthStr[cal.get(Calendar.MONTH)] + " " + cal.get(Calendar.YEAR);
            Toast.makeText(getContext(), dateStr, Toast.LENGTH_SHORT).show();
        });

        // OK confirms the currently active (highlighted/tapped) date.
        btnOk.setOnClickListener(v -> {
            date = calendarView.activeDate;
            etDate.setText(formatDisplay(date));
            dialog.dismiss();
        });

        // Cancel just closes the dialog.
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // ---------------------------------------------------------------------
    // Public API
    // ---------------------------------------------------------------------

    /** Sets the date (pass null to clear). */
    public void setDate(Date d) {
        this.date = d;
        etDate.setText(formatDisplay(d));
    }

    /** Returns the selected date or null if none. */
    public Date getDate() {
        return date;
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private String formatDisplay(Date d) {
        if (d == null) {
            return EMPTY_MASK;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        return sdf.format(d);
    }
}
