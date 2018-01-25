package com.karthikmlore.minipasswordmanager;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.haibison.android.lockpattern.LockPatternActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Update extends AppCompatActivity {
    private Context context;
    private DBHelper dbHandler;
    private SQLiteDatabase database;
    private String encrypted_password;
    private Crypter old_crypter,new_crypter;
    private String new_pattern;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update);
        context = Update.this;
        dbHandler = new DBHelper(context);
        Button done = (Button) findViewById(R.id.done);
        database = dbHandler.openDB(false);
        try {
            Cursor x = database.query("DEVDATA",new String[] {"MPASS","NIGHT"},null,null,null,null,null);
            if(x.moveToFirst()) {
                encrypted_password = x.getString(0);
            }
            x.close();
        } catch (SQLiteException e) {
        }
        dbHandler.closeDB();
        done.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText old_password = (EditText) findViewById(R.id.old_password);
                String prev_master_pass = old_password.getText().toString();
                old_crypter = new Crypter(prev_master_pass,true);
                try {
                    if(encrypted_password.equals(old_crypter.encrypt(prev_master_pass))) {
                        Intent intent = new Intent(LockPatternActivity.ACTION_CREATE_PATTERN, null, context, LockPatternActivity.class);
                        startActivityForResult(intent, 1);
                    }
                    else {
                        Toast.makeText(context, "Incorrect password", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1: {
                if(resultCode == RESULT_OK) {
                    new_pattern = new String(data.getCharArrayExtra(LockPatternActivity.EXTRA_PATTERN));
                    new_crypter = new Crypter(new_pattern);
                    copy_records_to_new_db();
                    delete_tables();
                    set_new_pattern();
                    Toast.makeText(context, "Update complete" , Toast.LENGTH_SHORT).show();
                    Intent secureActivity = new Intent(context, Login.class);
                    startActivity(secureActivity);
                    finish();
                    break;
                }
            }
        }
    }


    private void copy_records_to_new_db() {
        database = dbHandler.openDB(true);
        database.execSQL("CREATE TABLE RECORDS (ID INTEGER PRIMARY KEY, TITLE VARCHAR(60) NOT NULL, URL VARCHAR(255) NOT NULL, USERNAME VARCHAR(60) NOT NULL, PASSWORD VARCHAR(60) NOT NULL, NOTES TEXT NOT NULL);");
        ArrayList<List<String>> records = new ArrayList<>();
        String adder[] = new String[6];
        int j;
        try {
            Cursor x = database.query("MAINDATA",new String[] {"ID","NAME","URI","USERNAME","PASSWORD","NOTES"},null,null,null,null,null);
            if(x.moveToFirst()) {
                do {
                    records.add(Arrays.asList(x.getString(0), x.getString(1), x.getString(2), x.getString(3), x.getString(4), x.getString(5)));
                } while(x.moveToNext());
                x.close();
                for (List<String> list : records) {
                    j = 0;
                    for (String i : list) {
                        adder[j] = i;
                        j++;
                    }
                    try {
                        ContentValues secUpd = new ContentValues();
                        secUpd.put("ID",Integer.parseInt(adder[0]));
                        secUpd.put("TITLE", new_crypter.encrypt(old_crypter.decrypt(adder[1])));
                        secUpd.put("URL", new_crypter.encrypt(old_crypter.decrypt(adder[2])));
                        secUpd.put("USERNAME", new_crypter.encrypt(old_crypter.decrypt(adder[3])));
                        secUpd.put("PASSWORD", new_crypter.encrypt(old_crypter.decrypt(adder[4])));
                        secUpd.put("NOTES", new_crypter.encrypt(old_crypter.decrypt(adder[5])));
                        try {
                            database.insertOrThrow("RECORDS", null,secUpd);
                        }catch(SQLiteException e) {
                            Toast.makeText(context, "Error copying database" , Toast.LENGTH_SHORT).show();
                        }
                    } catch(Exception e) {
                        Toast.makeText(context, "Error re-encrypting Data" , Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (SQLiteException e) {
            Toast.makeText(context, "Error reading database" , Toast.LENGTH_SHORT).show();
        }
        dbHandler.closeDB();
    }


    private void set_new_pattern() {
        database = dbHandler.openDB(true);
        database.execSQL("CREATE TABLE APP_SETTINGS (PATTERN VARCHAR(255) NOT NULL);");
        ContentValues push_pattern = new ContentValues();
        try {
            push_pattern.put("PATTERN", new_crypter.encrypt(new_pattern));
        } catch (Exception e) {
            Toast.makeText(context, "Encryption error" , Toast.LENGTH_SHORT).show();
        }
        try {
            if(database.insertOrThrow("APP_SETTINGS", null, push_pattern) != 1)
                Toast.makeText(context, "Error storing new pattern" , Toast.LENGTH_SHORT).show();
        }catch(SQLiteException e) {
            Toast.makeText(context, "Error writing to database" , Toast.LENGTH_SHORT).show();
        }
        dbHandler.closeDB();
    }

    private void delete_tables(){
        database = dbHandler.openDB(true);
        database.execSQL("DROP TABLE MAINDATA;");
        database.execSQL("DROP TABLE DEVDATA;");
        dbHandler.closeDB();
    }
}