package com.swatiag1101.bingrrr1.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

//import com.swatiag1101.bingrrr1.MapsActivity;
import com.swatiag1101.bingrrr1.R;
import com.swatiag1101.bingrrr1.activity.LoginActivity;
import com.swatiag1101.bingrrr1.activity.MainActivity;

/**
 * A placeholder fragment containing a simple view.
 */
public class LoginActivityFragment extends Fragment implements View.OnClickListener {

    String stat;
    EditText name,password;
    TextView tv,tv1;
    FloatingActionButton submit;
    Button guest_login;
    SharedPreferences preferences;
    public LoginActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_login, container, false);

        name = (EditText) rootView.findViewById(R.id.name);
        password = (EditText) rootView.findViewById(R.id.password);
        submit = (FloatingActionButton) rootView.findViewById(R.id.login);
        guest_login = (Button) rootView.findViewById(R.id.guest_login);

        preferences = getActivity().getSharedPreferences("My_Profile", getActivity().MODE_PRIVATE);
        stat = preferences.getString("Status","");
        if (stat.equals("Wrong Details")) {
            Toast toast = Toast.makeText(getActivity(), "Invalid username or password.\n First time users, please click on 'Continue as Guest'", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL,0, 80);
            toast.show();
        }
        submit.setOnClickListener(this);
        guest_login.setOnClickListener(this);
        return rootView;

    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        if(id==R.id.login) {

            if(name.getText().toString().isEmpty() || password.getText().toString().isEmpty()){
                Toast toast = Toast.makeText(getActivity(), "Please enter username and password", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 80);
                toast.show();
            }else {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("Name", name.getText().toString());
                editor.putString("Password", password.getText().toString());
                editor.putString("Status", "Logged in");
                editor.commit();

                Intent i = new Intent(getActivity(), MainActivity.class);
                i.putExtra("Logged", "Yes");
                i.putExtra("name", name.getText().toString());
                i.putExtra("password", password.getText().toString());
                i.putExtra("pages", "Login");
                startActivity(i);
            }
        }else {
            Intent i = new Intent(getActivity(), MainActivity.class);
            i.putExtra("Logged", "Yes");
            i.putExtra("name", "");
            i.putExtra("password", "");
            i.putExtra("pages","Login");
            startActivity(i);
        }
    }
}
