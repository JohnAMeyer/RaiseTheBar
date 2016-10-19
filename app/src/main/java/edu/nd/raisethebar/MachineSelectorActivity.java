package edu.nd.raisethebar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MachineSelectorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine_selector);
        //use adaptor to populate listview with results from devices.php
    }

    //onclick attempt to connect with device
}
