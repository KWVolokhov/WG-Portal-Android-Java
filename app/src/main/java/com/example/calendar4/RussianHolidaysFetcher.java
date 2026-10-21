package com.example.calendar4;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RussianHolidaysFetcher {
    private static final String TAG = "RussianHolidaysFetcher";
    private static final String HOLIDAYS_API_URL = "https://date.nager.at/api/v3/PublicHolidays/";
    private static final String HOLIDAYS_API_URL_BACKUP = "https://date.nager.at/api/v2/PublicHolidays/";
    private static final int CONNECT_TIMEOUT_MS = 30000; // 30 seconds — BV5300Plus может быть медленным
    private static final int READ_TIMEOUT_MS = 30000;    // 30 seconds
    private static final int MAX_RETRIES = 2;            // повтор при таймауте/сетевой ошибке
    private final ExecutorService executor;

    public interface HolidaysFetchListener {
        void onHolidaysFetched(HashSet<String> holidays);

        void onError(String error);
    }

    public RussianHolidaysFetcher() {
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void fetchHolidaysForYear(int year, @NonNull HolidaysFetchListener listener) {

        executor.execute(() -> {
            HashSet<String> holidays = new HashSet<>();
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            int attempt = 0;

            // Список шлюзов по приоритетам для фолбэка при таймауте/ошибке:
            // 1 — htmlweb.ru (.ru домен), 2 — calendar.kuzyak.in (доп. шлюз),
            // 3 — date.nager.at v3 (основной), 4 — date.nager.at v2 (резерв)
            String[] apiUrls = {
                    "https://htmlweb.ru/service/holidays.php?country=RU&year=" + year,
                    "https://calendar.kuzyak.in/api/holidays?country=RU&year=" + year,
                    HOLIDAYS_API_URL + year + "/RU",
                    HOLIDAYS_API_URL_BACKUP + year + "/RU"
            };
            int urlIndex = 0;

            while (urlIndex < apiUrls.length) {
                try {

                    URL url = new URL(apiUrls[urlIndex]);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setConnectTimeout(CONNECT_TIMEOUT_MS);
                    urlConnection.setReadTimeout(READ_TIMEOUT_MS);
                    urlConnection.connect();

                    int responseCode = urlConnection.getResponseCode();
                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        urlConnection.disconnect();
                        urlConnection = null;
                        try {
                            reader.close();
                        } catch (Exception ex) {
                            Log.e(TAG, "Error closing reader", ex);
                        }
                        reader = null;
                        urlIndex++;
                        continue;
                    }

                    reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder buffer = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }

                    String jsonString = buffer.toString();
                    parseHolidaysJson(jsonString, holidays);

                    listener.onHolidaysFetched(holidays);
                    return;

                } catch (java.net.SocketTimeoutException e) {
                    Log.e(TAG, "Timeout on URL " + apiUrls[urlIndex] + " attempt " + (attempt + 1), e);
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                        urlConnection = null;
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (Exception ex) {
                            Log.e(TAG, "Error closing reader", ex);
                        }
                        reader = null;
                    }
                    attempt++;
                    if (attempt > MAX_RETRIES) {
                        urlIndex++;
                        attempt = 0;
                    }
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error fetching holidays from URL " + apiUrls[urlIndex], e);
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                        urlConnection = null;
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (Exception ex) {
                            Log.e(TAG, "Error closing reader", ex);
                        }
                        reader = null;
                    }
                    urlIndex++;
                    attempt = 0;
                }
            }

            // Все шлюзы исчерпаны
            listener.onError("Таймаут соединения с серверами праздников (попробованы все шлюзы, нет доступа к интернету)");
        });
    }

    // Task 144: годы для загрузки — текущий; с 1 сентября добавляется следующий год
    public static ArrayList<Integer> yearsToFetch() {
        ArrayList<Integer> years = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        years.add(cal.get(Calendar.YEAR));
        if (cal.get(Calendar.MONTH) >= Calendar.SEPTEMBER) years.add(cal.get(Calendar.YEAR) + 1);
        return years;
    }

    // Task 144: загрузка за все нужные годы, слушатель получает объединённый набор один раз
    public void fetchHolidaysForYears(@NonNull final HolidaysFetchListener listener) {
        fetchYears(yearsToFetch(), 0, new HashSet<>(), listener, 0);
    }

    // Последовательная загрузка по списку лет на однопоточном executor.
    // При ошибке одного года продолжает следующий, сообщает об ошибке только если все годы провалились.
    private void fetchYears(final ArrayList<Integer> years, final int index, final HashSet<String> collected, final HolidaysFetchListener listener, int failedCount) {
        if (index >= years.size()) {
            if (listener != null) {
                if (failedCount == years.size()) {
                    listener.onError("Не удалось загрузить праздники ни за один год");
                } else if (collected.isEmpty() && failedCount > 0) {
                    listener.onError("Частичная ошибка загрузки праздников");
                } else {
                    listener.onHolidaysFetched(collected);
                }
            }
            return;
        }
        fetchHolidaysForYear(years.get(index), new HolidaysFetchListener() {
            @Override
            public void onHolidaysFetched(HashSet<String> holidays) {
                collected.addAll(holidays);
                fetchYears(years, index + 1, collected, listener, failedCount);
            }

            @Override
            public void onError(String error) {
                fetchYears(years, index + 1, collected, listener, failedCount + 1);
            }
        });
    }

    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }

    // Разбор JSON праздников из разных форматов шлюзов (в порядке приоритетов шлюзов)
    private void parseHolidaysJson(String jsonString, HashSet<String> holidays) throws Exception {
        // Пытаемся как объект с полем holiday (htmlweb.ru — приоритет 1)
        try {
            JSONObject root = new JSONObject(jsonString);
            Object holidayObj = root.opt("holiday");
            if (holidayObj instanceof JSONArray) {
                JSONArray arr = (JSONArray) holidayObj;
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject h = arr.getJSONObject(i);
                    String date = h.optString("date", "");
                    if (!date.isEmpty()) holidays.add(date);
                }
                return;
            }
        } catch (Exception ignored) {
        }

        // Пытаемся как объект с полем holidays (calendar.kuzyak.in — приоритет 2)
        try {
            JSONObject root = new JSONObject(jsonString);
            Object holidaysObj = root.opt("holidays");
            if (holidaysObj instanceof JSONArray) {
                JSONArray arr = (JSONArray) holidaysObj;
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject h = arr.getJSONObject(i);
                    String date = h.optString("date", "");
                    if (!date.isEmpty()) holidays.add(date);
                }
                return;
            }
        } catch (Exception ignored) {
        }

        // Пытаемся как массив объектов (date.nager.at v3/v2 — приоритеты 3, 4)
        try {
            JSONArray holidaysArray = new JSONArray(jsonString);
            for (int i = 0; i < holidaysArray.length(); i++) {
                JSONObject holidayObj = holidaysArray.getJSONObject(i);
                String date = holidayObj.optString("date", "");
                if (!date.isEmpty()) holidays.add(date);
            }
            return;
        } catch (Exception ignored) {
            // не массив — формат не определён
        }

        throw new Exception("Неизвестный формат JSON праздников");
    }
}
