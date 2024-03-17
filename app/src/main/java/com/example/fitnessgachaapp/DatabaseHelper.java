package com.example.fitnessgachaapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "fitnessTrackerDatabase";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    public static final String TABLE_TRACKING = "tracking";

    // Tracking Table Columns
    public static final String KEY_TRACKING_ID = "_id";
    public static final String KEY_TRACKING_DATE = "date";
    public static final String KEY_TRACKING_DISTANCE = "distance";
    public static final String KEY_TRACKING_CALORIES = "calories";

    // SQL to create table
    private static final String CREATE_TABLE_TRACKING = "CREATE TABLE " +
            TABLE_TRACKING + "(" +
            KEY_TRACKING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            KEY_TRACKING_DATE + " TEXT," +
            KEY_TRACKING_DISTANCE + " REAL," +
            KEY_TRACKING_CALORIES + " REAL" +
            ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Creating required tables
        db.execSQL(CREATE_TABLE_TRACKING);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRACKING);
        onCreate(db);
    }

    public void addTrackingRecord(TrackingRecord record) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TRACKING_DATE, record.getDate());
        values.put(KEY_TRACKING_DISTANCE, record.getDistance());
        values.put(KEY_TRACKING_CALORIES, record.getCalories());

        // Inserting Row
        db.insert(TABLE_TRACKING, null, values);
        db.close(); // Closing database connection
    }

}