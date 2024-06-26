package com.example.fitnessgachaapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "fitnessTrackerDatabase";
    private static final int DATABASE_VERSION = 3;

    // Table Names
    public static final String TABLE_TRACKING = "tracking";
    public static final String TABLE_USER = "user";
    public static final String TABLE_GACHA = "gacha";

    // Tracking Table Columns
    public static final String KEY_TRACKING_ID = "_id";
    public static final String KEY_TRACKING_DATE = "date";
    public static final String KEY_TRACKING_DISTANCE = "distance";
    public static final String KEY_TRACKING_CALORIES = "calories";
    public static final String KEY_TRACKING_DURATION = "duration";

    // User Table Columns
    public static final String KEY_USER_ID = "_id";
    public static final String KEY_USER_TOTAL_CALORIES = "total_calories";

    // Gacha Table Columns
    public static final String KEY_GACHA_ID = "_id";
    public static final String KEY_GACHA_NAME = "name";
    private static final String KEY_GACHA_SPRITE_ID = "sprite_id";
    public static final String KEY_GACHA_DUPE = "dupe";

    // SQL to create table
    private static final String CREATE_TABLE_TRACKING = "CREATE TABLE " +
            TABLE_TRACKING + "(" +
            KEY_TRACKING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            KEY_TRACKING_DATE + " TEXT," +
            KEY_TRACKING_DISTANCE + " REAL," +
            KEY_TRACKING_CALORIES + " REAL," +
            KEY_TRACKING_DURATION + " LONG" +
            ");";

    // SQL to create User table
    private static final String CREATE_TABLE_USER = "CREATE TABLE " +
            TABLE_USER + "(" +
            KEY_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            KEY_USER_TOTAL_CALORIES + " REAL" +
            ");";

    // SQL to create Gacha table
    private static final String CREATE_TABLE_GACHA = "CREATE TABLE " +
            TABLE_GACHA + "(" +
            KEY_GACHA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            KEY_GACHA_NAME + " TEXT," +
            KEY_GACHA_SPRITE_ID + " INTEGER," +
            KEY_GACHA_DUPE + " INTEGER DEFAULT 0 " +
            ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Creating required tables
        db.execSQL(CREATE_TABLE_TRACKING);
        db.execSQL(CREATE_TABLE_USER);
        db.execSQL(CREATE_TABLE_GACHA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRACKING);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GACHA);
        onCreate(db);
    }

    public void addTrackingRecord(TrackingRecord record) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TRACKING_DATE, record.getDate());
        values.put(KEY_TRACKING_DISTANCE, record.getDistance());
        values.put(KEY_TRACKING_CALORIES, record.getCalories());
        values.put(KEY_TRACKING_DURATION, record.getDuration());

        // Inserting Row
        db.insert(TABLE_TRACKING, null, values);
        db.close(); // Closing database connection
    }

    public List<TrackingRecord> getAllTrackingRecords(String sortBy) {
        List<TrackingRecord> trackingRecords = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_TRACKING;
        if ("calories".equals(sortBy)) {
            selectQuery += " ORDER BY " + KEY_TRACKING_CALORIES + " DESC"; // Sort by calories in descending order
        } else {
            // Default sorting by date
            selectQuery += " ORDER BY " + KEY_TRACKING_DATE + " DESC"; // Sort by date in descending order
        }

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String date = cursor.getString(cursor.getColumnIndex(KEY_TRACKING_DATE));
                @SuppressLint("Range") float distance = cursor.getFloat(cursor.getColumnIndex(KEY_TRACKING_DISTANCE));
                @SuppressLint("Range") float calories = cursor.getFloat(cursor.getColumnIndex(KEY_TRACKING_CALORIES));
                @SuppressLint("Range") long duration = cursor.getLong(cursor.getColumnIndex(KEY_TRACKING_DURATION));

                TrackingRecord record = new TrackingRecord(date, distance, calories, duration);
                trackingRecords.add(record);
            } while (cursor.moveToNext());
        }

        // Close cursor and database to free up resources
        cursor.close();
        db.close();

        // Return the list of records
        return trackingRecords;
    }

    public void clearTrackingHistory() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TRACKING, null, null);
        db.close();
    }

    // Method to the user's total calories
    public void updateUserTotalCalories(float caloriesChange) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Initialize variables
        float totalCalories = 0;
        boolean userExists = false;

        // Check if a user record already exists
        Cursor cursor = db.query(TABLE_USER, new String[]{KEY_USER_TOTAL_CALORIES}, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            int caloriesIndex = cursor.getColumnIndex(KEY_USER_TOTAL_CALORIES);
            if (caloriesIndex >= 0) { // Ensure the column index is valid
                totalCalories = cursor.getFloat(caloriesIndex);
                userExists = true;
            }
        }
        cursor.close();

        totalCalories += caloriesChange;
        totalCalories = Math.max(0, totalCalories);
        int roundedTotalCalories = Math.round(totalCalories);

        ContentValues values = new ContentValues();
        values.put(KEY_USER_TOTAL_CALORIES, roundedTotalCalories);

        if (userExists) {
            // Update existing user's calories
            db.update(TABLE_USER, values, null, null);
        } else {
            // Insert new user with calories if no user was found
            db.insert(TABLE_USER, null, values);
        }
        db.close(); // Closing database connection
    }

    // Method to retrieve the total calories of the user
    public float getUserTotalCalories() {
        SQLiteDatabase db = this.getWritableDatabase();
        float totalCalories = 0;

        Cursor cursor = db.query(TABLE_USER, new String[]{KEY_USER_TOTAL_CALORIES}, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            totalCalories = cursor.getFloat(0);
        }

        cursor.close();
        db.close();
        return totalCalories;
    }

    public void addGachaPull(String name, String sprite) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if the gacha item already exists
        Cursor cursor = db.query(TABLE_GACHA, new String[] {KEY_GACHA_ID, KEY_GACHA_DUPE}, KEY_GACHA_NAME + " = ?", new String[]{name}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            // If the item exists, increment the dupe count
            @SuppressLint("Range") int gachaId = cursor.getInt(cursor.getColumnIndex(KEY_GACHA_ID));
            @SuppressLint("Range") int dupeCount = cursor.getInt(cursor.getColumnIndex(KEY_GACHA_DUPE)) + 1;

            ContentValues values = new ContentValues();
            values.put(KEY_GACHA_DUPE, dupeCount);

            db.update(TABLE_GACHA, values, KEY_GACHA_ID + " = ?", new String[]{String.valueOf(gachaId)});
            Log.d("GachaSystem", "Updated item: " + name + " | New dupe count: " + dupeCount);
        } else {
            // If the item doesn't exist, insert a new row
            ContentValues values = new ContentValues();
            values.put(KEY_GACHA_NAME, name);
            values.put(KEY_GACHA_SPRITE_ID, sprite);
            db.insert(TABLE_GACHA, null, values);
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();
    }

    public List<GachaItem> getAllGachaItems() {
        List<GachaItem> gachaItems = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_GACHA, new String[]{KEY_GACHA_ID, KEY_GACHA_NAME, KEY_GACHA_SPRITE_ID, KEY_GACHA_DUPE}, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                int idIndex = cursor.getColumnIndex(KEY_GACHA_ID);
                int nameIndex = cursor.getColumnIndex(KEY_GACHA_NAME);
                int spriteIdIndex = cursor.getColumnIndex(KEY_GACHA_SPRITE_ID);
                int dupeCountIndex = cursor.getColumnIndex(KEY_GACHA_DUPE);

                if (idIndex != -1 && nameIndex != -1 && spriteIdIndex != -1 && dupeCountIndex != -1) {
                    int id = cursor.getInt(idIndex);
                    String name = cursor.getString(nameIndex);
                    int spriteId = cursor.getInt(spriteIdIndex);
                    int dupeCount = cursor.getInt(dupeCountIndex);

                    GachaItem item = new GachaItem(id, name, spriteId, dupeCount);
                    gachaItems.add(item);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return gachaItems;
    }

    public int getUniqueGachaItemCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(DISTINCT " + KEY_GACHA_NAME + ") FROM " + TABLE_GACHA, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }
}