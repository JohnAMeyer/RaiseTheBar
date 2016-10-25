package edu.nd.raisethebar;

import android.bluetooth.BluetoothAdapter;
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
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

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
    private final static int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter ba;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connecting);
        ba = BluetoothAdapter.getDefaultAdapter();
        if (ba == null || !ba.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            this.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==REQUEST_ENABLE_BT){
            if(resultCode!=RESULT_OK){
                Toast.makeText(this, getString(R.string.bluetooth_needed), Toast.LENGTH_LONG).show();
                Log.d(TAG,"BT not activated");
                finish();
            } else {
                startService();
            }
        }
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
                    findViewById(R.id.is_device_on).setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(ba != null && ba.isEnabled())
            startService();
    }

    private void startService() {
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
    protected void onStop() {
        super.onStop();
        if(bc!=null) {
            unbindService(bc);
            bc = null;
        }
    }

    public void toggle(View v){
        if(isRecording){
            ArrayList<Tuple>[] data = bb.stopRecording();
            isRecording = false;
            //process
            Intent i = new Intent(this,SessionDisplayActivity.class).putExtra("reps",5);//TODO add other params here
            startActivity(i);
            finish();
        } else {
            bb.startRecording();
            ((Button)v).setText("Stop Recording");
            isRecording = true;
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