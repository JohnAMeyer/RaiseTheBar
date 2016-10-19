package edu.nd.raisethebar;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //check if logged in and stuff
        Intent intent = new Intent(this, SelectorActivity.class);
        startActivity(intent);
        finish();
    }
}
