package com.example.calendar4.localsql;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.calendar4.localsql.SQLNullHelper;
import com.example.calendar4.records.CalParamRecord;

/**
 * Task 139: менеджер таблицы CALPARAM (параметры приложения, 1 запись) -
 * вынесен из ManageSQLDatabase по образцу пер-табличных менеджеров.
 * Повторно использует уже открытое соединение (конструктор получает SQLiteDatabase).
 * Record-класс - CalParamRecord.
 */
public class CalParamSQLManage {

    public static final String TABLE_CALPARAM = "CALPARAM";

    private final SQLiteDatabase db;

    public CalParamSQLManage(SQLiteDatabase database) {
        db = database;
    }

    /** Получить запись параметров (первую в таблице). */
    public CalParamRecord get() {
        CalParamRecord record = null;
        Cursor cursor = db.query("CALPARAM", null, null, null, null, null, null, "1");
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            record = cursorToParam(cursor);
        }
        cursor.close();
        return record;
    }

    // Маппинг строки курсора CALPARAM в CalParamRecord (+ умолчания Tasks 54/100)
    private CalParamRecord cursorToParam(Cursor cursor) {
        CalParamRecord record = new CalParamRecord();
        int idxId = cursor.getColumnIndex("id");
        if (idxId >= 0 && !cursor.isNull(idxId)) record.id = cursor.getInt(idxId);
        record.Address = getString(cursor, "Address");
        record.Name = getString(cursor, "Name");
        record.Password = getString(cursor, "Password");
        record.Vedushii = getString(cursor, "Vedushii");
        record.VedushiiID = getString(cursor, "VedushiiID");
        record.StartPage = getString(cursor, "StartPage");
        record.Button1Id = getIntId(cursor, "Button1Id");
        record.Button2Id = getIntId(cursor, "Button2Id");
        record.Button3Id = getIntId(cursor, "Button3Id");
        record.Button4Id = getIntId(cursor, "Button4Id");
        record.Button5Id = getIntId(cursor, "Button5Id");
        record.Height = getInteger(cursor, "Height");
        record.Weight = getInteger(cursor, "Weight");
        record.Age = getInteger(cursor, "Age");
        record.AttachFolder = getString(cursor, "AttachFolder");
        record.DBName = getString(cursor, "DBName");
        record.SyncTime = getString(cursor, "SyncTime");
        // Task 54/100: умолчания, когда значение ещё не задано
        if (record.Height == null) record.Height = CalParamRecord.DEFAULT_HEIGHT;
        if (record.Weight == null) record.Weight = CalParamRecord.DEFAULT_WEIGHT;
        if (record.Age == null) record.Age = CalParamRecord.DEFAULT_AGE;
        if (record.AttachFolder == null || record.AttachFolder.trim().isEmpty()) {
            record.AttachFolder = CalParamRecord.DEFAULT_ATTACH_FOLDER;
        }
        if (record.DBName == null || record.DBName.trim().isEmpty()) record.DBName = CalParamRecord.DEFAULT_DB_NAME;
        return record;
    }

    // Upsert CalParamRecord into CALPARAM table
    public void upsert(CalParamRecord record) {
        ContentValues values = new ContentValues();
        SQLNullHelper.putText(values, "Address", record.Address);
        SQLNullHelper.putText(values, "Name", record.Name);
        SQLNullHelper.putText(values, "Password", record.Password);
        SQLNullHelper.putText(values, "Vedushii", record.Vedushii);
        SQLNullHelper.putText(values, "VedushiiID", record.VedushiiID);
        // StartPage: null = default (Календарь / MainActivity)
        if (record.StartPage != null) values.put("StartPage", record.StartPage);
        // Three configurable quick buttons (values = числовой id записи LIVETYPE)
        SQLNullHelper.putText(values, "Button1Id", record.Button1Id != null ? String.valueOf(record.Button1Id) : null);
        SQLNullHelper.putText(values, "Button2Id", record.Button2Id != null ? String.valueOf(record.Button2Id) : null);
        SQLNullHelper.putText(values, "Button3Id", record.Button3Id != null ? String.valueOf(record.Button3Id) : null);
        SQLNullHelper.putText(values, "Button4Id", record.Button4Id != null ? String.valueOf(record.Button4Id) : null);
        SQLNullHelper.putText(values, "Button5Id", record.Button5Id != null ? String.valueOf(record.Button5Id) : null);
        // Tasks 54/100: Рост/Вес/Возраст + папка вложений + имя базы
        SQLNullHelper.putInt(values, "Height", record.Height);
        SQLNullHelper.putInt(values, "Weight", record.Weight);
        SQLNullHelper.putInt(values, "Age", record.Age);
        SQLNullHelper.putText(values, "AttachFolder", record.AttachFolder);
        SQLNullHelper.putText(values, "DBName", record.DBName);
        SQLNullHelper.putText(values, "SyncTime", record.SyncTime);
        if (record.id != null) {
            int rowsAffected = db.update("CALPARAM", values, "id=?",
                    new String[]{String.valueOf(record.id)});
            if (rowsAffected == 0) db.insert("CALPARAM", null, values);
        } else {
            db.insert("CALPARAM", null, values);
        }
    }

    // Task 143: корректировка записи CALPARAM только в поле SyncTime (после успешного обмена)
    public void updateSyncTime(Integer recordId, String syncTime) {
        if (recordId == null) return;
        ContentValues values = new ContentValues();
        values.put("SyncTime", syncTime);
        db.update("CALPARAM", values, "id=?", new String[]{String.valueOf(recordId)});
    }

    // Значение String колонки (null, если колонки нет или значение NULL)
    private String getString(Cursor cursor, String column) {
        int idx = cursor.getColumnIndex(column);
        if (idx < 0 || cursor.isNull(idx)) return null;
        return cursor.getString(idx);
    }

    // Значение Integer колонки (null, если колонки нет или значение NULL)
    private Integer getInteger(Cursor cursor, String column) {
        int idx = cursor.getColumnIndex(column);
        if (idx < 0 || cursor.isNull(idx)) return null;
        return cursor.getInt(idx);
    }

    // Быстрая кнопка хранится TEXT-ом; null при отсутствии/нечисловом значении
    private Integer getIntId(Cursor cursor, String column) {
        String value = getString(cursor, column);
        if (value == null) return null;
        try {
            return Integer.valueOf(value);
        } catch (Exception e) {
            return null;
        }
    }
}
