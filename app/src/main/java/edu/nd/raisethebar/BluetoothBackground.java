package edu.nd.raisethebar;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.UUID;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static android.view.View.X;
import static android.view.View.Y;
import static android.view.View.Z;
import static java.lang.Thread.currentThread;
import static java.security.CryptoPrimitive.MAC;
import static java.util.UUID.fromString;

/**
 * Created by jack1 on 10/20/2016.
 */

public class BluetoothBackground extends Service {
    private static final String TAG = "RTB-BluetoothBackground";
    BluetoothDevice bd;
    BluetoothGatt bg;
    private BLECallback bc;
    private static final UUID UUID_MOV_SERV = UUID.fromString("f000aa80-0451-4000-b000-000000000000");
    private static final UUID UUID_MOV_DATA = UUID.fromString("f000aa81-0451-4000-b000-000000000000");
    private static final UUID UUID_MOV_CONF = UUID.fromString("f000aa82-0451-4000-b000-000000000000");
    private static final UUID UUID_MOV_PERI = UUID.fromString("f000aa83-0451-4000-b000-000000000000");
    private static final UUID CCC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static final byte[] ALL_MOTION = {0b01000000,0b0};
    private static final byte[] NOTIFY = {0b1,0b0};
    private Queue<Runnable> writes = new LinkedList<>();
    private ArrayList<Tuple> acc = new ArrayList<>();
    private ArrayList<Tuple> gyr = new ArrayList<>();
    private ArrayList<Tuple> mag = new ArrayList<>();
    private boolean isRecording = false;
    private RecordActivity a;


    public class LocalBinder extends Binder {
        BluetoothBackground getService() {
            return BluetoothBackground.this;
        }
    }
    Binder b = new LocalBinder();
    @Override
    public IBinder onBind(Intent intent) {
        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        if (ba.isEnabled()) {
            bd = ba.getRemoteDevice(intent.getStringExtra("MAC"));
        } else {
            stopSelf();
            return null;
            //how to handle softly?
        }
        bc = new BLECallback();
        bg = bd.connectGatt(this,true, bc);
        return b;
    }

    public void startRecording(){
        isRecording = true;
    }
    public ArrayList<Tuple>[] stopRecording(){
        isRecording = false;
        ArrayList<Tuple>[] arr = new ArrayList[3];
        arr[0]=acc;
        arr[1]=gyr;
        arr[2]=mag;
        return arr;
    }

    private class BLECallback extends BluetoothGattCallback {
        private boolean hasReceived = false;

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG,"Connected");
                gatt.discoverServices();
                a.progress(15);
            }else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG,"Disconnected");
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.d(TAG,characteristic.toString());
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.d(TAG,"Written: " + (status==BluetoothGatt.GATT_SUCCESS));
            if(writes.size()>0) new Handler(getMainLooper()).post(writes.poll());
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if(!hasReceived)a.progress(100);
            hasReceived = true;
            super.onCharacteristicChanged(gatt, characteristic);
            //Data received
            byte[] data = characteristic.getValue();
            float gyrX =  gyroConvert((short)(((data[1] & 0xFF) << 8) | (data[0] & 0xFF)));
            float gyrY =  gyroConvert((short)(((data[3] & 0xFF) << 8) | (data[2] & 0xFF)));
            float gyrZ =  gyroConvert((short)(((data[5] & 0xFF) << 8) | (data[4] & 0xFF)));

            float accX =  accConvert((short)(((data[7] & 0xFF) << 8) | (data[6] & 0xFF)));
            float accY =  accConvert((short)(((data[9] & 0xFF) << 8) | (data[8] & 0xFF)));
            float accZ =  accConvert((short)(((data[11] & 0xFF) << 8) | (data[10] & 0xFF)));

            float magX =  magConvert((short)(((data[13] & 0xFF) << 8) | (data[12] & 0xFF)));
            float magY =  magConvert((short)(((data[15] & 0xFF) << 8) | (data[14] & 0xFF)));
            float magZ =  magConvert((short)(((data[17] & 0xFF) << 8) | (data[16] & 0xFF)));

            if(isRecording) {
                long time = System.currentTimeMillis();
                acc.add(new Tuple(new float[]{accX,accY,accZ},time));
                gyr.add(new Tuple(new float[]{gyrX,gyrY,gyrZ},time));
                mag.add(new Tuple(new float[]{magX,magY,magZ},time));
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.d(TAG,"Written: " + (status==BluetoothGatt.GATT_SUCCESS));
            if(writes.size()>0) new Handler(getMainLooper()).post(writes.poll());
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.d(TAG, "Discovered Services");
            a.progress(30);

            final BluetoothGattService motionService = gatt.getService(UUID_MOV_SERV);
            final BluetoothGattCharacteristic motionConfigChar = motionService.getCharacteristic(UUID_MOV_CONF);
            final BluetoothGattCharacteristic motionDataChar = motionService.getCharacteristic(UUID_MOV_DATA);

            writes.add(new Runnable() {
                public void run() {
                    Log.d(TAG, "Local Enable: " + gatt.setCharacteristicNotification(motionDataChar, true));//Enabled locally
                    a.progress(40);

                    BluetoothGattDescriptor config = motionDataChar.getDescriptor(CCC);
                    config.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    Log.d(TAG, "Remote Enable: " + gatt.writeDescriptor(config));//Enabled remotely
                    a.progress(50);
                }
            });
            writes.add(new Runnable() {
                public void run() {
                    motionService.getCharacteristic(UUID_MOV_PERI).setValue(new byte[]{0b0});
                    Log.d(TAG, "Sensor on: " + gatt.writeCharacteristic(motionService.getCharacteristic(UUID_MOV_PERI)));
                }
            });
            writes.add(new Runnable() {
                public void run() {
                    motionConfigChar.setValue(ALL_MOTION);
                    Log.d(TAG, "Sensor on: " + gatt.writeCharacteristic(motionConfigChar));
                    a.progress(70);
                }
            });
            new Handler(getMainLooper()).post(writes.poll());
        }
    }

    private String string(byte[] data) {
        String s = "";
        for(byte b : data){
            s+=b+" ,";
        }
        return s;
    }

    public void register(Activity a){
        this.a = (RecordActivity) a;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if(bg!= null) {
            bg.disconnect();
            bg.close();
        }
        return super.onUnbind(intent);
    }

    float gyroConvert(short data){
        return (float)data/(32768F/500F);//((data * 1.0D) / (65536D / 500D));
    }
    float accConvert(int data){//assumes acceleration in range -2, +2
        return data / (32768F/8F);
    }
    float magConvert(int data){
        return data / (32768F/2450F); // documentation and code disagree here
    }
}
