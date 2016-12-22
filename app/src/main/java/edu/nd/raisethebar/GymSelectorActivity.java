package edu.nd.raisethebar;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;

import static android.R.attr.key;
import static android.R.attr.value;

/**
 * Created by jack1 on 10/18/2016.
 */

public class GymSelectorActivity extends AppCompatActivity {
    private static final String TAG = "RTB-GymSelector";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gym_selector);
        //TODO try and obtain gps info. but if locations is refused or turned off, have location prompt
        //https://developer.android.com/guide/topics/location/strategies.html

        HashMap<String, String> parameters = new HashMap<>();
        try {
            HTTP.AsyncCall ac = new HTTP.AsyncCall(HTTP.Method.GET,new URI("http://whaleoftime.com/gyms.php").toURL(),  parameters, new HTTP.AsyncCall.StringRunnable(){
                @Override
                public void run(String s) {
                    String[] items = null;
                    JSONArray result = null;
                    try {
                        result = new JSONObject(s).getJSONArray("gyms");
                        items = new String [result.length()];
                        for(int i = 0; i<result.length();i++){
                            items[i] = result.getJSONObject(i).get("name").toString();
                        }
                    } catch (JSONException e){
                        Log.e(TAG,"JSON Error",e);
                    }

                    ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>(GymSelectorActivity.this, android.R.layout.simple_list_item_1, items);
                    //populate list with items from gyms.php - may need to use a more advanced array adaptor if simple text does not work
                    ListView vid = (ListView) GymSelectorActivity.this.findViewById(R.id.gym_list);
                    vid.setAdapter(itemsAdapter);

                    //TODO previous gyms
                    boolean prev = false;
                    if(prev) {
                        LinearLayout l = (LinearLayout) findViewById(R.id.prev_gyms_layout);
                        l.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        l.setVisibility(LinearLayout.VISIBLE);
                    }

                    //on item select, fire new activity
                    final Context ct = GymSelectorActivity.this;
                    final JSONArray arr = result;
                    vid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent i = new Intent(ct, MachineSelectorActivity.class);
                            try {
                                i.putExtra("JSON", arr.getJSONObject(position).toString());
                            }catch (Exception e){
                                Log.e(TAG,"OnClickHandler",e);
                            }
                            startActivity(i);
                        }
                    });
                }
            });
            ac.execute();
        } catch (URISyntaxException | MalformedURLException e) {
            Log.e(TAG,"URI Error",e);
        }
    }
    public void home(View v){
        startActivity(new Intent(this, HomeActivity.class));
    }
}
