package com.example.hackintosh.lexicon;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by hackintosh on 2/21/17.
 */

public class LexiconDataModel extends SQLiteOpenHelper{

    /* Inner class that defines the table contents */
    public static class LexiconTable implements BaseColumns {
        public static final String TABLE_NAME = "";
        public static final String MESSAGE = "message";
        public static final String TRANSLATION = "translation";
    }

    public static class LexiconCurrentLanguage implements  BaseColumns {
        public static final String TABLE_NAME = "currentLanguage";
        public static final String LANGUAGE_FROM = "languageFrom";
        public static final String LANGUAGE_FROM_ID = "languageFrom_id";
        public static final String LANGUAGE_TO = "languageTo";
        public static final String LANGUAGE_TO_ID = "languageTo_id";
    }

    public static  class NotificationTime implements BaseColumns {
        public static final String TABLE_NAME = "notificationTime";
        public static final String TIME = "time";
    }

    private String LEXICON_TABLE;

    private static final String CURRENT_LANGUAGE_TABLE =
            "CREATE TABLE " + LexiconCurrentLanguage.TABLE_NAME + " (" +
                    LexiconCurrentLanguage._ID + " INTEGER PRIMARY KEY," +
                    LexiconCurrentLanguage.LANGUAGE_FROM + " TEXT," +
                    LexiconCurrentLanguage.LANGUAGE_FROM_ID + " TEXT," +
                    LexiconCurrentLanguage.LANGUAGE_TO + " TEXT," +
                    LexiconCurrentLanguage.LANGUAGE_TO_ID + " TEXT)";

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Lexicon.db";

    public LexiconDataModel(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {}
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
//        db.execSQL(SQL_DELETE_ENTRIES);
//        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void addNewLexiconTable(SQLiteDatabase db,String tableName) {
        LEXICON_TABLE = "CREATE TABLE " + tableName + " (" +
                LexiconTable._ID + " INTEGER PRIMARY KEY," +
                LexiconTable.MESSAGE + " TEXT," +
                LexiconTable.TRANSLATION + " TEXT)";

        db.execSQL(LEXICON_TABLE);
    }

    public void addNewLanguageTable(SQLiteDatabase db) {
        db.execSQL(CURRENT_LANGUAGE_TABLE);
    }

    public void dropTable(SQLiteDatabase db,String tableName) {
        String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + tableName;
        db.execSQL(SQL_DELETE_ENTRIES);
    }

    public void addNewTimeTable(SQLiteDatabase db) {
        String TIME_TABLE = "CREATE TABLE " + NotificationTime.TABLE_NAME + " (" +
                NotificationTime._ID + " INTEGER PRIMARY KEY," +
                NotificationTime.TIME + " TEXT)";
        db.execSQL(TIME_TABLE);
    }
}

