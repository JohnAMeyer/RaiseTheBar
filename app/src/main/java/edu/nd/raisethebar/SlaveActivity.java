package edu.nd.raisethebar;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import static android.R.attr.data;
import static android.R.attr.x;
import static android.R.attr.y;
import static edu.nd.raisethebar.R.id.StartClick;

/**
 * This class is for the device that is recording the data
 * @deprecated
 */

public class SlaveActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {
    public static final String TAG = "Slave Device";
    String SUUID = "fa87c0d0-afac-11de-8a39-0800200c9a66";

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mOrientation;
    //private FileWriter writer;
    private ArrayList<Tuple> accelerometer_event;
    private ArrayList<Tuple> tilt_event;
    private boolean toggle = false;
    Button startClick;
    ConnectionThread ct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slave);

        startClick = (Button) findViewById(R.id.StartClick);
        startClick.setOnClickListener(this);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

    }
    @Override
    protected void onStart() {
        super.onStart();
        ct = new ConnectionThread();
        ct.start();
    }
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    public void onClick(View v) {
        if (toggle) {
            // unregister listener
            mSensorManager.unregisterListener(this, mAccelerometer);
            mSensorManager.unregisterListener(this, mOrientation);
            ct = ct.send(this.accelerometer_event,tilt_event);
            startClick.setText("START RECORDING");
            toggle = false;
            ct.start(); //look for next device
        } else {
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(this, mOrientation, SensorManager.SENSOR_DELAY_NORMAL);
            toggle = true;
            accelerometer_event=new ArrayList<Tuple>();
            tilt_event=new ArrayList<Tuple>();
            startClick.setText("END RECORDING");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // find if the event is a acceleration or gyro
        if (event.sensor == mAccelerometer) {
            //write to accelerometer tuple arraylist
            accelerometer_event.add(new Tuple(event.values, event.timestamp));
        } else {
            tilt_event.add(new Tuple(event.values, event.timestamp));
            //gyroscope_event.add
            // write to array list
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    protected class ConnectionThread extends Thread {
        BluetoothSocket bs;
        public ConnectionThread send(ArrayList<Tuple>... events){
            try {
                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(bs.getOutputStream()));
                dos.writeByte(events.length);
                int z = -1;
                for(ArrayList<Tuple> data : events) {
                    dos.writeInt(data.size());
                    Log.d(TAG,"Reading all " + data.size() + "events in " + z++ + "th set");
                    for (int i = 0; i < data.size(); i++) {
                        Tuple t = data.get(i);
                        dos.writeLong(t.time);
                        dos.writeDouble(t.data[0]);
                        dos.writeDouble(t.data[1]);
                        dos.writeDouble(t.data[2]);
                        Log.d(TAG,t.toString());
                    }
                }
                Log.d(TAG, "Written");
                dos.flush();
                bs.getInputStream().read();
                dos.close();
            }catch(Exception e){

            }
            return new ConnectionThread();
        }
        @Override
        public void run() {
            try {
                BluetoothAdapter.getDefaultAdapter().setName("Weight Bar 1");
                BluetoothServerSocket bss = BluetoothAdapter.getDefaultAdapter().listenUsingInsecureRfcommWithServiceRecord("ccv_prototype", UUID.fromString(SUUID));
                Log.d(TAG,"Listening");
                bs = bss.accept();
                Log.d(TAG,"Accepted");

            } catch(Exception e){

            }
        }
    }
}