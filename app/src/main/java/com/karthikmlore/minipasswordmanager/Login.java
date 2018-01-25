package com.karthikmlore.minipasswordmanager;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.haibison.android.lockpattern.LockPatternActivity;
import com.haibison.android.lockpattern.util.Settings;

public class Login extends AppCompatActivity {
    private String[] encrypted_pattern;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        encrypted_pattern = getPattern();
        if(encrypted_pattern[1].equals("fresh")) {
            Intent setupActivity = new Intent(Login.this, NewPattern.class);
            startActivity(setupActivity);
            finish();
        }
        else if(encrypted_pattern[1].equals("regular"))
            login_screen();
    }

    private void login_screen(){
        Settings.Display.setMaxRetries(Login.this, 1);
        Intent intent = new Intent(LockPatternActivity.ACTION_COMPARE_PATTERN, null, Login.this, LockPatternActivity.class);
		intent.putExtra(LockPatternActivity.EXTRA_PATTERN, encrypted_pattern[0].toCharArray());
        startActivityForResult(intent, 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 2: {
				switch (resultCode) {
					case RESULT_OK:
						String pattern = new String(data.getCharArrayExtra(LockPatternActivity.EXTRA_PATTERN));
						Intent secureActivity = new Intent(Login.this, ServiceList.class);
						Bundle bundle = new Bundle();
						bundle.putString("Pattern", pattern);
						secureActivity.putExtras(bundle);
						startActivity(secureActivity);
						finish();
						break;
					case RESULT_CANCELED:
						finish();
						break;
					case LockPatternActivity.RESULT_FAILED:
                        Toast.makeText(Login.this, "Incorrect pattern" , Toast.LENGTH_SHORT).show();
                        login_screen();
						break;
					case LockPatternActivity.RESULT_FORGOT_PATTERN:
						Toast.makeText(Login.this, "Sorry can't help" , Toast.LENGTH_SHORT).show();
                        login_screen();
						break;
				}
            }
        }
    }

    private String[] getPattern() {
        DBHelper dbHandler = new DBHelper(Login.this);
        SQLiteDatabase database = dbHandler.openDB(false);
        String[] pattern = {"","fresh"};
        try {
            Cursor x = database.query("APP_SETTINGS",new String[] {"PATTERN"},null,null,null,null,null);
            if(x.moveToFirst()) {
                pattern[0] = x.getString(0);
                pattern[1] = "regular";
            }
            x.close();
        } catch (Exception e) {
            dbHandler.closeDB();
            pattern[1] = "update";
            Intent updateActivity = new Intent(Login.this, Update.class);
            startActivity(updateActivity);
            finish();
        }
        dbHandler.closeDB();
        return pattern;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.exit) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}