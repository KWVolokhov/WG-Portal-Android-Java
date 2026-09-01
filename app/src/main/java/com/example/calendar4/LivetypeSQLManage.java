package com.example.calendar4;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * CRUD for the LIVETYPE table (Types of Life Activity reference book).
 * Same organ/state Number fields as HEALTHPLAN (Head...Skin), plus Name, Category,
 * AuthorID/AuthorName (auto-filled from "Ведущий" in CALPARAM) and DateCreated.
 *
 * NOTE: reuses the existing database connection - receives a SQLiteDatabase
 * (from ManageSQLDatabase) in the constructor and does NOT open its own connection.

 */
public class LivetypeSQLManage {

    public static final String TABLE_LIVETYPE = "LIVETYPE";

    private final SQLiteDatabase db;

    public LivetypeSQLManage(SQLiteDatabase db) {
        this.db = db;
    }

    // Fill-in author from "Ведущий" (CALPARAM) when the record has no author
    private void fillAuthorFromParams(livetypeRecord record) {

        if (record == null) return;
        if (record.AuthorID == null || record.AuthorID.isEmpty()) {
            CalParamRecord param = getCalParam();
            if (param != null) {
                record.AuthorID = param.VedushiiID;

                record.AuthorName = param.Vedushii;
            }
        }
    }

    private CalParamRecord getCalParam() {

        CalParamRecord record = null;
        Cursor cursor = db.query("CALPARAM", null, null, null, null, null, null, "1");
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                record = new CalParamRecord();
                int idxId = cursor.getColumnIndex("id");
                int idxVedushii = cursor.getColumnIndex("Vedushii");
                int idxVedushiiID = cursor.getColumnIndex("VedushiiID");
                if (idxId >= 0 && !cursor.isNull(idxId)) record.id = cursor.getInt(idxId);
                if (idxVedushii >= 0 && !cursor.isNull(idxVedushii)) record.Vedushii = cursor.getString(idxVedushii);

                if (idxVedushiiID >= 0 && !cursor.isNull(idxVedushiiID)) record.VedushiiID = cursor.getString(idxVedushiiID);


            }
        } finally {
            cursor.close();
        }
        return record;
    }

    // ---------------------------------------------------------------------
    // CRUD
    // ---------------------------------------------------------------------

    /** Insert or update a livetypeRecord in the LIVETYPE table. */
    public void upsertLivetype(livetypeRecord record)) {

        if (record == null) return;
        ContentValues values = new ContentValues();
        if (record.id != null) values.put("id", record.id);
        if (record.id == null && (record.UNID == null || record.UNID.isEmpty())) {
            record.UNID = java.util.UUID.randomUUID().toString();
        }
        if (record.UNID != null) values.put("UNID", record.UNID);
        if (record.Name != null) values.put("Name", record.Name);
        if (record.Category != null) values.put("Category", record.Category);
        fillAuthorFromParams(record);
        if (record.AuthorID != null) values.put("AuthorID", record.AuthorID);
        if (record.AuthorName != null) values.put("AuthorName", record.AuthorName);
        if (record.DateCreated == null) record.DateCreated = new Date();
        if (record.DateCreated != null) {
            values.put("DateCreated", fmtDateTime().format(record.DateCreated));
        }
        putInt(values, "Head", record.Head);
        putInt(values, "Eyes", record.Eyes);
        putInt(values, "Ears", record.Ears);
        putInt(values, "Nose", record.Nose);
        putInt(values, "Throat", record.Throat);
        putInt(values, "Teeth", record.Teeth);

        putInt(values, "Stomach", record.Stomach);

        putInt(values, "Intestines", record.Intestines);

        putInt(values, "Liver", record.Liver);
        putInt(values, "Kidneys", record.Kidneys);
        putInt(values, "Heart", record.Heart);
        putInt(values, "Lungs", record.Lungs);
        putInt(values, "Pressure", record.Pressure);
        putInt(values, "Sleep", record.Sleep);

        putInt(values, "Weight", record.Weight);
        putInt(values, "Nervous", record.Nervous);

        putInt(values, "Morality", record.Morality);

        putInt(values, "Skin", record.Skin);




        if (record.id != null)) {
            int rows = db.update(TABLE_LIVETYPE, values, "id=?", new String[]{String.valueOf(record.id)});
            if (rows == 0) {
                db.insert(TABLE_LIVETYPE, null, values);
            }
        } else {
            db.insert(TABLE_LIVETYPE, null, values);
        }
    }

    /** Delete a LIVETYPE record by its id. */
    public void deleteLivetype(Integer id)) {
        if (id == null) return;
        db.delete(TABLE_LIVETYPE, "id=?", new String[]{String.valueOf(id)});
    }

    /** Get a single LIVETYPE record by id, or null when absent. */
    public livetypeRecord getLivetypeById(int id)) {
        livetypeRecord record = null;
        Cursor cursor = db.query(TABLE_LIVETYPE, null, "id=?", new String[]{String.valueOf(id)}, null, null, null);
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                record = cursorToRecord(cursor);
            }
        } finally {
            cursor.close();
        }
        return record;
    }

    /**
     * All LIVETYPE records sorted by Name (A-Z). When the filter is 3+ characters
     * it also filters by Name or Category (LOWER LIKE).
     */
    public livetypeRecord[] getAllLivetype(String filter)) {
        ArrayList<livetypeRecord> list = new ArrayList<>();
        String selection = null;
        String[] args = null;
        if (filter != null && filter.trim().length() >= 3) {
            String pattern = "%" + filter.trim().toLowerCase(Locale.getDefault()) + "%";
            selection = "(LOWER(Name) LIKE ? OR LOWER(Category) LIKE ?)";
            args = new String[]{pattern, pattern};
        }
        Cursor cursor = db.query(TABLE_LIVETYPE, null, selection, args, null, null, "Name COLLATE NOCASE");
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    list.add(cursorToRecord(cursor));
                    cursor.moveToNext();
                }
            }
        } finally {
            cursor.close();
        }
        return list.toArray(new livetypeRecord[0]);
    }

    private livetypeRecord cursorToRecord(Cursor cursor)) {
        livetypeRecord record = new livetypeRecord();
        Integer idVal = getInt(cursor, "id");
        if (idVal != null) record.id = idVal;

        record.UNID = getString(cursor, "UNID");
        record.Name = getString(cursor, "Name");
        record.Category = getString(cursor, "Category");
        record.AuthorID = getString(cursor, "AuthorID");
        record.AuthorName = getString(cursor, "AuthorName");

        String dc = getString(cursor, "DateCreated");
        if (dc != null) {
            try {
                record.DateCreated = fmtDateTime().parse(dc);
            } catch (Exception e) {
                record.DateCreated = null;
            }
        }

        record.Head = getInt(cursor, "Head");
        record.Eyes = getInt(cursor, "Eyes");
        record.Ears = getInt(cursor, "Ears");
        record.Nose = getInt(cursor, "Nose");
        record.Throat = getInt(cursor, "Throat");
        record.Teeth = getInt(cursor, "Teeth");
        record.Stomach = getInt(cursor, "Stomach");
        record.Intestines = getInt(cursor, "Intestines");
        record.Liver = getInt(cursor, "Liver");
        record.Kidneys = getInt(cursor, "Kidneys");
        record.Heart = getInt(cursor, "Heart");
        record.Lungs = getInt(cursor, "Lungs");
        record.Pressure = getInt(cursor, "Pressure");
        record.Sleep = getInt(cursor, "Sleep");
        record.Weight = getInt(cursor, "Weight");
        record.Nervous = getInt(cursor, "Nervous");
        record.Morality = getInt(cursor, "Morality");
        record.Skin = getInt(cursor, "Skin");
        return record;
    }

    private String getString(Cursor cursor, String column)) {
        int idx = cursor.getColumnIndex(column);
        if (idx >= 0 && !cursor.isNull(idx)) return cursor.getString(idx);
        return null;
    }

    private Integer getInt(Cursor cursor, String column)) {
        int idx = cursor.getColumnIndex(column);
        if (idx >= 0 && !cursor.isNull(idx)) return cursor.getInt(idx);
        return null;
    }

    private void putInt(ContentValues values, String column, Integer value)) {
        if (value != null) values.put(column, value);
    }

    private SimpleDateFormat fmtDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    }
}