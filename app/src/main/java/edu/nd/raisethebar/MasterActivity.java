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
import android.widget.EditText;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.UUID;

/**
 * This class is for the displaying side of the app
 */
public class MasterActivity extends AppCompatActivity {
    private static final double[] ZERO = {0D, 0D, 0D};
    ConnectionThread ct;
    String SUUID = "fa87c0d0-afac-11de-8a39-0800200c9a66";
    public static final String TAG = "MAIN ACTIVITY";
    private double[] times;
    private boolean goodForm;
    private int reps;
    private double[] smoothAccelerometer;
    private double[] velo;
    private double maxVel;
    private int weight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selector);
    }

    public void fireIntent(View view) {
        weight = Integer.parseInt(((EditText) findViewById(R.id.editWeight)).getText().toString());
        Intent intent = new Intent(this, DeviceDialog.class);
        this.startActivityForResult(intent, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setContentView(R.layout.waiting);
        Log.d(TAG, resultCode + "");
        if (resultCode == Activity.RESULT_OK) {
            ct = new ConnectionThread(data);
            Log.d(TAG, "Connection Thread Created");
            ct.start();
        }
    }

    public void dataReady(ArrayList<Tuple> accEvents, ArrayList<Tuple> tiltEvents) {
        process(accEvents, tiltEvents);
        int lengthAccelerometer = accEvents.size();
        final LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>();
        for (int i = 0; i < lengthAccelerometer; i++) {
            //series.appendData(new DataPoint((times[i]), (velo[i])), true, lengthAccelerometer);
            series.appendData(new DataPoint((times[i]), (smoothAccelerometer[i])), true, lengthAccelerometer);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.activity_master);
                GraphView graph = (GraphView) findViewById(R.id.graph);
                graph.addSeries(series);
                graph.setTitle("Velocity of the Bar");
                graph.getViewport().setScrollable(true);

                TextView form = (TextView) findViewById(R.id.textForm);
                if (goodForm) {
                    form.setText("Good Form");
                    form.setTextColor(0xff00ff00);
                } else {
                    form.setText("Bad Form");
                    form.setTextColor(0xffff0000);
                }

                int countReps = reps / 2;
                String numberRepsAsString = countReps + "";
                TextView numberReps = (TextView) findViewById(R.id.textNumberReps);
                numberReps.setText(numberRepsAsString);

                double countMaxSpeed = ((long) (maxVel * 100)) / 100D;
                String numberMaxSpeedAsString = Double.toString(countMaxSpeed);
                TextView maxSpeed = (TextView) findViewById(R.id.textMaxSpeed);
                maxSpeed.setText(numberMaxSpeedAsString);

                String weightAsString = Integer.toString(weight);
                TextView weightview = (TextView) findViewById(R.id.textWeight);
                weightview.setText(weightAsString);
            }
        });
    }

    protected void process(ArrayList<Tuple> accEvents, ArrayList<Tuple> tiltEvents) {
        long time_0 = accEvents.get(0).time;
        for (Tuple tu : accEvents) {
            tu.time -= time_0;
            //tu.time /= 1000000000;
        }
        LinkedList<Tuple> reimannVel = new LinkedList<>();
        velo = new double[accEvents.size()];
        times = new double[accEvents.size()];
        reimannVel.add(new Tuple(ZERO, 0));
        for (int i = 1; i < accEvents.size(); i++) {
            double[] vel = new double[3];
            long time = (accEvents.get(i).time - accEvents.get(i - 1).time);
            vel[0] = (accEvents.get(i).data[0] * time + reimannVel.peekLast().data[0]) / 1000000000D;
            vel[1] = (accEvents.get(i).data[1] * time + reimannVel.peekLast().data[1]) / 1000000000D;
            vel[2] = (accEvents.get(i).data[2] * time + reimannVel.peekLast().data[2]) / 1000000000D;
            reimannVel.add(new Tuple(vel, accEvents.get(i).time));
            Log.d("VEL", reimannVel.peekLast().toString());
            velo[i] = Math.hypot(Math.hypot(vel[0], vel[1]), vel[2]);
            times[i] = accEvents.get(i).time / 1000000000D;
            maxVel = (velo[i] > maxVel) ? velo[i] : maxVel;
        }

        smoothAccelerometer = new double[accEvents.size()];//might not be 4

        for (int i = 2; i < (accEvents.size() - 2); i++) {
            smoothAccelerometer[i] = ((accEvents.get(i - 2).data[2] + accEvents.get(i - 1).data[2]
                    + accEvents.get(i).data[2] + accEvents.get(i + 1).data[2] + accEvents.get(i + 2).data[2]) / 5);
        }
        for (int i = 0; i < (accEvents.size() - 1); i++) {
            if ((smoothAccelerometer[i] * smoothAccelerometer[i + 1]) < 0) {
                reps++;
            }
        }

        double norm_Of_g = Math.sqrt(tiltEvents.get(1).data[0] * tiltEvents.get(1).data[0] + tiltEvents.get(1).data[1] * tiltEvents.get(1).data[1]
                + tiltEvents.get(1).data[2] * tiltEvents.get(1).data[2]);


        for (int i = 0; i < (tiltEvents.size()); i++) {
            tiltEvents.get(i).data[0] /= norm_Of_g;
            tiltEvents.get(i).data[1] /= norm_Of_g;
            tiltEvents.get(i).data[2] /= norm_Of_g;
        }

        double[] rotation_array = new double[tiltEvents.size()];
        LOOP:
        for (int i = 0; i < (tiltEvents.size()); i++) {
            rotation_array[i] = Math.round(Math.toDegrees(Math.atan2(tiltEvents.get(i).data[0], tiltEvents.get(i).data[2])));
            if (rotation_array[i] >= 20) {
                goodForm = false;
                break LOOP;
            }
        }


    }

    class ConnectionThread extends Thread {
        private BluetoothSocket socket;

        public ConnectionThread(Intent data) {
            String address = data.getExtras().getString(DeviceDialog.EXTRA_DEVICE_ADDRESS);
            BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
            Log.d(TAG, "Named Device");
            try {
                socket = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString(SUUID));
                Log.d(TAG, "RFCOMM Socket");
            } catch (Exception e) {
                Log.e(TAG, "Socket Error", e);
            }
        }

        @Override
        public void run() {
            try {
                socket.connect();
                Log.d(TAG, "Connected");
                DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                int numnum = dis.readByte();
                ArrayList<Tuple>[] arrs = new ArrayList[numnum];
                for (int q = 0; q < numnum; q++) {
                    int num = dis.readInt();
                    Log.d(TAG, "Reading all " + num + "events in " + q + "th set");
                    arrs[q] = new ArrayList<>(num);
                    for (int i = 0; i < num; i++) {
                        double[] coord = new double[3];
                        long time = dis.readLong();
                        for (int j = 0; j < 3; j++)
                            coord[j] = dis.readDouble();
                        arrs[q].add(new Tuple(coord, time));
                        Log.d(TAG, arrs[q].get(arrs[q].size() - 1).toString());
                    }
                }
                Log.d(TAG, "Writing Back");
                socket.getOutputStream().write(new byte[1]);
                dis.close();
                dataReady(arrs[0], arrs[1]);
            } catch (Exception e) {
                Log.e(TAG, "Socket Error", e);
            }
        }
    }
}
