package edu.nd.raisethebar;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.UUID;

/**
 * This class is for the displaying side of the app
 */
public class MasterActivity extends AppCompatActivity {
    ConnectionThread ct;
    String SUUID = "fa87c0d0-afac-11de-8a39-0800200c9a66";
    public static final String TAG = "MAIN ACTIVITY";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selector);
    }
    public void fireIntent(View view){
        Intent intent = new Intent(this, DeviceDialog.class);
        this.startActivityForResult(intent, 0);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        Log.d(TAG,resultCode+"");
        if (resultCode == Activity.RESULT_OK) {
            ct = new ConnectionThread(data);
            Log.d(TAG,"Connection Thread Created");
            ct.start();
        }
    }
    class ConnectionThread extends Thread{
        private BluetoothSocket socket;

        public ConnectionThread(Intent data){
            String address = data.getExtras().getString(DeviceDialog.EXTRA_DEVICE_ADDRESS);
            BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
            Log.d(TAG,"Named Device");
            try {
                socket = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString(SUUID));
                Log.d(TAG,"RFCOMM Socket");
            } catch(Exception e){
                Log.e(TAG,"Socket Error",e);
            }
        }
        @Override
        public void run(){
            try {
                socket.connect();
                Log.d(TAG,"Connected");
                DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                int num = dis.readInt();
                Log.d(TAG,num + "");
                long[] times = new long[num];
                double[] x = new double[num];
                double[] y = new double[num];
                double[] z = new double[num];
                for(int i = 0; i<num; i++){
                    times[i] = dis.readLong();
                    x[i] = dis.readDouble();
                    y[i] = dis.readDouble();
                    z[i] = dis.readDouble();
                    Log.d(TAG,"{" + x[i] +"," + y[i] + "," + z[i] + "}");
                }
                socket.getOutputStream().write(new byte[1]);
                dis.close();
            } catch (Exception e){
                Log.e(TAG,"Socket Error",e);
            }
        }
    }
    protected class Tuple {
        long time;
        double[] data;

        public Tuple(double[] data, long time) {
            this.data = data;
            this.time = time;
        }
        public Tuple(float[] data, long time) {
            this.data = new double[data.length];
            for(int i = 0; i < data.length; i++){
                this.data[i] = data[i];
            }
            this.time = time;
        }
        @Override
        public String toString(){
            return "{" +time + ":" + Arrays.toString(data) + "}";
        }
    }
}
