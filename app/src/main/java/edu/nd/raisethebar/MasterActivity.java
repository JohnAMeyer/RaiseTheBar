package edu.nd.raisethebar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import android.widget.TextView;


/**
 * This class is for the displaying side of the app
 */
public class MasterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master);
    }
    @Override
    protected void onResume(){
        super.onResume();
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

}
