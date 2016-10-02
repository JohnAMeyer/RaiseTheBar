package edu.nd.raisethebar;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;


/**
 * This class is for the displaying side of the app
 */
public class MasterActivity extends AppCompatActivity {
    public static final double[] ZERO = {0D,0D,0D};
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
    boolean goodform = true;
    double[] smooth_accelerometer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master);
    }
    @Override
    protected void onResume(){
        super.onResume();
        /*
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
        if (goodform) {
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
         */
        GraphView graph = (GraphView) findViewById(R.id.graph);
        int[] arrayY = {1,15,3,8,7};
        int[] arrayX = {0,1,2,3,4};

        int lengthArrays = arrayX.length;

        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>();

        for (int i = 0; i < arrayY.length; i++) {
            series.appendData(new DataPoint((arrayX[i]), (arrayY[i])), true, lengthArrays);
        }

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
        String numberRepsAsString = countReps+"";
        TextView numberReps = (TextView) findViewById(R.id.textNumberReps);
        numberReps.setText(numberRepsAsString);

        double countMaxSpeed = 10;
        String numberMaxSpeedAsString = Double.toString(countMaxSpeed);
        TextView maxSpeed = (TextView) findViewById(R.id.textMaxSpeed);
        maxSpeed.setText(numberMaxSpeedAsString);
    }
    protected void changed(){

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
                goodform=false;
            }
        }
    }
    class Tuple {
        long time;
        double[] data;

        public Tuple(double[] data, long time) {
            this.data = data;
            this.time = time;
        }
        public Tuple(float[] data, long time) {
            this.data = new double[data.length];
            for(int i = 0; i < data.length; i++){
                this.data[i] = data[i];
            }
            this.time = time;
        }
        @Override
        public String toString(){
            return "{" +time + ":" + Arrays.toString(data) + "}";
        }
    }
}
