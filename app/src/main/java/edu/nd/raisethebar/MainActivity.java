package edu.nd.raisethebar;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Arrays;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import android.widget.TextView;
/**
 * This class combines both functionalities
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {
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
    protected void changed(){
        GraphView graph = (GraphView) findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)

        });
        graph.addSeries(series);
        graph.setTitle("Acceleration of the Bar");

        TextView form = (TextView) findViewById(R.id.textForm);
        boolean goodform = false;
        if (goodform) {
            form.setText("Good Form");
            form.setTextColor(0xff00ff00);
        } else {
            form.setText("Bad Form");
            form.setTextColor(0xffff0000);
        }

        int countReps = 5;
        String numberRepsAsString = Integer.toString(countReps);
        TextView numberReps = (TextView) findViewById(R.id.textNumberReps);
        numberReps.setText(numberRepsAsString);

        float countMaxSpeed = 10;
        String numberMaxSpeedAsString = Float.toString(countMaxSpeed);
        TextView maxSpeed = (TextView) findViewById(R.id.textMaxSpeed);
        maxSpeed.setText(numberMaxSpeedAsString);
    }
    protected void process(){
        
    }

    public void onClick(View v) {
        if (toggle) {
            // unregister listener
            mSensorManager.unregisterListener(this, mAccelerometer);
            mSensorManager.unregisterListener(this, mOrientation);
            toggle = false;
            Log.d("Speed Data", accelerometer_event.toString());
            Log.d("Tilt Data", tilt_event.toString());
            setContentView(R.layout.activity_master);
            changed();
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