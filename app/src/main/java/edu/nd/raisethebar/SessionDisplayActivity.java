package edu.nd.raisethebar;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static edu.nd.raisethebar.R.id.goodForm;
import static edu.nd.raisethebar.R.id.graph;

/**
 * Class that displays the results of a session.
 *
 * @author JohnAMeyer
 */
public class SessionDisplayActivity extends AppCompatActivity {
    public static final String TAG = "RTB-SessionDisplay";

    @Override
    /**
     * Sets up the GUI using data encapsulated in the intent.
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_display);
        //TODO update graphics based on intent
        ((TextView) findViewById(R.id.repCount)).setText("Reps: " + getIntent().getIntExtra("reps", 0));
        ((TextView) findViewById(goodForm)).setText(!getIntent().getBooleanExtra("form", false) ? "Good form." : "Form was shaky.");
        double[] times = getIntent().getDoubleArrayExtra("times");
        double[] components = getIntent().getDoubleArrayExtra("components");
        int length = times.length;
        WeightedObservedPoints wop = new WeightedObservedPoints();
        for (int i = 0; i < length; i++) {
            Log.v(TAG,"[" + times[i] + ", " + components[i] + "]");
            wop.add(times[i],components[i]);
        }

        GraphView graph = (GraphView) findViewById(R.id.graph);

        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>();
        for (int i = 0; i < length; i++) {
            series.appendData(new DataPoint((times[i]), (components[i])), true, length);
        }
        series.setColor(getResources().getColor(R.color.colorPrimaryDark));
        graph.addSeries(series);

        graph.addSeries(series);
        graph.setTitle("Acceleration of the Bar");
        graph.getViewport().setScrollable(true);
    }
}
