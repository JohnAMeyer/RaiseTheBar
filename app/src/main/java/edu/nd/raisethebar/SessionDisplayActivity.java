package edu.nd.raisethebar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Class that displays the resutls of a session.
 *
 * @author JohnAMeyer
 */
public class SessionDisplayActivity extends AppCompatActivity {

    @Override
    /**
     * Sets up the GUI using data encapsulated in the intent.
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_display);
        //TODO update graphics based on intent
        ((TextView) findViewById(R.id.repCount)).setText("Reps: " + getIntent().getIntExtra("reps", 0));
        ((TextView) findViewById(R.id.goodForm)).setText(!getIntent().getBooleanExtra("form", false) ? "Good form." : "Form was shaky.");
    }
}
