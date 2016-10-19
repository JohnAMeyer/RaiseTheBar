package edu.nd.raisethebar;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by jack1 on 10/18/2016.
 */

public class GymSelectorActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gym_selector);
        //try and obtain gps infor, but if locations is refused or turned off, have location prompt
        //https://developer.android.com/guide/topics/location/strategies.html

        //populate list with items from gyms.php

        //on item select, fire new activity
    }
}
