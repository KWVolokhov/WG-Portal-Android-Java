package com.example.calendar4;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Task 139: менеджер таблицы CONTACTS - вынесен из ManageSQLDatabase по образцу
 * пер-табличных менеджеров. Повторно использует уже открытое соединение
 * (конструктор получает SQLiteDatabase). Record-класс - ContactRecord.
 * Операции с контактами пишут запись в HISTORY (таблица HISTORY).
 */
public class ContactsSQLManage {

    public static final String TABLE_CONTACTS = "CONTACTS";

    private final SQLiteDatabase db;

    public ContactsSQLManage(SQLiteDatabase database) {
        db = database;
    }

    /** Удалить контакт по id, предварительно записав снимок в HISTORY. */
    public void deleteContact(Integer id) {
        if (id == null) return;
        // Snapshot the contact before deletion
        ContactRecord record = getContactById(id);
        // Log "Удал. Конт.:" before actually deleting (fields go to BodyText)
        addHistory("Удал. Конт.:", record, buildContactBodyText(record));
        db.delete("CONTACTS", "id=?", new String[]{String.valueOf(id)});
    }

    /** Fetch a single contact by its numeric id. */
    public ContactRecord getContactById(Integer id) {
        if (id == null) return null;
        Cursor cursor = db.query("CONTACTS", null, "id=?",
                new String[]{String.valueOf(id)}, null, null, null);
        ContactRecord result = null;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            result = cursorToContact(cursor);
        }
        cursor.close();
        return result;
    }

    /** Find a contact whose stored Phone begins with the given 10 digits. */
    public ContactRecord getContactByPhone(String phoneDigits) {
        if (phoneDigits == null || phoneDigits.isEmpty()) return null;
        Cursor cursor = db.query("CONTACTS", null, null, null, null, null, null);
        ContactRecord result = null;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                ContactRecord c = cursorToContact(cursor);
                if (c != null && phoneEquals(c.Phone, phoneDigits)) {
                    result = c;
                    break;
                }
                cursor.moveToNext();
            }
        }
        cursor.close();
        return result;
    }

    // True if stored phone value starts with the given 10 digits
    private boolean phoneEquals(String stored, String phoneDigits) {
        if (stored == null) return false;
        StringBuilder digits = new StringBuilder();
        for (int i = 0; i < stored.length() && digits.length() < 10; i++) {
            char ch = stored.charAt(i);
            if (Character.isDigit(ch)) digits.append(ch);
        }
        return digits.length() >= 10 && digits.toString().equals(phoneDigits);
    }

    /** Найти контакт по Фамилия+Имя (когда телефон устройства недоступен). */
    public ContactRecord getContactBySurnameFirstName(String surname, String firstName) {
        if (surname == null || firstName == null) return null;
        Cursor cursor = db.query("CONTACTS", null,
                "Surname=? AND FirstName=?",
                new String[]{surname, firstName}, null, null, null);
        ContactRecord result = null;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            result = cursorToContact(cursor);
        }
        cursor.close();
        return result;
    }

    /** Записать в HISTORY запись о действии над контактом (Form=History). */
    private void addHistory(String action, ContactRecord record, String bodyText) {
        if (record == null) return;
        calPlanRecord history = new calPlanRecord();
        history.Form = "History";
        history.Okdate = new Date();
        // History records must appear in the calendar list on the day the entry was made
        history.StartDate = new Date();
        history.AuthorName = ManageSQLDatabase.AuthorName;
        history.AuthorID = ManageSQLDatabase.AuthorID;
        StringBuilder name = new StringBuilder(action);
        if (record.Surname != null) name.append(" ").append(record.Surname);
        if (record.FirstName != null) name.append(" ").append(record.FirstName);
        history.Name = name.toString();
        if (bodyText != null && !bodyText.isEmpty()) history.BodyText = bodyText;
        // History is stored in its own HISTORY table
        new HistorySQLManage(db).upsertHistory(history);
    }

    /** Непустые поля контакта через ','; русские подписи как в карточке. */
    private String buildContactBodyText(ContactRecord r) {
        if (r == null) return null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        StringBuilder sb = new StringBuilder();
        appendField(sb, "Фамилия", r.Surname);
        appendField(sb, "Имя", r.FirstName);
        appendField(sb, "Отчество", r.Patronymic);
        appendField(sb, "Телефон", r.Phone);
        appendField(sb, "Информация", InfoFieldView.plainText(r.Info));
        appendField(sb, "Телефон 2", r.Phone2);
        appendField(sb, "Email", r.Email);
        appendField(sb, "Дата рождения", r.BirthDate == null ? null : sdf.format(r.BirthDate));
        appendField(sb, "Дом.Адресс", r.HomeAddress);
        appendField(sb, "Дата получения", r.DateReceived == null ? null : sdf.format(r.DateReceived));
        appendField(sb, "EntryID", r.EntryID);
        return sb.length() == 0 ? null : sb.toString();
    }

    private void appendField(StringBuilder sb, String fieldName, String value) {
        if (value == null) return;
        String v = value.trim();
        if (v.isEmpty()) return;
        if (sb.length() > 0) sb.append(",");
        sb.append(fieldName).append("=").append(v);
    }

    /** Маппинг строки курсора CONTACTS (включая Author/LastUpdated и даты Task 44/53). */
    private ContactRecord cursorToContact(Cursor cursor) {
        ContactRecord record = new ContactRecord();
        record.id = getInteger(cursor, "id");
        record.Surname = getString(cursor, "Surname");
        record.FirstName = getString(cursor, "FirstName");
        record.Patronymic = getString(cursor, "Patronymic");
        record.Phone = getString(cursor, "Phone");
        record.Info = getString(cursor, "Info");
        record.Phone2 = getString(cursor, "Phone2");
        record.Email = getString(cursor, "Email");
        record.HomeAddress = getString(cursor, "HomeAddress");
        record.EntryID = getString(cursor, "EntryID");
        record.AuthorID = getString(cursor, "AuthorID");
        record.AuthorName = getString(cursor, "AuthorName");
        record.LastUpdatedByID = getString(cursor, "LastUpdatedByID");
        record.LastUpdatedBy = getString(cursor, "LastUpdatedBy");
        fillDateFields(record, cursor);
        return record;
    }

    // Дата-поля контакта: BirthDate/DateReceived - "yyyy-MM-dd", Created/Modified - со временем
    private void fillDateFields(ContactRecord record, Cursor cursor) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        int idxBirthDate = cursor.getColumnIndex("BirthDate");
        if (idxBirthDate >= 0 && !cursor.isNull(idxBirthDate)) {
            try { record.BirthDate = sdf.parse(cursor.getString(idxBirthDate)); } catch (Exception e) {}
        }
        int idxDateReceived = cursor.getColumnIndex("DateReceived");
        if (idxDateReceived >= 0 && !cursor.isNull(idxDateReceived)) {
            try { record.DateReceived = sdf.parse(cursor.getString(idxDateReceived)); } catch (Exception e) {}
        }
        record.DateCreated = parseDateFlexible(cursor, cursor.getColumnIndex("DateCreated"));
        record.DateModified = parseDateFlexible(cursor, cursor.getColumnIndex("DateModified"));
    }

    // Task 44: даты могут храниться со временем - пробуем оба формата.
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

    /** Автор из "Ведущий" (CALPARAM), когда у записи автора ещё нет (Task 53). */
    private void fillAuthorFromParams(ContactRecord record) {
        if (record == null) return;
        if (record.AuthorID == null || record.AuthorID.isEmpty()) {
            record.AuthorID = ManageSQLDatabase.AuthorID;
            record.AuthorName = ManageSQLDatabase.AuthorName;
        }
        if (record.LastUpdatedByID == null || record.LastUpdatedByID.isEmpty()) {
            record.LastUpdatedByID = ManageSQLDatabase.AuthorID;
            record.LastUpdatedBy = ManageSQLDatabase.AuthorName;
        }
    }

    /** Upsert ContactRecord into CONTACTS table + запись в HISTORY (Добавление/Изменение). */
    public void upsertContact(ContactRecord record) {
        // Task 53: Автор/Обновивший из "Ведущий" (CALPARAM), если ещё не заданы
        fillAuthorFromParams(record);
        ContentValues values = new ContentValues();
        fillContactValues(values, record);
        boolean updated = false;
        if (record.id != null) {
            int rowsAffected = db.update("CONTACTS", values, "id=?",
                    new String[]{String.valueOf(record.id)});
            if (rowsAffected == 0) db.insert("CONTACTS", null, values);
            else updated = true;
        } else {
            db.insert("CONTACTS", null, values);
        }
        addHistory(updated ? "Изменение" : "Добавление", record, null);
    }

    // Все поля контакта (включая даты) в ContentValues
    private void fillContactValues(ContentValues values, ContactRecord record) {
        if (record.Surname != null) values.put("Surname", record.Surname);
        if (record.FirstName != null) values.put("FirstName", record.FirstName);
        if (record.Patronymic != null) values.put("Patronymic", record.Patronymic);
        if (record.Phone != null) values.put("Phone", record.Phone);
        if (record.Info != null) values.put("Info", record.Info);
        if (record.Phone2 != null) values.put("Phone2", record.Phone2);
        if (record.Email != null) values.put("Email", record.Email);
        if (record.HomeAddress != null) values.put("HomeAddress", record.HomeAddress);
        if (record.EntryID != null) values.put("EntryID", record.EntryID);
        if (record.AuthorID != null) values.put("AuthorID", record.AuthorID);
        if (record.AuthorName != null) values.put("AuthorName", record.AuthorName);
        if (record.LastUpdatedByID != null) values.put("LastUpdatedByID", record.LastUpdatedByID);
        if (record.LastUpdatedBy != null) values.put("LastUpdatedBy", record.LastUpdatedBy);
        fillContactDateValues(values, record);
    }

    // Дата-поля контакта в ContentValues: BirthDate/DateReceived "yyyy-MM-dd", остальные со временем
    private void fillContactDateValues(ContentValues values, ContactRecord record) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        if (record.BirthDate != null) values.put("BirthDate", sdf.format(record.BirthDate));
        if (record.DateReceived != null) values.put("DateReceived", sdf.format(record.DateReceived));
        // Task 44/53: даты создания/обновления хранятся со временем
        if (record.DateCreated != null) values.put("DateCreated", fmtDateTimeString(record.DateCreated));
        if (record.DateModified != null) values.put("DateModified", fmtDateTimeString(record.DateModified));
    }

    /** Контакты с фильтром (3+ символа - поиск по нескольким полям). */
    public ContactRecord[] getContacts(String filter) {
        ArrayList<ContactRecord> contactsList = new ArrayList<>();
        Cursor cursor = queryContacts(filter);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                contactsList.add(cursorToContact(cursor));
                cursor.moveToNext();
            }
        }
        cursor.close();
        ContactRecord[] contacts = new ContactRecord[contactsList.size()];
        contactsList.toArray(contacts);
        return contacts;
    }

    // Выборка контактов: фильтр 3+ символа ищет по 8 полям (LOWER LIKE)
    private Cursor queryContacts(String filter) {
        if (filter != null && filter.length() >= 3) {
            String searchPattern = "%" + filter.toLowerCase() + "%";
            String selection = "LOWER(Surname) LIKE ? OR LOWER(FirstName) LIKE ? OR " +
                    "LOWER(Patronymic) LIKE ? OR LOWER(Phone) LIKE ? OR " +
                    "LOWER(Info) LIKE ? OR LOWER(Phone2) LIKE ? OR " +
                    "LOWER(Email) LIKE ? OR LOWER(HomeAddress) LIKE ?";
            String[] selectionArgs = new String[]{
                    searchPattern, searchPattern, searchPattern, searchPattern,
                    searchPattern, searchPattern, searchPattern, searchPattern
            };
            return db.query("CONTACTS", null, selection, selectionArgs, null, null, "Surname, FirstName");
        }
        return db.query("CONTACTS", null, null, null, null, null, "Surname, FirstName");
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
}
