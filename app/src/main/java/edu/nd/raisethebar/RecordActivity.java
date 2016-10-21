package edu.nd.raisethebar;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.R.attr.data;
import static android.R.attr.name;

public class RecordActivity extends AppCompatActivity {
    private static final String TAG = "RTB-Record";
    BluetoothBackground bb;
    BluetoothConnector bc;
    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connecting);
    }
    public void progress(final int i){
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (i == 100) {
                    setContentView(R.layout.activity_record_weight);
                } else {
                    if (android.os.Build.VERSION.SDK_INT > 23)
                        ((ProgressBar) findViewById(R.id.connect_progress)).setProgress(i, true);
                    else
                        ((ProgressBar) findViewById(R.id.connect_progress)).setProgress(i);
                }
            }
        });
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

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(bc);
        bc = null;
    }

    public void toggle(View v){
        if(isRecording){
            ArrayList<Tuple>[] data = bb.stopRecording();
            //process
        } else {
            bb.startRecording();
        }
    }

    //trigger display mode and push data to cloud
    class BluetoothConnector implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bb = ((BluetoothBackground.LocalBinder) service).getService();
            bb.register(RecordActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bb.register(null);
            bb = null;
        }
    }
}