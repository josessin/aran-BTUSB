package com.apps.luma.serial_bt_usb;

/**
 * Interface to implement and get various callbacks from the Serial (bt or USB) comunications
 */
public interface SerialCallback {

    enum ErrorType{
        PORT_NOT_OPEN,
        PORT_IS_NULL,
        /**
         * The android user has not granted extra permissions to connect via USB
         */
        PERM_NOT_GRANTED,
        /**
         * The device id does not match with the 'expectedDeviceID' (def: 0x2341)
         * you can change this via the 'setExpectedDeviceID(int expectedDeviceID)' method
         */
        DEVICE_VENDOR_ID_INCORRECT,
        /**
         * This error usually occurs when the paired device is not on
         * Else, you should attempt to reconnect, sometimes sockets.connect() fails.
         */
        FAILED_TO_CREATE_BT_SOCKET,
        /**
         *This android device has no bluetooth support
         */
        NO_BLUETOOTH_ADAPTER_FOUND,
        /**
         *The remote device has been turned off or disconnected.
         */
        BT_DEVICE_DISCONNECTED,
        /**
         * The bt device is null. Unknown reasons, might be a bug
         */
        BT_DEVICE_IS_NULL,
        /**
         *There is no USB device connected to this android device
         */
        NO_DEVICE_CONNECTED
    }
    /**
     * Callback were the data received is sent when USB or BT device sends data
     */
    abstract void onDataReceived(String data);
    /**
     *Connected with device callback
     */
    abstract void onConnectionStablished();
    /**
     *Connection finished
     */
    abstract void onConnectionFinished();
    /**
     * Error during or in the attemp to connect to the device,
     * @param : The type of error as described in ErrorTupe
     */
    abstract void onErrorConnecting(ErrorType type);
}
