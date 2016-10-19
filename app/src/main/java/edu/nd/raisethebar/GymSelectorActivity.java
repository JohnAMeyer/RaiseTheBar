package edu.nd.raisethebar;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.json.JSONObject;

/**
 * Created by jack1 on 10/18/2016.
 */

public class GymSelectorActivity extends AppCompatActivity {
    private static final String TAG = "RTB-GymSelectorActivity";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gym_selector);
        //try and obtain gps infor, but if locations is refused or turned off, have location prompt
        //https://developer.android.com/guide/topics/location/strategies.html
        JSONObject result;
        try {
             result = RawToJSON.toJSON(this, R.raw.gyms);
        } catch (Exception e){
            Log.e(TAG,"JSON Error",e);
        }
        //String[] items = result...;
        ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
        //populate list with items from gyms.php - may need to use a more advanced array adaptor if simple text does not work

        //on item select, fire new activity
    }
}
