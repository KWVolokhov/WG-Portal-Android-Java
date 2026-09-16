package com.example.calendar4;

import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

/**
 * Task 139: загрузка праздников (интернет -> HOLIDAYS -> календарь) - вынесено из
 * MainActivity. Проверяется необходимость обновления (нет данных с начала месяца
 * или сегодня 1-е число), затем RussianHolidaysFetcher и сохранение в БД.
 */
final class RussianHolidaysLoader {

    private RussianHolidaysLoader() {
    }

    /** Загружает праздники текущего года: при необходимости - из интернета, затем в календарь. */
    static void fetch(MainActivity act) {
        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR);
        int currentMonth = cal.get(Calendar.MONTH) + 1; // 1-based month
        int currentDay = cal.get(Calendar.DAY_OF_MONTH);
        // Check if holidays need to be updated (on 1st day of month or if no data)
        boolean isFirstDayOfMonth = (currentDay == 1);
        boolean needsUpdate = act.owerDb.needsHolidayUpdate("RUS", currentYear, currentMonth);
        if (!needsUpdate && !isFirstDayOfMonth) {
            loadFromDatabase(act);
            return;
        }
        act.holidaysFetcher = new RussianHolidaysFetcher(act);
        act.holidaysFetcher.fetchHolidaysForYear(currentYear, new RussianHolidaysFetcher.HolidaysFetchListener() {
            @Override
            public void onHolidaysFetched(HashSet<String> holidays) {
                saveHolidays(act, currentYear, holidays);
                loadFromDatabase(act);
            }

            @Override
            public void onError(String error) {
                act.runOnUiThread(() -> Toast.makeText(act,
                        "Ошибка загрузки праздников: " + error, Toast.LENGTH_SHORT).show());
                // Try to load from database anyway
                //WG 11.08.26 loadFromDatabase(act);   //Нарушение свежей безопасности Андроида
            }
        });
    }

    // Преобразует HashSet строк "yyyy-MM-dd Имя" в holidayRecord[] и сохраняет в БД
    private static void saveHolidays(MainActivity act, int currentYear, HashSet<String> holidays) {
        holidayRecord[] holidayRecords = new holidayRecord[holidays.size()];
        int index = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        for (String holidayStr : holidays) {
            // Assuming format is "yyyy-MM-dd HolidayName" or just date
            try {
                String[] parts = holidayStr.split(" ", 2);
                if (parts.length >= 1) {
                    Date holidayDate = sdf.parse(parts[0]);
                    String holidayName = parts.length > 1 ? parts[1] : "Праздник";
                    holidayRecords[index++] = new holidayRecord("RUS", holidayDate, holidayName);
                }
            } catch (Exception e) {
                // Skip invalid entries
            }
        }
        if (index > 0) {
            holidayRecord[] validRecords = new holidayRecord[index];
            System.arraycopy(holidayRecords, 0, validRecords, 0, index);
            act.owerDb.upsertHolidays("RUS", currentYear, validRecords);
        }
    }

    /** Загружает праздники из HOLIDAYS и передаёт их в RussianCalendarView. */
    static void loadFromDatabase(MainActivity act) {
        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR);
        holidayRecord[] holidays = act.owerDb.getHolidays("RUS", currentYear);
        // Convert to HashSet<String> for RussianCalendarView
        HashSet<String> holidayStrings = new HashSet<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        for (holidayRecord holiday : holidays) {
            if (holiday.HolidayDate != null) holidayStrings.add(sdf.format(holiday.HolidayDate));
        }
        RussianCalendarView russianCalendar = act.findViewById(R.id.calendarView1);
        russianCalendar.setHolidays(holidayStrings);
        act.runOnUiThread(() -> {
            String msg = "Праздники загружены: " + holidayStrings.size() + " дней";
            Toast.makeText(act, msg, Toast.LENGTH_SHORT).show();
        });
    }
}
