package com.example.hackintosh.lexicon;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hackintosh on 2/21/17.
 */

public class LexiconDataBase {

    private LexiconDataModel mDbHelper;
    private SQLiteDatabase db;
    private Context context;

    public LexiconDataBase(Context context) {
        mDbHelper = new LexiconDataModel(context);
        db = mDbHelper.getWritableDatabase();
        this.context = context;
    }
    // Gets the data repository in write mode

    public long addElement(String tableName, String message, String translation) {
        if(!checkIfExist(tableName)) {
            mDbHelper.addNewLexiconTable(db,tableName);
        }

        db = mDbHelper.getWritableDatabase();

        //Delete all table
        //db.delete(LexiconData.LexiconTable.TABLE_NAME,null,null);

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(LexiconDataModel.LexiconTable.MESSAGE, message);
        values.put(LexiconDataModel.LexiconTable.TRANSLATION, translation);

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(tableName, null, values);

        showTable(tableName,LexiconDataModel.LexiconTable.MESSAGE,LexiconDataModel.LexiconTable.TRANSLATION);

        return  newRowId;
    }

    public void deleteElement(String tableName, String message) {
        String[] delete_elements = new String[] {message};
        db.delete(tableName, LexiconDataModel.LexiconTable.MESSAGE + " LIKE ?", delete_elements);

    }

    public List<String[]> getTable(String tableName) {
        if(checkIfExist(tableName)) {
            Cursor cursor = db.rawQuery("select * from " + tableName, null);

            List<String[]> items = new ArrayList<String[]>();
            while (cursor.moveToNext()) {
                String[] item = new String[2];
                item[0] = cursor.getString(
                        cursor.getColumnIndexOrThrow(LexiconDataModel.LexiconTable.MESSAGE));
                item[1] = cursor.getString(
                        cursor.getColumnIndexOrThrow(LexiconDataModel.LexiconTable.TRANSLATION));
                items.add(0, item);
            }
            cursor.close();

            Log.d("DataBase", "" + items);

            return items;
        }

        return null;
    }

    public void deleteTable(String tableName) {
        String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + tableName;
        db.execSQL(SQL_DELETE_ENTRIES);
    }

    public boolean checkIfExist(String tableName) {
        Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+ tableName +"'", null);

        if(cursor.getCount() <= 0) { return false; }
        else { return true; }
    }

//    public boolean checkIfEmpty(String tableName) {
//        Cursor  cursor = db.rawQuery("select * from " + tableName,null);
//        cursor.moveToFirst();
//        int empty = cursor.getInt(0);
//        cursor.close();
//        if(empty <= 0) { return true; }// IS EMPTY
//        else { return false; } // IS POPULATED
//    }

    public void setCurrentLanguage(String languageFrom, String languageFrom_id,
                                   String languageTo, String languageTo_id) {

        //db.delete("currentLanguage",null,null);
        //mDbHelper.dropTable(db,LexiconDataModel.LexiconCurrentLanguage.TABLE_NAME);
        ContentValues values = new ContentValues();
        values.put(LexiconDataModel.LexiconCurrentLanguage.LANGUAGE_FROM, languageFrom);
        values.put(LexiconDataModel.LexiconCurrentLanguage.LANGUAGE_FROM_ID, languageFrom_id);
        values.put(LexiconDataModel.LexiconCurrentLanguage.LANGUAGE_TO, languageTo);
        values.put(LexiconDataModel.LexiconCurrentLanguage.LANGUAGE_TO_ID, languageTo_id);

        if(!checkIfExist(LexiconDataModel.LexiconCurrentLanguage.TABLE_NAME)) {
            // Insert the new row, returning the primary key value of the new row
            db = mDbHelper.getWritableDatabase();
            mDbHelper.addNewLanguageTable(db);
            long newRowId = db.insert(LexiconDataModel.LexiconCurrentLanguage.TABLE_NAME, null, values);
            //showTable(LexiconDataModel.LexiconCurrentLanguage.TABLE_NAME,"languageFrom","languageTo");
        }
        else {
            db = mDbHelper.getReadableDatabase();

            // Which row to update, based on the title
            String selection = LexiconDataModel.LexiconCurrentLanguage._ID + " LIKE ?";
            String[] selectionArgs = {"1"};

            int count = db.update(
                    "currentLanguage",
                    values,
                    selection,
                    selectionArgs);
            //showTable(LexiconDataModel.LexiconCurrentLanguage.TABLE_NAME,"languageFrom","languageTo");
        }

    }

    public void getCurrentLanguages() {
        db = mDbHelper.getReadableDatabase();
        if(checkIfExist(LexiconDataModel.LexiconCurrentLanguage.TABLE_NAME)) {
            Cursor cursor = db.rawQuery("select * from currentLanguage", null);
            MainActivity mainActivity = (MainActivity) context;
            while (cursor.moveToNext()) {
                mainActivity.setTranslateTo(cursor.getString(
                        cursor.getColumnIndexOrThrow("languageTo")));
                mainActivity.setTranslateFrom(cursor.getString(
                        cursor.getColumnIndexOrThrow("languageFrom")));
                mainActivity.setTranslateTo_id(cursor.getString(
                        cursor.getColumnIndexOrThrow("languageTo_id")));
            }
            cursor.close();
        }
    }

    public void showTable(String tableName, String tableColumn1, String tableColumn2) {
        if(checkIfExist(tableName)) {
            Cursor cursor = db.rawQuery("select * from " + tableName, null);
            Log.d("Show table","table exist");
            List<String[]> items = new ArrayList<String[]>();
            while (cursor.moveToNext()) {
                String[] item = new String[2];
                item[0] = cursor.getString(
                        cursor.getColumnIndexOrThrow(tableColumn1));
                item[1] = cursor.getString(
                        cursor.getColumnIndexOrThrow(tableColumn2));
                items.add(0, item);
            }
            cursor.close();

            for(String[] item: items) {
                Log.d("DataBase item_0", "" + item[0]);
                Log.d("DataBase item_1", "" + item[1]);
            }

        }
        else {
            Log.d("Show Table", "table doesn't exit");
        }
    }

    public List<String> getDBlanguages() {
        List<String> languages = new ArrayList<String>();
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        if (cursor.moveToFirst()) {
            while ( !cursor.isAfterLast() ) {
                languages.add(cursor.getString(0));
                cursor.moveToNext();
            }
        }
        List<String> lang = new ArrayList<String>();
        for(String language: languages) {
            //Log.d("DataBase Languages", "" + language);
            if(language.length() == 2) {
                lang.add(language);
            }
        }
        languages.clear();
        languages = lang;
        for(String lang2: languages) {
            Log.d("DataBase Languages", "" + lang2);
        }
        return languages;
    }

    public void setNotificationTIme(int time) {
        ContentValues values = new ContentValues();
        values.put(LexiconDataModel.NotificationTime.TIME, time);

        if (!checkIfExist(LexiconDataModel.NotificationTime.TABLE_NAME)) {
            db = mDbHelper.getWritableDatabase();
            mDbHelper.addNewTimeTable(db);
            long newRowId = db.insert(LexiconDataModel.NotificationTime.TABLE_NAME, null, values);
        } else {
            db = mDbHelper.getReadableDatabase();
            String selection = LexiconDataModel.NotificationTime._ID + " LIKE ?";
            String[] selectionArgs = {"1"};

            int count = db.update(
                    "notificationTime",
                    values,
                    selection,
                    selectionArgs);
        }
    }

    public int getNotificationTime() {
        String tableName = LexiconDataModel.NotificationTime.TABLE_NAME;
        if (checkIfExist(tableName)) {
            Cursor cursor = db.rawQuery("select * from " + tableName, null);
            Log.d("Show table", "table exist");
            List<Integer> items = new ArrayList<Integer>();
            while (cursor.moveToNext()) {
                String item = cursor.getString(cursor.getColumnIndexOrThrow(LexiconDataModel.NotificationTime.TIME));
                items.add(0, Integer.parseInt(item));
            }
            cursor.close();
            return items.get(0);

        } else {
            Log.d("Show Table", "table doesn't exit");
            return 0;
        }
    }

//    db = mDbHelper.getReadableDatabase();
//
//    // Define a projection that specifies which columns from the database
//    // you will actually use after this query.
//    String[] projection = {
//            LexiconDataModel.LexiconTable._ID,
//            LexiconDataModel.LexiconTable.MESSAGE,
//            LexiconDataModel.LexiconTable.TRANSLATION
//    };
//
//    // Filter results WHERE "title" = 'My Title'
//    String selection = LexiconDataModel.LexiconTable.MESSAGE + " = ?";
//    String[] selectionArgs = { "show this message" };
//
//    // How you want the results sorted in the resulting Cursor
//    String sortOrder =
//            LexiconDataModel.LexiconTable.TRANSLATION + " DESC";
//
//    Cursor cursor = db.query(
//            LexiconDataModel.LexiconTable.TABLE_NAME,       // The table to query
//            projection,                               // The columns to return
//            selection,                                // The columns for the WHERE clause
//            selectionArgs,                            // The values for the WHERE clause
//            null,                                     // don't group the rows
//            null,                                     // don't filter by row groups
//            sortOrder                                 // The sort order
//    );
//
//    List itemIds = new ArrayList<>();
//    while(cursor.moveToNext()) {
//        String itemId = cursor.getString(
//                cursor.getColumnIndexOrThrow(LexiconDataModel.LexiconTable.MESSAGE));
//        itemIds.add(itemId);
//    }
//    cursor.close();
//
//    Log.d("DataBase","" + itemIds);
}
