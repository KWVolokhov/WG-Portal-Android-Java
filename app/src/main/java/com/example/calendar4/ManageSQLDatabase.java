package com.example.calendar4;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton-доступ к локальной БД SQLite (WGPlanDatabase.db): создание/миграции схемы
 * (все DDL - только в ConstantsSQLDb) и фасад к пер-табличным менеджерам (Task 139):
 * CalPlanSQLManage, CalParamSQLManage, ContactsSQLManage, HolidaysSQLManage, SmsDefaultsSeeder,
 * а также HistorySQLManage / NoteRememSQLManage / HealthSQLManage / LivetypeSQLManage / SmsSQLManage.
 * Экраны продолжают работать через методы этого класса - вызовы не менялись.
 */
public class ManageSQLDatabase extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 10;
    public static final String DATABASE_NAME = "WGPlanDatabase.db";

    public static String AuthorName = null;
    public static String AuthorID = null;
    private static ManageSQLDatabase instance = null;

    protected ManageSQLDatabase(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
        CalParamRecord param = getCalParam();
        if (param != null) {
            AuthorName = param.Vedushii;
            AuthorID = param.VedushiiID;
        }
    }

    /** Единственный экземпляр; getApplicationContext() предотвращает утечки памяти. */
    public static synchronized ManageSQLDatabase getInstance(Context context) {
        if (instance == null) instance = new ManageSQLDatabase(context.getApplicationContext());
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createAllTables(db);
        SmsDefaultsSeeder.seed(this, db);
    }

    // Создание всех таблиц (DDL - только в ConstantsSQLDb) + справочники
    private void createAllTables(SQLiteDatabase db) {
        db.execSQL(ConstantsSQLDb.CREATE_TABLE_CALPLAN);
        // Tables separated from CALPLAN (Form=History -> HISTORY, Form=Note/Remember -> NOTEPLAN,
        // Form=HealthEat/HealthDrink/HealthSport -> HEALTHPLAN)
        db.execSQL(ConstantsSQLDb.CREATE_TABLE_HISTORY);
        db.execSQL(ConstantsSQLDb.CREATE_TABLE_NOTEPLAN);
        db.execSQL(ConstantsSQLDb.CREATE_TABLE_HEALTHPLAN);
        // LIVETYPE reference table (Типы жизнедеятельности)
        db.execSQL(ConstantsSQLDb.CREATE_TABLE_LIVETYPE);
        insertLivetypeDefaults(db);
        db.execSQL(ConstantsSQLDb.CREATE_TABLE_CLASSIFICATOR);
        for (String insertCom : ConstantsSQLDb.INSERT_CLASSIFICATOR) {
            db.execSQL(insertCom);
        }
        db.execSQL(ConstantsSQLDb.CREATE_TABLE_CALPARAM);
        db.execSQL(ConstantsSQLDb.CREATE_TABLE_HOLIDAYS);
        db.execSQL(ConstantsSQLDb.CREATE_TABLE_CONTACTS);
        // Task 106: таблица СМС (та же локальная БД)
        db.execSQL(ConstantsSQLDb.CREATE_TABLE_SMSCALPLAN);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        upgradeToV7(db, oldVersion);
        upgradeToV10(db, oldVersion);
        safetyNetColumns(db);
    }

    // Миграции версий 2..7: CALPARAM, выделение HISTORY/NOTEPLAN/HEALTHPLAN, LIVETYPE
    private void upgradeToV7(SQLiteDatabase db, int oldVersion) {
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
        if (oldVersion < 3) splitCalplanByForm(db);
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
        if (oldVersion < 5) upgradeV5(db);
        // Version 6: Button4Id / Button5Id quick buttons (5 buttons total).
        // Re-runs for every database older than 6; addColumnIfMissing is safe.
        if (oldVersion < 6) upgradeV6(db);
        // Version 7 (Tasks 40-42): LIVETYPE - Form/StepCounter, HEALTHPLAN - шаги/еда/напитки
        if (oldVersion < 7) upgradeV7(db);
    }

    // Version 3: перенос строк CALPLAN в новые таблицы и очистка CALPLAN
    private void splitCalplanByForm(SQLiteDatabase db) {
        try {
            db.execSQL(ConstantsSQLDb.CREATE_TABLE_HISTORY);
            db.execSQL(ConstantsSQLDb.CREATE_TABLE_NOTEPLAN);
            db.execSQL(ConstantsSQLDb.CREATE_TABLE_HEALTHPLAN);
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

    // Version 5: иконки LIVETYPE + Button1Id..Button3Id
    private void upgradeV5(SQLiteDatabase db) {
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

    // Version 6: пять быстрых кнопок CALPARAM
    private void upgradeV6(SQLiteDatabase db) {
        addColumnIfMissing(db, "CALPARAM", "Button1Id", "TEXT");
        addColumnIfMissing(db, "CALPARAM", "Button2Id", "TEXT");
        addColumnIfMissing(db, "CALPARAM", "Button3Id", "TEXT");
        addColumnIfMissing(db, "CALPARAM", "Button4Id", "TEXT");
        addColumnIfMissing(db, "CALPARAM", "Button5Id", "TEXT");
    }

    // Version 7 (Tasks 40-42): Form/StepCounter в LIVETYPE, шаги/питание в HEALTHPLAN
    private void upgradeV7(SQLiteDatabase db) {
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
            db.execSQL("UPDATE LIVETYPE SET Form=UNID WHERE UNID IN " +
                    "('HealthSport','HealthEat','HealthDrink','HealthStress','HealthJoy') AND (Form IS NULL OR Form='')");
            for (String updCom : ConstantsSQLDb.UPDATE_LIVETYPE_DEFAULTS) {
                db.execSQL(updCom);
            }
        } catch (Exception e) {
            // Non-fatal
        }
    }

    // Миграции версий 8..10: авторы CONTACTS, габариты CALPARAM, SMSCALPLAN
    private void upgradeToV10(SQLiteDatabase db, int oldVersion) {
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
                SmsDefaultsSeeder.seed(this, db);
            } catch (Exception e) {
                // Non-fatal
            }
        }
    }

    // Safety net (Task 34): гарантируем наличие всех колонок, какие нужны приложению
    // (старые сборки могли иметь завышенную версию БД и пропустить классические миграции).
    private void safetyNetColumns(SQLiteDatabase db) {
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
            SmsDefaultsSeeder.seed(this, db);
        } catch (Exception e) {
            // Non-fatal
        }
        try {
            for (String updCom : ConstantsSQLDb.UPDATE_LIVETYPE_ICONS) {
                db.execSQL(updCom);
            }
            try {
                db.execSQL("UPDATE LIVETYPE SET Form=UNID WHERE UNID IN " +
                        "('HealthSport','HealthEat','HealthDrink','HealthStress','HealthJoy') AND (Form IS NULL OR Form='')");
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

    /** Заполнение LIVETYPE предустановками (INSERT OR IGNORE - только недостающие UNID). */
    private void insertLivetypeDefaults(SQLiteDatabase db) {
        try {
            for (String insertCom : ConstantsSQLDb.INSERT_LIVETYPE) {
                db.execSQL(insertCom.replaceFirst("(?i)^INSERT INTO", "INSERT OR IGNORE INTO"));
            }
        } catch (Exception e) {
            // Non-fatal: seeding failure must not break the app
        }
    }

    /** Тестовая функция для проверки (произвольный SELECT в Map строк). */
    public Map<Integer, Object> execSelectArrMap(String sqlSelect) throws Exception {
        Map<Integer, Object> retF = new HashMap<Integer, Object>();
        Integer recCounter, colCounter;
        Cursor retC = getReadableDatabase().rawQuery(sqlSelect, null);
        if (retC.getCount() > 0) {
            retC.moveToFirst();
            for (recCounter = 0; recCounter < retC.getCount(); recCounter++) {
                Map<String, String> record = new HashMap<String, String>();
                String colNames[] = retC.getColumnNames();
                for (colCounter = 0; colCounter < colNames.length; colCounter++) {
                    record.put(colNames[colCounter], retC.getString(retC.getColumnIndexOrThrow(colNames[colCounter])));
                }
                retF.put(recCounter, record);
                retC.moveToNext();
            }
        }
        return retF;
    }

    // ===================== Фасад CALPLAN (CalPlanSQLManage) =====================

    // Upsert записи: по Form выбирается своя таблица (History/Note/Remember/Health*/CALPLAN)
    public void upsertCalPlan(calPlanRecord record) {
        if (record == null) return;
        if ("History".equals(record.Form)) {
            new HistorySQLManage(getWritableDatabase()).upsertHistory(record);
            return;
        }
        if ("Note".equals(record.Form) || "Remember".equals(record.Form)) {
            new NoteRememSQLManage(getWritableDatabase()).upsertNote(record);
            return;
        }
        if ("HealthEat".equals(record.Form) || "HealthDrink".equals(record.Form) || "HealthSport".equals(record.Form)) {
            new HealthSQLManage(getWritableDatabase()).upsertHealth(record);
            return;
        }
        new CalPlanSQLManage(getWritableDatabase()).upsert(record);
    }

    /** Delete a CALPLAN record by its id. */
    public void deleteCalPlan(Integer id) {
        new CalPlanSQLManage(getWritableDatabase()).delete(id);
    }

    /** Delete a record by its Form: HISTORY / NOTEPLAN / HEALTHPLAN / CALPLAN. */
    public void deleteCalPlanRecord(calPlanRecord record) {
        if (record == null || record.id == null) return;
        if ("History".equals(record.Form)) {
            new HistorySQLManage(getWritableDatabase()).deleteHistory(record.id);
            return;
        }
        if ("Note".equals(record.Form) || "Remember".equals(record.Form)) {
            new NoteRememSQLManage(getWritableDatabase()).deleteNote(record.id);
            return;
        }
        if ("HealthEat".equals(record.Form) || "HealthDrink".equals(record.Form) || "HealthSport".equals(record.Form)) {
            new HealthSQLManage(getWritableDatabase()).deleteHealth(record.id);
            return;
        }
        deleteCalPlan(record.id);
    }

    /** Записи CALPLAN, действующие в указанный день. */
    public calPlanRecord[] getCalPlan(Date date) {
        return new CalPlanSQLManage(getReadableDatabase()).getByDate(date);
    }

    /** Все записи CALPLAN заданной Form. */
    public calPlanRecord[] getCalPlanByForm(String form) {
        return new CalPlanSQLManage(getReadableDatabase()).getByForm(form);
    }

    /** "Проекты\Все" - Project/Task/Request, отсортировано по StartDate. */
    public calPlanRecord[] getProjectsTasks(String filter) {
        return new CalPlanSQLManage(getReadableDatabase()).getProjectsTasks(filter);
    }

    /** "Проекты\Все"/"Проекты\Рабочие" (workOnly - только Inwork/Intest). */
    public calPlanRecord[] getProjectsTasks(String filter, boolean workOnly) {
        return new CalPlanSQLManage(getReadableDatabase()).getProjectsTasks(filter, workOnly);
    }

    /** Задачи проекта (Задача.RequestUNID = UNID проекта). */
    public calPlanRecord[] getTasksByProject(String projectUNID) {
        return new CalPlanSQLManage(getReadableDatabase()).getTasksByProject(projectUNID);
    }

    /** Проекты, привязанные к заявке (Проект.RequestName = название заявки). */
    public calPlanRecord[] getProjectsByRequestName(String requestName) {
        return new CalPlanSQLManage(getReadableDatabase()).getProjectsByRequestName(requestName);
    }

    // Записи HISTORY за конкретный день (экран HistoryActivity)
    public calPlanRecord[] getCalPlanHistory(Date date) {
        return new HistorySQLManage(getWritableDatabase()).getHistoryByDate(date);
    }

    // Дневной список MainActivity: CALPLAN + NOTEPLAN (History/Health не показываются)
    public calPlanRecord[] getDayRecords(Date date) {
        ArrayList<calPlanRecord> list = new ArrayList<>();
        calPlanRecord[] cal = getCalPlan(date);
        if (cal != null) list.addAll(Arrays.asList(cal));
        calPlanRecord[] notes = new NoteRememSQLManage(getWritableDatabase()).getNotesByDate(date);
        if (notes != null) list.addAll(Arrays.asList(notes));
        return list.toArray(new calPlanRecord[0]);
    }

    // ===================== Фасад CALPARAM (CalParamSQLManage) =====================

    /** Параметры приложения (1 запись CALPARAM). */
    public CalParamRecord getCalParam() {
        return new CalParamSQLManage(getReadableDatabase()).get();
    }

    /** Сохранение параметров приложения. */
    public void upsertCalParam(CalParamRecord record) {
        new CalParamSQLManage(getWritableDatabase()).upsert(record);
    }

    // ===================== Фасад CONTACTS (ContactsSQLManage) =====================

    /** Удалить контакт (со снимком в HISTORY). */
    public void deleteContact(Integer id) {
        new ContactsSQLManage(getWritableDatabase()).deleteContact(id);
    }

    /** Контакт по id. */
    public ContactRecord getContactById(Integer id) {
        return new ContactsSQLManage(getReadableDatabase()).getContactById(id);
    }

    /** Контакт по первым 10 цифрам телефона. */
    public ContactRecord getContactByPhone(String phoneDigits) {
        return new ContactsSQLManage(getReadableDatabase()).getContactByPhone(phoneDigits);
    }

    /** Контакт по Фамилия+Имя. */
    public ContactRecord getContactBySurnameFirstName(String surname, String firstName) {
        return new ContactsSQLManage(getReadableDatabase()).getContactBySurnameFirstName(surname, firstName);
    }

    /** Сохранить контакт (+ запись в HISTORY). */
    public void upsertContact(ContactRecord record) {
        new ContactsSQLManage(getWritableDatabase()).upsertContact(record);
    }

    /** Контакты с фильтром (3+ символа). */
    public ContactRecord[] getContacts(String filter) {
        return new ContactsSQLManage(getReadableDatabase()).getContacts(filter);
    }

    // ===================== Фасад HOLIDAYS (HolidaysSQLManage) =====================

    /** Праздники страны за год. */
    public holidayRecord[] getHolidays(String countryCode, int year) {
        return new HolidaysSQLManage(getReadableDatabase()).getHolidays(countryCode, year);
    }

    /** Сохранение праздников года. */
    public void upsertHolidays(String countryCode, int year, holidayRecord[] holidays) {
        new HolidaysSQLManage(getWritableDatabase()).upsertHolidays(countryCode, year, holidays);
    }

    /** Нужна ли догрузка праздников (нет записей с начала месяца). */
    public boolean needsHolidayUpdate(String countryCode, int year, int month) {
        return new HolidaysSQLManage(getReadableDatabase()).needsHolidayUpdate(countryCode, year, month);
    }
}
