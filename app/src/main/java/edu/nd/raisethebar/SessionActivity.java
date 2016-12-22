package edu.nd.raisethebar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import static edu.nd.raisethebar.R.string.pref;

public class SessionActivity extends AppCompatActivity {
    private static final String TAG = "RTB-Session";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        Date date = null;
        try {
            date = SimpleDateFormat.getDateInstance().parse(getIntent().getStringExtra("date"));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        HashMap<String, String> parameters = new HashMap<>();
        int id = getSharedPreferences(getString(pref), Context.MODE_PRIVATE).getInt("id", -1);//TODO define somewhere
        parameters.put("user", "" + id);
        parameters.put("date", getIntent().getStringExtra("date"));
        try {
            HTTP.AsyncCall ac = new HTTP.AsyncCall(HTTP.Method.GET, new URI("http://whaleoftime.com/sessions.php").toURL(), parameters, new HTTP.AsyncCall.StringRunnable() {
                @Override
                public void run(String s) {
                    JSONArray result = null;
                    String[] items = null;
                    try {
                        result = new JSONObject(s).getJSONArray("sessions");
                        items = new String[result.length()];
                        for (int i = 0; i < result.length(); i++) {
                            items[i] = result.getJSONObject(i).get("machine").toString();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "JSON Error", e);
                    }
                    //use adaptor to populate listview with results from devices.php

                    ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>(SessionActivity.this, android.R.layout.simple_list_item_1, items);
                    //populate list with items from gyms.php - may need to use a more advanced array adaptor if simple text does not work
                    ListView vid = (ListView) findViewById(R.id.session_list);
                    vid.setAdapter(itemsAdapter);

                    //on item select, fire new activity
                    final Context ct = SessionActivity.this;
                    final JSONArray arr = result;
                    vid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Log.d(TAG, "Item No. " + position);
                            Intent i = new Intent(SessionActivity.this, SessionDisplayActivity.class).putExtra("reps", 5);//TODO add other parameters here
                            startActivity(i);
                        }
                    });
                }
            });
            ac.execute();
        } catch (URISyntaxException | MalformedURLException e) {
            Log.e(TAG, "URI Error", e);
        }
    }
}
