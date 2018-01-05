package com.apps.luma.serial_bt_usb;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Bluetooth Serial Conncetion. The device to connect must be named either HC-06 or HC-05
 */

public class Bluetooth extends Serial {

    private final String HC_06 = "HC-06";
    private final String HC_05 = "HC-05";
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//Serial Port Service ID
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private BluetoothAdapter bluetoothAdapter;
    private OutputStream outputStream;
    private InputStream inputStream;
    private boolean stopThread;
    private boolean manualDisconnect;

    public Bluetooth(Activity activity, SerialCallback serialCallback) {

        super(activity, serialCallback, SerialComType.BLUETOOTH);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        activity.registerReceiver(broadcastReceiver, filter);

    }


    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                    startConnections();
                } else if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
                    BTinit();
                }
            } else if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice temp = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (temp.getName() == HC_05 || temp.getName() == HC_06) {
                    Log.i("Bluetooth", "Found an " + temp.getName() + "!");
                    connect(temp);
                }
            } else if (intent.getAction().equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {

                if(!manualDisconnect) {
                    serialCallback.onErrorConnecting(SerialCallback.ErrorType.BT_DEVICE_DISCONNECTED);
                    manualDisconnect = false;
                }
            }

        }
    };


    private void BTinit() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            serialCallback.onErrorConnecting(SerialCallback.ErrorType.NO_BLUETOOTH_ADAPTER_FOUND);
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivity(enableAdapter);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            startConnections();
        }

    }


    private boolean BTconnect() {
        boolean connected = true;
        try {
            if (device != null) {
                if (socket != null) {
                    socket.close();
                }
                socket = device.createRfcommSocketToServiceRecord(PORT_UUID);
                socket.connect();

            } else {
                serialCallback.onErrorConnecting(SerialCallback.ErrorType.BT_DEVICE_IS_NULL);
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("Bluetooth", "Creacion del socket fallo...?");
            serialCallback.onErrorConnecting(SerialCallback.ErrorType.FAILED_TO_CREATE_BT_SOCKET);
            connected = false;
        }
        if (connected) {
            try {
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                Log.i("Bluetooth", "Output Socked se rompio...?");
                e.printStackTrace();
            }
            try {
                inputStream = socket.getInputStream();
            } catch (IOException e) {
                Log.i("Bluetooth", "Input Socked se rompio...?");
                e.printStackTrace();
            }

        }

        return connected;
    }

    private void beginListenForData() {
        stopThread = false;
        Thread thread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopThread) {
                    try {
                        int byteCount = inputStream.available();
                        if (byteCount > 0) {
                            byte[] rawBytes = new byte[byteCount];
                            inputStream.read(rawBytes);
                            final String string = new String(rawBytes, "UTF-8");
                            serialCallback.onDataReceived(string);
                        }
                    } catch (IOException ex) {
                        stopThread = true;
                        Log.i("Bluetooth", "Thread Interrupted");
                    }
                }
            }
        });

        thread.start();
    }

    private void startConnections() {

        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        if (bondedDevices.isEmpty()) {
            Toast.makeText(activity.getApplicationContext(), "Please Pair the Device first", Toast.LENGTH_SHORT).show();
        } else {
            for (BluetoothDevice iterator : bondedDevices) {
                Log.i("Bluetooth", "Iterating. Device: " + iterator.getAddress());
                if (iterator.getName().equals(HC_05) || iterator.getName().equals(HC_06)) {
                    connect(iterator);
                    break;
                }
            }
        }
    }

    private void connect(BluetoothDevice btd) {
        device = btd;
        if (BTconnect()) {
            beginListenForData();
            serialCallback.onConnectionStablished();
            Log.i("Bluetooth", "Conneccion establecida con dispositivo: " + btd.getName());
        }
    }

    public void start() {
        manualDisconnect = false;
        BTinit();
    }

    public void send(String text) {
        String string = text;
        string.concat("\n");
        try {
            outputStream.write(string.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        stopThread = true;
        try {
            if (outputStream != null)
                outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (inputStream != null)
                inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        manualDisconnect = true;
        serialCallback.onConnectionFinished();

    }

    public void destroy() {
        if (activity != null && broadcastReceiver != null)
            activity.unregisterReceiver(broadcastReceiver);
    }
}
