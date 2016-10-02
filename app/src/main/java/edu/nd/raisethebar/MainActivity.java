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

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import android.widget.EditText;
import android.widget.TextView;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

/**
 * This class combines both functionalities
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {
    public static final double[] ZERO = {0D,0D,0D};
    public static final String TAG = "MAIN ACTIVITY";
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mOrientation;
    //private FileWriter writer;
    private ArrayList<Tuple> accelerometer_event;
    private ArrayList<Tuple> tilt_event;
    private boolean toggle = false;
    EditText weightEdit;
    private int weight=0;
    double maxVel = 0D;
    double[] velo;
    double[] times;
    int reps =0;
    double norm_Of_g=0;
    boolean goodForm = true;
    double[] smooth_accelerometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_weight);

        Button StartClick = (Button) findViewById(R.id.StartClick);
        StartClick.setOnClickListener(this);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        weightEdit = (EditText)findViewById(R.id.editWeight);


    }
    protected void changed(){
        process();
        GraphView graph = (GraphView) findViewById(R.id.graph);

        int lengthAccelerometer = accelerometer_event.size();

        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>();

        for (int i = 0; i < lengthAccelerometer; i++) {
            //series.appendData(new DataPoint((times[i]), (velo[i])), true, lengthAccelerometer);
            series.appendData(new DataPoint((times[i]), (smooth_accelerometer[i])), true, lengthAccelerometer);
        }

        graph.addSeries(series);
        graph.setTitle("Velocity of the Bar");
        graph.getViewport().setScrollable(true);

        TextView form = (TextView) findViewById(R.id.textForm);
        if (goodForm) {
            form.setText("Good Form");
            form.setTextColor(0xff00ff00);
        } else {
            form.setText("Bad Form");
            form.setTextColor(0xffff0000);
        }

        int countReps = reps/2;
        String numberRepsAsString = countReps+"";
        TextView numberReps = (TextView) findViewById(R.id.textNumberReps);
        numberReps.setText(numberRepsAsString);

        double countMaxSpeed = ((long)(maxVel*100))/100D;
        String numberMaxSpeedAsString = Double.toString(countMaxSpeed);
        TextView maxSpeed = (TextView) findViewById(R.id.textMaxSpeed);
        maxSpeed.setText(numberMaxSpeedAsString);

        String weightAsString=Integer.toString(weight);
        TextView weightview = (TextView) findViewById(R.id.textWeight);
        weightview.setText(weightAsString);
        new Thread(){
            @Override
            public void run() {
                sendData();
            }
        }.start();
    }
    protected void process(){
        long time_0 = accelerometer_event.get(0).time;
        for (Tuple tu : accelerometer_event){
            tu.time-= time_0;
            //tu.time /= 1000000000;
        }
        LinkedList<Tuple> reimannVel = new LinkedList<>();
        velo = new double[accelerometer_event.size()];
        times = new double[accelerometer_event.size()];
        reimannVel.add(new Tuple(ZERO,0));
        for (int i = 1; i < accelerometer_event.size();i++){
            double[] vel = new double[3];
            long time = (accelerometer_event.get(i).time-accelerometer_event.get(i-1).time);
            vel[0] = (accelerometer_event.get(i).data[0]*time + reimannVel.peekLast().data[0])/1000000000D;
            vel[1] = (accelerometer_event.get(i).data[1]*time + reimannVel.peekLast().data[1])/1000000000D;
            vel[2] = (accelerometer_event.get(i).data[2]*time + reimannVel.peekLast().data[2])/1000000000D;
            reimannVel.add(new Tuple(vel,accelerometer_event.get(i).time));
            Log.d("VEL",reimannVel.peekLast().toString());
            velo[i] = Math.hypot(Math.hypot(vel[0],vel[1]),vel[2]);
            times[i] = accelerometer_event.get(i).time/1000000000D;
            maxVel = (velo[i]>maxVel)?velo[i] : maxVel;
        }

        smooth_accelerometer = new double[accelerometer_event.size()];

        for (int i = 2; i<(accelerometer_event.size()-2);i++){
            smooth_accelerometer[i]=((accelerometer_event.get(i-2).data[2]+accelerometer_event.get(i-1).data[2]
            +accelerometer_event.get(i).data[2]+accelerometer_event.get(i+1).data[2]+accelerometer_event.get(i+2).data[2])/5);
        }
        for (int i = 0; i<(accelerometer_event.size()-1);i++){
            if ((smooth_accelerometer[i]*smooth_accelerometer[i+1])<0){
               reps++;
           }
        }

        norm_Of_g = Math.sqrt(tilt_event.get(1).data[0] * tilt_event.get(1).data[0]+tilt_event.get(1).data[1]*tilt_event.get(1).data[1]
                + tilt_event.get(1).data[2]*tilt_event.get(1).data[2]);


        for (int i = 0; i<(tilt_event.size());i++){
            tilt_event.get(i).data[0]/=norm_Of_g;
            tilt_event.get(i).data[1]/=norm_Of_g;
            tilt_event.get(i).data[2]/=norm_Of_g;
        }

        double [] rotation_array = new double [tilt_event.size()];
        for (int i = 0; i<(tilt_event.size());i++){
            rotation_array[i]=Math.round(Math.toDegrees(Math.atan2(tilt_event.get(i).data[0], tilt_event.get(i).data[2])));
            if (rotation_array[i]>=20){
                goodForm =false;
            }
        }



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
            TextView onbutton = (TextView) findViewById(R.id.StartClick);
            onbutton.setText("Stop Recording");
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
            mSensorManager.registerListener(this, mOrientation, SensorManager.SENSOR_DELAY_UI);
            toggle = true;
            accelerometer_event=new ArrayList<Tuple>();
            tilt_event=new ArrayList<Tuple>();
            try{
                weight = Integer.parseInt(weightEdit.getText().toString());
            }catch(Exception e){
                weight=0;
            }

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

    protected void sendData(){
        try {
            Log.d(TAG,"Sending");
            URL url = new URL("http://whaleoftime.com/update.php?goodform=" + (goodForm ? 1 : 0) + "&reps=" + reps + "&machine=" + "weight1");
            Log.d(TAG,url.toString());
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            urlConnection.getInputStream().available();
            urlConnection.disconnect();
        }catch(Exception e){
            Log.e(TAG,"URL Error",e);
        }
    }
}