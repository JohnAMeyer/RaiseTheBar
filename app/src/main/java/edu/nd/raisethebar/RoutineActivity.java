package edu.nd.raisethebar;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Handles routine management and viewing - work in progress.
 * @author JohnAMeyer
 */
public class RoutineActivity extends AppCompatActivity {
    private static final String TAG = "RTB-Routine";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine);
        ((TextView)findViewById(R.id.routine_title)).setText(SimpleDateFormat.getDateInstance().format(new Date()) + " Routine");
        //TODO get list of exercises for the day
        JSONObject[] items = new JSONObject[0];

            ((ListView) findViewById(R.id.routine_todo)).setAdapter(new ArrayAdapter<JSONObject>(this,-1,items) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View v = convertView;
                    if (v == null) {
                        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        v = inflater.inflate(R.layout.item_routine, null);
                    }

                    try {
                        JSONObject jo = getItem(position);
                        ((TextView) v.findViewById(R.id.routine_item_name)).setText(jo.getString("name"));
                        //TODO((TextView) v.findViewById(R.id.routine_item_progress));
                    } catch (JSONException je) {
                        Log.e(TAG, "JSON Parse Exception", je);
                    }
                    return v;
                }
            });
    }
}
