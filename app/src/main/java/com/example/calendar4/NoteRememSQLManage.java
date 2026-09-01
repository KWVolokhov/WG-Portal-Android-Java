package com.example.calendar4;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Class that services ONLY the Form=Note and Form=Remember records. All of them
 * are stored in their own SQLite table "NOTEPLAN" created from
 * ConstantsSQLDb.CREATE_TABLE_NOTEPLAN. The record class stays calPlanRecord
 * (only the needed fields are written/read).
 *
 * NOTE: the class reuses the already existing database connection - it receives
 * a SQLiteDatabase (from ManageSQLDatabase) in the constructor and does NOT open
 * its own connection/settings.
 *
 * Methods: upsertNote, deleteNote, getNoteById, getNotesByDate.
 */
public class NoteRememSQLManage {

    public static final String TABLE_NOTEPLAN = "NOTEPLAN";

    private final SQLiteDatabase db;

    public NoteRememSQLManage(SQLiteDatabase db) {
        this.db = db;
    }

    // ---------------------------------------------------------------------
    // Fill-in author from "Ведущий" (CALPARAM) when the record has no author
    // ---------------------------------------------------------------------
    private void fillAuthorFromParams(calPlanRecord record) {
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
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            record = new CalParamRecord();
            int idxId = cursor.getColumnIndex("id");
            int idxAddress = cursor.getColumnIndex("Address");
            int idxName = cursor.getColumnIndex("Name");
            int idxPassword = cursor.getColumnIndex("Password");
            int idxVedushii = cursor.getColumnIndex("Vedushii");
            int idxVedushiiID = cursor.getColumnIndex("VedushiiID");
            if (idxId >= 0 && !cursor.isNull(idxId)) record.id = cursor.getInt(idxId);
            if (idxAddress >= 0 && !cursor.isNull(idxAddress)) record.Address = cursor.getString(idxAddress);
            if (idxName >= 0 && !cursor.isNull(idxName)) record.Name = cursor.getString(idxName);
            if (idxPassword >= 0 && !cursor.isNull(idxPassword)) record.Password = cursor.getString(idxPassword);
            if (idxVedushii >= 0 && !cursor.isNull(idxVedushii)) record.Vedushii = cursor.getString(idxVedushii);
            if (idxVedushiiID >= 0 && !cursor.isNull(idxVedushiiID)) record.VedushiiID = cursor.getString(idxVedushiiID);
        }
        cursor.close();
        return record;
    }

    // ---------------------------------------------------------------------
    // CRUD
    // ---------------------------------------------------------------------

    /** Insert or update one Note/Remember record in the NOTEPLAN table. */
    public void upsertNote(calPlanRecord record) {
        if (record == null) return;
        ContentValues values = new ContentValues();

        if (record.id != null) values.put("id", record.id);
        if (record.id == null && (record.UNID == null || record.UNID.isEmpty())) {
            record.UNID = java.util.UUID.randomUUID().toString();
        }
        if (record.UNID != null) values.put("UNID", record.UNID);
        if (record.Form == null) record.Form = "Note";
        values.put("Form", record.Form);
        if (record.Okdate != null) values.put("Okdate", fmt(record.Okdate));
        fillAuthorFromParams(record);
        if (record.AuthorID != null) values.put("AuthorID", record.AuthorID);
        if (record.AuthorName != null) values.put("AuthorName", record.AuthorName);
        if (record.LastUpdatedByID != null) values.put("LastUpdatedByID", record.LastUpdatedByID);
        if (record.LastUpdatedBy != null) values.put("LastUpdatedBy", record.LastUpdatedBy);
        if (record.LastUpdatedDate != null) values.put("LastUpdatedDate", fmt(record.LastUpdatedDate));
        if (record.Name != null) values.put("Name", record.Name);
        if (record.Status != null) values.put("Status", record.Status);
        if (record.StatusID != null) values.put("StatusID", record.StatusID);
        if (record.StartDate != null) values.put("StartDate", fmt(record.StartDate));
        if (record.BodyText != null) values.put("BodyText", record.BodyText);
        if (record.Comment != null) values.put("Comment", record.Comment);
        if (record.KeyWords != null) values.put("KeyWords", record.KeyWords);
        if (record.Revisions != null) values.put("Revisions", record.Revisions);

        if (record.id != null) {
            int rows = db.update(TABLE_NOTEPLAN, values, "id=?",
                    new String[]{String.valueOf(record.id)});
            if (rows == 0) {
                db.insert(TABLE_NOTEPLAN, null, values);
            }
        } else {
            db.insert(TABLE_NOTEPLAN, null, values);
        }
    }

    /** Get a single Note/Remember record by id, or null when absent. */
    public calPlanRecord getNoteById(int id) {
        calPlanRecord record = null;
        Cursor cursor = db.query(TABLE_NOTEPLAN, null,
                "id=?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            record = cursorToRecord(cursor);
        }
        cursor.close();
        return record;
    }

    /** All NOTEPLAN records (Form=Note / Form=Remember) whose StartDate equals the given day. */
    public calPlanRecord[] getNotesByDate(Date date) {
        ArrayList<calPlanRecord> list = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Cursor cursor = db.query(TABLE_NOTEPLAN, null,
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
        int idxId = cursor.getColumnIndex("id");
        int idxUNID = cursor.getColumnIndex("UNID");
        int idxForm = cursor.getColumnIndex("Form");
        int idxOkdate = cursor.getColumnIndex("Okdate");
        int idxAuthorID = cursor.getColumnIndex("AuthorID");
        int idxAuthorName = cursor.getColumnIndex("AuthorName");
        int idxLastUpdatedByID = cursor.getColumnIndex("LastUpdatedByID");
        int idxLastUpdatedBy = cursor.getColumnIndex("LastUpdatedBy");
        int idxLastUpdatedDate = cursor.getColumnIndex("LastUpdatedDate");
        int idxName = cursor.getColumnIndex("Name");
        int idxStatus = cursor.getColumnIndex("Status");
        int idxStatusID = cursor.getColumnIndex("StatusID");
        int idxStartDate = cursor.getColumnIndex("StartDate");
        int idxBodyText = cursor.getColumnIndex("BodyText");
        int idxComment = cursor.getColumnIndex("Comment");
        int idxKeyWords = cursor.getColumnIndex("KeyWords");
        int idxRevisions = cursor.getColumnIndex("Revisions");

        if (idxId >= 0 && !cursor.isNull(idxId)) record.id = cursor.getInt(idxId);
        if (idxUNID >= 0 && !cursor.isNull(idxUNID)) record.UNID = cursor.getString(idxUNID);
        if (idxForm >= 0 && !cursor.isNull(idxForm)) record.Form = cursor.getString(idxForm);
        if (idxAuthorID >= 0 && !cursor.isNull(idxAuthorID)) record.AuthorID = cursor.getString(idxAuthorID);
        if (idxAuthorName >= 0 && !cursor.isNull(idxAuthorName)) record.AuthorName = cursor.getString(idxAuthorName);
        if (idxLastUpdatedByID >= 0 && !cursor.isNull(idxLastUpdatedByID)) record.LastUpdatedByID = cursor.getString(idxLastUpdatedByID);
        if (idxLastUpdatedBy >= 0 && !cursor.isNull(idxLastUpdatedBy)) record.LastUpdatedBy = cursor.getString(idxLastUpdatedBy);
        if (idxName >= 0 && !cursor.isNull(idxName)) record.Name = cursor.getString(idxName);
        if (idxStatus >= 0 && !cursor.isNull(idxStatus)) record.Status = cursor.getString(idxStatus);
        if (idxStatusID >= 0 && !cursor.isNull(idxStatusID)) record.StatusID = cursor.getString(idxStatusID);
        if (idxBodyText >= 0 && !cursor.isNull(idxBodyText)) record.BodyText = cursor.getString(idxBodyText);
        if (idxComment >= 0 && !cursor.isNull(idxComment)) record.Comment = cursor.getString(idxComment);
        if (idxKeyWords >= 0 && !cursor.isNull(idxKeyWords)) record.KeyWords = cursor.getString(idxKeyWords);
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

    public void deleteNote(Integer id) {
    }
}