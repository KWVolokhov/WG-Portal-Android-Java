package com.example.calendar4;

import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Task 106/112/121: тестовые записи СМС (Входящая, Исходящая) с Ведущим из параметров.
 * Task 139: вынесен из ManageSQLDatabase (сидирование при создании/миграции БД).
 */
public final class SmsDefaultsSeeder {

    private SmsDefaultsSeeder() {
    }

    /** Заполняет SMSCALPLAN двумя тестовыми записями, если их ещё нет. */
    public static void seed(ManageSQLDatabase owner, SQLiteDatabase db) {
        try {
            ContactRecord[] contacts = new ContactsSQLManage(owner.getReadableDatabase()).getContacts("");
            String vedushiiName = ManageSQLDatabase.AuthorName;
            String vedushiiId = ManageSQLDatabase.AuthorID;
            if (vedushiiName == null) {
                String[] fromParams = vedushiiFromParams(owner);
                vedushiiName = fromParams[0];
                vedushiiId = fromParams[1];
            }
            if (vedushiiName == null && contacts.length > 0) {
                ContactRecord vedushii = contacts[0];
                vedushiiName = (vedushii.Surname != null ? vedushii.Surname + " " : "")
                        + (vedushii.FirstName != null ? vedushii.FirstName : "");
                vedushiiId = vedushii.EntryID != null ? vedushii.EntryID
                        : (vedushii.id != null ? String.valueOf(vedushii.id) : null);
            }
            if (vedushiiName == null || vedushiiName.trim().isEmpty()) vedushiiName = "Ведущий";
            insertTestSms(db, vedushiiId, vedushiiName.trim(), nowString());
        } catch (Exception e) {
            // Non-fatal: СМС - вспомогательные данные
        }
    }

    // Ведущий/его ID из CALPARAM (когда статические AuthorName ещё не заполнены)
    private static String[] vedushiiFromParams(ManageSQLDatabase owner) {
        CalParamRecord param = new CalParamSQLManage(owner.getReadableDatabase()).get();
        if (param != null) return new String[]{param.Vedushii, param.VedushiiID};
        return new String[]{null, null};
    }

    private static String nowString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    // Task 112: UNID фиксированные - INSERT OR IGNORE не создаёт дубли при повторном входе.
    // Task 121: у обеих тестовых записей указаны оба адресата From и To (= Ведущий).
    private static void insertTestSms(SQLiteDatabase db, String vedushiiId, String vedushiiName, String now) {
        // Task 112/121: удалить старые тестовые записи (чтобы обновить обе стороны From/To)
        db.execSQL("DELETE FROM SMSCALPLAN WHERE UNID IN ('sms-test-incoming','sms-test-outgoing')" +
                " OR (Subject='Тестовая запись' AND Body='Hello world!')");
        insertOneTestSms(db, "sms-test-incoming", "Incoming", vedushiiId, vedushiiName, now);
        insertOneTestSms(db, "sms-test-outgoing", "Outgoing", vedushiiId, vedushiiName, now);
    }

    private static void insertOneTestSms(SQLiteDatabase db, String unid, String type,
                                         String vedushiiId, String vedushiiName, String now) {
        db.execSQL("INSERT OR IGNORE INTO SMSCALPLAN " +
                "(UNID, Type, FromID, FromName, ToID, ToName, Subject, Body, Status, DateReceived) VALUES (" +
                "'" + unid + "', '" + type + "', " +
                sqlStr(vedushiiId) + ", " + sqlStr(vedushiiName) + ", " +
                sqlStr(vedushiiId) + ", " + sqlStr(vedushiiName) + ", " +
                "'Тестовая запись', 'Hello world!', 'New', '" + now + "')");
    }

    /** Оборачивает строку в одинарные кавычки для SQL (null -> ''). */
    private static String sqlStr(String value) {
        if (value == null) return "''";
        return "'" + value.replace("'", "''") + "'";
    }
}
