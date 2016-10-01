package edu.nd.raisethebar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * This class is for the device that is recording the data
 */


public class SlaveActivity extends AppCompatActivity {
    private SensorManager mSensorManager;
    private Sensor mSensor;

    public SlaveActivity(){
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slave);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
     protected void onPause(){
     super.onPause();
         mSensorManager.unregisterListener(this);
     }

}
