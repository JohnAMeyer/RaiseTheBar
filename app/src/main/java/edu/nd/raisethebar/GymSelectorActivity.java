package edu.nd.raisethebar;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.HashMap;

import static edu.nd.raisethebar.R.string.pref;

/**
 * Created by jack1 on 10/18/2016.
 */

public class GymSelectorActivity extends AppCompatActivity {
    private static final String TAG = "RTB-GymSelector";
    private static final int LOCATION_REQUEST = 1;
    private LocationManager lm;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gym_selector);
        //TODO try and obtain gps info. but if locations is refused or turned off, have location prompt?
        //https://developer.android.com/guide/topics/location/strategies.html

        //TODO convert to new API?
        lm = (LocationManager) getSystemService(LOCATION_SERVICE);//TODO fine vs coarse
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
            }
            //TODO change graphics to represent failure
            return;
        }

        locationReceived(lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER));//TODO maybe check accuracy of provided location
    }

    private void locationReceived(Location l) {
        HashMap<String, String> parameters = new HashMap<>();
        DecimalFormat df = new DecimalFormat("#.0000");
        parameters.put("long", df.format(l.getLongitude()));
        parameters.put("lat", df.format(l.getLatitude()));
        try {
            HTTP.AsyncCall ac = new HTTP.AsyncCall(HTTP.Method.GET, new URI("http://whaleoftime.com/gyms.php").toURL(), parameters, new HTTP.AsyncCall.StringRunnable() {
                @Override
                public void run(String s) {
                    final Context ct = GymSelectorActivity.this;

                    Gym[] items = null;
                    JSONArray result = null;
                    try {
                        result = new JSONObject(s).getJSONArray("gyms");
                        items = Gym.fromJSONArr(result);
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON Error", e);
                    }

                    ArrayAdapter<Gym> itemsAdapter = new ArrayAdapter<Gym>(ct, android.R.layout.simple_list_item_1, items) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            Gym g = getItem(position);
                            if (convertView == null) {
                                convertView = new GymView(getContext(), g);
                            }
                            return convertView;
                        }
                    };
                    //populate list with items from gyms.php - may need to use a more advanced array adaptor if simple text does not work
                    ListView vid = (ListView) findViewById(R.id.gym_list);
                    vid.setAdapter(itemsAdapter);

                    String last = getSharedPreferences(getString(pref), MODE_PRIVATE).getString("last-gym", null);//TODO get from API instead as JSONArray
                    if (last != null) {
                        final String[] strings = {last};
                        Gym[] gyms = new Gym[strings.length];
                        try {
                            for (int i = 0; i < strings.length; i++)
                                gyms[i] = Gym.fromJSON(new JSONObject(strings[i]));
                        } catch (JSONException e) {
                            Log.e(TAG, "Replace", e);
                        }

                        LinearLayout l = (LinearLayout) findViewById(R.id.prev_gyms_layout);
                        l.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        l.setVisibility(LinearLayout.VISIBLE);
                        ListView prev = (ListView) findViewById(R.id.prev_gyms);
                        prev.setAdapter(new ArrayAdapter<Gym>(ct, android.R.layout.simple_list_item_1, gyms) {
                            @Override
                            public View getView(int position, View convertView, ViewGroup parent) {
                                Gym g = getItem(position);
                                if (convertView == null) {
                                    convertView = new GymView(getContext(), g);
                                }
                                return convertView;
                            }
                        });
                        prev.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                Intent i = new Intent(ct, MachineSelectorActivity.class);
                                try {
                                    i.putExtra("JSON", strings[position]);
                                    getSharedPreferences(getString(pref), MODE_PRIVATE).edit().putString("last-gym", strings[position]).apply();
                                } catch (Exception e) {
                                    Log.e(TAG, "OnClickHandler", e);
                                }
                                startActivity(i);
                            }
                        });
                    }

                    //on item select, fire new activity
                    final JSONArray arr = result;
                    vid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent i = new Intent(ct, MachineSelectorActivity.class);
                            try {
                                i.putExtra("JSON", arr.getJSONObject(position).toString());
                                getSharedPreferences(getString(pref), MODE_PRIVATE).edit().putString("last-gym", arr.getJSONObject(position).toString()).apply();
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
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == LOCATION_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {//TODO see if redundant IDE error fixing code can be removed
                    return;
                }
                locationReceived(lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER));
            }
        }
    }

    public void home(View v) {
        startActivity(new Intent(this, HomeActivity.class));
    }


}

class Gym {
    String name;
    String dist;

    public Gym(String name, String dist) {
        this.dist = dist;
        this.name = name;
    }

    static Gym[] fromJSONArr(JSONArray ja) throws JSONException {
        Gym[] items = new Gym[ja.length()];
        for (int i = 0; i < ja.length(); i++) {
            final JSONObject jo = ja.getJSONObject(i);
            items[i] = new Gym(jo.get("name").toString(), jo.getString("distance"));
        }
        return items;
    }

    static Gym fromJSON(JSONObject jo) throws JSONException {
        return new Gym(jo.get("name").toString(), jo.getString("distance"));
    }

    public String getDist() {
        return dist;
    }

    public String getName() {
        return name;
    }
}

class GymView extends RelativeLayout {
    public GymView(Context context, Gym g) {
        super(context);
        inflate(context, R.layout.gym_item, this);
        ((TextView) findViewById(R.id.gym_name)).setText(g.getName());
        ((TextView) findViewById(R.id.distance)).setText(g.getDist());
    }
}
