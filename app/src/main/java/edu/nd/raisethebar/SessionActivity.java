package edu.nd.raisethebar;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import static edu.nd.raisethebar.R.string.pref;

/**
 * Handles getting the list of sessions on a given day.
 *
 * @author JohnAMeyer
 */
public class SessionActivity extends AppCompatActivity {
    private static final String TAG = "RTB-Session";

    @Override
    /**
     * Makes a call to the API and sets up the GUI.
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        HashMap<String, String> parameters = new HashMap<>();
        int id = getSharedPreferences(getString(pref), Context.MODE_PRIVATE).getInt("id", 1);//TODO define somewhere
        parameters.put("user", "" + id);
        parameters.put("date", getIntent().getStringExtra("date"));
        ((TextView)findViewById(R.id.textView3)).setText("Sessions of " + getIntent().getStringExtra("date"));
        try {
            new HTTP.AsyncCall(HTTP.Method.GET, new URI("http://whaleoftime.com/sessions.php").toURL(), parameters, new HTTP.AsyncCall.StringRunnable() {
                @Override
                public void run(String s) {
                    JSONArray result = null;
                    Session[] items = null;
                    try {
                        result = new JSONObject(s).getJSONArray("sessions");
                        items = Session.fromArray(result);
                    } catch (Exception e) {
                        Log.e(TAG, "JSON Error", e);
                    }
                    //use adaptor to populate listview with results from devices.php

                    ArrayAdapter<Session> itemsAdapter = new ArrayAdapter<Session>(SessionActivity.this, android.R.layout.simple_list_item_1, items) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            return convertView == null ? new SessionListItem(getContext(), getItem(position)) : convertView;
                        }
                    };
                    ListView vid = (ListView) findViewById(R.id.session_list);
                    vid.setAdapter(itemsAdapter);

                    //on item select, fire new activity
                    final Context ct = SessionActivity.this;
                    final JSONArray arr = result;
                    vid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Log.d(TAG, "Item No. " + position);
                            //Intent i = new Intent(SessionActivity.this, SessionDisplayActivity.class).putExtra("reps", 5);//TODO add other parameters here
                            //startActivity(i);
                            //TODO restore the specialized view
                        }
                    });
                }
            }).execute();
        } catch (URISyntaxException | MalformedURLException e) {
            Log.e(TAG, "URI Error", e);
        }
    }
}

/**
 * Representation of Session JSONObject for the sake of using adapters.
 */
class Session {
    private String machine;
    private int reps;
    private int weight;
    private String form;
    private String time;

    private Session(String machine, int reps, int weight, String form, String time) {
        this.machine = machine;
        this.reps = reps;
        this.weight = weight;
        this.form = form;
        this.time = time;
    }

    /**
     * Creates a Session from the data in a JSONObject.
     *
     * @param jo the JSON representation
     * @throws JSONException invalid JSON
     */
    Session(JSONObject jo) throws JSONException {
        this(jo.getString("machine"), jo.getInt("reps"), jo.getInt("weight"), jo.getString("form"), jo.getString("time"));//TODO handle time better
    }

    /**
     * Utility method to batch-convert a JSONArray to Sessions.
     *
     * @param ja a JSONArray of Session JSONObjects
     * @return an array of Sessions
     * @throws JSONException invalid JSONArray or JSONObject
     */
    static Session[] fromArray(JSONArray ja) throws JSONException {
        Session[] s = new Session[ja.length()];
        for (int i = 0; i < ja.length(); i++)
            s[i] = new Session(ja.getJSONObject(i));
        return s;
    }

    public String getMachine() {
        return machine;
    }

    public int getReps() {
        return reps;
    }

    public int getWeight() {
        return weight;
    }

    public String getForm() {
        return form;
    }

    public String getTime() {
        return time;
    }
}

/**
 * A representation of a Session for display (Adapter) purposes.
 */
class SessionListItem extends RelativeLayout {

    /**
     * Default constructor from a Session object.
     *
     * @param s the Session to create a visual representation of.
     */
    public SessionListItem(Context context, Session s) {
        super(context);
        inflate(context, R.layout.item_session, this);
        ((TextView) findViewById(R.id.session_machine)).setText(s.getMachine());
        ((TextView) findViewById(R.id.session_reps)).setText("Reps: " + s.getReps());
        ((TextView) findViewById(R.id.session_weight)).setText(" Weight: " + s.getWeight());
        ((TextView) findViewById(R.id.session_form)).setText(" " + s.getForm());
        ((TextView) findViewById(R.id.session_time)).setText(s.getTime()+ " EST");
    }
}