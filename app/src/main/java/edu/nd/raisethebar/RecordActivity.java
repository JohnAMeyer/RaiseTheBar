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
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * A class that handles the GUI side of connecting to the device and recording the data.
 *
 * @author JohnAMeyer
 */
public class RecordActivity extends AppCompatActivity {
    public static final double STDDEV = .5;
    private static final String TAG = "RTB-Record";
    private final static int REQUEST_ENABLE_BT = 1;
    BluetoothBackground bb;
    BluetoothConnector bc;
    private boolean isRecording = false;
    private BluetoothAdapter ba;

    @Override
    /**
     * Sets up the GUI and prepares for connecting to the device.
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connecting);
        ba = BluetoothAdapter.getDefaultAdapter();
        if (ba == null || !ba.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            this.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    /**
     * Handles the request for turning Bluetooth on.
     */
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

    /**
     * Handles the background service's progress in connecting to the device.
     *
     * @param i the progress (out of 100)
     */
    void progress(final int i) {
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
    /**
     * Calls for starting the Bluetooth service.
     */
    protected void onStart() {
        super.onStart();
        if (ba != null && ba.isEnabled())
            startService();
    }

    /**
     * Starts the Bluetooth service with Intent-provided MAC.
     */
    private void startService() {
        String mac = getIntent().getStringExtra("MAC");
        bc = new BluetoothConnector();
        Intent i = new Intent(this, BluetoothBackground.class).putExtra("MAC", mac);
        bindService(i, bc, Context.BIND_AUTO_CREATE);
    }

    @Override
    /**
     * Stops the background Service.
     */
    protected void onStop() {
        super.onStop();
        if (bc != null) {
            unbindService(bc);
            bc = null;
        }
    }

    /**
     * Handles the Start/Stop recording button.
     *
     * @param v the calling view - irrelevant
     */
    public void toggle(View v) {
        if (isRecording) {
            ArrayList<Tuple>[] data = bb.stopRecording();
            isRecording = false;
            Intent i = new Intent(this, SessionDisplayActivity.class);
            process(data, i);//TODO add other parameters here
            sendData(i);
            startActivity(i);
            finish();
        } else {
            bb.startRecording();
            ((Button) v).setText("Stop Recording");
            isRecording = true;
        }
    }

    /**
     * Submits the acquired data to the server.
     *
     * @param i the intent to package the data in.
     */
    private void sendData(Intent i) {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("goodform", i.getBooleanExtra("form", true) ? "0" : "1");
        parameters.put("reps", "" + i.getIntExtra("reps", -1));
        parameters.put("machine", "" + getIntent().getIntExtra("machine", -1));
        parameters.put("user", "1");
        parameters.put("weight", ((TextView) findViewById(R.id.editWeight)).getText().toString());
        try {
            new HTTP.AsyncCall(HTTP.Method.GET, new URI("http://whaleoftime.com/update.php").toURL(), parameters, HTTP.AsyncCall.NO_CALLBACK).execute();
        } catch (MalformedURLException | URISyntaxException e) {
            Log.e(TAG, "HTTP Error", e);
        }
    }

    /**
     * Processes the data using the DataAnalysis class once the data collection is complete.
     *
     * @param data the data collected
     * @param i    the intent to package the data in.
     */
    private void process(ArrayList<Tuple>[] data, Intent i) {
        try {
            ArrayList<Tuple> acc = data[0];
            double[][] dbs = new double[acc.size()][3];
            for (int j = 0; j < acc.size(); j++) {
                dbs[j] = acc.get(j).data;
            }
            RealMatrix rm = new Array2DRowRealMatrix(dbs);
            RealMatrix covar = DataAnalysis.covarianceMatrix(rm);
            double[] eigVals = DataAnalysis.eigenvalues(covar);
            RealVector eigVector = DataAnalysis.eigenvectorFromValue((eigVals[0] > eigVals[1]) ? ((eigVals[0] > eigVals[2]) ? 0 : 2) : ((eigVals[1] > eigVals[2]) ? 1 : 2));
            Log.d(TAG, "Axis: " + eigVector.toString());
            double[] components = DataAnalysis.components(rm, eigVector);
            Log.d(TAG, Arrays.toString(components));
            int count = DataAnalysis.counter(components);
            Log.d(TAG, "Count is: " + count);
            i.putExtra("reps", count);
            //Stability
            Vector2D[] planar = DataAnalysis.planarize(rm, eigVector);
            double var = DataAnalysis.rVariance(planar);
            Log.d(TAG, "Var: " + var);//.01 seems like a decent cutoff
            i.putExtra("form", var > .01D);
        } catch (Exception e) {
            Log.e(TAG, "General Processing Error: ", e);
        }
    }

    //trigger display mode and push data to cloud

    /**
     * @author JohnAMeyer
     * @since 10/2/2016
     */

    public static class Tuple {
        long time;
        double[] data;

        public Tuple(double[] data, long time) {
            this.data = data;
            this.time = time;
        }

        public Tuple(float[] data, long time) {
            this.data = new double[data.length];
            for (int i = 0; i < data.length; i++) {
                this.data[i] = data[i];
            }
            this.time = time;
        }

        @Override
        public String toString() {
            return "{" + time + ":" + Arrays.toString(data) + "}";
        }
    }

    /**
     * Handles Bluetooth connection.
     */
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