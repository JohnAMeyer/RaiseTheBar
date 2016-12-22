package edu.nd.raisethebar;

import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.R.attr.data;

public class RecordActivity extends AppCompatActivity {
    private static final String TAG = "RTB-Record";
    private final static int REQUEST_ENABLE_BT = 1;
    public static final double STDDEV = .5;
    BluetoothBackground bb;
    BluetoothConnector bc;
    private boolean isRecording = false;
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
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode != RESULT_OK) {
                Toast.makeText(this, getString(R.string.bluetooth_needed), Toast.LENGTH_LONG).show();
                Log.d(TAG, "BT not activated");
                finish();
            } else {
                startService();
            }
        }
    }

    public void progress(final int i) {
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
        if (ba != null && ba.isEnabled())
            startService();
    }

    private void startService() {
        String mac = null;
        try {
            mac = new JSONObject(getIntent().getStringExtra("JSON")).getString("MAC");
        } catch (JSONException e) {
            Log.e(TAG, "Failed to get MAC from Intent", e);
        }
        bc = new BluetoothConnector();
        Intent i = new Intent(this, BluetoothBackground.class).putExtra("MAC", mac);
        bindService(i, bc, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bc != null) {
            unbindService(bc);
            bc = null;
        }
    }

    public void toggle(View v) {
        if (isRecording) {
            ArrayList<Tuple>[] data = bb.stopRecording();
            isRecording = false;
            Intent i = new Intent(this, SessionDisplayActivity.class);
            process(data, i);//TODO add other parameters here
            startActivity(i);
            finish();
        } else {
            bb.startRecording();
            ((Button) v).setText("Stop Recording");
            isRecording = true;
        }
    }

    private void process(ArrayList<Tuple>[] data, Intent i) {
        /*ArrayList<Tuple> acc = data[0];
        final int accsize = acc.size();
        double[] magnitude = new double[accsize], x = new double[accsize], y = new double[accsize], z = new double[accsize];
        double[] avg = new double[3];
        for (int j = 0; j < accsize; j++) {
            Tuple t = acc.get(j);
            double x1 = t.data[0];
            double y1 = t.data[1];
            double z1 = t.data[2];
            x[j] = x1;
            y[j] = y1;
            z[j] = z1;
            avg[0] += x1;
            avg[1] += y1;
            avg[2] += z1;
        }
        avg[0]/=accsize;
        avg[1]/=accsize;
        avg[2]/=accsize;

        for (int j = 0; j < accsize; j++) {
            x[j]-=avg[0];
            y[j]-=avg[1];
            z[j]-=avg[2];
            magnitude[j] = Math.hypot(x[j], Math.hypot(y[j], z[j]));
            Log.d(TAG,magnitude[j]+ "~,,, " + x[j] + "~,, " + y[j] + "~, " + z[j]);
        }

        double mean = 0.0;
        double std = 0.0;
        for (int j = 0; j < accsize; j++) {
            //calculate std
            double h = magnitude[j];
            double delta = h - mean;
            mean += delta / accsize;
            std += delta * (h - mean);
        }
        std /= (accsize - 1);
        int prev = 0;
        int reps = 0;
        for (int j = 0; j < accsize; j++) {
            double zscore = (magnitude[j] - mean)/std;
            int val = (int) Math.round(zscore/ STDDEV);
            //Log.d(TAG,""+magnitude[j]);// + ", " + val);
        }
        reps/=2;
        ArrayList<Tuple> mag = data[2];*/
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