package com.swatiag1101.bingrrr1.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.onesignal.OneSignal;
import com.swatiag1101.bingrrr1.R;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    SharedPreferences preferences;
    String stat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        preferences = getSharedPreferences("My_Profile",getApplicationContext().MODE_PRIVATE);

        String name = preferences.getString("Name","");
        String pass = preferences.getString("Password","");
        stat = preferences.getString("Status","");

        if (getIntent().getBooleanExtra("EXIT", false)) {
            finish();
        }else {
            if (stat.equals("Logged in")) {
                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                i.putExtra("Logged", "Yes");
                i.putExtra("name", name);
                i.putExtra("password", pass);
                startActivity(i);
            } else {
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
