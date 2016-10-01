package edu.nd.raisethebar;

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

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class is for the device that is recording the data
 */


public class SlaveActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mOrientation;
    //private FileWriter writer;
    private ArrayList<Tuple> accelerometer_event;
    private ArrayList<Tuple> tilt_event;
    private boolean toggle = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slave);

        Button StartClick = (Button) findViewById(R.id.StartClick);
        StartClick.setOnClickListener(this);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

    }

    public void onClick(View v) {
        if (toggle) {
            // unregister listener
            mSensorManager.unregisterListener(this, mAccelerometer);
            mSensorManager.unregisterListener(this, mOrientation);
            toggle = false;
            Log.d("Speed Data", accelerometer_event.toString());
            Log.d("Tilt Data", tilt_event.toString());
        } else {
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(this, mOrientation, SensorManager.SENSOR_DELAY_NORMAL);
            toggle = true;
            accelerometer_event=new ArrayList<Tuple>();
            tilt_event=new ArrayList<Tuple>();

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


        // read event.values -- write to correct list in tuple class
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    class Tuple {
        long time;
        float[] data;

        public Tuple(float[] data, long time) {
            this.data = data;
            this.time = time;
        }
        @Override
        public String toString(){
            return "{" +time + ":" + Arrays.toString(data) + "}";
        }
    }
}