package edu.nd.raisethebar;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import static android.R.attr.name;

public class RecordActivity extends AppCompatActivity {
    private static final String TAG = "RTB-Record";
    BluetoothBackground bb;
    BluetoothConnector bc;
    BluetoothBackground.LocalBinder bin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_weight);

    }

    @Override
    protected void onResume() {
        super.onResume();
        String mac = null;
        try{
            mac = new JSONObject(getIntent().getStringExtra("JSON")).getString("MAC");
        } catch (JSONException e){
            Log.e(TAG,"Failed to get MAC from Intent",e);
        }
        bc = new BluetoothConnector();
        Intent i = new Intent(this, BluetoothBackground.class).putExtra("MAC",mac);
        bindService(i, bc, Context.BIND_AUTO_CREATE);
    }
    public void toggle(View v){
        //wait for start press and end press
    }

    //trigger display mode and push data to cloud
    class BluetoothConnector implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bin = (BluetoothBackground.LocalBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }
}