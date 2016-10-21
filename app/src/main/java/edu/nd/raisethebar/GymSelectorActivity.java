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
        //try and obtain gps infor, but if locations is refused or turned off, have location prompt
        //https://developer.android.com/guide/topics/location/strategies.html
        JSONArray result = null;
        String[] items = null;
        try {
            result = RawToJSON.toJSON(this, R.raw.gyms).getJSONArray("gyms");
            items = new String [result.length()];
            for(int i = 0; i<result.length();i++){
                items[i] = result.getJSONObject(i).get("name").toString();
            }
        } catch (Exception e){
            Log.e(TAG,"JSON Error",e);
        }
        ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
        //populate list with items from gyms.php - may need to use a more advanced array adaptor if simple text does not work
        ListView vid = (ListView) this.findViewById(R.id.gym_list);
        vid.setAdapter(itemsAdapter);

        //on item select, fire new activity
        final Context ct = this;
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
}
