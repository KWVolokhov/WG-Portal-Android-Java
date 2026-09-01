package com.example.calendar4;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Class that services ONLY the Form=HealthEat / Form=HealthDrink / Form=HealthSport
 * records. All of them are stored in their own SQLite table "HEALTHPLAN" created
 * from ConstantsSQLDb.CREATE_TABLE_HEALTHPLAN. The record class is healthPlanRecord
 * (calPlanRecord does not fit because of the organ/morality/skin number fields).
 *
 * NOTE: the class reuses the already existing database connection - it receives
 * a SQLiteDatabase (from ManageSQLDatabase) in the constructor and does NOT open
 * its own connection/settings.
 *
 * Methods: upsertHealth, deleteHealth, getHealthById, getHealthByDate.
 */
public class HealthSQLManage {

    public static final String TABLE_HEALTHPLAN = "HEALTHPLAN";

    private final SQLiteDatabase db;

    public HealthSQLManage(SQLiteDatabase db) {
        this.db = db;
    }

    // ---------------------------------------------------------------------
    // Conversions
    // ---------------------------------------------------------------------

    /** Copies common calPlanRecord fields into a healthPlanRecord. */
    public static healthPlanRecord toHealthPlan(calPlanRecord r) {
        if (r == null) return null;
        healthPlanRecord h = new healthPlanRecord();
        h.id = r.id;
        h.UNID = r.UNID;
        h.Form = r.Form;
        h.Okdate = r.Okdate;
        h.AuthorID = r.AuthorID;
        h.AuthorName = r.AuthorName;
        h.LastUpdatedByID = r.LastUpdatedByID;
        h.LastUpdatedBy = r.LastUpdatedBy;
        h.LastUpdatedDate = r.LastUpdatedDate;
        h.StartDate = r.StartDate;
        h.EndDate = r.EndDate;
        h.Name = r.Name;
        h.BodyText = r.BodyText;
        h.Comment = r.Comment;
        h.Revisions = r.Revisions;
        return h;
    }

    /** Copies a healthPlanRecord into the calPlanRecord used by the shared edit screens. */
    public static calPlanRecord toCalPlan(healthPlanRecord h) {
        if (h == null) return null;
        calPlanRecord r = new calPlanRecord();
        r.id = h.id;
        r.UNID = h.UNID;
        r.Form = h.Form;
        r.Okdate = h.Okdate;
        r.AuthorID = h.AuthorID;
        r.AuthorName = h.AuthorName;
        r.LastUpdatedByID = h.LastUpdatedByID;
        r.LastUpdatedBy = h.LastUpdatedBy;
        r.LastUpdatedDate = h.LastUpdatedDate;
        r.StartDate = h.StartDate;
        r.EndDate = h.EndDate;
        r.Name = h.Name;
        r.BodyText = h.BodyText;
        r.Comment = h.Comment;
        r.Revisions = h.Revisions;
        return r;
    }

    // ---------------------------------------------------------------------
    // Fill-in author from "Ведущий" (CALPARAM) when the record has no author
    // ---------------------------------------------------------------------
    private void fillAuthorFromParams(healthPlanRecord record) {
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

    /** Insert or update a healthPlanRecord in the HEALTHPLAN table. */
    public void upsertHealth(healthPlanRecord record) {
        if (record == null) return;
        ContentValues values = new ContentValues();

        if (record.id != null) values.put("id", record.id);
        if (record.id == null && (record.UNID == null || record.UNID.isEmpty())) {
            record.UNID = java.util.UUID.randomUUID().toString();
        }
        if (record.UNID != null) values.put("UNID", record.UNID);
        if (record.Form == null) record.Form = "HealthEat";
        values.put("Form", record.Form);
        if (record.Okdate != null) values.put("Okdate", fmt(record.Okdate));
        // Author comes from the "Ведущий" (CALPARAM) parameters
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
        if (record.EndDate != null) values.put("EndDate", fmt(record.EndDate));
        if (record.Revisions != null) values.put("Revisions", record.Revisions);

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

        if (record.id != null) {
            int rows = db.update(TABLE_HEALTHPLAN, values, "id=?",
                    new String[]{String.valueOf(record.id)});
            if (rows == 0) {
                db.insert(TABLE_HEALTHPLAN, null, values);
            }
        } else {
            db.insert(TABLE_HEALTHPLAN, null, values);
        }
    }

    /** Overload - converts a calPlanRecord (from the shared edit screens) then saves. */
    public void upsertHealth(calPlanRecord record) {
        upsertHealth(toHealthPlan(record));
    }

    /** Delete a Health record by its id. */
    public void deleteHealth(Integer id) {
        if (id == null) return;
        db.delete(TABLE_HEALTHPLAN, "id=?", new String[]{String.valueOf(id)});
    }

    /** Get a single Health record by id, or null when absent. */
    public healthPlanRecord getHealthById(int id) {
        healthPlanRecord record = null;
        Cursor cursor = db.query(TABLE_HEALTHPLAN, null,
                "id=?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            record = cursorToRecord(cursor);
        }
        cursor.close();
        return record;
    }

    /**
     * Health records whose StartDate equals the given day.
     * When the filter is 3+ characters it also filters by Name (LOWER LIKE).
     */
    public healthPlanRecord[] getHealthByDate(Date date, String filter) {
        ArrayList<healthPlanRecord> list = new ArrayList<>();
        StringBuilder selection = new StringBuilder("StartDate=?");
        ArrayList<String> args = new ArrayList<>();
        args.add(fmt(date));
        if (filter != null && filter.trim().length() >= 3) {
            selection.append(" AND LOWER(Name) LIKE ?");
            args.add("%" + filter.trim().toLowerCase(Locale.getDefault()) + "%");
        }
        Cursor cursor = db.query(TABLE_HEALTHPLAN, null, selection.toString(),
                args.toArray(new String[0]), null, null, "StartDate");
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                list.add(cursorToRecord(cursor));
                cursor.moveToNext();
            }
        }
        cursor.close();
        return list.toArray(new healthPlanRecord[0]);
    }

    private healthPlanRecord cursorToRecord(Cursor cursor) {
        healthPlanRecord record = new healthPlanRecord();
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
        int idxBodyText = cursor.getColumnIndex("BodyText");
        int idxComment = cursor.getColumnIndex("Comment");
        int idxStartDate = cursor.getColumnIndex("StartDate");
        int idxEndDate = cursor.getColumnIndex("EndDate");
        int idxRevisions = cursor.getColumnIndex("Revisions");

        if (idxId >= 0 && !cursor.isNull(idxId)) record.id = cursor.getInt(idxId);
        if (idxUNID >= 0 && !cursor.isNull(idxUNID)) record.UNID = cursor.getString(idxUNID);
        if (idxForm >= 0 && !cursor.isNull(idxForm)) record.Form = cursor.getString(idxForm);
        if (idxAuthorID >= 0 && !cursor.isNull(idxAuthorID)) record.AuthorID = cursor.getString(idxAuthorID);
        if (idxAuthorName >= 0 && !cursor.isNull(idxAuthorName)) record.AuthorName = cursor.getString(idxAuthorName);
        if (idxLastUpdatedByID >= 0 && !cursor.isNull(idxLastUpdatedByID)) record.LastUpdatedByID = cursor.getString(idxLastUpdatedByID);
        if (idxLastUpdatedBy >= 0 && !cursor.isNull(idxLastUpdatedBy)) record.LastUpdatedBy = cursor.getString(idxLastUpdatedBy);
        if (idxName >= 0 && !cursor.isNull(idxName)) record.Name = cursor.getString(idxName);
        if (idxBodyText >= 0 && !cursor.isNull(idxBodyText)) record.BodyText = cursor.getString(idxBodyText);
        if (idxComment >= 0 && !cursor.isNull(idxComment)) record.Comment = cursor.getString(idxComment);
        if (idxRevisions >= 0 && !cursor.isNull(idxRevisions)) record.Revisions = cursor.getString(idxRevisions);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        parseDate(cursor, idxOkdate, sdf, d -> record.Okdate = d);
        parseDate(cursor, idxLastUpdatedDate, sdf, d -> record.LastUpdatedDate = d);
        parseDate(cursor, idxStartDate, sdf, d -> record.StartDate = d);
        parseDate(cursor, idxEndDate, sdf, d -> record.EndDate = d);

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

    private Integer getInt(Cursor cursor, String column) {
        int idx = cursor.getColumnIndex(column);
        if (idx >= 0 && !cursor.isNull(idx)) return cursor.getInt(idx);
        return null;
    }

    private void parseDate(Cursor cursor, int idx, SimpleDateFormat sdf,
                           java.util.function.Consumer<Date> setter) {
        if (idx >= 0 && !cursor.isNull(idx)) {
            try {
                setter.accept(sdf.parse(cursor.getString(idx)));
            } catch (Exception e) {
                setter.accept(null);
            }
        }
    }

    private String fmt(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
    }

    private void putInt(ContentValues values, String column, Integer value) {
        if (value != null) values.put(column, value);
    }

    private SimpleDateFormat fmtDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    }

}