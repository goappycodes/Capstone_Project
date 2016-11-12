package com.swatiag1101.bingrrr1.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;

import com.onesignal.OneSignal;
import com.swatiag1101.bingrrr1.R;
import com.swatiag1101.bingrrr1.data.FoodContract;
import com.swatiag1101.bingrrr1.data.WeatherProvider;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private static final String LOG_TAG = LoginActivity.class.getSimpleName();
    SharedPreferences preferences;
    String stat;
    private static final String[] GET_USER_PROJECTION = new String[] {
            FoodContract.FoodEntry.COLUMN_KEY,
            FoodContract.FoodEntry.username,
            FoodContract.FoodEntry.password,
            FoodContract.FoodEntry.status
    };

    private static final int INDEX_USERNAME = 1;
    private static final int INDEX_PASSWORD = 2;
    private static final int INDEX_STATUS = 3;

    String username;
    String password;
    String status;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Uri userUri = FoodContract.FoodEntry.CONTENT_URI;

        Cursor cursor = getContentResolver().query(userUri, GET_USER_PROJECTION, null, null, null);
        if(cursor!=null) {
            if (cursor.moveToFirst()) {
                username = cursor.getString(INDEX_USERNAME);
                password = cursor.getString(INDEX_PASSWORD);
                status = cursor.getString(INDEX_STATUS);

                Log.d(LOG_TAG, "Cursor data: " + username + " " + password + " " + status);
            }
        }
        preferences = getSharedPreferences("My_Profile",getApplicationContext().MODE_PRIVATE);

        String name = preferences.getString("Name","");
        String pass = preferences.getString("Password","");
        stat = preferences.getString("Status","");

        if (getIntent().getBooleanExtra("EXIT", false)) {
            finish();
        }else {
            if(status!=null) {
                if (status.equals("Logged in")) {
                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                    i.putExtra("Logged", "Yes");
                    i.putExtra("name", name);
                    i.putExtra("password", pass);
                    startActivity(i);
                } else {
                    setContentView(R.layout.activity_login);

                }
            }else{
                setContentView(R.layout.activity_login);
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(stat.equals("Wrong Details")){
            stat="";
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("Status",stat);
            editor.apply();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

    }


}
