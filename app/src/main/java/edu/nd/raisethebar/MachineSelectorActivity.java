package edu.nd.raisethebar;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MachineSelectorActivity extends AppCompatActivity {
    private static final String TAG = "RTB-MachineSelector";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine_selector);
        Intent intent = getIntent();
        int gymId = -1;
        try {
            JSONObject gym = new JSONObject(intent.getStringExtra("JSON"));
            gymId = gym.getInt("id");
            //makes call to server with id

        } catch(JSONException e){
            Log.e(TAG,"JSON Error",e);
        }

        JSONArray result = null;
        String[] items = null;
        try {
            result = RawToJSON.toJSON(this, R.raw.machines).getJSONArray("machines");
            items = new String [result.length()];
            for(int i = 0; i<result.length();i++){
                items[i] = result.getJSONObject(i).get("name").toString();
            }
        } catch (Exception e){
            Log.e(TAG,"JSON Error",e);
        }
        //use adaptor to populate listview with results from devices.php

        ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
        //populate list with items from gyms.php - may need to use a more advanced array adaptor if simple text does not work
        ListView vid = (ListView) findViewById(R.id.machine_list);
        vid.setAdapter(itemsAdapter);

        //on item select, fire new activity
        final Context ct = this;
        final JSONArray arr = result;
        vid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String MAC = null;
                Intent i = new Intent(ct, RecordActivity.class);
                try {
                    MAC = arr.getJSONObject(position).getString("MAC");
                    i.putExtra("JSON", arr.getJSONObject(position).toString());
                }catch (Exception e){
                    Log.e(TAG,"OnClickHandler",e);
                }
                Toast.makeText(ct, getString(R.string.attempt_bluetooth), Toast.LENGTH_LONG).show();

                startActivity(i);
            }
        });
    }



    //onclick attempt to connect with device
}
