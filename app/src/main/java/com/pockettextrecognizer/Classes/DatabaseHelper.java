package com.pockettextrecognizer.Classes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "savedText.db";
    public static final String TABLE_NAME = "savedText_table";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "NAME";
    public static final String COL_3 = "TEXT";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + TABLE_NAME +
                "(ID INTEGER PRIMARY KEY AUTOINCREMENT, NAME TEXT, TEXT TEXT)");
}

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String name, String text){

        SQLiteDatabase db = this.getWritableDatabase();

        /**
         * Creating ContentValues instance to store values in order to be processed.
         */
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2,name);
        contentValues.put(COL_3,text);

        /**
        * Inserting contentValues to the table of the database.
        */
        long result = db.insert(TABLE_NAME,null, contentValues);
        if (result == -1)
            return false;
        else
            return true;
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        return result;
    }

    public Integer updateData(String id, String name, String text) {

        SQLiteDatabase db = this.getWritableDatabase();

        /**
         * Creating ContentValues instance to store values in order to be processed.
         */
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1,id);
        contentValues.put(COL_2,name);
        contentValues.put(COL_3,text);

        /**
         * SQL query updates data by ID input
         */
        return db.update(TABLE_NAME, contentValues, "ID = ?", new String[] { id });
    }

    public Integer deleteData(String id){

        SQLiteDatabase db = this.getWritableDatabase();
        /**
        * SQL query deletes data by ID input
        */
        return db.delete(TABLE_NAME, "ID = ?", new String[] { id });
    }

}