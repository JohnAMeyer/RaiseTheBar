package edu.nd.raisethebar;

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
    private static final byte[] ALL_MOTION = {0b1111111,0b0};
    private static final byte[] NOTIFY = {0b1,0b0};
    private Queue<Runnable> writes = new LinkedList<>();


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


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private class BLECallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG,"Connected");
                gatt.discoverServices();
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
            super.onCharacteristicChanged(gatt, characteristic);
            //Data received
            byte[] data = characteristic.getValue();
            float gyrX =  gyroConvert((data[1] <<8) + data[0]);
            float gyrY =  gyroConvert((data[3] <<8) + data[2]);
            float gyrZ =  gyroConvert((data[5] <<8) + data[4]);

            float accX =  accConvert((data[7] <<8) + data[6]);
            float accY =  accConvert((data[9] <<8) + data[8]);
            float accZ =  accConvert((data[11] <<8) + data[10]);

            float magX =  magConvert((data[13] <<8) + data[12]);
            float magY =  magConvert((data[15] <<8) + data[14]);
            float magZ =  magConvert((data[17] <<8) + data[16]);

            Log.d(TAG,"{"+gyrX + " " + gyrY + " " + gyrZ+"},{"+accX + " " + accY + " " + accZ+"},{"+magX + " " + magY + " " + magZ+"}");
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


            final BluetoothGattService motionService = gatt.getService(UUID_MOV_SERV);
            final BluetoothGattCharacteristic motionConfigChar = motionService.getCharacteristic(UUID_MOV_CONF);
            final BluetoothGattCharacteristic motionDataChar = motionService.getCharacteristic(UUID_MOV_DATA);

            writes.add(new Runnable() {
                public void run() {
                    Log.d(TAG, "Local Enable: " + gatt.setCharacteristicNotification(motionDataChar, true));//Enabled locally

                    BluetoothGattDescriptor config = motionDataChar.getDescriptor(CCC);
                    config.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    Log.d(TAG, "Remote Enable: " + gatt.writeDescriptor(config));//Enabled remotely
                }
            });
            writes.add(new Runnable() {
                public void run() {
                    motionConfigChar.setValue(ALL_MOTION);
                    Log.d(TAG, "Sensor on: " + gatt.writeCharacteristic(motionConfigChar));
                }
            });

            new Handler(getMainLooper()).post(writes.poll());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bg.close();
    }
    float gyroConvert(int data){
        return (float)((data * 1.0D) / (65536D / 500D));
    }
    float accConvert(int data){//assumes acceleration in range -2, +2
        return (float)((data * 1.0D) / (32768/2));
    }
    float magConvert(int data){
        return 1.0F * data;
    }
}
