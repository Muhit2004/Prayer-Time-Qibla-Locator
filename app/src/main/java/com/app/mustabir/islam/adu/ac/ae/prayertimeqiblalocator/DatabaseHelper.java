package com.app.mustabir.islam.adu.ac.ae.prayertimeqiblalocator;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import com.app.mustabir.islam.adu.ac.ae.prayertimeqiblalocator.models.TasbihCounter;
import com.app.mustabir.islam.adu.ac.ae.prayertimeqiblalocator.models.UserPreference;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    //Database name and version
    private static final String DATABASE_NAME = "prayer_app.db";
    private static final int DATABASE_VERSION = 3;

    // ── Table: user_preferences ──────────────────────────────────
    public static final String TABLE_PREFS       = "user_preferences";
    public static final String COL_PREF_ID       = "id";
    public static final String COL_COUNTRY       = "country";
    public static final String COL_CITY          = "city";
    public static final String COL_CALC_METHOD   = "calculation_method";
    public static final String COL_TASBIH_TARGET = "tasbih_target";
    public static final String COL_THEME         = "theme";
    public static final String COL_LAT           = "latitude";
    public static final String COL_LNG           = "longitude";

    // ── Table: tasbih_counter ────────────────────────────────────
    public static final String TABLE_TASBIH      = "tasbih_counter";
    public static final String COL_TASBIH_ID     = "id";
    public static final String COL_DHIKR_NAME    = "dhikr_name";
    public static final String COL_CURRENT_COUNT = "current_count";
    public static final String COL_TARGET_COUNT  = "target_count";

    // ── Table: saved_prayer_times ────────────────────────────────
    public static final String TABLE_PRAYER      = "saved_prayer_times";
    public static final String COL_PRAYER_ID     = "id";
    public static final String COL_DATE          = "prayer_date";
    public static final String COL_FAJR          = "fajr";
    public static final String COL_DHUHR         = "dhuhr";
    public static final String COL_ASR           = "asr";
    public static final String COL_MAGHRIB       = "maghrib";
    public static final String COL_ISHA          = "isha";
    public static final String COL_PRAYER_CITY   = "city";

    //  ── SQL to create user_preferences table ───────────────────────────────

    private static final String CREATE_PREFS =
            "CREATE TABLE " + TABLE_PREFS + " (" +
                    COL_PREF_ID       + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_COUNTRY       + " TEXT DEFAULT 'United Arab Emirates', " +
                    COL_CITY          + " TEXT, " +
                    COL_CALC_METHOD   + " TEXT, " +
                    COL_TASBIH_TARGET + " INTEGER DEFAULT 33, " +
                    COL_THEME         + " TEXT DEFAULT 'Light', " +
                    COL_LAT           + " REAL DEFAULT 24.4539, " +
                    COL_LNG           + " REAL DEFAULT 54.3773);";

    private static final String CREATE_TASBIH =
            "CREATE TABLE " + TABLE_TASBIH + " (" +
                    COL_TASBIH_ID     + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_DHIKR_NAME    + " TEXT, " +
                    COL_CURRENT_COUNT + " INTEGER DEFAULT 0); " ;

    private static final String CREATE_PRAYER =
            "CREATE TABLE " + TABLE_PRAYER + " (" +
                    COL_PRAYER_ID   + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_DATE        + " TEXT, " +
                    COL_FAJR        + " TEXT, " +
                    COL_DHUHR       + " TEXT, " +
                    COL_ASR         + " TEXT, " +
                    COL_MAGHRIB     + " TEXT, " +
                    COL_ISHA        + " TEXT, " +
                    COL_PRAYER_CITY + " TEXT);";



    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PREFS);
        db.execSQL(CREATE_TASBIH);
        db.execSQL(CREATE_PRAYER);

        // Default preferences — Abu Dhabi
        db.execSQL("INSERT INTO " + TABLE_PREFS +
                " (country,city, calculation_method,  tasbih_target, theme, latitude, longitude) " +
                "VALUES ('United Arab Emirates','Abu Dhabi', 'Muslim World League', 33, 'Light', 24.4539, 54.3773)");


        db.execSQL("INSERT INTO " + TABLE_TASBIH + " (dhikr_name, current_count) VALUES ('SubhanAllah', 0)");
        db.execSQL("INSERT INTO " + TABLE_TASBIH + " (dhikr_name, current_count) VALUES ('Alhamdulillah', 0)");
        db.execSQL("INSERT INTO " + TABLE_TASBIH + " (dhikr_name, current_count) VALUES ('Allahu Akbar', 0)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PREFS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASBIH);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRAYER);
        onCreate(db);
    }

    // ── User Preferences ─────────────────────────────────────────

    public UserPreference getPreferences() {
    SQLiteDatabase DB = this.getReadableDatabase();

    Cursor cursor = DB.rawQuery("SELECT * FROM " + TABLE_PREFS, null);

    UserPreference pref = new UserPreference();

    if(cursor.moveToFirst()){
        pref.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_PREF_ID)));
        pref.setCountry(cursor.getString(cursor.getColumnIndexOrThrow(COL_COUNTRY)));
        pref.setCity(cursor.getString(cursor.getColumnIndexOrThrow(COL_CITY)));
        pref.setCalculationMethod(cursor.getString(cursor.getColumnIndexOrThrow(COL_CALC_METHOD)));
        pref.setTasbihTarget(cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASBIH_TARGET)));
        pref.setTheme(cursor.getString(cursor.getColumnIndexOrThrow(COL_THEME)));
        pref.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LAT)));
        pref.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LNG)));
    }
    cursor.close();
    DB.close();
    return pref;

    }

    public void updatePreferences(UserPreference pref) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Convert the boolean back to an integer (1 or 0) for SQLite

        String sql = "UPDATE " + TABLE_PREFS + " SET "
                + COL_COUNTRY + " = '" + pref.getCountry() + "', "
                + COL_CITY + " = '" + pref.getCity() + "', "
                + COL_CALC_METHOD + " = '" + pref.getCalculationMethod() + "', "
                + COL_TASBIH_TARGET + " = " + pref.getTasbihTarget() + ", "
                + COL_THEME + " = '" + pref.getTheme() + "', "
                + COL_LAT + " = " + pref.getLatitude() + ", "
                + COL_LNG + " = " + pref.getLongitude()
                + " WHERE " + COL_PREF_ID + " = 1";

        db.execSQL(sql);
        db.close();
    }

    public List<TasbihCounter> getAllTasbih() {
        List<TasbihCounter> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TASBIH, null);

        if (cursor.moveToFirst()) {
            do {
                TasbihCounter t = new TasbihCounter();

                t.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASBIH_ID)));
                t.setDhikrName(cursor.getString(cursor.getColumnIndexOrThrow(COL_DHIKR_NAME)));
                t.setCurrentCount(cursor.getInt(cursor.getColumnIndexOrThrow(COL_CURRENT_COUNT)));

                list.add(t);
            } while (cursor.moveToNext()); // Loops down row-by-row
        }

        cursor.close();
        db.close();
        return list;
    }

    public void updateTasbihCount(int id, int count) {
        SQLiteDatabase db = this.getWritableDatabase();

        String sql = "UPDATE " + TABLE_TASBIH + " SET "
                + COL_CURRENT_COUNT + " = " + count
                + " WHERE " + COL_TASBIH_ID + " = " + id;

        db.execSQL(sql);
        db.close();
    }

    public void resetTasbihCount(int id) {
        updateTasbihCount(id, 0);
    }

    public void savePrayerTimes(String date, String fajr, String dhuhr, String asr,
                                String maghrib, String isha, String city) {
        SQLiteDatabase db = this.getWritableDatabase();

        // 1. Delete any existing record for this exact date to clear out duplicates
        String deleteSql = "DELETE FROM " + TABLE_PRAYER + " WHERE " + COL_DATE + " = '" + date + "'";
        db.execSQL(deleteSql);

        // 2. Insert the fresh timetable row cleanly using a raw SQL INSERT string
        String insertSql = "INSERT INTO " + TABLE_PRAYER + " ("
                + COL_DATE + ", " + COL_FAJR + ", " + COL_DHUHR + ", "
                + COL_ASR + ", " + COL_MAGHRIB + ", " + COL_ISHA + ", " + COL_PRAYER_CITY + ") "
                + "VALUES ('" + date + "', '" + fajr + "', '" + dhuhr + "', '"
                + asr + "', '" + maghrib + "', '" + isha + "', '" + city + "')";

        db.execSQL(insertSql);
        db.close();
    }

    // ── Read Prayer Times ───────────────────────────────────────
    public Cursor getPrayerTimesByDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();

        String sql = "SELECT * FROM " + TABLE_PRAYER + " WHERE " + COL_DATE + " = '" + date + "'";

        // Execute the query and return the Cursor pointer to the Fragment
        return db.rawQuery(sql, null);
    }

    public void updateTasbihName(int id, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();

        String sql = "UPDATE " + TABLE_TASBIH + " SET "
                + COL_DHIKR_NAME + " = '" + newName + "' "
                + "WHERE " + COL_TASBIH_ID + " = " + id;

        db.execSQL(sql);
        db.close();
    }



}
