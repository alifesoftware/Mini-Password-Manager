package com.karthikmlore.minipasswordmanager;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;


public class DBHelper extends SQLiteOpenHelper {

    private Context context;
    private SQLiteDatabase database;

    public DBHelper(Context context){
        super(context,"Araine",null,1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE TABLE APP_SETTINGS (PATTERN VARCHAR(255) NOT NULL);");
            db.execSQL("CREATE TABLE RECORDS (ID INTEGER PRIMARY KEY, TITLE VARCHAR(60) NOT NULL, URL VARCHAR(255) NOT NULL, USERNAME VARCHAR(60) NOT NULL, PASSWORD VARCHAR(60) NOT NULL, NOTES TEXT NOT NULL);");
        } catch(SQLException e) {
            Toast.makeText(context, "Error Setting Up Database", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
    }

    public SQLiteDatabase openDB(boolean write) {
        try {
            if (write)
                database = getWritableDatabase();
            else
                database = getReadableDatabase();
        } catch (SQLiteException e) {
            Toast.makeText(context, "Error accessing database", Toast.LENGTH_SHORT).show();
        }
        return database;
    }

    public void closeDB() {
        close();
    }


}