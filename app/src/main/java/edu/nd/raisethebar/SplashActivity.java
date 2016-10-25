package edu.nd.raisethebar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //check if logged in and stuff
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.pref), Context.MODE_PRIVATE);
        String user = sharedPref.getString("email",null);
        Intent intent;
        if(user == null){
            intent = new Intent(this,LoginActivity.class);
        } else {
            String pass = sharedPref.getString("pass",null);
            //hash weirdness acc. to http://www.codeproject.com/Articles/704865/Salted-Password-Hashing-Doing-it-Right
            //TODO server call
            boolean valid = false;
            Toast.makeText(this, getString(valid?R.string.login_prev_ok:R.string.login_prev_fail), Toast.LENGTH_SHORT).show();
            if(valid)
                intent = new Intent(this, SelectorActivity.class);
            else
                intent = new Intent(this,LoginActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
