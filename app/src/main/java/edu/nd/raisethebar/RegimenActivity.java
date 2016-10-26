package edu.nd.raisethebar;

import android.content.Context;
import android.database.DataSetObserver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static java.security.AccessController.getContext;

public class RegimenActivity extends AppCompatActivity {
    private static final String TAG = "RTB-Regimen";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regimen);
        ((TextView)findViewById(R.id.regimen_title)).setText(SimpleDateFormat.getDateInstance().format(new Date()) + " Regimen");
        //TODO get list of exercises for the day
        JSONObject[] items = new JSONObject[0];

            ((ListView) findViewById(R.id.regimen_todo)).setAdapter(new ArrayAdapter<JSONObject>(this,-1,items) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View v = convertView;
                    if (v == null) {
                        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        v = inflater.inflate(R.layout.regimen_item, null);
                    }

                    try {
                        JSONObject jo = getItem(position);
                        ((TextView) v.findViewById(R.id.regimen_item_name)).setText(jo.getString("name"));
                        //TODO((TextView) v.findViewById(R.id.regimen_item_progress));
                    } catch (JSONException je) {
                        Log.e(TAG, "JSON Parse Exception", je);
                    }
                    return v;
                }
            });
    }
}
