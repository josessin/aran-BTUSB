package com.apps.luma.serial_bt_usb;

import android.app.Activity;

/**
 * Base class for USB and Bluetooth.
 * Defines methods to interact with the device
 */

public abstract class Serial {

    public enum SerialComType{
        BLUETOOTH,
        USB
    }
    protected SerialComType serialComType;
    protected Activity activity;
    protected SerialCallback serialCallback;

    public Serial(Activity activity, SerialCallback serialCallback, SerialComType serialComType){
        this.activity = activity;
        this.serialCallback = serialCallback;
        this.serialComType= serialComType;
    }

    /**
     * Attempt to connect with Serial device. Begin the connection
     */
    public abstract void start();
    /**
     * Disconnect with Serial device. Close the connection
     */
    public abstract void stop();
    /**
     * Send data to connected device
     */
    public abstract void send(String text);
    /**
     * Destroy should be called to free the resources.
     */
    public abstract void destroy();

}
