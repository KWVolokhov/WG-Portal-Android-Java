package com.example.calendar4;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
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
 * Task 144: с 1 сентября проверяется и загружается также следующий год.
 * Task 166: производственный календарь - нерабочие дни (включая переносы) хранятся
 * в статическом кэше и загружаются для любого запрашиваемого года.
 * Task 167: если календарь текущего года не получен, попытка повторяется 1 раз за запуск приложения.
 */
final class RussianHolidaysLoader {

    private RussianHolidaysLoader() {
    }

    /** Статический кэш нерабочих дней (производственный календарь) по датам "yyyy-MM-dd". */
    private static final HashSet<String> holidaysCache = new HashSet<>();
    /** Года, для которые кэш уже заполнен (из БД или интернета). */
    private static final HashSet<Integer> cachedYears = new HashSet<>();
    /** Флаг: был ли выполнен хотя бы один успешный fetch текущего года за запуск приложения. */
    private static boolean currentYearFetched = false;

    /** Проверка наличия сетевого подключения. */
    private static boolean isNetworkAvailable(Context ctx) {
        try {
            ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                Network actNet = cm.getActiveNetwork();
                if (actNet != null) {
                    NetworkCapabilities capability = cm.getNetworkCapabilities(actNet);
                    if (capability != null) {
                        return capability.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                                capability.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
                    }
                }
            }
        } catch (Exception ignored) { }
        return false;
    }

    /**
     * Загружает праздники текущего года: при необходимости - из интернета, затем в календарь.
     */
    static void fetch(MainActivity act) {
        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR);
        int currentMonth = cal.get(Calendar.MONTH) + 1;
        int currentDay = cal.get(Calendar.DAY_OF_MONTH);
        boolean isFirstDayOfMonth = (currentDay == 1);
        boolean needsUpdate = MainActivity.owerDb.needsHolidayUpdate("RUS", currentYear, currentMonth);
        if (RussianHolidaysFetcher.yearsToFetch().size() > 1 && MainActivity.owerDb.needsHolidayUpdate("RUS", currentYear + 1, 1))
            needsUpdate = true;
        if (!needsUpdate && !isFirstDayOfMonth) {
            loadFromDatabase(act, currentYear);
            return;
        }
        if (!isNetworkAvailable(act)) {
            act.runOnUiThread(() -> Toast.makeText(act,
                    "Нет доступа к интернету — праздники загружены из локальной базы", Toast.LENGTH_LONG).show());
            loadFromDatabase(act, currentYear);
            return;
        }
        act.holidaysFetcher = new RussianHolidaysFetcher();
        act.holidaysFetcher.fetchHolidaysForYears(
                new RussianHolidaysFetcher.HolidaysFetchListener() {
                    @Override
                    public void onHolidaysFetched(HashSet<String> holidays) {
                        saveHolidays(currentYear, holidays);
                        currentYearFetched = true;
                        loadFromDatabase(act, currentYear);
                    }
                    @Override
                    public void onError(String error) {
                        act.runOnUiThread(() -> Toast.makeText(act,
                                "Ошибка загрузки праздников: " + error, Toast.LENGTH_SHORT).show());
                        loadFromDatabase(act, currentYear);
                    }
                });
    }

    /**
     * Task 167: гарантирует, что производственный календарь текущего года получен.
     * Вызывается при первом использовании RussianCalendarView.
     * Если данные за текущий год ещё не загружены, делает одну повторную попытку
     * скачать их независимо от даты.
     */
    static void ensureCurrentYearLoaded(MainActivity act) {
        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR);
        if (cachedYears.contains(currentYear) && !holidaysCache.isEmpty()) return;
        holidayRecord[] dbHolidays = MainActivity.owerDb.getHolidays("RUS", currentYear);
        if (dbHolidays != null && dbHolidays.length > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            for (holidayRecord hr : dbHolidays) {
                if (hr.HolidayDate != null) holidaysCache.add(sdf.format(hr.HolidayDate));
            }
            cachedYears.add(currentYear);
            return;
        }
        if (currentYearFetched) return;
        if (!isNetworkAvailable(act)) return;
        currentYearFetched = true;
        act.holidaysFetcher = new RussianHolidaysFetcher();
        act.holidaysFetcher.fetchHolidaysForYear(currentYear, new RussianHolidaysFetcher.HolidaysFetchListener() {
            @Override
            public void onHolidaysFetched(HashSet<String> holidays) {
                saveHolidays(currentYear, holidays);
                loadFromDatabase(act, currentYear);
            }
            @Override
            public void onError(String error) {
                act.runOnUiThread(() -> Toast.makeText(act,
                        "Ошибка загрузки производственного календаря: " + error, Toast.LENGTH_SHORT).show());
                loadFromDatabase(act, currentYear);
            }
        });
    }

    /**
     * Task 166: загружает производственный календарь для указанного года.
     * Если в кэше/БД нет данных — пытается скачать с интернета.
     */
    static void ensureYearLoaded(Context ctx, int year, final YearLoadedListener listener) {
        if (cachedYears.contains(year) && !holidaysCache.isEmpty()) {
            listener.onYearLoaded(year, getHolidaysForYear(ctx, year));
            return;
        }
        if (MainActivity.owerDb == null) MainActivity.owerDb = ManageSQLDatabase.getInstance(ctx);
        holidayRecord[] dbHolidays = MainActivity.owerDb.getHolidays("RUS", year);
        if (dbHolidays != null && dbHolidays.length > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            for (holidayRecord hr : dbHolidays) {
                if (hr.HolidayDate != null) holidaysCache.add(sdf.format(hr.HolidayDate));
            }
            cachedYears.add(year);
            listener.onYearLoaded(year, getHolidaysForYear(ctx, year));
            return;
        }
        if (!isNetworkAvailable(ctx)) {
            listener.onYearLoaded(year, new HashSet<String>());
            return;
        }
        RussianHolidaysFetcher fetcher = new RussianHolidaysFetcher();
        fetcher.fetchHolidaysForYear(year, new RussianHolidaysFetcher.HolidaysFetchListener() {
            @Override
            public void onHolidaysFetched(HashSet<String> holidays) {
                saveHolidays(year, holidays);
                cachedYears.add(year);
                listener.onYearLoaded(year, getHolidaysForYear(ctx, year));
            }
            @Override
            public void onError(String error) {
                listener.onYearLoaded(year, new HashSet<String>());
            }
        });
    }

    public interface YearLoadedListener {
        void onYearLoaded(int year, HashSet<String> holidays);
    }

    /**
     * Возвращает нерабочие дни для указанного года из кэша или из локальной БД.
     * Если данных нет — возвращает пустое множество.
     */
    static HashSet<String> getHolidaysForYear(Context ctx, int year) {
        if (cachedYears.contains(year) && !holidaysCache.isEmpty()) {
            HashSet<String> result = new HashSet<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Calendar c = Calendar.getInstance();
            for (String dateStr : holidaysCache) {
                try {
                    c.setTime(sdf.parse(dateStr));
                    if (c.get(Calendar.YEAR) == year) {
                        result.add(dateStr);
                    }
                } catch (Exception ignored) { }
            }
            return result;
        }
        holidayRecord[] holidays = MainActivity.owerDb.getHolidays("RUS", year);
        HashSet<String> holidayStrings = new HashSet<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        for (holidayRecord holiday : holidays) {
            if (holiday.HolidayDate != null) {
                String ds = sdf.format(holiday.HolidayDate);
                holidayStrings.add(ds);
                holidaysCache.add(ds);
            }
        }
        cachedYears.add(year);
        return holidayStrings;
    }

    // Преобразует HashSet строк "yyyy-MM-dd Имя" в holidayRecord[] и сохраняет в БД
    private static void saveHolidays(int currentYear, HashSet<String> holidays) {
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
            MainActivity.owerDb.upsertHolidays("RUS", currentYear, validRecords);
        }
    }

    /**
     * Загружает праздники из HOLIDAYS и передаёт их в RussianCalendarView.
     */
    static void loadFromDatabase(MainActivity act, int year) {
        holidayRecord[] holidays = MainActivity.owerDb.getHolidays("RUS", year);
        HashSet<String> holidayStrings = new HashSet<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        for (holidayRecord holiday : holidays) {
            if (holiday.HolidayDate != null) {
                String ds = sdf.format(holiday.HolidayDate);
                holidayStrings.add(ds);
                holidaysCache.add(ds);
            }
        }
        cachedYears.add(year);
        RussianCalendarView russianCalendar = act.findViewById(R.id.calendarView1);
        russianCalendar.setHolidays(holidayStrings);
        act.runOnUiThread(() -> {
            String msg = "Производственный календарь загружен: " + holidayStrings.size() + " дней";
            Toast.makeText(act, msg, Toast.LENGTH_SHORT).show();
        });
    }
}
