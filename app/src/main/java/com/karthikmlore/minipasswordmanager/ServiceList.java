package com.karthikmlore.minipasswordmanager;

import android.Manifest;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.annotation.SuppressLint;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

@SuppressLint("NewApi")
public class ServiceList extends AppCompatActivity implements SearchView.OnQueryTextListener, SearchView.OnCloseListener{
	private static List<Records> records = new ArrayList<>();
    private DBHelper dbHandler;
    private Crypter crypter;
    private Context context;
    private LayoutInflater inflater;
    private ServiceListAdapter adapter;
    private RelativeLayout root;
    private String pattern;
    private SearchView search;
    private boolean serachVisible = false;
    private static final int PERM_GRANT=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        pattern = bundle.getString("Pattern");
        setContentView(R.layout.services_list);
		
		context = ServiceList.this;
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/RobotoCondensed-Regular.ttf");
        inflater =  (LayoutInflater)(context).getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        root = (RelativeLayout) findViewById(R.id.servicesListView);
        ExpandableListView servicelist = (ExpandableListView) findViewById(R.id.servicesList);
		
        FloatingActionButton add_service = (FloatingActionButton) findViewById(R.id.add_service);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        search = (SearchView) findViewById(R.id.searchService);
        search.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        search.setIconifiedByDefault(false);
        search.setQuery("", false);
        search.setOnQueryTextListener(this);
        search.setOnCloseListener(this);
        servicelist.requestFocus();
        root.requestFocus();
		
		dbHandler = new DBHelper(context);
        crypter = new Crypter(pattern);
        loadData();
        checkNoRecord();
        adapter = new ServiceListAdapter(records, context, typeFace);
        servicelist.setIndicatorBounds(0, 20);
        servicelist.setAdapter(adapter);
        registerForContextMenu(servicelist);
		
        add_service.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popup(null);
            }
        });
    }

    private void popup(final String[] values) {
        final SetService set_functions = new SetService(context,dbHandler,crypter);
        ScrollView set_service_view = (ScrollView) inflater.inflate(R.layout.set_service, null);
        final EditText title,url,username,password,notes;
        Button generate_password, show_password;
        String popup_title;
        String button_text;
        title = (EditText) set_service_view.findViewById(R.id.set_title);
        url = (EditText) set_service_view.findViewById(R.id.set_url);
        username = (EditText) set_service_view.findViewById(R.id.set_username);
        password = (EditText) set_service_view.findViewById(R.id.set_password);
        notes = (EditText) set_service_view.findViewById(R.id.set_notes);
        generate_password = (Button) set_service_view.findViewById(R.id.generate_password);
        show_password = (Button) set_service_view.findViewById(R.id.show_password);
        int width = password.getMeasuredWidth();
        generate_password.setMinimumWidth((width - 10) / 2);
        show_password.setMinimumWidth((width-10)/2);
        if(values != null) {
            title.setText(values[1]);
            url.setText(values[2]);
            username.setText(values[3]);
            password.setText(values[4]);
            notes.setText(values[5]);
            popup_title = "Edit " + values[1];
            button_text = "Update";
        }
        else {
            title.setText("");
            url.setText("");
            username.setText("");
            password.setText("");
            notes.setText("");
            popup_title = "Add new service";
            button_text = "Add";
        }
        generate_password.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final LinearLayout password_generator_view = (LinearLayout) inflater.inflate(R.layout.password_generator, null);
                final Button inc_pwd = (Button) password_generator_view.findViewById(R.id.increase_size);
                Button dec_pwd = (Button) password_generator_view.findViewById(R.id.decrease_size);
                final TextView password_len = (TextView) password_generator_view.findViewById(R.id.passwordLength);
                inc_pwd.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        int l = Integer.parseInt(password_len.getText().toString());
                        if(l<255) l++;
                        else Toast.makeText(context, "Max length is 255", Toast.LENGTH_SHORT).show();
                        password_len.setText(Integer.toString(l));
                    }
                });
                dec_pwd.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        int l = Integer.parseInt(password_len.getText().toString());
                        if(l>4) l--;
                        else Toast.makeText(context, "Min length is 4", Toast.LENGTH_SHORT).show();
                        password_len.setText(Integer.toString(l));
                    }
                });
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                boolean lower, upper, numbers, symbols;
                                String passwordLength;
                                lower = ((CheckBox) password_generator_view.findViewById(R.id.lowercase)).isChecked();
                                upper = ((CheckBox) password_generator_view.findViewById(R.id.uppercase)).isChecked();
                                numbers = ((CheckBox) password_generator_view.findViewById(R.id.numbers)).isChecked();
                                symbols = ((CheckBox) password_generator_view.findViewById(R.id.symbols)).isChecked();
                                passwordLength = password_len.getText().toString();
                                if (!(lower || upper || numbers || symbols))
                                    break;
                                password.setText(set_functions.passwordGenerator(lower, upper, numbers, symbols, passwordLength));
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };
                new AlertDialog.Builder(context).setView(password_generator_view).setPositiveButton("Generate", dialogClickListener).setNegativeButton("Cancel", dialogClickListener).show();
            }
        });

        show_password.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    return true;
                } else {
                    password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    return true;
                }
            }
        });

        final AlertDialog popUp = new AlertDialog.Builder(context).setView(set_service_view).setTitle(popup_title).setPositiveButton(button_text, null).setNegativeButton("Cancel", null).create();
        popUp.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button add = popUp.getButton(AlertDialog.BUTTON_POSITIVE);
                add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (set_functions.isEmpty(title.getText().toString())) {
                            Toast.makeText(context, "Enter service title", Toast.LENGTH_SHORT).show();
                        } else if (set_functions.isEmpty(username.getText().toString())) {
                            Toast.makeText(context, "Enter username", Toast.LENGTH_SHORT).show();
                        } else if (set_functions.isEmpty(password.getText().toString())) {
                            Toast.makeText(context, "Enter password", Toast.LENGTH_SHORT).show();
                        } else {
                            String[] record_content = new String[]{title.getText().toString(), url.getText().toString(), username.getText().toString(), password.getText().toString(), notes.getText().toString()};
                            if (values != null) {
                                if (set_functions.updateService(Integer.parseInt(values[0]), record_content)) {
                                    Toast.makeText(context, "Service updated successfully", Toast.LENGTH_SHORT).show();
                                    reloadData();
                                    popUp.dismiss();
                                } else
                                    Toast.makeText(context, "Error updating service", Toast.LENGTH_SHORT).show();
                            } else {
                                if (set_functions.addService(record_content)) {
                                    Toast.makeText(context, "Service added successfully", Toast.LENGTH_SHORT).show();
                                    reloadData();
                                    popUp.dismiss();
                                } else
                                    Toast.makeText(context, "Error adding service", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
                Button cancel = popUp.getButton(AlertDialog.BUTTON_NEGATIVE);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        popUp.dismiss();
                    }
                });
            }
        });
        popUp.show();
    }
    private void reloadData() {
        loadData();
        adapter.populateList(records);
        adapter.notifyDataSetChanged();
        checkNoRecord();
    }
    private void loadData() {
        records.clear();
        ArrayList<List<String>> recs = new ArrayList<>();
        String adder[] = new String[6];
        SQLiteDatabase db = dbHandler.openDB(false);
        int j;
        try {
            Cursor x = db.query("RECORDS", new String[]{"ID", "TITLE", "URL", "USERNAME", "PASSWORD", "NOTES"}, null, null, null, null, null);
            if(x.moveToFirst()) {
                do {
                    recs.add(Arrays.asList(x.getString(0), x.getString(1), x.getString(2), x.getString(3), x.getString(4), x.getString(5)));
                } while(x.moveToNext());
                x.close();
                for (List<String> list : recs) {
                    j = 0;
                    for (String i : list) {
                        adder[j] = i;
                        j++;
                    }
                    try {
                        Records ser = createService(crypter.decrypt(adder[1]), crypter.decrypt(adder[2]), adder[0]);
                        ser.setRecordData(createRecords(crypter.decrypt(adder[3]), crypter.decrypt(adder[4]), crypter.decrypt(adder[5])));
                        records.add(ser);
                    } catch (InvalidKeyException | NoSuchAlgorithmException
                            | NoSuchPaddingException
                            | IllegalBlockSizeException | BadPaddingException e1) {
                        Toast.makeText(context, "Decryption Error", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (SQLiteException e) {
            Toast.makeText(context, "Error accessing database" , Toast.LENGTH_SHORT).show();
            dbHandler.closeDB();
        }
        dbHandler.closeDB();
    }

    private void checkNoRecord() {
        TextView noRecords;
        noRecords = (TextView) findViewById(R.id.noRecords);
        if(records.isEmpty())
            noRecords.setText("No Records");
        else
            noRecords.setText(" ");
    }

    private boolean deleteRecord(int id) {
        long success = -1;
        try {
            SQLiteDatabase database = dbHandler.openDB(true);
            success = database.delete("RECORDS", "ID=" + id, null);
        } catch (SQLiteException e) {}
        dbHandler.closeDB();
        return success == 1;
    }

    private Records createService(String title, String url, String id) {
        return new Records(title, url, id);
    }

    private List<RecordData> createRecords(String username, String password,String notes) {
        List<RecordData> result = new ArrayList<>();
        RecordData item = new RecordData(username, password, notes);
        result.add(item);
        return result;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId()==R.id.servicesList) {
            String[] menuItems = getResources().getStringArray(R.array.longTouchMenu);
            ExpandableListView.ExpandableListContextMenuInfo mInfo = (ExpandableListView.ExpandableListContextMenuInfo)menuInfo;
            String mTitle = records.get(ExpandableListView.getPackedPositionGroup(mInfo.packedPosition)).getTitle();
            menu.setHeaderTitle(mTitle);
            for (int i = 0; i<menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int menuItemId = item.getItemId();
        String[] menuItems = getResources().getStringArray(R.array.longTouchMenu);
        final ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
        final String id = records.get(ExpandableListView.getPackedPositionGroup(info.packedPosition)).getId();
        if(menuItems[menuItemId].equals("Edit")) {
            Records record = records.get(ExpandableListView.getPackedPositionGroup(info.packedPosition));
            List<RecordData> record_data = record.getRecordData();
            popup(new String[] {id,record.getTitle(),record.getUrl(),record_data.get(0).getUsername(),record_data.get(0).getPassword(),record_data.get(0).getNotes()});
            return true;
        }
        else if(menuItems[menuItemId].equals("Delete")) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            if(deleteRecord(Integer.parseInt(id))){
                                Toast.makeText(context, "Deleted service" , Toast.LENGTH_SHORT).show();
                                reloadData();
                            }
                            else
                                Toast.makeText(context, "Error deleting service" , Toast.LENGTH_SHORT).show();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                }
            };
            new AlertDialog.Builder(this).setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();
        }
        else if(menuItems[menuItemId].equals("Open in browser")) {
            Intent openBrowser = new Intent(Intent.ACTION_VIEW);
            try {
                openBrowser.setData(Uri.parse(records.get(ExpandableListView.getPackedPositionGroup(info.packedPosition)).getUrl()));
                startActivity(openBrowser);
            }catch(Exception e) {
                Toast.makeText(context, "Invalid url" , Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERM_GRANT: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    im_ex_port();
                else
                    Toast.makeText(context, "Permission denied, import/export failed" , Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void getPermissionAndIExPort() {
        if (Build.VERSION.SDK_INT < 23) {
            im_ex_port();
            return;
        }
        if (ContextCompat.checkSelfPermission(ServiceList.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(ServiceList.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch(which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                ActivityCompat.requestPermissions(ServiceList.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERM_GRANT);
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                Toast.makeText(context, "Permission denied, import/export failed" , Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                };
                new AlertDialog.Builder(this).setTitle("Why write permission?").setMessage("Read/Write permission to external storage is required to store and retrieve backup files.").setPositiveButton("Continue", dialogClickListener).setNegativeButton("Cancel", dialogClickListener).show();
            } else {
                ActivityCompat.requestPermissions(ServiceList.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERM_GRANT);
            }
        }
        else
            im_ex_port();
    }
    public void im_ex_port() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String dbPath = context.getDatabasePath("Araine").getPath();
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        boolean done = false;
                        File directory = new File(Environment.getExternalStorageDirectory() +"/MiniPasswordManager");
                        if(directory.exists())
                            done = true;
                        else
                            done = directory.mkdir();
                        if(done) {
                            File dbFile = new File(dbPath);
                            File encryptedDB = new File(Environment.getExternalStorageDirectory()+"/MiniPasswordManager/secure");
                            try {
                                if(crypter.encryptDB(dbFile, encryptedDB))
                                    Toast.makeText(context, "Exported successfully" , Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(context, "Error encrypting file, export failed" , Toast.LENGTH_SHORT).show();
                            } catch(Exception e) {
                                Toast.makeText(context, "Error creating file, export failed" , Toast.LENGTH_SHORT).show();
                            }
                        }
                        else
                            Toast.makeText(context, "Error creating directory, export failed" , Toast.LENGTH_SHORT).show();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        boolean backupExists = new File(Environment.getExternalStorageDirectory()+"/MiniPasswordManager/", "/secure").exists();
                        SQLiteDatabase database;
                        if (backupExists) {
                            ArrayList<List<String>> records = new ArrayList<>();
                            String adder[] = new String[6];
                            File encryptedDB = new File(Environment.getExternalStorageDirectory()+"/MiniPasswordManager/secure");
                            File decryptedDB = new File(dbPath.replace("Araine","temp_db"));
                            try {
                                if(!(crypter.decryptDB(encryptedDB, decryptedDB))) {
                                    Toast.makeText(context, "Error decrypting backup, import failed", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            } catch(Exception e){
                                Toast.makeText(context, "Error decrypting backup, import failed" , Toast.LENGTH_SHORT).show();
                                return;
                            }
                            SQLiteDatabase extDatabase = SQLiteDatabase.openDatabase(dbPath.replace("Araine","temp_db"),null,SQLiteDatabase.OPEN_READONLY);
                            database = dbHandler.openDB(true);
                            int j;
                            long id = System.currentTimeMillis()/1000;
                            try {
                                Cursor x = extDatabase.query("RECORDS",new String[] {"ID","TITLE","URL","USERNAME","PASSWORD","NOTES"},null,null,null,null,null);
                                if(x.moveToFirst()) {
                                    do {
                                        records.add(Arrays.asList(x.getString(0),x.getString(1),x.getString(2),x.getString(3),x.getString(4),x.getString(5)));
                                    } while(x.moveToNext());
                                    x.close();
                                    for (List<String> list : records) {
                                        id++;
                                        j = 0;
                                        for (String i : list) {
                                            adder[j] = i;
                                            j++;
                                        }
                                        ContentValues addRecord = new ContentValues();
                                        addRecord.put("ID", id);
                                        addRecord.put("TITLE", adder[1]);
                                        addRecord.put("URL",adder[2]);
                                        addRecord.put("USERNAME", adder[3]);
                                        addRecord.put("PASSWORD", adder[4]);
                                        addRecord.put("NOTES", adder[5]);
                                        try {
                                            database.insertOrThrow("RECORDS",null, addRecord);
                                        }catch(SQLiteException e) {
                                            extDatabase.close();
                                            dbHandler.closeDB();
                                            decryptedDB.delete();
                                            Toast.makeText(context, "Error restoring backup, import failed" , Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                    }
                                }
                            } catch (SQLiteException e) {
                                extDatabase.close();
                                dbHandler.closeDB();
                                decryptedDB.delete();
                                Toast.makeText(context, "Error reading backup, import failed" , Toast.LENGTH_SHORT).show();
                                return;
                            }
                            extDatabase.close();
                            dbHandler.closeDB();
                            decryptedDB.delete();
                            reloadData();
                            Toast.makeText(context, "Imported successfully" , Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(context, "No backup available, import failed" , Toast.LENGTH_SHORT).show();
                            return;
                        }


                }
            }
        };
        new AlertDialog.Builder(this).setTitle(R.string.im_ex_port).setMessage(R.string.im_ex_port_caution).setPositiveButton("Export", dialogClickListener).setNegativeButton("Import", dialogClickListener).show();
    }

    @Override
    public boolean onQueryTextChange(String query) {
        adapter.filterData(query);
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        adapter.filterData(query);
        return false;
    }

    @Override
    public boolean onClose() {
        adapter.filterData("");
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.im_ex_port) {
            getPermissionAndIExPort();
            return true;
        }
        else if (id == R.id.change_pattern) {
            Intent setupActivity = new Intent(context, NewPattern.class);
            Bundle bundle = new Bundle();
            bundle.putString("pattern",pattern);
            setupActivity.putExtras(bundle);
            startActivity(setupActivity);
            finish();
            return true;
        }
        else if (id == R.id.about) {
            ScrollView about_app_view = (ScrollView) inflater.inflate(R.layout.about,null);
            int presentYear = Calendar.getInstance().get(Calendar.YEAR);
            String year = "2014";
            if(presentYear > 2014)
                year = "2014-" + presentYear;
            TextView copyright = (TextView) about_app_view.findViewById(R.id.copyRight);
            if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT)
                copyright.setText(Html.fromHtml("Copyright \u00a9 "+ year +" <a href=\"https://www.twitter.com/karthik_mlore\">Karthik M'lore</a>. All Rights Reserved.", Html.FROM_HTML_MODE_LEGACY));
            else
                copyright.setText(Html.fromHtml("Copyright \u00a9 "+ year +" <a href=\"https://www.twitter.com/karthik_mlore\">Karthik M'lore</a>. All Rights Reserved."));
            copyright.setMovementMethod(LinkMovementMethod.getInstance());
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            };
            new AlertDialog.Builder(this).setView(about_app_view).setPositiveButton("Ok", dialogClickListener).show();
            return true;
        }
        else if(id==R.id.search) {
            if(serachVisible) {
                search.getLayoutParams().height = 0;
                search.setQuery("",false);
                adapter.filterData("");
                root.requestFocus();
                serachVisible=false;
            }
            else {
                search.getLayoutParams().height = 100;
                search.requestLayout();
                search.requestFocus();
                serachVisible=true;
            }
        }
        else if (id == R.id.exit) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
