package com.apps.luma.serial_bt_usb;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by Jose Antonini on 02/01/2018.
 */

public class USB extends Serial{
    public final String ACTION_USB_PERMISSION = R.string.app_name + ".USB_PERMISSION";
    private UsbManager usbManager;
    private UsbDevice device;
    private UsbSerialDevice serialPort;
    private UsbDeviceConnection connection;
    private boolean USBIsReady;
    private int expecteddeviceId = 0x2341;
    /**
     * Activity and a usbCallback receiver (can be the same). The data received via serial
     * will be sent to onDataReceived
     */
    public USB(Activity activity, SerialCallback serialCallback){
        super(activity,serialCallback,SerialComType.USB);
        IntentFilter filter = new IntentFilter();
        usbManager = (UsbManager) activity.getSystemService(activity.USB_SERVICE);
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        activity.registerReceiver(broadcastReceiver, filter);
    }

    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() { //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0) {
            String data = null;
            try {
                data = new String(arg0, "UTF-8");
                data.concat("/n");
                serialCallback.onDataReceived(data);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }


        }
    };
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { //Broadcast Receiver to automatically start and stop the Serial connection.

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    connection = usbManager.openDevice(device);
                    serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                    if (serialPort != null) {
                        if (serialPort.open()) { //Set Serial Connection Parameters.

                            serialPort.setBaudRate(9600);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            serialPort.read(mCallback);
                            serialCallback.onConnectionStablished();
                            USBIsReady = true;
                        } else {
                            Log.d("Serial", "PORT NOT OPEN");
                            USBIsReady = false;
                            serialCallback.onErrorConnecting(SerialCallback.ErrorType.PORT_NOT_OPEN);
                        }
                    } else {
                        Log.d("Serial", "PORT IS NULL");
                        USBIsReady = false;
                        serialCallback.onErrorConnecting(SerialCallback.ErrorType.PORT_IS_NULL);
                    }
                } else {
                    Log.d("Serial", "PERM NOT GRANTED");
                    USBIsReady = false;
                    serialCallback.onErrorConnecting(SerialCallback.ErrorType.PERM_NOT_GRANTED);
                }
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                start();
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                stop();
                USBIsReady = false;
            }
        }
    };

    public void start() {

        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                if (deviceVID == expecteddeviceId)//Arduino Vendor ID
                {
                    Intent i = new Intent(ACTION_USB_PERMISSION);
                    PendingIntent pi = PendingIntent.getBroadcast(activity, 0,i, 0);
                    usbManager.requestPermission(device, pi);
                    break;
                } else {
                    connection = null;
                    device = null;
                }
            }
            if(device == null){
                serialCallback.onErrorConnecting(SerialCallback.ErrorType.DEVICE_VENDOR_ID_INCORRECT);
            }
        }
        else{
            serialCallback.onErrorConnecting(SerialCallback.ErrorType.NO_DEVICE_CONNECTED);
        }
    }

    public int getExpecteddeviceId() {
        return expecteddeviceId;
    }

    public void setExpecteddeviceId(int expecteddeviceId) {
        this.expecteddeviceId = expecteddeviceId;
    }

    public void stop() {
        if(serialPort != null)
            serialPort.close();
        serialCallback.onConnectionFinished();

    }

    public void destroy(){
        if(activity != null && broadcastReceiver != null)
            activity.unregisterReceiver(broadcastReceiver);
    }

    public void send(String string) {
        if(USBIsReady)
            serialPort.write(string.getBytes());
    }

}


