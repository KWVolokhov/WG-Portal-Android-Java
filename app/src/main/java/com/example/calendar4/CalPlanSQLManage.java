package com.example.calendar4;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

/**
 * Task 139: менеджер таблицы CALPLAN (Project/Task/Request) - вынесен из
 * ManageSQLDatabase по образцу пер-табличных менеджеров (HistorySQLManage и др.).
 * Повторно использует уже открытое соединение (конструктор получает SQLiteDatabase).
 * Record-класс - calPlanRecord.
 */
public class CalPlanSQLManage {

    public static final String TABLE_CALPLAN = "CALPLAN";

    private final SQLiteDatabase db;

    public CalPlanSQLManage(SQLiteDatabase database) {
        db = database;
    }

    /** Upsert (Insert or Update) calPlanRecord в CALPLAN (только Project/Task/Request). */
    public void upsert(calPlanRecord record) {
        if (record == null) return;
        ContentValues values = new ContentValues();
        fillCalPlanValues(values, record);
        // Try to update first, if no rows affected then insert
        int rowsAffected = db.update("CALPLAN", values, "id=?",
                new String[]{String.valueOf(record.id)});
        if (rowsAffected == 0) db.insert("CALPLAN", null, values);
    }

    // Автозаполняемые и пользовательские поля записи CALPLAN
    private void fillCalPlanValues(ContentValues values, calPlanRecord record) {
        if (record.id != null) values.put("id", record.id);
        // Generate a unique ID for brand-new records (RequestUNID references these)
        if (record.id == null && (record.UNID == null || record.UNID.isEmpty())) {
            record.UNID = java.util.UUID.randomUUID().toString();
        }
        if (record.UNID != null) values.put("UNID", record.UNID);
        if (record.Okdate != null) values.put("Okdate", fmtDateTimeString(record.Okdate));
        if (record.AuthorID != null) values.put("AuthorID", record.AuthorID);
        if (record.LastUpdatedByID != null) values.put("LastUpdatedByID", record.LastUpdatedByID);
        if (record.LastUpdatedBy != null) values.put("LastUpdatedBy", record.LastUpdatedBy);
        if (record.LastUpdatedDate != null) {
            values.put("LastUpdatedDate", fmtDateTimeString(record.LastUpdatedDate));
        }
        if (record.HoldDate != null) values.put("HoldDate", fmtDateTimeString(record.HoldDate));
        if (record.Revisions != null) values.put("Revisions", record.Revisions);
        if (record.Form != null) values.put("Form", record.Form);
        if (record.Name != null) values.put("Name", record.Name);
        if (record.Priority != null) values.put("Priority", record.Priority);
        if (record.AuthorName != null) values.put("AuthorName", record.AuthorName);
        if (record.RequestName != null) values.put("RequestName", record.RequestName);
        if (record.RequestUNID != null) values.put("RequestUNID", record.RequestUNID);
        if (record.Status != null) values.put("Status", record.Status);
        if (record.StatusID != null) values.put("StatusID", record.StatusID);
        if (record.MainSystem != null) values.put("MainSystem", record.MainSystem);
        if (record.AnalitikID != null) values.put("AnalitikID", record.AnalitikID);
        if (record.AnalitikName != null) values.put("AnalitikName", record.AnalitikName);
        if (record.ExectorID != null) values.put("ExectorID", record.ExectorID);
        if (record.ExectorName != null) values.put("ExectorName", record.ExectorName);
        if (record.BodyText != null) values.put("BodyText", record.BodyText);
        if (record.Comment != null) values.put("Comment", record.Comment);
        if (record.StartDate != null) {
            // Task 115: Дата старта хранится в SQLite с 0ч 0м 0с.
            values.put("StartDate", fmtDateTimeString(startOfDay(record.StartDate)));
        }
        if (record.EndDate != null) values.put("EndDate", fmtDateTimeString(record.EndDate));
        if (record.InstallOrder != null) values.put("InstallOrder", record.InstallOrder);
        if (record.KeyWords != null) values.put("KeyWords", record.KeyWords);
    }

    /** Delete a CALPLAN record by its id. */
    public void delete(Integer id) {
        if (id == null) return;
        db.delete("CALPLAN", "id=?", new String[]{String.valueOf(id)});
    }

    /** Записи CALPLAN, действующие в указанный день: день внутри [StartDate..EndDate]. */
    public calPlanRecord[] getByDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateStr = sdf.format(date);
        // Task 115: StartDate может храниться с 00:00:00 - сравниваем только дату (первые 10 символов)
        // Task 38: без EndDate запись действует бесконечно.
        return query("substr(StartDate,1,10)<=? AND (EndDate IS NULL OR substr(EndDate,1,10)>=?)",
                new String[]{dateStr, dateStr});
    }

    /** Все записи CALPLAN заданной Form (например "Project" для выбора в карточке). */
    public calPlanRecord[] getByForm(String form) {
        return query("Form=?", new String[]{form});
    }

    /** "Проекты\Все" (workOnly=false) - Project/Task/Request, сортировка по StartDate. */
    public calPlanRecord[] getProjectsTasks(String filter) {
        return getProjectsTasks(filter, false);
    }

    /**
     * "Проекты" screen query (All or Work mode).
     *
     * @param workOnly when true keeps only projects/tasks in status
     *                 "В работе"/"Тестирование" (StatusID Inwork/Intest);
     *                 when false all projects and tasks are returned.
     */
    public calPlanRecord[] getProjectsTasks(String filter, boolean workOnly) {
        String baseWhere = "(Form='Project' OR Form='Task' OR Form='Request')";
        if (workOnly) {
            baseWhere += " AND (StatusID IN ('Inwork','Intest') OR Status IN ('В работе','Тестирование'))";
        }
        calPlanRecord[] arr;
        if (filter != null && filter.trim().length() >= 3) {
            String pattern = "%" + filter.trim().toLowerCase(Locale.getDefault()) + "%";
            arr = query(baseWhere + " AND LOWER(Name) LIKE ?", new String[]{pattern});
        } else {
            arr = query(baseWhere, null);
        }
        if (arr == null || arr.length == 0) return new calPlanRecord[0];
        ArrayList<calPlanRecord> list = new ArrayList<>(Arrays.asList(arr));
        // WG: Collections.sort вместо list.sort(...) - Java-8 метод sort отсутствует на Android 8.
        Collections.sort(list, (a, b) -> {
            Date da = a.StartDate;
            Date db2 = b.StartDate;
            if (da == null && db2 == null) return 0;
            if (da == null) return 1;
            if (db2 == null) return -1;
            return da.compareTo(db2);
        });
        return list.toArray(new calPlanRecord[0]);
    }

    // Task 124: список задач, привязанных к проекту (Задача.RequestUNID = UNID проекта)
    public calPlanRecord[] getTasksByProject(String projectUNID) {
        if (projectUNID == null || projectUNID.isEmpty()) return new calPlanRecord[0];
        return query("Form='Task' AND RequestUNID=?", new String[]{projectUNID});
    }

    // Task 124: проекты, привязанные к заявке (Проект.RequestName = название заявки)
    public calPlanRecord[] getProjectsByRequestName(String requestName) {
        if (requestName == null || requestName.trim().isEmpty()) return new calPlanRecord[0];
        return query("Form='Project' AND RequestName=?", new String[]{requestName.trim()});
    }

    // Универсальный запрос CALPLAN, используется всеми выборками класса
    private calPlanRecord[] query(String selection, String[] args) {
        ArrayList<calPlanRecord> recordsList = new ArrayList<>();
        Cursor cursor = db.query("CALPLAN", null, selection, args, null, null, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                recordsList.add(cursorToCalPlan(cursor));
                cursor.moveToNext();
            }
        }
        cursor.close();
        calPlanRecord[] records = new calPlanRecord[recordsList.size()];
        recordsList.toArray(records);
        return records;
    }

    // Маппинг строки курсора CALPLAN в calPlanRecord (все поля + даты Task 44/53)
    private calPlanRecord cursorToCalPlan(Cursor cursor) {
        calPlanRecord record = new calPlanRecord();
        record.id = getInt(cursor, "id");
        record.UNID = getString(cursor, "UNID");
        record.Form = getString(cursor, "Form");
        record.Priority = getInt(cursor, "Priority");
        record.AuthorID = getString(cursor, "AuthorID");
        record.AuthorName = getString(cursor, "AuthorName");
        record.Name = getString(cursor, "Name");
        record.RequestName = getString(cursor, "RequestName");
        record.RequestUNID = getString(cursor, "RequestUNID");
        record.Status = getString(cursor, "Status");
        record.StatusID = getString(cursor, "StatusID");
        record.MainSystem = getString(cursor, "MainSystem");
        record.AnalitikID = getString(cursor, "AnalitikID");
        record.AnalitikName = getString(cursor, "AnalitikName");
        record.ExectorID = getString(cursor, "ExectorID");
        record.ExectorName = getString(cursor, "ExectorName");
        record.LastUpdatedByID = getString(cursor, "LastUpdatedByID");
        record.LastUpdatedBy = getString(cursor, "LastUpdatedBy");
        record.BodyText = getString(cursor, "BodyText");
        record.Comment = getString(cursor, "Comment");
        record.InstallOrder = getString(cursor, "InstallOrder");
        record.KeyWords = getString(cursor, "KeyWords");
        record.Revisions = getString(cursor, "Revisions");
        record.Okdate = parseDateFlexible(cursor, cursor.getColumnIndex("Okdate"));
        record.LastUpdatedDate = parseDateFlexible(cursor, cursor.getColumnIndex("LastUpdatedDate"));
        record.StartDate = parseDateFlexible(cursor, cursor.getColumnIndex("StartDate"));
        record.EndDate = parseDateFlexible(cursor, cursor.getColumnIndex("EndDate"));
        record.HoldDate = parseDateFlexible(cursor, cursor.getColumnIndex("HoldDate"));
        return record;
    }

    // Значение int колонки (null, если колонки нет или значение NULL)
    private Integer getInt(Cursor cursor, String column) {
        int idx = cursor.getColumnIndex(column);
        if (idx < 0 || cursor.isNull(idx)) return null;
        return cursor.getInt(idx);
    }

    // Значение String колонки (null, если колонки нет или значение NULL)
    private String getString(Cursor cursor, String column) {
        int idx = cursor.getColumnIndex(column);
        if (idx < 0 || cursor.isNull(idx)) return null;
        return cursor.getString(idx);
    }

    // Task 44: Okdate/LastUpdatedDate/EndDate/HoldDate хранятся со временем.
    private Date parseDateFlexible(Cursor cursor, int columnIndex) {
        if (columnIndex == -1 || cursor.isNull(columnIndex)) return null;
        String s = cursor.getString(columnIndex);
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(s);
        } catch (Exception e) {
            try {
                return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(s);
            } catch (Exception e2) {
                return null;
            }
        }
    }

    private String fmtDateTimeString(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(date);
    }

    /** Task 115: обнуляет время даты (0ч 0м 0с 0мс) для хранения Даты старта. */
    private static Date startOfDay(Date d) {
        if (d == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}
