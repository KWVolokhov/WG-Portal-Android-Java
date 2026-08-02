package com.example.calendar4;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.ObjectOutputStream;
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
        //db.execSQL("DROP TABLE IF EXISTS REQUESTPLAN");
        db.execSQL("DROP TABLE IF EXISTS CLASSIFICATOR");
        db.execSQL(ConstantsSQLDb.CREATE_TABLE_CLASSIFICATOR);
        for(String inesrtCom : ConstantsSQLDb.INSERT_CLASSIFICATOR){
            db.execSQL(inesrtCom);
        }
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database schema changes here
    }
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

}