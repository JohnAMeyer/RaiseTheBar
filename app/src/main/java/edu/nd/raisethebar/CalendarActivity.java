package edu.nd.raisethebar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by jack1 on 10/18/2016.
 */

public class CalendarActivity extends AppCompatActivity {
    private static final String TAG = "RTB-Calendar";
    SessionCalendarView calendar;
    SimpleDateFormat format = new SimpleDateFormat("MM/dd/yy");
    private static final String MANY = "#66ff66";
    private static final String FEW = "#ffff99";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_select);

        HashMap<Date,Integer> events = new HashMap<>();//Integer is the color
        JSONArray result = null;
        try {
            result = RawToJSON.toJSON(this, R.raw.session_cal).getJSONArray("days");
            for(int i = 0; i<result.length();i++){
                Date date = format.parse(result.getJSONObject(i).getString("date"));
                Integer color = result.getJSONObject(i).getInt("sessions")>4?Color.parseColor(MANY):Color.parseColor(FEW);
                events.put(date,color);
            }
        } catch (JSONException | IOException e) {
            Log.e(TAG, "JSON/IO Error", e);
        } catch (ParseException e){
            Log.e(TAG, "Date Parsing Error", e);
        }

        SessionCalendarView cv = ((SessionCalendarView)findViewById(R.id.calendar_view));
        cv.updateCalendar(events);

        cv.setEventHandler(new SessionCalendarView.EventHandler(){
            @Override
            public void onDayLongPress(Date date){
                DateFormat df = SimpleDateFormat.getDateInstance();
                Toast.makeText(CalendarActivity.this, df.format(date), Toast.LENGTH_LONG).show();
                Log.d(TAG,df.format(date));
            }

            @Override
            public void onDayClick(Date date) {
                DateFormat df = SimpleDateFormat.getDateInstance();
                Intent i = new Intent(CalendarActivity.this,SessionActivity.class).putExtra("date",df.format(date));
                startActivity(i);
            }
        });
    }
}
