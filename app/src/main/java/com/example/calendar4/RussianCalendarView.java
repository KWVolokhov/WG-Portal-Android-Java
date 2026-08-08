package com.example.calendar4;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class RussianCalendarView extends ConstraintLayout {
    private GridView calendarGridView;
    private CalendarAdapter adapter;
    private Calendar currentCalendar;
    private TextView monthYearText;
    private HashSet<String> holidays;
    private HashSet<Integer> weekendDays;
    private OnDateSelectedListener dateSelectedListener;

    public interface OnDateSelectedListener {
        void onDateSelected(Date date);
    }

    public RussianCalendarView(Context context) {
        super(context);
        init(context);
    }

    public RussianCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RussianCalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    private void init(Context context) {
        // Inflate the custom calendar layout
        LayoutInflater.from(context).inflate(R.layout.russian_calendar_view, this, true);

        calendarGridView = findViewById(R.id.calendarGridView);
        monthYearText = findViewById(R.id.monthYearText);

        currentCalendar = Calendar.getInstance();
        holidays = new HashSet<>();
        weekendDays = new HashSet<>();

        adapter = new CalendarAdapter(context);
        calendarGridView.setAdapter(adapter);

        // Set click listener for dates
        calendarGridView.setOnItemClickListener((parent, view, position, id) -> {
            if (adapter.getItem(position) != null && dateSelectedListener != null) {
                dateSelectedListener.onDateSelected(adapter.getItem(position));
            }
        });

        updateCalendar();
    }

    public void setOnDateSelectedListener(OnDateSelectedListener listener) {
        this.dateSelectedListener = listener;
    }

    public void setHolidays(HashSet<String> holidays) {
        this.holidays = holidays != null ? holidays : new HashSet<>();
        updateCalendar();
    }

    public void setCurrentMonth(int year, int month) {
        currentCalendar.set(Calendar.YEAR, year);
        currentCalendar.set(Calendar.MONTH, month);
        updateCalendar();
    }
    private void updateCalendar() {
        // Update month/year text
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", new Locale("ru", "RU"));
        monthYearText.setText(sdf.format(currentCalendar.getTime()));

        // Calculate weekend days for current month
        calculateWeekends();

        // Update adapter
        adapter.notifyDataSetChanged();
    }

    private void calculateWeekends() {
        weekendDays.clear();
        Calendar tempCal = (Calendar) currentCalendar.clone();
        tempCal.set(Calendar.DAY_OF_MONTH, 1);

        int daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int day = 1; day <= daysInMonth; day++) {
            tempCal.set(Calendar.DAY_OF_MONTH, day);
            int dayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK);
            // Saturday = 7, Sunday = 1
            if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                weekendDays.add(day);
            }
        }
    }

    private class CalendarAdapter extends BaseAdapter {
        private Context context;
        private List<Date> dates;
        private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        public CalendarAdapter(Context context) {
            this.context = context;
            dates = new ArrayList<>();
            generateDates();
        }

        private void generateDates() {
            dates.clear();
            Calendar tempCal = (Calendar) currentCalendar.clone();
            tempCal.set(Calendar.DAY_OF_MONTH, 1);

            // Get the day of week for the first day of month (1 = Sunday, 7 = Saturday)
            int firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK);

            // Adjust for Monday as first day of week (Russian standard)
            int offset = firstDayOfWeek - Calendar.MONDAY;
            if (offset < 0) offset += 7;

            // Add empty cells for days before the first day of month
            for (int i = 0; i < offset; i++) {
                dates.add(null);
            }

            // Add all days of the month
            int daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);
            for (int day = 1; day <= daysInMonth; day++) {
                tempCal.set(Calendar.DAY_OF_MONTH, day);
                dates.add(tempCal.getTime());
            }
        }

        @Override
        public int getCount() {
            return dates.size();
        }

        @Override
        public Date getItem(int position) {
            return dates.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView;
            if (convertView == null) {
                textView = new TextView(context);
                textView.setLayoutParams(new GridView.LayoutParams(120, 120));
                textView.setGravity(android.view.Gravity.CENTER);
                textView.setTextSize(18);
                textView.setPadding(8, 8, 8, 8);
            } else {
                textView = (TextView) convertView;
            }

            Date date = getItem(position);

            if (date == null) {
                textView.setText("");
                textView.setBackgroundColor(Color.TRANSPARENT);
            } else {
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                int day = cal.get(Calendar.DAY_OF_MONTH);
                String dateStr = dateFormat.format(date);

                textView.setText(String.valueOf(day));

                // Check if it's a weekend
                boolean isWeekend = weekendDays.contains(day);

                // Check if it's a holiday
                boolean isHoliday = holidays.contains(dateStr);

                // Set background color
                if (isHoliday || isWeekend) {
                    textView.setBackgroundColor(Color.parseColor("#FFCDD2")); // Light red
                    textView.setTextColor(Color.RED);
                } else {
                    textView.setBackgroundColor(Color.WHITE);
                    textView.setTextColor(Color.BLACK);
                }
            }

            return textView;
        }
    }
}
