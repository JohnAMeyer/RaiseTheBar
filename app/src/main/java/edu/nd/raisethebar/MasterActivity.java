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

}
