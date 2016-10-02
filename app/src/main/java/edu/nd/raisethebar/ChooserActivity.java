package edu.nd.raisethebar;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import edu.nd.raisethebar.MainActivity;
import edu.nd.raisethebar.R;
import edu.nd.raisethebar.SlaveActivity;

public class ChooserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chooser);
    }
    @Override
    protected void onStart(){
        super.onStart();
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            //enable bluetooth first
        }
    }
    public void user(View view){
        Intent intent = new Intent(this, MasterActivity.class);
        startActivity(intent);
    }
    public void machine(View view){
        Intent intent = new Intent(this, SlaveActivity.class);
        startActivity(intent);
    }
}
