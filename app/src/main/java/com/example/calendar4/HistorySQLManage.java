package com.example.calendar4;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Class that services ONLY the Form=History records. All HISTORY data is stored
 * in its own SQLite table "HISTORY" created from
 * ConstantsSQLDb.CREATE_TABLE_HISTORY. The record class stays calPlanRecord
 * (only the needed fields are written/read).
 *
 * NOTE: the class reuses the already existing database connection - it receives
 * a SQLiteDatabase (from ManageSQLDatabase) in the constructor and does NOT open
 * its own connection/settings.
 *
 * Methods: upsertHistory, deleteHistory, getHistoryById, getHistoryByDate.
 */
public class HistorySQLManage {

    public static final String TABLE_HISTORY = "HISTORY";

    private final SQLiteDatabase db;

    public HistorySQLManage(SQLiteDatabase db) {
        this.db = db;
    }

    // ---------------------------------------------------------------------
    // Fill-in author from "Ведущий" (CALPARAM) when the record has no author
    // ---------------------------------------------------------------------
    private void fillAuthorFromParams(calPlanRecord record) {
        if (record.AuthorID == null || record.AuthorID.isEmpty()) {
			record.AuthorName = ManageSQLDatabase.AuthorName;
			record.AuthorID = ManageSQLDatabase.AuthorID;
        }
    }

    // ---------------------------------------------------------------------
    // CRUD
    // ---------------------------------------------------------------------

    /** Insert or update one History record in the HISTORY table. */
    public void upsertHistory(calPlanRecord record) {
        if (record == null) return;
        ContentValues values = new ContentValues();

        if (record.id != null) values.put("id", record.id);
        if (record.id == null && (record.UNID == null || record.UNID.isEmpty())) {
            record.UNID = java.util.UUID.randomUUID().toString();
        }
        if (record.UNID != null) values.put("UNID", record.UNID);
        if (record.Okdate != null) values.put("Okdate", fmt(record.Okdate));
        fillAuthorFromParams(record);
        if (record.AuthorID != null) values.put("AuthorID", record.AuthorID);
        if (record.AuthorName != null) values.put("AuthorName", record.AuthorName);
        if (record.LastUpdatedByID != null) values.put("LastUpdatedByID", record.LastUpdatedByID);
        if (record.LastUpdatedBy != null) values.put("LastUpdatedBy", record.LastUpdatedBy);
        if (record.LastUpdatedDate != null) values.put("LastUpdatedDate", fmt(record.LastUpdatedDate));
        if (record.Name != null) values.put("Name", record.Name);
        if (record.BodyText != null) values.put("BodyText", record.BodyText);
        if (record.Comment != null) values.put("Comment", record.Comment);
        if (record.StartDate != null) values.put("StartDate", fmt(record.StartDate));
        if (record.Revisions != null) values.put("Revisions", record.Revisions);

        if (record.id != null) {
            int rows = db.update(TABLE_HISTORY, values, "id=?",
                    new String[]{String.valueOf(record.id)});
            if (rows == 0) {
                db.insert(TABLE_HISTORY, null, values);
            }
        } else {
            db.insert(TABLE_HISTORY, null, values);
        }
    }

    /** Delete a History record by its id. */
    public void deleteHistory(Integer id) {
        if (id == null) return;
        db.delete(TABLE_HISTORY, "id=?", new String[]{String.valueOf(id)});
    }

    /** Get a single History record by id, or null when absent. */
    public calPlanRecord getHistoryById(int id) {
        calPlanRecord record = null;
        Cursor cursor = db.query(TABLE_HISTORY, null,
                "id=?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            record = cursorToRecord(cursor);
        }
        cursor.close();
        return record;
    }

    /** All History records whose StartDate equals the given day. */
    public calPlanRecord[] getHistoryByDate(Date date) {
        ArrayList<calPlanRecord> list = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Cursor cursor = db.query(TABLE_HISTORY, null,
                "StartDate=?", new String[]{sdf.format(date)}, null, null, "StartDate");
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                list.add(cursorToRecord(cursor));
                cursor.moveToNext();
            }
        }
        cursor.close();
        return list.toArray(new calPlanRecord[0]);
    }

    private calPlanRecord cursorToRecord(Cursor cursor) {
        calPlanRecord record = new calPlanRecord();
        record.Form = "History";
        int idxId = cursor.getColumnIndex("id");
        int idxUNID = cursor.getColumnIndex("UNID");
        int idxOkdate = cursor.getColumnIndex("Okdate");
        int idxAuthorID = cursor.getColumnIndex("AuthorID");
        int idxAuthorName = cursor.getColumnIndex("AuthorName");
        int idxLastUpdatedByID = cursor.getColumnIndex("LastUpdatedByID");
        int idxLastUpdatedBy = cursor.getColumnIndex("LastUpdatedBy");
        int idxLastUpdatedDate = cursor.getColumnIndex("LastUpdatedDate");
        int idxName = cursor.getColumnIndex("Name");
        int idxBodyText = cursor.getColumnIndex("BodyText");
        int idxComment = cursor.getColumnIndex("Comment");
        int idxStartDate = cursor.getColumnIndex("StartDate");
        int idxRevisions = cursor.getColumnIndex("Revisions");

        if (idxId >= 0 && !cursor.isNull(idxId)) record.id = cursor.getInt(idxId);
        if (idxUNID >= 0 && !cursor.isNull(idxUNID)) record.UNID = cursor.getString(idxUNID);
        if (idxAuthorID >= 0 && !cursor.isNull(idxAuthorID)) record.AuthorID = cursor.getString(idxAuthorID);
        if (idxAuthorName >= 0 && !cursor.isNull(idxAuthorName)) record.AuthorName = cursor.getString(idxAuthorName);
        if (idxLastUpdatedByID >= 0 && !cursor.isNull(idxLastUpdatedByID)) record.LastUpdatedByID = cursor.getString(idxLastUpdatedByID);
        if (idxLastUpdatedBy >= 0 && !cursor.isNull(idxLastUpdatedBy)) record.LastUpdatedBy = cursor.getString(idxLastUpdatedBy);
        if (idxName >= 0 && !cursor.isNull(idxName)) record.Name = cursor.getString(idxName);
        if (idxBodyText >= 0 && !cursor.isNull(idxBodyText)) record.BodyText = cursor.getString(idxBodyText);
        if (idxComment >= 0 && !cursor.isNull(idxComment)) record.Comment = cursor.getString(idxComment);
        if (idxRevisions >= 0 && !cursor.isNull(idxRevisions)) record.Revisions = cursor.getString(idxRevisions);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        if (idxOkdate >= 0 && !cursor.isNull(idxOkdate)) {
            try { record.Okdate = sdf.parse(cursor.getString(idxOkdate)); } catch (Exception e) { record.Okdate = null; }
        }
        if (idxLastUpdatedDate >= 0 && !cursor.isNull(idxLastUpdatedDate)) {
            try { record.LastUpdatedDate = sdf.parse(cursor.getString(idxLastUpdatedDate)); } catch (Exception e) { record.LastUpdatedDate = null; }
        }
        if (idxStartDate >= 0 && !cursor.isNull(idxStartDate)) {
            try { record.StartDate = sdf.parse(cursor.getString(idxStartDate)); } catch (Exception e) { record.StartDate = null; }
        }
        return record;
    }

    private String fmt(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
    }
}