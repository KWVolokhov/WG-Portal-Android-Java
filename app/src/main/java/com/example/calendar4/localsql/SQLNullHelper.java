package com.example.calendar4.localsql;

import android.content.ContentValues;

/**
 * Унифицированная обработка null при сохранении полей в SQLite.
 * Текстовые поля: null -> "" (пустая строка), чтобы db.update() обнулял колонку.
 * Числовые поля: null -> 0 (или 0.0 для Double), чтобы не оставалось NULL.
 * Используется во всех *SQLManage менеджерах вместо `if (x != null) values.put(...)`.
 */
public final class SQLNullHelper {

    private SQLNullHelper() {
    }

    public static void putText(ContentValues values, String column, String value) {
        values.put(column, value != null ? value : "");
    }

    public static void putInt(ContentValues values, String column, Integer value) {
        values.put(column, value != null ? value : 0);
    }

    public static void putDouble(ContentValues values, String column, Double value) {
        values.put(column, value != null ? value : 0.0);
    }
}
