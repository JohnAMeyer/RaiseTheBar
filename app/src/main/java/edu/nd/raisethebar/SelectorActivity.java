package edu.nd.raisethebar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;

/**
 * Home selection screen choosing which feature to access.
 *
 * @author aemiledonoghue
 * @since 10/6/16
 */

public class SelectorActivity extends AppCompatActivity {
    private static final String TAG = "RTB-Selector";

    @Override
    /**
     * Sets up GUI of selector.
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_selector);
    }

    /**
     * GUI-trigger for going to the GymSelectorActivity.
     *
     * @param view calling view - irrelevant
     * @see GymSelectorActivity
     */
    public void newSession(View view) {
        Intent intent = new Intent(this, GymSelectorActivity.class);
        startActivity(intent);
    }

    /**
     * GUI-trigger for going to the CalendarActvity.
     *
     * @param view calling view - irrelevant
     * @see CalendarActivity
     */
    public void previousSession(View view) {
        Intent intent = new Intent(this, CalendarActivity.class);
        startActivity(intent);
    }

    /**
     * GUI-trigger for going to the RoutineActivity
     *
     * @param view calling view - irrelevant
     * @see RoutineActivity
     */
    public void regimens(View view) {
        Intent intent = new Intent(this, RoutineActivity.class);
        startActivity(intent);
    }

}