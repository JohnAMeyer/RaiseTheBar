package edu.nd.raisethebar;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

        JSONArray result = null;
        String[] items = null;
        try {
            result = RawToJSON.toJSON(this, R.raw.sessions).getJSONArray("sessions");
            items = new String [result.length()];
            for(int i = 0; i<result.length();i++){
                items[i] = result.getJSONObject(i).get("machine").toString();
            }
        } catch (Exception e){
            Log.e(TAG,"JSON Error",e);
        }
        //use adaptor to populate listview with results from devices.php

        ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
        //populate list with items from gyms.php - may need to use a more advanced array adaptor if simple text does not work
        ListView vid = (ListView) findViewById( R.id.session_list);
        vid.setAdapter(itemsAdapter);

        //on item select, fire new activity
        final Context ct = this;
        final JSONArray arr = result;
        vid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG,"Item No. " + position);
                Intent i = new Intent(SessionActivity.this,SessionDisplayActivity.class).putExtra("reps",5);//TODO add other params here
                startActivity(i);
            }
        });
    }
}
