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
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

public class MachineSelectorActivity extends AppCompatActivity {
    private static final String TAG = "RTB-MachineSelector";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine_selector);
        Intent intent = getIntent();
        int gymId = -1;//handle error case
        try {
            JSONObject gym = new JSONObject(intent.getStringExtra("JSON"));
            gymId = gym.getInt("id");
            Log.d(TAG,""+gymId);
            HashMap<String, String> parameters = new HashMap<>();
            parameters.put("gym",""+gymId);
            try {
                HTTP.AsyncCall ac = new HTTP.AsyncCall(HTTP.Method.GET, new URI("http://whaleoftime.com/devices.php").toURL(), parameters, new HTTP.AsyncCall.StringRunnable() {
                    @Override
                    public void run(String s) {
                        JSONArray result = null;
                        String[] items = null;
                        try {
                            result = new JSONObject(s).getJSONArray("machines");
                            items = new String[result.length()];
                            for (int i = 0; i < result.length(); i++) {
                                items[i] = result.getJSONObject(i).get("name").toString();
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON Error", e);
                        }
                        //use adaptor to populate listview with results from devices.php

                        ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>(MachineSelectorActivity.this, android.R.layout.simple_list_item_1, items);
                        //populate list with items from gyms.php - may need to use a more advanced array adaptor if simple text does not work
                        ListView vid = (ListView) findViewById(R.id.machine_list);
                        vid.setAdapter(itemsAdapter);

                        //on item select, fire new activity
                        final Context ct = MachineSelectorActivity.this;
                        final JSONArray arr = result;
                        vid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                String MAC = null;
                                Intent i = new Intent(ct, RecordActivity.class);
                                try {
                                    MAC = arr.getJSONObject(position).getString("MAC");
                                    i.putExtra("MAC", MAC);
                                } catch (Exception e) {
                                    Log.e(TAG, "OnClickHandler", e);
                                }

                                startActivity(i);
                            }
                        });
                    }
                });
                ac.execute();
            } catch (URISyntaxException | MalformedURLException e) {
                Log.e(TAG, "URI Error", e);
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON Error", e);
        }
    }
    //onclick attempt to connect with device
}
