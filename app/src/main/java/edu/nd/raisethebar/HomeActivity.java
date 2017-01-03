package edu.nd.raisethebar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import static edu.nd.raisethebar.R.string.pref;

/**
 * An Activity that skips choosing a gym if the user is at home using their own device.
 *
 * @author JohnAMeyer
 * @since 12/21/2016
 */
public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "RTB-Home";

    @Override
    /**
     * Gets MAC from settings and fires the RecordActivity with that previously chosen MAC.
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String homeMAC = getSharedPreferences(getString(pref), Context.MODE_PRIVATE).getString("home-mac", null);
        Log.d(TAG, "Home was set as: " + homeMAC);
        if (homeMAC == null) {
            //TODO fire prompt to search for home device
            getSharedPreferences(getString(pref), Context.MODE_PRIVATE).edit().putString("home-mac", "B0:B4:48:C9:A2:06").apply();
            homeMAC = "B0:B4:48:C9:A2:06";
            Log.d(TAG, "Selected as: " + homeMAC);
        }
        //TODO display some machine type dialog
        startActivity(new Intent(this, RecordActivity.class).putExtra("MAC", homeMAC));

    }
}
