package com.karthikmlore.minipasswordmanager;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.haibison.android.lockpattern.LockPatternActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class NewPattern extends AppCompatActivity {

    private Crypter old_crypter, new_crypter;
    private Context context;
    private DBHelper dbHandler;
    private SQLiteDatabase database;
    private String pattern, new_pattern;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = NewPattern.this;
        dbHandler = new DBHelper(context);
        try {
            pattern = getIntent().getExtras().getString("pattern");
        } catch(Exception e) {
            pattern = null;
        }
        Intent intent = new Intent(LockPatternActivity.ACTION_CREATE_PATTERN, null, context, LockPatternActivity.class);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1: {
                if (resultCode == RESULT_OK) {
                    new_pattern = new String(data.getCharArrayExtra(LockPatternActivity.EXTRA_PATTERN));
                    new_crypter = new Crypter(new_pattern);
                    ContentValues push_pattern = new ContentValues();
                    try {
                        push_pattern.put("PATTERN", new_crypter.encrypt(new_pattern));
                        database = dbHandler.openDB(true);
                        if (pattern == null) {
                            try {
                                if (database.insertOrThrow("APP_SETTINGS", null, push_pattern) != 1)
                                    Toast.makeText(context, "Error storing new pattern", Toast.LENGTH_SHORT).show();
                                else {
                                    Toast.makeText(context, "Pattern set", Toast.LENGTH_SHORT).show();
                                    move_to_login();
                                }

                            } catch (SQLiteException e) {
                                Toast.makeText(context, "Error writing to database", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            old_crypter = new Crypter(pattern);
                            try {
                                if (database.update("APP_SETTINGS", push_pattern, "PATTERN = '" + old_crypter.encrypt(pattern) + "'", null) == 1) {
                                    recrypt_records();
                                    Toast.makeText(context, "Pattern changed", Toast.LENGTH_SHORT).show();
                                    move_to_login();
                                }
                            } catch (Exception e) {
                                Toast.makeText(context, "Error setting new pattern", Toast.LENGTH_SHORT).show();
                            }

                        }
                        dbHandler.closeDB();
                    } catch (Exception e) {
                        Toast.makeText(context, "Encryption error", Toast.LENGTH_SHORT).show();
                    }
                } else finish();
                break;
            }

        }
    }

    private void recrypt_records() {
        database = dbHandler.openDB(true);
        ArrayList<List<String>> records = new ArrayList<>();
        String adder[] = new String[6];
        int j;
        try {
            Cursor x = database.query("RECORDS",new String[] {"ID","TITLE","URL","USERNAME","PASSWORD","NOTES"},null,null,null,null,null);
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
                        secUpd.put("TITLE", new_crypter.encrypt(old_crypter.decrypt(adder[1])));
                        secUpd.put("URL", new_crypter.encrypt(old_crypter.decrypt(adder[2])));
                        secUpd.put("USERNAME", new_crypter.encrypt(old_crypter.decrypt(adder[3])));
                        secUpd.put("PASSWORD", new_crypter.encrypt(old_crypter.decrypt(adder[4])));
                        secUpd.put("NOTES", new_crypter.encrypt(old_crypter.decrypt(adder[5])));
                        try {
                            database.update("RECORDS", secUpd, "ID="+Integer.parseInt(adder[0]), null);
                        }catch(SQLiteException e) {
                            Toast.makeText(context, "Error Writing to Database" , Toast.LENGTH_SHORT).show();
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

    private void move_to_login() {
        Intent secureActivity = new Intent(context, Login.class);
        startActivity(secureActivity);
        finish();
    }
}