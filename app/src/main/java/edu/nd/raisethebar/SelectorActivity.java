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
    private final static int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_selector);
    }

    @Override
    protected void onStart() {
        super.onStart();
        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        if (ba == null || !ba.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==REQUEST_ENABLE_BT){
            if(resultCode!=RESULT_OK){
                Toast.makeText(this, getString(R.string.bluetooth_needed), Toast.LENGTH_LONG).show();
                finish();
            }
        }
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