package edu.nd.raisethebar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Toast;

/**
 * Created by aemiledonoghue on 10/6/16.
 */


public class SelectorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_selector);

        Button button_new_session = (Button) findViewById(R.id.button_new_session);
    }

    public void new_session(View view) {
        Intent intent = new Intent(this, GymSelectorActivity.class);
        startActivity(intent);
    }

    public void previous_session(View view) {
        Intent intent = new Intent(this, CalendarActivity.class);
        startActivity(intent);
    }

}