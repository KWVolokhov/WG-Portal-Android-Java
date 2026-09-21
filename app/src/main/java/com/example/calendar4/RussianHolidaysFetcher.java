package com.example.calendar4;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RussianHolidaysFetcher {
    private static final String TAG = "RussianHolidaysFetcher";
    private static final String HOLIDAYS_API_URL = "https://date.nager.at/api/v3/PublicHolidays/";
    private Context context;
    private ExecutorService executor;
    private HolidaysFetchListener listener;

    public interface HolidaysFetchListener {
        void onHolidaysFetched(HashSet<String> holidays);
        void onError(String error);
    }

    public RussianHolidaysFetcher(Context context) {
        this.context = context;
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void fetchHolidaysForYear(int year, @NonNull HolidaysFetchListener listener) {
        this.listener = listener;

        executor.execute(() -> {
            HashSet<String> holidays = new HashSet<>();
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {
                // Using Nager.Date API for Russian holidays (country code: RU)
                String urlString = HOLIDAYS_API_URL + year + "/RU";
                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setConnectTimeout(10000); // 10 seconds
                urlConnection.setReadTimeout(10000);
                urlConnection.connect();

                int responseCode = urlConnection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    if (listener != null) {
                        listener.onError("HTTP error code: " + responseCode);
                    }
                    return;
                }

                reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder buffer = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                String jsonString = buffer.toString();
                JSONArray holidaysArray = new JSONArray(jsonString);

                SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());

                for (int i = 0; i < holidaysArray.length(); i++) {
                    JSONObject holidayObj = holidaysArray.getJSONObject(i);
                    String date = holidayObj.getString("date");
                    holidays.add(date);
                }

                if (listener != null) {
                    listener.onHolidaysFetched(holidays);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error fetching holidays", e);
                if (listener != null) {
                    listener.onError("Error: " + e.getMessage());
                }
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception e) {
                        Log.e(TAG, "Error closing reader", e);
                    }
                }
            }
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
        this.listener = listener;
        fetchYears(yearsToFetch(), 0, new HashSet<String>(), listener);
    }

    // Последовательная загрузка по списку лет на однопоточном executor
    private void fetchYears(final ArrayList<Integer> years, final int index, final HashSet<String> collected, final HolidaysFetchListener listener) {
        if (index >= years.size()) {
            if (listener != null) listener.onHolidaysFetched(collected);
            return;
        }
        fetchHolidaysForYear(years.get(index), new HolidaysFetchListener() {
            @Override
            public void onHolidaysFetched(HashSet<String> holidays) {
                collected.addAll(holidays);
                fetchYears(years, index + 1, collected, listener);
            }

            @Override
            public void onError(String error) {
                if (listener != null) listener.onError(error);
            }
        });
    }

    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
