package edu.nd.raisethebar;

        import android.app.Activity;
        import android.content.Intent;
        import android.os.Bundle;
        import android.view.View;
        import android.view.Window;
        import android.widget.Button;
        import android.widget.CalendarView;
        import android.widget.Toast;

/**
 * Created by aemiledonoghue on 10/6/16.
 */
/**
 * Created by aemiledonoghue on 10/6/16.
 */


public class SelectorActivity extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }
    }
    /**
     * Created by aemiledonoghue on 10/6/16.
     */

    public static class PreviousActivity extends Activity {

        CalendarView calendar;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_calendar_select);


            calendar = (CalendarView) findViewById(R.id.calendarView);
            calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                @Override
                public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                    Toast.makeText(getApplicationContext(), dayOfMonth + "/" + month + "/" + year + "/", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    /**
     * Created by aemiledonoghue on 10/12/16.
     */

    public static class NewActivity {
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_gym_selector);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_selector);

        Button button_new_session = (Button) findViewById(R.id.button_new_session);
        button_new_session.setOnClickListener(this);
    }

    public void new_session(View view) {
        Intent intent = new Intent(this, NewActivity.class);
        startActivity(intent);
    }

    public void previous_session(View view) {
        Intent intent = new Intent(this, PreviousActivity.class);
        startActivity(intent);
    }

}