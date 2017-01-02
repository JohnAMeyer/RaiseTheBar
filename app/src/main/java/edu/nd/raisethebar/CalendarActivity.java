package edu.nd.raisethebar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import static android.R.attr.id;
import static edu.nd.raisethebar.R.string.pref;

/**
 * Created by jack1 on 10/18/2016.
 */

public class CalendarActivity extends AppCompatActivity {
    private static final String TAG = "RTB-Calendar";
    SessionCalendarView calendar;
    SimpleDateFormat format = new SimpleDateFormat("MM/dd/yy");
    SimpleDateFormat api = new SimpleDateFormat("yyyy-MM-dd");
    private static final String MANY = "#66ff66";
    private static final String FEW = "#ffff99";
    HashSet<String> set = new HashSet<>();
    private DateFormat df = SimpleDateFormat.getDateInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_select);
        HashMap<String,String> parameters = new HashMap<>();
        int id = getSharedPreferences(getString(pref), Context.MODE_PRIVATE).getInt("id", 1);//TODO define somewhere
        parameters.put("user",""+id);
        try {
            new HTTP.AsyncCall(HTTP.Method.GET, new URI("http://whaleoftime.com/sessions.php").toURL(), parameters, new HTTP.AsyncCall.StringRunnable() {
                @Override
                public void run(String s) {
                    HashMap<Date, Integer> events = new HashMap<>();//Integer is the color
                    JSONArray result = null;
                    try {
                        result = new JSONObject(s).getJSONArray("days");
                        for (int i = 0; i < result.length(); i++) {
                            Date date = api.parse(result.getJSONObject(i).getString("date"));
                            Log.d(TAG,api.format(date));
                            Integer color = result.getJSONObject(i).getInt("sessions") > 4 ? Color.parseColor(MANY) : Color.parseColor(FEW);
                            events.put(date, color);
                            set.add(df.format(date));
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON/IO Error", e);
                    } catch (ParseException e) {
                        Log.e(TAG, "Date Parsing Error", e);
                    }

                    SessionCalendarView cv = ((SessionCalendarView) findViewById(R.id.calendar_view));
                    cv.setEvents(events);

                    cv.setEventHandler(new SessionCalendarView.EventHandler() {
                        @Override
                        public void onDayLongPress(Date date) {
                            Toast.makeText(CalendarActivity.this, df.format(date), Toast.LENGTH_LONG).show();
                            Log.d(TAG, df.format(date));
                        }

                        @Override
                        public void onDayClick(Date date) {
                            if (!set.contains(df.format(date)))
                                return;
                            Intent i = new Intent(CalendarActivity.this, SessionActivity.class).putExtra("date", api.format(date));
                            startActivity(i);
                        }
                    });
                }
            }).execute();
        } catch (URISyntaxException | MalformedURLException e) {
            Log.e(TAG, "URI Error", e);
        }
    }
}
