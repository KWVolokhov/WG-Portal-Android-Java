package com.example.calendar4;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ManageSQLDatabase extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 10;
    public static final String DATABASE_NAME = "WGPlanDatabase.db";
	
	public static String AuthorName = null;
	public static String AuthorID = null;
    private static ManageSQLDatabase instance=null;

    // 1. Делаем конструктор приватным
    protected ManageSQLDatabase(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
        CalParamRecord param = getCalParam();
        if (param != null) {
            AuthorName = param.Vedushii;
            AuthorID = param.VedushiiID;
        }
    }

    // 2. Метод для получения единственного экземпляра
    public static synchronized ManageSQLDatabase getInstance(Context context) {
        if (instance == null) {
            // Использование getApplicationContext() предотвращает утечки памяти
            instance = new ManageSQLDatabase(context.getApplicationContext());
        }
        return instance;
    }

    /*public ManageSQLDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }*/
	
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Execute the SQL statement defined abov
        //db.execSQL("DROP TABLE IF EXISTS CALPLAN");
        db.execSQL(ConstantsSQLDb.CREATE_TABLE_CALPLAN);

        // Tables separated from CALPLAN (Form=History -> HISTORY, Form=Note/Remember -> NOTEPLAN,
        // Form=HealthEat/HealthDrink/HealthSport -> HEALTHPLAN)
        db.execSQL(ConstantsSQLDb.CREATE_TABLE_HISTORY);
        db.execSQL(ConstantsSQLDb.CREATE_TABLE_NOTEPLAN);
        db.execSQL(ConstantsSQLDb.CREATE_TABLE_HEALTHPLAN);

        // LIVETYPE reference table (Типы жизнедеятельности) - created for fresh installs
        db.execSQL(ConstantsSQLDb.CREATE_TABLE_LIVETYPE);
        insertLivetypeDefaults(db);

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

        // Task 106: таблица СМС (та же локальная БД) + 2 тестовые записи
        db.execSQL(ConstantsSQLDb.CREATE_TABLE_SMSCALPLAN);
        seedSmsDefaults(db);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Version 2: added Vedushii / VedushiiID columns to CALPARAM
        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE CALPARAM ADD COLUMN Vedushii TEXT");
                db.execSQL("ALTER TABLE CALPARAM ADD COLUMN VedushiiID TEXT");
            } catch (Exception e) {
                // Columns may already exist - ignore
            }
        }
        // Version 3: HISTORY, NOTEPLAN, HEALTHPLAN tables were split out of CALPLAN
        if (oldVersion < 3) {
            try {
                db.execSQL(ConstantsSQLDb.CREATE_TABLE_HISTORY);
                db.execSQL(ConstantsSQLDb.CREATE_TABLE_NOTEPLAN);
                db.execSQL(ConstantsSQLDb.CREATE_TABLE_HEALTHPLAN);

                // Migrate existing rows from CALPLAN into the new tables, then remove them
                // from CALPLAN so that only Project / Task / Request stay there.
                db.execSQL("INSERT OR IGNORE INTO HISTORY " +
                        "(UNID, Okdate, AuthorID, AuthorName, LastUpdatedByID, LastUpdatedBy, LastUpdatedDate, Name, BodyText, Comment, StartDate, Revisions) " +
                        "SELECT UNID, Okdate, AuthorID, AuthorName, LastUpdatedByID, LastUpdatedBy, LastUpdatedDate, Name, BodyText, Comment, StartDate, Revisions " +
                        "FROM CALPLAN WHERE Form='History'");
                db.execSQL("DELETE FROM CALPLAN WHERE Form='History'");

                db.execSQL("INSERT OR IGNORE INTO NOTEPLAN " +
                        "(UNID, Form, Okdate, AuthorID, AuthorName, LastUpdatedByID, LastUpdatedBy, LastUpdatedDate, Name, Status, StatusID, StartDate, BodyText, Comment, KeyWords, Revisions) " +
                        "SELECT UNID, Form, Okdate, AuthorID, AuthorName, LastUpdatedByID, LastUpdatedBy, LastUpdatedDate, Name, Status, StatusID, StartDate, BodyText, Comment, KeyWords, Revisions " +
                        "FROM CALPLAN WHERE Form='Note' OR Form='Remember'");
                db.execSQL("DELETE FROM CALPLAN WHERE Form='Note' OR Form='Remember'");

                db.execSQL("INSERT OR IGNORE INTO HEALTHPLAN " +
                        "(UNID, Form, Okdate, AuthorID, AuthorName, LastUpdatedByID, LastUpdatedBy, LastUpdatedDate, Name, BodyText, Comment, StartDate, EndDate, Revisions) " +
                        "SELECT UNID, Form, Okdate, AuthorID, AuthorName, LastUpdatedByID, LastUpdatedBy, LastUpdatedDate, Name, BodyText, Comment, StartDate, EndDate, Revisions " +
                        "FROM CALPLAN WHERE Form='HealthEat' OR Form='HealthDrink' OR Form='HealthSport'");
                db.execSQL("DELETE FROM CALPLAN WHERE Form='HealthEat' OR Form='HealthDrink' OR Form='HealthSport'");
            } catch (Exception e) {
                // Tables/rows may already be migrated - ignore
            }
        }
        // Version 4: LIVETYPE reference table + StartPage column in CALPARAM
        if (oldVersion < 4) {
            try {
                db.execSQL(ConstantsSQLDb.CREATE_TABLE_LIVETYPE);
                db.execSQL("ALTER TABLE CALPARAM ADD COLUMN StartPage TEXT");
                insertLivetypeDefaults(db);
            } catch (Exception e) {
                // Table/column may already exist - ignore
            }
        }
        // Version 5: Icon column in LIVETYPE + three configurable quick buttons in CALPARAM
        if (oldVersion < 5) {
            addColumnIfMissing(db, "LIVETYPE", "Icon", "TEXT");
            addColumnIfMissing(db, "CALPARAM", "Button1Id", "TEXT");
            addColumnIfMissing(db, "CALPARAM", "Button2Id", "TEXT");
            addColumnIfMissing(db, "CALPARAM", "Button3Id", "TEXT");
            try {
                for (String updCom : ConstantsSQLDb.UPDATE_LIVETYPE_ICONS) {
                    db.execSQL(updCom);
                }
            } catch (Exception e) {
                // Non-fatal
            }
        }
        // Version 6: Button4Id / Button5Id quick buttons (5 buttons total).
        // Re-runs for every database older than 6; "addColumnIfMissing" is safe
        // even when some of the columns already exist.
        if (oldVersion < 6) {
            addColumnIfMissing(db, "CALPARAM", "Button1Id", "TEXT");
            addColumnIfMissing(db, "CALPARAM", "Button2Id", "TEXT");
            addColumnIfMissing(db, "CALPARAM", "Button3Id", "TEXT");
            addColumnIfMissing(db, "CALPARAM", "Button4Id", "TEXT");
            addColumnIfMissing(db, "CALPARAM", "Button5Id", "TEXT");
        }
        // Version 7 (Tasks 40-42): LIVETYPE - Form is its own column (ID of Category),
        // StepCounter flag for the pedometer, Steps/FoodWeight/DrinkValue/Kallory
        // in both LIVETYPE and HEALTHPLAN.
        if (oldVersion < 7) {
            addColumnIfMissing(db, "LIVETYPE", "Form", "TEXT");
            addColumnIfMissing(db, "LIVETYPE", "StepCounter", "INTEGER");
            addColumnIfMissing(db, "LIVETYPE", "Steps", "INTEGER");
            addColumnIfMissing(db, "LIVETYPE", "FoodWeight", "INTEGER");
            addColumnIfMissing(db, "LIVETYPE", "DrinkValue", "INTEGER");
            addColumnIfMissing(db, "LIVETYPE", "Kallory", "INTEGER");
            addColumnIfMissing(db, "HEALTHPLAN", "Steps", "INTEGER");
            addColumnIfMissing(db, "HEALTHPLAN", "FoodWeight", "INTEGER");
            addColumnIfMissing(db, "HEALTHPLAN", "DrinkValue", "INTEGER");
            addColumnIfMissing(db, "HEALTHPLAN", "Kallory", "INTEGER");
            try {
                // У старых предустановленных записей Form хранился внутри UNID
                db.execSQL("UPDATE LIVETYPE SET Form=UNID WHERE UNID IN ('HealthSport','HealthEat','HealthDrink','HealthStress','HealthJoy') AND (Form IS NULL OR Form='')");
                for (String updCom : ConstantsSQLDb.UPDATE_LIVETYPE_DEFAULTS) {
                    db.execSQL(updCom);
                }
            } catch (Exception e) {
                // Non-fatal
            }
        }

        // Version 8 (Task 53): CONTACTS - Автор/Обновивший (ID + имя)
        if (oldVersion < 8) {
            addColumnIfMissing(db, "CONTACTS", "AuthorID", "TEXT");
            addColumnIfMissing(db, "CONTACTS", "AuthorName", "TEXT");
            addColumnIfMissing(db, "CONTACTS", "LastUpdatedByID", "TEXT");
            addColumnIfMissing(db, "CONTACTS", "LastUpdatedBy", "TEXT");
        }

        // Version 9 (Tasks 54/100): CALPARAM - Рост/Вес/Возраст + папка вложений + имя базы
        if (oldVersion < 9) {
            addColumnIfMissing(db, "CALPARAM", "Height", "INTEGER");
            addColumnIfMissing(db, "CALPARAM", "Weight", "INTEGER");
            addColumnIfMissing(db, "CALPARAM", "Age", "INTEGER");
            addColumnIfMissing(db, "CALPARAM", "AttachFolder", "TEXT");
            addColumnIfMissing(db, "CALPARAM", "DBName", "TEXT");
        }

        // Version 10 (Task 106): таблица СМС SMSCALPLAN + 2 тестовые записи
        if (oldVersion < 10) {
            try {
                db.execSQL(ConstantsSQLDb.CREATE_TABLE_SMSCALPLAN);
                seedSmsDefaults(db);
            } catch (Exception e) {
                // Non-fatal
            }
        }

        // Safety net (Task 34): whatever version the old database recorded, make sure
        // that every column the application needs really exists. Old production builds
        // suffered from "no such column: Button1Id / Button4Id ... while compiling:
        // UPDATE CALPARAM SET ..." because their recorded version was already high,
        // so the classic oldVersion < N checks never ran for them.
        try { db.execSQL(ConstantsSQLDb.CREATE_TABLE_LIVETYPE); } catch (Exception e) { }
        addColumnIfMissing(db, "CALPARAM", "Vedushii", "TEXT");
        addColumnIfMissing(db, "CALPARAM", "VedushiiID", "TEXT");
        addColumnIfMissing(db, "CALPARAM", "StartPage", "TEXT");
        addColumnIfMissing(db, "CALPARAM", "Button1Id", "TEXT");
        addColumnIfMissing(db, "CALPARAM", "Button2Id", "TEXT");
        addColumnIfMissing(db, "CALPARAM", "Button3Id", "TEXT");
        addColumnIfMissing(db, "CALPARAM", "Button4Id", "TEXT");
        addColumnIfMissing(db, "CALPARAM", "Button5Id", "TEXT");
        addColumnIfMissing(db, "LIVETYPE", "Icon", "TEXT");
        addColumnIfMissing(db, "LIVETYPE", "Form", "TEXT");
        addColumnIfMissing(db, "LIVETYPE", "StepCounter", "INTEGER");
        addColumnIfMissing(db, "LIVETYPE", "Steps", "INTEGER");
        addColumnIfMissing(db, "LIVETYPE", "FoodWeight", "INTEGER");
        addColumnIfMissing(db, "LIVETYPE", "DrinkValue", "INTEGER");
        addColumnIfMissing(db, "LIVETYPE", "Kallory", "INTEGER");
        addColumnIfMissing(db, "HEALTHPLAN", "Steps", "INTEGER");
        addColumnIfMissing(db, "HEALTHPLAN", "FoodWeight", "INTEGER");
        addColumnIfMissing(db, "HEALTHPLAN", "DrinkValue", "INTEGER");
        addColumnIfMissing(db, "HEALTHPLAN", "Kallory", "INTEGER");
        addColumnIfMissing(db, "CONTACTS", "AuthorID", "TEXT");
        addColumnIfMissing(db, "CONTACTS", "AuthorName", "TEXT");
        addColumnIfMissing(db, "CONTACTS", "LastUpdatedByID", "TEXT");
        addColumnIfMissing(db, "CONTACTS", "LastUpdatedBy", "TEXT");
        addColumnIfMissing(db, "CALPARAM", "Height", "INTEGER");
        addColumnIfMissing(db, "CALPARAM", "Weight", "INTEGER");
        addColumnIfMissing(db, "CALPARAM", "Age", "INTEGER");
        addColumnIfMissing(db, "CALPARAM", "AttachFolder", "TEXT");
        addColumnIfMissing(db, "CALPARAM", "DBName", "TEXT");
        // Task 106: таблица СМС создаётся и наполняется тестовыми записями на всякий случай
        try {
            db.execSQL(ConstantsSQLDb.CREATE_TABLE_SMSCALPLAN);
            seedSmsDefaults(db);
        } catch (Exception e) {
            // Non-fatal
        }
        try {
            for (String updCom : ConstantsSQLDb.UPDATE_LIVETYPE_ICONS) {
                db.execSQL(updCom);
            }
            try {
                db.execSQL("UPDATE LIVETYPE SET Form=UNID WHERE UNID IN ('HealthSport','HealthEat','HealthDrink','HealthStress','HealthJoy') AND (Form IS NULL OR Form='')");
            } catch (Exception e) {}
            for (String updCom : ConstantsSQLDb.UPDATE_LIVETYPE_DEFAULTS) {
                db.execSQL(updCom);
            }
            insertLivetypeDefaults(db);
        } catch (Exception e) {
            // Non-fatal
        }
    }

    /** True when the given column already exists in the table. */
    private boolean columnExists(SQLiteDatabase db, String table, String column) {
        Cursor cursor = db.rawQuery("PRAGMA table_info(" + table + ")", null);
        try {
            int nameIdx = cursor.getColumnIndex("name");
            while (cursor.moveToNext()) {
                if (column.equals(cursor.getString(nameIdx))) return true;
            }
        } catch (Exception e) {
            // ignore
        } finally {
            cursor.close();
        }
        return false;
    }

    /** Adds the given text column to the table when it is still missing. */
    private void addColumnIfMissing(SQLiteDatabase db, String table, String column, String type) {
        try {
            if (!columnExists(db, table, column)) {
                db.execSQL("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type);
            }
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * Fills the LIVETYPE reference table with the initial records
     * (Прогулка/Бургер/Кофе 200мл/Авария/Гулянка). INSERT OR IGNORE keeps the
     * existing user records - only missing UNIDs are added.
     */
    private void insertLivetypeDefaults(SQLiteDatabase db) {
        try {
            for (String insertCom : ConstantsSQLDb.INSERT_LIVETYPE) {
                // Turn "INSERT INTO" into "INSERT OR IGNORE INTO" to avoid duplicates
                db.execSQL(insertCom.replaceFirst("(?i)^INSERT INTO", "INSERT OR IGNORE INTO"));
            }
        } catch (Exception e) {
            // Non-fatal: seeding failure must not break the app
        }
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

    // Upsert (Insert or Update) calPlanRecord. Depending on the Form value the record
    // is saved to its own table: HISTORY / NOTEPLAN / HEALTHPLAN / CALPLAN (Project, Task, Request)
    public void upsertCalPlan(calPlanRecord record) {
        if (record == null) return;

        // Form=History -> HISTORY table
        if ("History".equals(record.Form)) {
            new HistorySQLManage(this.getWritableDatabase()).upsertHistory(record);
            return;
        }
        // Form=Note / Form=Remember -> NOTEPLAN table
        if ("Note".equals(record.Form) || "Remember".equals(record.Form)) {
            new NoteRememSQLManage(this.getWritableDatabase()).upsertNote(record);
            return;
        }
        // Form=HealthEat / HealthDrink / HealthSport -> HEALTHPLAN table
        if ("HealthEat".equals(record.Form) || "HealthDrink".equals(record.Form) || "HealthSport".equals(record.Form)) {
            new HealthSQLManage(this.getWritableDatabase()).upsertHealth(record);
            return;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Auto-filled fields
        if (record.id != null) values.put("id", record.id);
        // Generate a unique ID for brand-new records (RequestUNID references these)
        if (record.id == null && (record.UNID == null || record.UNID.isEmpty())) {
            record.UNID = java.util.UUID.randomUUID().toString();
        }
        if (record.UNID != null) values.put("UNID", record.UNID);
        if (record.Okdate != null) {
            values.put("Okdate", fmtDateTimeString(record.Okdate));
        }
        if (record.AuthorID != null) values.put("AuthorID", record.AuthorID);
        if (record.LastUpdatedByID != null) values.put("LastUpdatedByID", record.LastUpdatedByID);
        if (record.LastUpdatedBy != null) values.put("LastUpdatedBy", record.LastUpdatedBy);
        if (record.LastUpdatedDate != null) {
            values.put("LastUpdatedDate", fmtDateTimeString(record.LastUpdatedDate));
        }
        if (record.HoldDate != null) {
            values.put("HoldDate", fmtDateTimeString(record.HoldDate));
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
            values.put("EndDate", fmtDateTimeString(record.EndDate));
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

    // Delete a record by its Form: HISTORY / NOTEPLAN / HEALTHPLAN / CALPLAN
    public void deleteCalPlanRecord(calPlanRecord record) {
        if (record == null || record.id == null) return;
        if ("History".equals(record.Form)) {
            new HistorySQLManage(this.getWritableDatabase()).deleteHistory(record.id);
            return;
        }
        if ("Note".equals(record.Form) || "Remember".equals(record.Form)) {
            new NoteRememSQLManage(this.getWritableDatabase()).deleteNote(record.id);
            return;
        }
        if ("HealthEat".equals(record.Form) || "HealthDrink".equals(record.Form) || "HealthSport".equals(record.Form)) {
            new HealthSQLManage(this.getWritableDatabase()).deleteHealth(record.id);
            return;
        }
        deleteCalPlan(record.id);
    }

    // Delete a CONTACTS record by its id.
    // Before deleting, logs a History entry into CALPLAN with the contact fields
    // as "field=value,field=value,..." in BodyText.
    public void deleteContact(Integer id) {
        if (id == null) return;
        SQLiteDatabase db = this.getWritableDatabase();

        // Snapshot the contact before deletion
        ContactRecord record = getContactById(id);

        // Log "Удал. Конт.:" before actually deleting (fields go to BodyText)
        addHistory("Удал. Конт.:", record, buildContactBodyText(record));

        db.delete("CONTACTS", "id=?", new String[]{String.valueOf(id)});
    }

    // Fetch a single contact by its numeric id
    public ContactRecord getContactById(Integer id) {
        if (id == null) return null;
        SQLiteDatabase db = this.getReadableDatabase();
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

    // Find a contact whose stored Phone begins with the given 10 digits
    public ContactRecord getContactByPhone(String phoneDigits) {
        if (phoneDigits == null || phoneDigits.isEmpty()) return null;
        SQLiteDatabase db = this.getReadableDatabase();
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

    // Find a contact by exact Surname + FirstName (used when the device phone is unavailable)
    public ContactRecord getContactBySurnameFirstName(String surname, String firstName) {
        if (surname == null || firstName == null) return null;
        SQLiteDatabase db = this.getReadableDatabase();
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

    // Append a History entry into CALPLAN for a CONTACTS operation
    private void addHistory(String action, ContactRecord record, String bodyText) {
        if (record == null) return;
        calPlanRecord history = new calPlanRecord();
        history.Form = "History";
        history.Okdate = new Date();
        // History records must appear in the calendar list on the day the entry was made
        history.StartDate = new Date();

        history.AuthorName = AuthorName;
        history.AuthorID = AuthorID;

        StringBuilder name = new StringBuilder(action);
        if (record.Surname != null) name.append(" ").append(record.Surname);
        if (record.FirstName != null) name.append(" ").append(record.FirstName);
        history.Name = name.toString();

        if (bodyText != null && !bodyText.isEmpty()) history.BodyText = bodyText;

        // History is stored in its own HISTORY table
        new HistorySQLManage(this.getWritableDatabase()).upsertHistory(history);
    }

    // Non-empty fields of a contact delimited by ',': "Фамилия=Иванов,Имя=Пётр,...".
    // Uses the Russian captions from the edit form (activity_editcontact.xml).
    // Special fields (AuthorID/AuthorName, LastUpdatedByID/LastUpdatedBy, DateCreated / DateModified) are NOT stored in History.
    private String buildContactBodyText(ContactRecord r) {
        if (r == null) return null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
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

    private ContactRecord cursorToContact(Cursor cursor) {
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
        int idxAuthorID = cursor.getColumnIndex("AuthorID");
        int idxAuthorName = cursor.getColumnIndex("AuthorName");
        int idxLastUpdatedByID = cursor.getColumnIndex("LastUpdatedByID");
        int idxLastUpdatedBy = cursor.getColumnIndex("LastUpdatedBy");

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
        if (idxAuthorID >= 0 && !cursor.isNull(idxAuthorID)) record.AuthorID = cursor.getString(idxAuthorID);
        if (idxAuthorName >= 0 && !cursor.isNull(idxAuthorName)) record.AuthorName = cursor.getString(idxAuthorName);
        if (idxLastUpdatedByID >= 0 && !cursor.isNull(idxLastUpdatedByID)) record.LastUpdatedByID = cursor.getString(idxLastUpdatedByID);
        if (idxLastUpdatedBy >= 0 && !cursor.isNull(idxLastUpdatedBy)) record.LastUpdatedBy = cursor.getString(idxLastUpdatedBy);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        if (idxBirthDate >= 0 && !cursor.isNull(idxBirthDate)) {
            try { record.BirthDate = sdf.parse(cursor.getString(idxBirthDate)); } catch (Exception e) {}
        }
        if (idxDateReceived >= 0 && !cursor.isNull(idxDateReceived)) {
            try { record.DateReceived = sdf.parse(cursor.getString(idxDateReceived)); } catch (Exception e) {}
        }
        // Task 44/53: DateCreated/DateModified могут храниться со временем
        if (idxDateCreated >= 0) record.DateCreated = parseDateFlexible(cursor, idxDateCreated);
        if (idxDateModified >= 0) record.DateModified = parseDateFlexible(cursor, idxDateModified);
        return record;
    }

    // Task 44: Okdate/LastUpdatedDate/EndDate/HoldDate хранятся со временем.
    private Date parseDateFlexible(Cursor cursor, int columnIndex) {
        if (columnIndex == -1 || cursor.isNull(columnIndex)) return null;
        String s = cursor.getString(columnIndex);
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).parse(s);
        } catch (Exception e) {
            try {
                return new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).parse(s);
            } catch (Exception e2) {
                return null;
            }
        }
    }

    private String fmtDateTimeString(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(date);
    }

    // Get calPlan records by date from CALPLAN table
    public calPlanRecord[] getCalPlan(Date date) {
        ArrayList<calPlanRecord> recordsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        String dateStr = sdf.format(date);

        // Task 38: запись показывается на выбранный день, если день лежит внутри
        // [StartDate .. EndDate]; если EndDate не задан - запись действует бесконечно.
        // Query records where the given date is within the validity range
        Cursor cursor = db.query("CALPLAN",
                null,
                "StartDate<=? AND (EndDate IS NULL OR EndDate>=?)",
                new String[]{dateStr, dateStr},
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
                record.Okdate = parseDateFlexible(cursor, idxOkdate);
                record.LastUpdatedDate = parseDateFlexible(cursor, idxLastUpdatedDate);
                record.StartDate = parseDateFlexible(cursor, idxStartDate);
                record.EndDate = parseDateFlexible(cursor, idxEndDate);
                record.HoldDate = parseDateFlexible(cursor, idxHoldDate);

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

        // Get all CALPLAN records of a given Form (e.g. "Project" for the "В Проекте" picker)
    public calPlanRecord[] getCalPlanByForm(String form) {
        return queryCalPlan("Form=?", new String[]{form});
    }

    // Get CALPLAN records of Form='Project' AND Form='Task' (the "Проекты\Все" screen),
    // sorted by StartDate (earliest first); records without a date go last.
    // When the filter is 3+ characters it also filters by Name (LOWER LIKE).
    public calPlanRecord[] getProjectsTasks(String filter) {
        return getProjectsTasks(filter, false);
    }

    /**
     * "Проекты" screen query (All or Work mode).
     *
     * @param workOnly when true keeps only projects/tasks in status
     *                 "В работе"/"Тестирование" (StatusID Inwork/Intest);
     *                 when false all projects and tasks are returned.
     */
    public calPlanRecord[] getProjectsTasks(String filter, boolean workOnly) {
        String baseWhere = "(Form='Project' OR Form='Task')";
        if (workOnly) {
            baseWhere += " AND (StatusID IN ('Inwork','Intest') OR Status IN ('В работе','Тестирование'))";
        }
        calPlanRecord[] arr;
        if (filter != null && filter.trim().length() >= 3) {
            String pattern = "%" + filter.trim().toLowerCase(java.util.Locale.getDefault()) + "%";
            arr = queryCalPlan(baseWhere + " AND LOWER(Name) LIKE ?",
                    new String[]{pattern});
        } else {
            arr = queryCalPlan(baseWhere, null);
        }
        if (arr == null || arr.length == 0) return new calPlanRecord[0];

        ArrayList<calPlanRecord> list = new ArrayList<>(Arrays.asList(arr));
        // WG: раньше был list.sort(...) — это Java-8 метод ArrayList.sort, которого нет на старых
        // Android (появился с API 24). ContactsActivity/ParamsActivity этот путь НЕ используют,
        // поэтому падал только экран Проекты. Collections.sort доступен на всех версиях Android.
        Collections.sort(list, (a, b) -> {
            Date da = a.StartDate;
            Date db = b.StartDate;
            if (da == null && db == null) return 0;
            if (da == null) return 1;
            if (db == null) return -1;
            return da.compareTo(db);
        });
        return list.toArray(new calPlanRecord[0]);
    }

    // Get CALPLAN records of Form='History' for a concrete date
    // (day view in HistoryActivity). History lives in its own HISTORY table.
    public calPlanRecord[] getCalPlanHistory(Date date) {
        return new HistorySQLManage(this.getWritableDatabase()).getHistoryByDate(date);
    }

    // Day list for the MainActivity: CALPLAN (Project/Task/Request) + NOTEPLAN (Note/Remember).
    // History and Health are not shown in the MainActivity day list.
    public calPlanRecord[] getDayRecords(Date date) {
        ArrayList<calPlanRecord> list = new ArrayList<>();
        calPlanRecord[] cal = getCalPlan(date);
        if (cal != null) list.addAll(Arrays.asList(cal));
        calPlanRecord[] notes = new NoteRememSQLManage(this.getWritableDatabase()).getNotesByDate(date);
        if (notes != null) list.addAll(Arrays.asList(notes));
        return list.toArray(new calPlanRecord[0]);
    }

    // Generic CALPLAN query used by getCalPlan / getCalPlanByForm / getCalPlanHistory
    private calPlanRecord[] queryCalPlan(String selection, String[] args) {
        ArrayList<calPlanRecord> recordsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());

        Cursor cursor = db.query("CALPLAN", null, selection, args, null, null, null);

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
                record.Okdate = parseDateFlexible(cursor, idxOkdate);
                record.LastUpdatedDate = parseDateFlexible(cursor, idxLastUpdatedDate);
                record.StartDate = parseDateFlexible(cursor, idxStartDate);
                record.EndDate = parseDateFlexible(cursor, idxEndDate);
                record.HoldDate = parseDateFlexible(cursor, idxHoldDate);

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
        //SQLiteDatabase db = this.getReadableDatabase();
        CalParamRecord record = null;

        Cursor cursor = this.getReadableDatabase().query("CALPARAM",
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
            int idxVedushii = cursor.getColumnIndex("Vedushii");
            int idxVedushiiID = cursor.getColumnIndex("VedushiiID");
            int idxStartPage = cursor.getColumnIndex("StartPage");
            int idxButton1Id = cursor.getColumnIndex("Button1Id");
            int idxButton2Id = cursor.getColumnIndex("Button2Id");
            int idxButton3Id = cursor.getColumnIndex("Button3Id");
			int idxButton4Id = cursor.getColumnIndex("Button4Id");
			int idxButton5Id = cursor.getColumnIndex("Button5Id");

            // Fill record fields
            if (idxId >= 0 && !cursor.isNull(idxId)) record.id = cursor.getInt(idxId);
            if (idxAddress >= 0 && !cursor.isNull(idxAddress)) record.Address = cursor.getString(idxAddress);
            if (idxName >= 0 && !cursor.isNull(idxName)) record.Name = cursor.getString(idxName);
            if (idxPassword >= 0 && !cursor.isNull(idxPassword)) record.Password = cursor.getString(idxPassword);
            if (idxVedushii >= 0 && !cursor.isNull(idxVedushii)) record.Vedushii = cursor.getString(idxVedushii);
            if (idxVedushiiID >= 0 && !cursor.isNull(idxVedushiiID)) record.VedushiiID = cursor.getString(idxVedushiiID);
            if (idxStartPage >= 0 && !cursor.isNull(idxStartPage)) record.StartPage = cursor.getString(idxStartPage);
            if (idxButton1Id >= 0 && !cursor.isNull(idxButton1Id)) {
                try { record.Button1Id = Integer.valueOf(cursor.getString(idxButton1Id)); } catch (Exception e) { record.Button1Id = null; }
            }
            if (idxButton2Id >= 0 && !cursor.isNull(idxButton2Id)) {
                try { record.Button2Id = Integer.valueOf(cursor.getString(idxButton2Id)); } catch (Exception e) { record.Button2Id = null; }
            }
            if (idxButton3Id >= 0 && !cursor.isNull(idxButton3Id)) {
                try { record.Button3Id = Integer.valueOf(cursor.getString(idxButton3Id)); } catch (Exception e) { record.Button3Id = null; }
            }
			if (idxButton4Id >= 0 && !cursor.isNull(idxButton4Id)) {
                try { record.Button4Id = Integer.valueOf(cursor.getString(idxButton4Id)); } catch (Exception e) { record.Button4Id = null; }
            }
            if (idxButton5Id >= 0 && !cursor.isNull(idxButton5Id)) {
                try { record.Button5Id = Integer.valueOf(cursor.getString(idxButton5Id)); } catch (Exception e) { record.Button5Id = null; }
            }

            // Tasks 54/100: Рост/Вес/Возраст + папка вложений + имя базы
            int idxHeight = cursor.getColumnIndex("Height");
            int idxWeight = cursor.getColumnIndex("Weight");
            int idxAge = cursor.getColumnIndex("Age");
            int idxAttachFolder = cursor.getColumnIndex("AttachFolder");
            int idxDBName = cursor.getColumnIndex("DBName");
            if (idxHeight >= 0 && !cursor.isNull(idxHeight)) record.Height = cursor.getInt(idxHeight);
            if (idxWeight >= 0 && !cursor.isNull(idxWeight)) record.Weight = cursor.getInt(idxWeight);
            if (idxAge >= 0 && !cursor.isNull(idxAge)) record.Age = cursor.getInt(idxAge);
            if (idxAttachFolder >= 0 && !cursor.isNull(idxAttachFolder)) record.AttachFolder = cursor.getString(idxAttachFolder);
            if (idxDBName >= 0 && !cursor.isNull(idxDBName)) record.DBName = cursor.getString(idxDBName);

            // Task 54/100: умолчания, когда значение ещё не задано
            if (record.Height == null) record.Height = CalParamRecord.DEFAULT_HEIGHT;
            if (record.Weight == null) record.Weight = CalParamRecord.DEFAULT_WEIGHT;
            if (record.Age == null) record.Age = CalParamRecord.DEFAULT_AGE;
            if (record.AttachFolder == null || record.AttachFolder.trim().isEmpty()) record.AttachFolder = CalParamRecord.DEFAULT_ATTACH_FOLDER;
            if (record.DBName == null || record.DBName.trim().isEmpty()) record.DBName = CalParamRecord.DEFAULT_DB_NAME;
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
        if (record.Vedushii != null) values.put("Vedushii", record.Vedushii);
        if (record.VedushiiID != null) values.put("VedushiiID", record.VedushiiID);
        // StartPage: null = default (Календарь / MainActivity)
        if (record.StartPage != null) values.put("StartPage", record.StartPage);
        // Three configurable quick buttons (values = числовой id записи LIVETYPE)
        if (record.Button1Id != null) values.put("Button1Id", String.valueOf(record.Button1Id));
        if (record.Button2Id != null) values.put("Button2Id", String.valueOf(record.Button2Id));
        if (record.Button3Id != null) values.put("Button3Id", String.valueOf(record.Button3Id));
		if (record.Button4Id != null) values.put("Button4Id", String.valueOf(record.Button4Id));
		if (record.Button5Id != null) values.put("Button5Id", String.valueOf(record.Button5Id));
        // Tasks 54/100: Рост/Вес/Возраст + папка вложений + имя базы
        if (record.Height != null) values.put("Height", record.Height);
        if (record.Weight != null) values.put("Weight", record.Weight);
        if (record.Age != null) values.put("Age", record.Age);
        if (record.AttachFolder != null) values.put("AttachFolder", record.AttachFolder);
        if (record.DBName != null) values.put("DBName", record.DBName);

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

    // Fill-in author from "Ведущий" (CALPARAM) when the record has no author (Task 53)
    private void fillAuthorFromParams(ContactRecord record) {
        if (record == null) return;
        if (record.AuthorID == null || record.AuthorID.isEmpty()) {
            record.AuthorID = AuthorID;
            record.AuthorName = AuthorName;
        }
        if (record.LastUpdatedByID == null || record.LastUpdatedByID.isEmpty()) {
            record.LastUpdatedByID = AuthorID;
            record.LastUpdatedBy = AuthorName;
        }
    }

    // Upsert ContactRecord into CONTACTS table
    public void upsertContact(ContactRecord record) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Task 53: Автор/Обновивший из "Ведущий" (CALPARAM), если ещё не заданы
        fillAuthorFromParams(record);

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

        // Date fields
        if (record.BirthDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            values.put("BirthDate", sdf.format(record.BirthDate));
        }
        if (record.DateReceived != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            values.put("DateReceived", sdf.format(record.DateReceived));
        }
        // Task 44/53: даты создания/обновления хранятся со временем
        if (record.DateCreated != null) values.put("DateCreated", fmtDateTimeString(record.DateCreated));
        if (record.DateModified != null) values.put("DateModified", fmtDateTimeString(record.DateModified));

        // Try to update first (if id exists), if no rows affected then insert
        if (record.id != null) {
            int rowsAffected = db.update("CONTACTS", values, "id=?",
                    new String[]{String.valueOf(record.id)});

            if (rowsAffected == 0) {
                db.insert("CONTACTS", null, values);
                addHistory("Добавление", record, null);
            } else {
                addHistory("Изменение", record, null);
            }
        } else {
            db.insert("CONTACTS", null, values);
            addHistory("Добавление", record, null);
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
                // Reuse the single mapping routine (reads Author/LastUpdated too,
                // and parses dates stored with time - Task 44/53)
                ContactRecord record = cursorToContact(cursor);

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

    // =====================================================================
    // Task 106: тестовые записи СМС (Входящая, Исходящая) с Ведущим из параметров
    // =====================================================================

    private void seedSmsDefaults(SQLiteDatabase db) {
        try {
            ContactRecord[] contacts = getContacts("");
            ContactRecord vedushii = null;
            String vedushiiName = AuthorName;
            String vedushiiId = AuthorID;
            if (vedushiiName == null) {
                CalParamRecord param = getCalParam();
                if (param != null) {
                    vedushiiName = param.Vedushii;
                    vedushiiId = param.VedushiiID;
                }
                if (vedushiiName == null && contacts.length > 0) {
                    vedushii = contacts[0];
                    vedushiiName = (vedushii.Surname != null ? vedushii.Surname + " " : "")
                            + (vedushii.FirstName != null ? vedushii.FirstName : "");
                    vedushiiId = vedushii.EntryID != null ? vedushii.EntryID
                            : (vedushii.id != null ? String.valueOf(vedushii.id) : null);
                }
            }
            if (vedushiiName == null || vedushiiName.trim().isEmpty()) vedushiiName = "Ведущий";

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String now = sdf.format(new Date());

            // Входящая: от Ведущего (FromID заполнен - контакт сопоставлен)
            db.execSQL("INSERT OR IGNORE INTO SMSCALPLAN (UNID, Type, FromID, FromName, ToID, ToName, Subject, Body, Status, DateReceived) VALUES (" +
                    "'" + java.util.UUID.randomUUID() + "', 'Incoming', " +
                    sqlStr(vedushiiId) + ", " + sqlStr(vedushiiName.trim()) + ", '', '', " +
                    "'Тестовая запись', 'Hello world!', 'New', '" + now + "')");

            // Исходящая: к Ведущему (ToID заполнен - контакт сопоставлен)
            db.execSQL("INSERT OR IGNORE INTO SMSCALPLAN (UNID, Type, FromID, FromName, ToID, ToName, Subject, Body, Status, DateReceived) VALUES (" +
                    "'" + java.util.UUID.randomUUID() + "', 'Outgoing', " +
                    "'', '', " + sqlStr(vedushiiId) + ", " + sqlStr(vedushiiName.trim()) + ", " +
                    "'Тестовая запись', 'Hello world!', 'New', '" + now + "')");
        } catch (Exception e) {
            // Non-fatal: СМС - вспомогательные данные
        }
    }

    /** Оборачивает строку в одинарные кавычки для SQL (null -> ''). */
    private String sqlStr(String value) {
        if (value == null) return "''";
        return "'" + value.replace("'", "''") + "'";
    }
}
