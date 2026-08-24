package com.example.calendar4;

import android.app.DatePickerDialog;
import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Reusable date input control (analogue of the phone control, but for dates).
 *
 * Layout: a single horizontal line containing a masked display field
 * "__.__.____" (dd.MM.yyyy) on the left and a "Выбор" (pick) button on the right.
 * The display field is non-editable; the date is chosen via a CalendarView-style
 * {@link DatePickerDialog}. The chosen date is stored internally as a
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

        // ----- pick button -----
        Button btnPick = new Button(context);
        btnPick.setText("Выбор");
        btnPick.setSingleLine(true);
        LinearLayout.LayoutParams lpBtn = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        addView(btnPick, lpBtn);

        btnPick.setOnClickListener(v -> showPicker());
    }

    /** Opens a CalendarView-based date picker and stores the selected date. */
    private void showPicker() {
        final Calendar cal = Calendar.getInstance();
        if (date != null) {
            cal.setTime(date);
        }
        DatePickerDialog dpd = new DatePickerDialog(
                getContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar c = Calendar.getInstance();
                        c.clear();
                        c.set(year, month, dayOfMonth);
                        date = c.getTime();
                        etDate.setText(formatDisplay(date));
                    }
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        dpd.show();
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
