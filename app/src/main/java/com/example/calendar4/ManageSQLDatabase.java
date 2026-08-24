package com.example.calendar4;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ManageSQLDatabase extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "WGPlanDatabase.db";
    public ManageSQLDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Execute the SQL statement defined abov
        //db.execSQL("DROP TABLE IF EXISTS CALPLAN");
        db.execSQL(ConstantsSQLDb.CREATE_TABLE_CALPLAN);

        //db.execSQL("DROP TABLE IF EXISTS REQUESTPLAN");   //Это заявка на проект, пока не нужна

        //db.execSQL("DROP TABLE IF EXISTS CLASSIFICATOR");
        db.execSQL(ConstantsSQLDb.CREATE_TABLE_CLASSIFICATOR);
        for(String inesrtCom : ConstantsSQLDb.INSERT_CLASSIFICATOR){
            db.execSQL(inesrtCom);
        }

        // Create CALPARAM table
        //db.execSQL("DROP TABLE IF EXISTS CALPARAM");
        db.execSQL(ConstantsSQLDb.CREATE_TABLE_CALPARAM);

        // Create HOLIDAYS table (without DROP to preserve data)
        db.execSQL(ConstantsSQLDb.CREATE_TABLE_HOLIDAYS);

        // Create CONTACTS table
        db.execSQL(ConstantsSQLDb.CREATE_TABLE_CONTACTS);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database schema changes here
    }



    // Тестовая функция для проверки
    public Map<Integer, Object> execSelectArrMap(String sqlSelect) throws Exception{
        Map<Integer, Object> retF = new HashMap<Integer, Object>();
        Integer recCounter, colCounter;
        Cursor retC = this.getReadableDatabase().rawQuery(sqlSelect, null);
        if (retC.getCount()>0) {
            retC.moveToFirst();
            for(recCounter=0; recCounter<retC.getCount(); recCounter++){
                Map<String, String> record = new HashMap<String, String>();
                String colNames[]=retC.getColumnNames();
                for(colCounter=0;colCounter<colNames.length;colCounter++) {
                    record.put(colNames[colCounter], retC.getString(retC.getColumnIndexOrThrow(colNames[colCounter])));
                }
                retF.put(recCounter, record);
                retC.moveToNext();
            }
        }
        return retF;
    }

    // Upsert (Insert or Update) calPlanRecord into CALPLAN table
    public void upsertCalPlan(calPlanRecord record) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Auto-filled fields
        if (record.id != null) values.put("id", record.id);
        if (record.UNID != null) values.put("UNID", record.UNID);
        if (record.Okdate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            values.put("Okdate", sdf.format(record.Okdate));
        }
        if (record.AuthorID != null) values.put("AuthorID", record.AuthorID);
        if (record.LastUpdatedByID != null) values.put("LastUpdatedByID", record.LastUpdatedByID);
        if (record.LastUpdatedBy != null) values.put("LastUpdatedBy", record.LastUpdatedBy);
        if (record.LastUpdatedDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            values.put("LastUpdatedDate", sdf.format(record.LastUpdatedDate));
        }
        if (record.HoldDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            values.put("HoldDate", sdf.format(record.HoldDate));
        }
        if (record.Revisions != null) values.put("Revisions", record.Revisions);

        // User input fields
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
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            values.put("StartDate", sdf.format(record.StartDate));
        }
        if (record.EndDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            values.put("EndDate", sdf.format(record.EndDate));
        }
        if (record.InstallOrder != null) values.put("InstallOrder", record.InstallOrder);
        if (record.KeyWords != null) values.put("KeyWords", record.KeyWords);

        // Try to update first, if no rows affected then insert
        int rowsAffected = db.update("CALPLAN", values, "id=?",
                new String[]{String.valueOf(record.id)});

        if (rowsAffected == 0) {
            // Insert new record
            db.insert("CALPLAN", null, values);
        }
    }

    // Delete a CALPLAN record by its id
    public void deleteCalPlan(Integer id) {
        if (id == null) return;
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("CALPLAN", "id=?", new String[]{String.valueOf(id)});
    }

    // Delete a CONTACTS record by its id
    public void deleteContact(Integer id) {
        if (id == null) return;
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("CONTACTS", "id=?", new String[]{String.valueOf(id)});
    }

    // Get calPlan records by date from CALPLAN table
    public calPlanRecord[] getCalPlan(Date date) {
        ArrayList<calPlanRecord> recordsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        String dateStr = sdf.format(date);

        // Query records where StartDate matches the given date
        Cursor cursor = db.query("CALPLAN",
                null,
                "StartDate=?",
                new String[]{dateStr},
                null, null, null);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                calPlanRecord record = new calPlanRecord();

                // Get column indices
                int idxId = cursor.getColumnIndex("id");
                int idxUNID = cursor.getColumnIndex("UNID");
                int idxForm = cursor.getColumnIndex("Form");
                int idxPriority = cursor.getColumnIndex("Priority");
                int idxOkdate = cursor.getColumnIndex("Okdate");
                int idxAuthorID = cursor.getColumnIndex("AuthorID");
                int idxAuthorName = cursor.getColumnIndex("AuthorName");
                int idxName = cursor.getColumnIndex("Name");
                int idxRequestName = cursor.getColumnIndex("RequestName");
                int idxRequestUNID = cursor.getColumnIndex("RequestUNID");
                int idxStatus = cursor.getColumnIndex("Status");
                int idxStatusID = cursor.getColumnIndex("StatusID");
                int idxMainSystem = cursor.getColumnIndex("MainSystem");
                int idxAnalitikID = cursor.getColumnIndex("AnalitikID");
                int idxAnalitikName = cursor.getColumnIndex("AnalitikName");
                int idxExectorID = cursor.getColumnIndex("ExectorID");
                int idxExectorName = cursor.getColumnIndex("ExectorName");
                int idxLastUpdatedByID = cursor.getColumnIndex("LastUpdatedByID");
                int idxLastUpdatedBy = cursor.getColumnIndex("LastUpdatedBy");
                int idxLastUpdatedDate = cursor.getColumnIndex("LastUpdatedDate");
                int idxBodyText = cursor.getColumnIndex("BodyText");
                int idxComment = cursor.getColumnIndex("Comment");
                int idxStartDate = cursor.getColumnIndex("StartDate");
                int idxEndDate = cursor.getColumnIndex("EndDate");
                int idxHoldDate = cursor.getColumnIndex("HoldDate");
                int idxInstallOrder = cursor.getColumnIndex("InstallOrder");
                int idxKeyWords = cursor.getColumnIndex("KeyWords");
                int idxRevisions = cursor.getColumnIndex("Revisions");

                // Fill record fields
                if (idxId >= 0 && !cursor.isNull(idxId)) record.id = cursor.getInt(idxId);
                if (idxUNID >= 0 && !cursor.isNull(idxUNID)) record.UNID = cursor.getString(idxUNID);
                if (idxForm >= 0 && !cursor.isNull(idxForm)) record.Form = cursor.getString(idxForm);
                if (idxPriority >= 0 && !cursor.isNull(idxPriority)) record.Priority = cursor.getInt(idxPriority);
                if (idxAuthorID >= 0 && !cursor.isNull(idxAuthorID)) record.AuthorID = cursor.getString(idxAuthorID);
                if (idxAuthorName >= 0 && !cursor.isNull(idxAuthorName)) record.AuthorName = cursor.getString(idxAuthorName);
                if (idxName >= 0 && !cursor.isNull(idxName)) record.Name = cursor.getString(idxName);
                if (idxRequestName >= 0 && !cursor.isNull(idxRequestName)) record.RequestName = cursor.getString(idxRequestName);
                if (idxRequestUNID >= 0 && !cursor.isNull(idxRequestUNID)) record.RequestUNID = cursor.getString(idxRequestUNID);
                if (idxStatus >= 0 && !cursor.isNull(idxStatus)) record.Status = cursor.getString(idxStatus);
                if (idxStatusID >= 0 && !cursor.isNull(idxStatusID)) record.StatusID = cursor.getString(idxStatusID);
                if (idxMainSystem >= 0 && !cursor.isNull(idxMainSystem)) record.MainSystem = cursor.getString(idxMainSystem);
                if (idxAnalitikID >= 0 && !cursor.isNull(idxAnalitikID)) record.AnalitikID = cursor.getString(idxAnalitikID);
                if (idxAnalitikName >= 0 && !cursor.isNull(idxAnalitikName)) record.AnalitikName = cursor.getString(idxAnalitikName);
                if (idxExectorID >= 0 && !cursor.isNull(idxExectorID)) record.ExectorID = cursor.getString(idxExectorID);
                if (idxExectorName >= 0 && !cursor.isNull(idxExectorName)) record.ExectorName = cursor.getString(idxExectorName);
                if (idxLastUpdatedByID >= 0 && !cursor.isNull(idxLastUpdatedByID)) record.LastUpdatedByID = cursor.getString(idxLastUpdatedByID);
                if (idxLastUpdatedBy >= 0 && !cursor.isNull(idxLastUpdatedBy)) record.LastUpdatedBy = cursor.getString(idxLastUpdatedBy);
                if (idxBodyText >= 0 && !cursor.isNull(idxBodyText)) record.BodyText = cursor.getString(idxBodyText);
                if (idxComment >= 0 && !cursor.isNull(idxComment)) record.Comment = cursor.getString(idxComment);
                if (idxInstallOrder >= 0 && !cursor.isNull(idxInstallOrder)) record.InstallOrder = cursor.getString(idxInstallOrder);
                if (idxKeyWords >= 0 && !cursor.isNull(idxKeyWords)) record.KeyWords = cursor.getString(idxKeyWords);
                if (idxRevisions >= 0 && !cursor.isNull(idxRevisions)) record.Revisions = cursor.getString(idxRevisions);

                // Parse dates
                if (idxOkdate >= 0 && !cursor.isNull(idxOkdate)) {
                    try {
                        record.Okdate = sdf.parse(cursor.getString(idxOkdate));
                    } catch (Exception e) {
                        record.Okdate = null;
                    }
                }
                if (idxLastUpdatedDate >= 0 && !cursor.isNull(idxLastUpdatedDate)) {
                    try {
                        record.LastUpdatedDate = sdf.parse(cursor.getString(idxLastUpdatedDate));
                    } catch (Exception e) {
                        record.LastUpdatedDate = null;
                    }
                }
                if (idxStartDate >= 0 && !cursor.isNull(idxStartDate)) {
                    try {
                        record.StartDate = sdf.parse(cursor.getString(idxStartDate));
                    } catch (Exception e) {
                        record.StartDate = null;
                    }
                }
                if (idxEndDate >= 0 && !cursor.isNull(idxEndDate)) {
                    try {
                        record.EndDate = sdf.parse(cursor.getString(idxEndDate));
                    } catch (Exception e) {
                        record.EndDate = null;
                    }
                }
                if (idxHoldDate >= 0 && !cursor.isNull(idxHoldDate)) {
                    try {
                        record.HoldDate = sdf.parse(cursor.getString(idxHoldDate));
                    } catch (Exception e) {
                        record.HoldDate = null;
                    }
                }

                recordsList.add(record);
                cursor.moveToNext();
            }
        }

        cursor.close();

        // Convert ArrayList to array
        calPlanRecord[] records = new calPlanRecord[recordsList.size()];
        recordsList.toArray(records);

        return records;
    }

    // Get CalParam record (single record) from CALPARAM table
    public CalParamRecord getCalParam() {
        SQLiteDatabase db = this.getReadableDatabase();
        CalParamRecord record = null;

        Cursor cursor = db.query("CALPARAM",
                null,
                null, null, null, null, null, "1");

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            record = new CalParamRecord();

            // Get column indices
            int idxId = cursor.getColumnIndex("id");
            int idxAddress = cursor.getColumnIndex("Address");
            int idxName = cursor.getColumnIndex("Name");
            int idxPassword = cursor.getColumnIndex("Password");

            // Fill record fields
            if (idxId >= 0 && !cursor.isNull(idxId)) record.id = cursor.getInt(idxId);
            if (idxAddress >= 0 && !cursor.isNull(idxAddress)) record.Address = cursor.getString(idxAddress);
            if (idxName >= 0 && !cursor.isNull(idxName)) record.Name = cursor.getString(idxName);
            if (idxPassword >= 0 && !cursor.isNull(idxPassword)) record.Password = cursor.getString(idxPassword);
        }

        cursor.close();

        return record;
    }

    // Upsert CalParamRecord into CALPARAM table
    public void upsertCalParam(CalParamRecord record) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        if (record.Address != null) values.put("Address", record.Address);
        if (record.Name != null) values.put("Name", record.Name);
        if (record.Password != null) values.put("Password", record.Password);

        // Try to update first (if id exists), if no rows affected then insert
        if (record.id != null) {
            int rowsAffected = db.update("CALPARAM", values, "id=?",
                    new String[]{String.valueOf(record.id)});

            if (rowsAffected == 0) {
                // Insert new record
                db.insert("CALPARAM", null, values);
            }
        } else {
            // Insert new record
            db.insert("CALPARAM", null, values);
        }
    }

    // Get holidays for a specific country and year
    public holidayRecord[] getHolidays(String countryCode, int year) {
        ArrayList<holidayRecord> holidaysList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String dateFrom = year + "-01-01";
        String dateTo = year + "-12-31";

        Cursor cursor = db.query("HOLIDAYS",
                null,
                "CountryCode=? AND HolidayDate>=? AND HolidayDate<=?",
                new String[]{countryCode, dateFrom, dateTo},
                null, null, "HolidayDate");

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                holidayRecord record = new holidayRecord();

                int idxId = cursor.getColumnIndex("id");
                int idxCountryCode = cursor.getColumnIndex("CountryCode");
                int idxHolidayDate = cursor.getColumnIndex("HolidayDate");
                int idxHolidayName = cursor.getColumnIndex("HolidayName");

                if (idxId >= 0 && !cursor.isNull(idxId)) record.id = cursor.getInt(idxId);
                if (idxCountryCode >= 0 && !cursor.isNull(idxCountryCode)) record.CountryCode = cursor.getString(idxCountryCode);
                if (idxHolidayName >= 0 && !cursor.isNull(idxHolidayName)) record.HolidayName = cursor.getString(idxHolidayName);

                if (idxHolidayDate >= 0 && !cursor.isNull(idxHolidayDate)) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                        record.HolidayDate = sdf.parse(cursor.getString(idxHolidayDate));
                    } catch (Exception e) {
                        record.HolidayDate = null;
                    }
                }

                holidaysList.add(record);
                cursor.moveToNext();
            }
        }

        cursor.close();

        holidayRecord[] holidays = new holidayRecord[holidaysList.size()];
        holidaysList.toArray(holidays);

        return holidays;
    }

    // Insert or update holidays for a specific country and year
    public void upsertHolidays(String countryCode, int year, holidayRecord[] holidays) {
        SQLiteDatabase db = this.getWritableDatabase();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());

        for (holidayRecord holiday : holidays) {
            ContentValues values = new ContentValues();
            values.put("CountryCode", countryCode);
            values.put("HolidayDate", sdf.format(holiday.HolidayDate));
            values.put("HolidayName", holiday.HolidayName);

            int rowsAffected = db.update("HOLIDAYS", values,
                    "CountryCode=? AND HolidayDate=?",
                    new String[]{countryCode, sdf.format(holiday.HolidayDate)});

            if (rowsAffected == 0) {
                db.insert("HOLIDAYS", null, values);
            }
        }
    }

    // Check if holidays need to be updated
    public boolean needsHolidayUpdate(String countryCode, int year, int month) {
        SQLiteDatabase db = this.getReadableDatabase();

        String dateFrom = year + "-" + String.format("%02d", month) + "-01";

        Cursor cursor = db.query("HOLIDAYS",
                new String[]{"COUNT(*) as count"},
                "CountryCode=? AND HolidayDate>=?",
                new String[]{countryCode, dateFrom},
                null, null, null);

        boolean needsUpdate = true;
        if (cursor.moveToFirst()) {
            int count = cursor.getInt(0);
            needsUpdate = (count == 0);
        }

        cursor.close();
        return needsUpdate;
    }

    // Upsert ContactRecord into CONTACTS table
    public void upsertContact(ContactRecord record) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        if (record.Surname != null) values.put("Surname", record.Surname);
        if (record.FirstName != null) values.put("FirstName", record.FirstName);
        if (record.Patronymic != null) values.put("Patronymic", record.Patronymic);
        if (record.Phone != null) values.put("Phone", record.Phone);
        if (record.Info != null) values.put("Info", record.Info);
        if (record.Phone2 != null) values.put("Phone2", record.Phone2);
        if (record.Email != null) values.put("Email", record.Email);
        if (record.HomeAddress != null) values.put("HomeAddress", record.HomeAddress);
        if (record.EntryID != null) values.put("EntryID", record.EntryID);

        // Date fields
        if (record.BirthDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            values.put("BirthDate", sdf.format(record.BirthDate));
        }
        if (record.DateReceived != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            values.put("DateReceived", sdf.format(record.DateReceived));
        }
        if (record.DateCreated != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            values.put("DateCreated", sdf.format(record.DateCreated));
        }
        if (record.DateModified != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            values.put("DateModified", sdf.format(record.DateModified));
        }

        // Try to update first (if id exists), if no rows affected then insert
        if (record.id != null) {
            int rowsAffected = db.update("CONTACTS", values, "id=?",
                    new String[]{String.valueOf(record.id)});

            if (rowsAffected == 0) {
                db.insert("CONTACTS", null, values);
            }
        } else {
            db.insert("CONTACTS", null, values);
        }
    }

    // Get contacts by filter (search in multiple fields)
    public ContactRecord[] getContacts(String filter) {
        ArrayList<ContactRecord> contactsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;

        if (filter != null && filter.length() >= 3) {
            // Search in multiple fields when filter is 3+ characters
            String searchPattern = "%" + filter.toLowerCase() + "%";
            String selection = "LOWER(Surname) LIKE ? OR LOWER(FirstName) LIKE ? OR " +
                    "LOWER(Patronymic) LIKE ? OR LOWER(Phone) LIKE ? OR " +
                    "LOWER(Info) LIKE ? OR LOWER(Phone2) LIKE ? OR " +
                    "LOWER(Email) LIKE ? OR LOWER(HomeAddress) LIKE ?";
            String[] selectionArgs = new String[]{
                    searchPattern, searchPattern, searchPattern, searchPattern,
                    searchPattern, searchPattern, searchPattern, searchPattern
            };

            cursor = db.query("CONTACTS",
                    null,
                    selection,
                    selectionArgs,
                    null, null, "Surname, FirstName");
        } else {
            // Return all contacts if no filter
            cursor = db.query("CONTACTS",
                    null,
                    null, null, null, null, "Surname, FirstName");
        }

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                ContactRecord record = new ContactRecord();

                int idxId = cursor.getColumnIndex("id");
                int idxSurname = cursor.getColumnIndex("Surname");
                int idxFirstName = cursor.getColumnIndex("FirstName");
                int idxPatronymic = cursor.getColumnIndex("Patronymic");
                int idxPhone = cursor.getColumnIndex("Phone");
                int idxInfo = cursor.getColumnIndex("Info");
                int idxPhone2 = cursor.getColumnIndex("Phone2");
                int idxEmail = cursor.getColumnIndex("Email");
                int idxHomeAddress = cursor.getColumnIndex("HomeAddress");
                int idxEntryID = cursor.getColumnIndex("EntryID");
                int idxBirthDate = cursor.getColumnIndex("BirthDate");
                int idxDateReceived = cursor.getColumnIndex("DateReceived");
                int idxDateCreated = cursor.getColumnIndex("DateCreated");
                int idxDateModified = cursor.getColumnIndex("DateModified");

                if (idxId >= 0 && !cursor.isNull(idxId)) record.id = cursor.getInt(idxId);
                if (idxSurname >= 0 && !cursor.isNull(idxSurname)) record.Surname = cursor.getString(idxSurname);
                if (idxFirstName >= 0 && !cursor.isNull(idxFirstName)) record.FirstName = cursor.getString(idxFirstName);
                if (idxPatronymic >= 0 && !cursor.isNull(idxPatronymic)) record.Patronymic = cursor.getString(idxPatronymic);
                if (idxPhone >= 0 && !cursor.isNull(idxPhone)) record.Phone = cursor.getString(idxPhone);
                if (idxInfo >= 0 && !cursor.isNull(idxInfo)) record.Info = cursor.getString(idxInfo);
                if (idxPhone2 >= 0 && !cursor.isNull(idxPhone2)) record.Phone2 = cursor.getString(idxPhone2);
                if (idxEmail >= 0 && !cursor.isNull(idxEmail)) record.Email = cursor.getString(idxEmail);
                if (idxHomeAddress >= 0 && !cursor.isNull(idxHomeAddress)) record.HomeAddress = cursor.getString(idxHomeAddress);
                if (idxEntryID >= 0 && !cursor.isNull(idxEntryID)) record.EntryID = cursor.getString(idxEntryID);

                // Parse dates
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                if (idxBirthDate >= 0 && !cursor.isNull(idxBirthDate)) {
                    try { record.BirthDate = sdf.parse(cursor.getString(idxBirthDate)); } catch (Exception e) {}
                }
                if (idxDateReceived >= 0 && !cursor.isNull(idxDateReceived)) {
                    try { record.DateReceived = sdf.parse(cursor.getString(idxDateReceived)); } catch (Exception e) {}
                }
                if (idxDateCreated >= 0 && !cursor.isNull(idxDateCreated)) {
                    try { record.DateCreated = sdf.parse(cursor.getString(idxDateCreated)); } catch (Exception e) {}
                }
                if (idxDateModified >= 0 && !cursor.isNull(idxDateModified)) {
                    try { record.DateModified = sdf.parse(cursor.getString(idxDateModified)); } catch (Exception e) {}
                }

                contactsList.add(record);
                cursor.moveToNext();
            }
        }

        if (cursor != null) {
            cursor.close();
        }

        ContactRecord[] contacts = new ContactRecord[contactsList.size()];
        contactsList.toArray(contacts);

        return contacts;
    }
}
