package edu.nd.raisethebar;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Toast;

import static android.R.attr.tag;
import static android.bluetooth.BluetoothAdapter.getDefaultAdapter;

/**
 * Created by aemiledonoghue on 10/6/16.
 */


public class SelectorActivity extends AppCompatActivity {
    private static final String TAG = "RTB-Selector";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_selector);
    }

    public void newSession(View view) {
        Intent intent = new Intent(this, GymSelectorActivity.class);
        startActivity(intent);
    }

    public void previousSession(View view) {
        Intent intent = new Intent(this, CalendarActivity.class);
        startActivity(intent);
    }

}