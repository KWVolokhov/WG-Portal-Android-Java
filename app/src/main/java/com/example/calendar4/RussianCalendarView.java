package com.example.calendar4;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
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
    private ImageButton prevMonthButton;
    private ImageButton nextMonthButton;
    private ImageView chineseZodiacIcon;
    private ImageView westernZodiacIcon;
    private HashSet<String> holidays;
    private HashSet<Integer> weekendDays;
    private OnDateSelectedListener dateSelectedListener;
    private Date currentDate;
    public Date activeDate; // Active/selected date

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
        prevMonthButton = findViewById(R.id.prevMonthButton);
        nextMonthButton = findViewById(R.id.nextMonthButton);
        chineseZodiacIcon = findViewById(R.id.chineseZodiacIcon);
        westernZodiacIcon = findViewById(R.id.westernZodiacIcon);

        currentCalendar = Calendar.getInstance();
        currentDate = Calendar.getInstance().getTime();
        activeDate = Calendar.getInstance().getTime(); // Initialize active date to today
        holidays = new HashSet<>();
        weekendDays = new HashSet<>();

        adapter = new CalendarAdapter(context);
        calendarGridView.setAdapter(adapter);

        // Set click listener for dates
        calendarGridView.setOnItemClickListener((parent, view, position, id) -> {
            Date selectedDate = adapter.getItem(position);
            if (selectedDate != null) {
                // Set as active date
                activeDate = selectedDate;
                // Show date details with calendar and working days count
                showDateDetails(selectedDate);
                // Also call the external listener if set
                if (dateSelectedListener != null) {
                    dateSelectedListener.onDateSelected(selectedDate);
                }
                // Refresh calendar to show active date frame
                adapter.notifyDataSetChanged();
            }
        });

        // Set navigation button listeners
        prevMonthButton.setOnClickListener(v -> navigateToPreviousMonth());
        nextMonthButton.setOnClickListener(v -> navigateToNextMonth());

        // Set long click listener for detailed date info
        calendarGridView.setOnItemLongClickListener((parent, view, position, id) -> {
            Date selectedDate = adapter.getItem(position);
            if (selectedDate != null) {
                showDateDetails(selectedDate);
                return true;
            }
            return false;
        });

        updateCalendar();
    }

    private void navigateToPreviousMonth() {
        currentCalendar.add(Calendar.MONTH, -1);
        // Set active date to the last day of the previous month
        Calendar tempCal = (Calendar) currentCalendar.clone();
        tempCal.set(Calendar.DAY_OF_MONTH, tempCal.getActualMaximum(Calendar.DAY_OF_MONTH));
        activeDate = tempCal.getTime();
        updateCalendar();
    }

    private void navigateToNextMonth() {
        currentCalendar.add(Calendar.MONTH, 1);
        // Set active date to the first day of the next month
        Calendar tempCal = (Calendar) currentCalendar.clone();
        tempCal.set(Calendar.DAY_OF_MONTH, 1);
        activeDate = tempCal.getTime();
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

    public void goToToday() {
        currentCalendar = Calendar.getInstance();
        activeDate = Calendar.getInstance().getTime();
        updateCalendar();
    }

    public int getCalendarDaysBetween(Date startDate, Date endDate) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(startDate);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(endDate);

        // Reset time to midnight for accurate day calculation
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);

        cal2.set(Calendar.HOUR_OF_DAY, 0);
        cal2.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.SECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);

        long diff = cal2.getTimeInMillis() - cal1.getTimeInMillis();
        return (int) (diff / (24 * 60 * 60 * 1000));
    }

    public int getWorkingDaysBetween(Date startDate, Date endDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);

        int workingDays = 0;

        // Ensure start is before end
        if (cal.after(endCal)) {
            Calendar temp = cal;
            cal = endCal;
            endCal = temp;
        }

        // Reset time to midnight
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        endCal.set(Calendar.HOUR_OF_DAY, 0);
        endCal.set(Calendar.MINUTE, 0);
        endCal.set(Calendar.SECOND, 0);
        endCal.set(Calendar.MILLISECOND, 0);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        while (!cal.after(endCal)) {
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            String dateStr = dateFormat.format(cal.getTime());

            // Count if it's not Saturday (7) or Sunday (1) and not a holiday
            if (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY) {
                if (!holidays.contains(dateStr)) {
                    workingDays++;
                }
            }

            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        return workingDays;
    }

    private void updateCalendar() {
        // Update month/year text
        SimpleDateFormat sdf = new SimpleDateFormat("LLLL yyyy", new Locale("ru", "RU"));
        monthYearText.setText(sdf.format(currentCalendar.getTime()));

        // Change month/year text color based on whether it's current month
        Calendar today = Calendar.getInstance();
        if (currentCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                currentCalendar.get(Calendar.MONTH) == today.get(Calendar.MONTH)) {
            monthYearText.setTextColor(Color.WHITE); // Current month - white
        } else {
            monthYearText.setTextColor(Color.GRAY); // Other month - gray
        }

        // Update zodiac icons
        updateZodiacIcons();

        // Update adapter to regenerate dates for current month
        adapter.generateDates();
        adapter.notifyDataSetChanged();
    }

    private void updateZodiacIcons() {
        // Update Chinese zodiac icon based on year
        int year = currentCalendar.get(Calendar.YEAR);
        String chineseZodiac = getChineseZodiac(year);
        // Set placeholder icon (in real app, you would have actual zodiac icons)
        chineseZodiacIcon.setImageResource(android.R.drawable.ic_dialog_info);

        // Update Western zodiac icon based on month
        int month = currentCalendar.get(Calendar.MONTH);
        String westernZodiac = getWesternZodiac(month);
        // Set placeholder icon (in real app, you would have actual zodiac icons)
        westernZodiacIcon.setImageResource(android.R.drawable.ic_dialog_info);

    }

    private String getChineseZodiac(int year) {
        String[] zodiacs = {"Крыса", "Бык", "Тигр", "Кролик", "Дракон", "Змея",
                "Лошадь", "Коза", "Обезьяна", "Петух", "Собака", "Свинья"};
        int index = (year - 1900) % 12;
        if (index < 0) index += 12;
        return zodiacs[index];
    }

    private String getWesternZodiac(int month) {
        String[] zodiacs = {"Козерог", "Водолей", "Рыбы", "Овен", "Телец", "Близнецы",
                "Рак", "Лев", "Дева", "Весы", "Скорпион", "Стрелец"};
        return zodiacs[month];
    }

    private void calculateWeekends() {
        weekendDays.clear();
        // This method is kept for compatibility but weekends are now calculated per-date in adapter
    }

    private void showDateDetails(Date selectedDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(selectedDate);

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy", new Locale("ru", "RU"));
        String dateStr = sdf.format(selectedDate);

        int calendarDays = getCalendarDaysBetween(new Date(), selectedDate);
        int workingDays = getWorkingDaysBetween(selectedDate, new Date());

        String details = String.format("%s, кд:%d, рд:%d",
                dateStr, calendarDays, workingDays);

        android.widget.Toast.makeText(getContext(), details, android.widget.Toast.LENGTH_LONG).show();
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

        public void generateDates() {
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
                textView.setLayoutParams(new GridView.LayoutParams(60, 60)); // 2x smaller
                textView.setGravity(android.view.Gravity.CENTER);
                textView.setTextSize(12); // Smaller text
                textView.setPadding(4, 4, 4, 4); // Smaller padding
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

                // Check if it's a weekend by checking the actual day of week for this date
                Calendar dayCal = Calendar.getInstance();
                dayCal.setTime(date);
                int dayOfWeek = dayCal.get(Calendar.DAY_OF_WEEK);
                boolean isWeekend = (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY);

                // Check if it's a holiday
                boolean isHoliday = holidays.contains(dateStr);

                // Set background and text color
                if (isHoliday || isWeekend) {
                    textView.setBackgroundColor(Color.parseColor("#FFCDD2")); // Light red for weekends/holidays
                    textView.setTextColor(Color.RED);
                } else {
                    textView.setBackgroundColor(Color.TRANSPARENT); // Transparent for working days
                    textView.setTextColor(Color.WHITE); // White text for working days
                }

                // Highlight current day with blue text (takes priority)
                String currentDateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentDate);
                if (dateStr.equals(currentDateStr)) {
                    textView.setTextColor(Color.BLUE);
                    textView.setTypeface(null, android.graphics.Typeface.BOLD);
                }

                // Highlight active date with a frame/border
                String activeDateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(activeDate);
                if (dateStr.equals(activeDateStr)) {
                    // Draw a custom border/frame around the active date
                    textView.setBackgroundResource(R.drawable.active_date_frame);
                    // Ensure text color is preserved
                    if (!dateStr.equals(currentDateStr)) {
                        if (isHoliday || isWeekend) {
                            textView.setTextColor(Color.RED);
                        } else {
                            textView.setTextColor(Color.WHITE);
                        }
                    }
                }
            }

            return textView;
        }
    }
}