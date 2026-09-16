package com.example.calendar4;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Task 139: менеджер таблицы HOLIDAYS (праздники) - вынесен из ManageSQLDatabase
 * по образцу пер-табличных менеджеров. Повторно использует уже открытое соединение
 * (конструктор получает SQLiteDatabase). Record-класс - holidayRecord.
 */
public class HolidaysSQLManage {

    public static final String TABLE_HOLIDAYS = "HOLIDAYS";

    private final SQLiteDatabase db;

    public HolidaysSQLManage(SQLiteDatabase database) {
        db = database;
    }

    /** Праздники страны за год, упорядоченные по дате. */
    public holidayRecord[] getHolidays(String countryCode, int year) {
        ArrayList<holidayRecord> holidaysList = new ArrayList<>();
        String dateFrom = year + "-01-01";
        String dateTo = year + "-12-31";
        Cursor cursor = db.query("HOLIDAYS",
                null,
                "CountryCode=? AND HolidayDate>=? AND HolidayDate<=?",
                new String[]{countryCode, dateFrom, dateTo},
                null, null, "HolidayDate");
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                holidaysList.add(cursorToHoliday(cursor));
                cursor.moveToNext();
            }
        }
        cursor.close();
        holidayRecord[] holidays = new holidayRecord[holidaysList.size()];
        holidaysList.toArray(holidays);
        return holidays;
    }

    // Маппинг строки курсора HOLIDAYS в holidayRecord
    private holidayRecord cursorToHoliday(Cursor cursor) {
        holidayRecord record = new holidayRecord();
        int idxId = cursor.getColumnIndex("id");
        int idxCountryCode = cursor.getColumnIndex("CountryCode");
        int idxHolidayDate = cursor.getColumnIndex("HolidayDate");
        int idxHolidayName = cursor.getColumnIndex("HolidayName");
        if (idxId >= 0 && !cursor.isNull(idxId)) record.id = cursor.getInt(idxId);
        if (idxCountryCode >= 0 && !cursor.isNull(idxCountryCode)) record.CountryCode = cursor.getString(idxCountryCode);
        if (idxHolidayName >= 0 && !cursor.isNull(idxHolidayName)) record.HolidayName = cursor.getString(idxHolidayName);
        if (idxHolidayDate >= 0 && !cursor.isNull(idxHolidayDate)) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                record.HolidayDate = sdf.parse(cursor.getString(idxHolidayDate));
            } catch (Exception e) {
                record.HolidayDate = null;
            }
        }
        return record;
    }

    /** Insert or update holidays for a specific country and year. */
    public void upsertHolidays(String countryCode, int year, holidayRecord[] holidays) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        for (holidayRecord holiday : holidays) {
            ContentValues values = new ContentValues();
            values.put("CountryCode", countryCode);
            values.put("HolidayDate", sdf.format(holiday.HolidayDate));
            values.put("HolidayName", holiday.HolidayName);
            int rowsAffected = db.update("HOLIDAYS", values,
                    "CountryCode=? AND HolidayDate=?",
                    new String[]{countryCode, sdf.format(holiday.HolidayDate)});
            if (rowsAffected == 0) db.insert("HOLIDAYS", null, values);
        }
    }

    /** Проверка: нет ли в БД праздников с начала указанного месяца. */
    public boolean needsHolidayUpdate(String countryCode, int year, int month) {
        String dateFrom = year + "-" + String.format("%02d", month) + "-01";
        Cursor cursor = db.query("HOLIDAYS",
                new String[]{"COUNT(*) as count"},
                "CountryCode=? AND HolidayDate>=?",
                new String[]{countryCode, dateFrom},
                null, null, null);
        boolean needsUpdate = true;
        if (cursor.moveToFirst()) needsUpdate = (cursor.getInt(0) == 0);
        cursor.close();
        return needsUpdate;
    }
}
