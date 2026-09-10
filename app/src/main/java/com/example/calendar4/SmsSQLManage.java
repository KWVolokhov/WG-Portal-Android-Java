package com.example.calendar4;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Task 106/108: CRUD для таблицы SMSCALPLAN (отдельный java-класс по поиску/хранению СМС).
 *
 * Правила фильтрации (Task 108):
 * - Входящие/Исходящие/Все содержат только СМС, у которых точно сопоставлен контакт
 *   с получателем или отправителем (ID контакта заполнено);
 * - Входящие - только Type='Incoming' (заполнен ToID);
 * - Исходящие - только Type='Outgoing' (заполнен FromID);
 * - Все - все типы с заполненным ID контакта (FromID или ToID);
 * - Корзина - все записи, у которых ID контакта НЕ заполнено (FromID и ToID пустые).
 */
public class SmsSQLManage {

    public static final String TABLE_SMSCALPLAN = "SMSCALPLAN";

    private final SQLiteDatabase db;

    public SmsSQLManage(SQLiteDatabase db) {
        this.db = db;
    }

    // ---------------------------------------------------------------------
    // Task 108: выборка по папке и фильтру
    // ---------------------------------------------------------------------

    /**
     * Возвращает записи СМС для заданной папки (all/income/outcome/trash).
     * Если filter задан (3+ символа), ищется вхождение в Subject/Body/FromName/ToName.
     */
    public smsRecord[] getSms(String folder, String filter) {
        StringBuilder selection = new StringBuilder();
        ArrayList<String> args = new ArrayList<>();

        String folderClause = folderClause(folder);
        if (folderClause != null && !folderClause.isEmpty()) {
            selection.append(folderClause);
        }

        if (filter != null && filter.length() >= 3) {
            if (selection.length() > 0) selection.append(" AND ");
            selection.append("(LOWER(Subject) LIKE ? OR LOWER(Body) LIKE ? OR ")
                    .append("LOWER(FromName) LIKE ? OR LOWER(ToName) LIKE ?)");
            String p = "%" + filter.toLowerCase(Locale.getDefault()) + "%";
            args.add(p);
            args.add(p);
            args.add(p);
            args.add(p);
        }

        String sel = selection.length() > 0 ? selection.toString() : null;
        String[] selArgs = args.isEmpty() ? null : args.toArray(new String[0]);

        Cursor cursor = db.query(TABLE_SMSCALPLAN, null, sel, selArgs, null, null, "id DESC");
        ArrayList<smsRecord> list = new ArrayList<>();
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    smsRecord r = cursorToRecord(cursor);
                    if (r != null) list.add(r);
                    cursor.moveToNext();
                }
            }
        } finally {
            cursor.close();
        }
        return list.toArray(new smsRecord[0]);
    }

    /** Условие SQL для папки (Task 108). */
    private String folderClause(String folder) {
        if (folder == null || SmsActivity.FOLDER_ALL.equals(folder)) {
            // Все типы с точно сопоставленным контактом (ID заполнено).
            // Внешние скобки обязательны: условие объединяется с фильтром через AND.
            return "((FromID IS NOT NULL AND FromID<>'') OR (ToID IS NOT NULL AND ToID<>''))";
        }
        if (SmsActivity.FOLDER_INCOME.equals(folder)) {
            return "(Type='" + smsRecord.TYPE_INCOMING + "' AND ToID IS NOT NULL AND ToID<>'')";
        }
        if (SmsActivity.FOLDER_OUTCOME.equals(folder)) {
            return "(Type='" + smsRecord.TYPE_OUTGOING + "' AND FromID IS NOT NULL AND FromID<>'')";
        }
        if (SmsActivity.FOLDER_TRASH.equals(folder)) {
            return "((FromID IS NULL OR FromID='') AND (ToID IS NULL OR ToID=''))";
        }
        return null;
    }

    // ---------------------------------------------------------------------
    // CRUD
    // ---------------------------------------------------------------------

    public void upsertSms(smsRecord record) {
        if (record == null) return;
        ContentValues values = new ContentValues();
        if (record.id != null) values.put("id", record.id);
        if (record.UNID == null || record.UNID.isEmpty()) {
            record.UNID = java.util.UUID.randomUUID().toString();
        }
        values.put("UNID", record.UNID);
        if (record.Type != null) values.put("Type", record.Type);
        if (record.FromID != null) values.put("FromID", record.FromID);
        if (record.FromName != null) values.put("FromName", record.FromName);
        if (record.ToID != null) values.put("ToID", record.ToID);
        if (record.ToName != null) values.put("ToName", record.ToName);
        if (record.Subject != null) values.put("Subject", record.Subject);
        if (record.Body != null) values.put("Body", record.Body);
        if (record.Status != null) values.put("Status", record.Status);
        if (record.DateReceived == null) record.DateReceived = new Date();
        values.put("DateReceived", fmtDateTime().format(record.DateReceived));

        if (record.id != null) {
            int rows = db.update(TABLE_SMSCALPLAN, values, "id=?",
                    new String[]{String.valueOf(record.id)});
            if (rows == 0) db.insert(TABLE_SMSCALPLAN, null, values);
        } else {
            db.insert(TABLE_SMSCALPLAN, null, values);
        }
    }

    public void deleteSms(Integer id) {
        if (id == null) return;
        db.delete(TABLE_SMSCALPLAN, "id=?", new String[]{String.valueOf(id)});
    }

    private smsRecord cursorToRecord(Cursor cursor) {
        smsRecord r = new smsRecord();
        int idxId = cursor.getColumnIndex("id");
        int idxUNID = cursor.getColumnIndex("UNID");
        int idxType = cursor.getColumnIndex("Type");
        int idxFromID = cursor.getColumnIndex("FromID");
        int idxFromName = cursor.getColumnIndex("FromName");
        int idxToID = cursor.getColumnIndex("ToID");
        int idxToName = cursor.getColumnIndex("ToName");
        int idxSubject = cursor.getColumnIndex("Subject");
        int idxBody = cursor.getColumnIndex("Body");
        int idxStatus = cursor.getColumnIndex("Status");
        int idxDate = cursor.getColumnIndex("DateReceived");

        if (idxId >= 0 && !cursor.isNull(idxId)) r.id = cursor.getInt(idxId);
        if (idxUNID >= 0 && !cursor.isNull(idxUNID)) r.UNID = cursor.getString(idxUNID);
        if (idxType >= 0 && !cursor.isNull(idxType)) r.Type = cursor.getString(idxType);
        if (idxFromID >= 0 && !cursor.isNull(idxFromID)) r.FromID = cursor.getString(idxFromID);
        if (idxFromName >= 0 && !cursor.isNull(idxFromName)) r.FromName = cursor.getString(idxFromName);
        if (idxToID >= 0 && !cursor.isNull(idxToID)) r.ToID = cursor.getString(idxToID);
        if (idxToName >= 0 && !cursor.isNull(idxToName)) r.ToName = cursor.getString(idxToName);
        if (idxSubject >= 0 && !cursor.isNull(idxSubject)) r.Subject = cursor.getString(idxSubject);
        if (idxBody >= 0 && !cursor.isNull(idxBody)) r.Body = cursor.getString(idxBody);
        if (idxStatus >= 0 && !cursor.isNull(idxStatus)) r.Status = cursor.getString(idxStatus);
        if (idxDate >= 0 && !cursor.isNull(idxDate)) r.DateReceived = parseDate(cursor.getString(idxDate));
        return r;
    }

    private Date parseDate(String value) {
        if (value == null) return null;
        try {
            return fmtDateTime().parse(value);
        } catch (Exception e) {
            try {
                return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(value);
            } catch (Exception e2) {
                return null;
            }
        }
    }

    private SimpleDateFormat fmtDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    }
}