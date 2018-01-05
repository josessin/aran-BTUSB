package com.apps.luma.aranbtusb;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.apps.luma.serial_bt_usb.Bluetooth;
import com.apps.luma.serial_bt_usb.Serial;
import com.apps.luma.serial_bt_usb.SerialCallback;
import com.apps.luma.serial_bt_usb.USB;


public class SerialActivity extends AppCompatActivity implements SerialCallback {

    private Serial serial;
    private Button btnStart,btnStop,btnSend,btnClear;
    private TextView tv;
    private EditText editText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.serial_com);

        Serial.SerialComType sct = (Serial.SerialComType) getIntent().getSerializableExtra("Serial.SerialComType");
        //Create appropriate serial
        if(sct == Serial.SerialComType.USB)
            serial = new USB(this,this);
        else
            serial = new Bluetooth(this,this);
        //Get UIs
        tv = (TextView)findViewById(R.id.textDisplay);
        editText = (EditText)findViewById(R.id.editText);
        btnStart = (Button)findViewById(R.id.buttonStart);
        btnSend = (Button)findViewById(R.id.buttonSend);
        btnClear = (Button)findViewById(R.id.buttonClear);
        btnStop = (Button)findViewById(R.id.buttonStop);
        disableButtons(btnSend,btnStop,btnClear);
        //register events
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStartClicked();
            }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSendClicked();
            }
        });
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClearClicked();
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStopClicked();
            }
        });
        //start immediately
        onStartClicked();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        serial.stop();
        serial.destroy();
    }

    private void onStartClicked(){
        disableButtons(btnStart);
        serial.start();

    }

    private void onSendClicked(){
        String text = editText.getText().toString();
        editText.setText("");
        serial.send(text);
    }

    private void onClearClicked(){
       tv.setText("");

    }

    private void onStopClicked(){
        serial.stop();
        disableButtons(btnStop,btnSend);
        enableButtons(btnStart);
    }


    public void onDataReceived(String data) {
        if(!data.isEmpty() || !data.equals("\n") || data.startsWith(" "))
            tvAppend(tv, "From Arduino: " + data);
    }


    public void onConnectionStablished() {
        tvAppend(tv,R.string.usbConnected);
        enableButtons(btnSend,btnStop,btnClear);

    }


    public void onConnectionFinished() {
        tvAppend(tv,R.string.usbDisconnected);
        disableButtons(btnStop,btnSend);
        enableButtons(btnStart);
    }

    public void onErrorConnecting(ErrorType type) {
        //TODO: switch statement y manejar errores (re-ask permitions, etc)
        disableButtons(btnStop,btnSend);
        tvAppend(tv,type.toString());
        enableButtons(btnStart);
    }


    private void tvAppend(TextView tv, int textID) {
        String ftext = getResources().getString(textID) + "\n";
        tvAppend(tv,ftext);
    }

    private void tvAppend(TextView tv, String text) {
        final TextView ftv = tv;
        final String ftext = text + "\n";

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ftv.append(ftext);
            }
        });
    }

    private void disableButtons(Button...btns){
        for(Button b: btns){
            b.setEnabled(false);
        }
    }
    private void enableButtons(Button...btns){
        for(Button b: btns){
            b.setEnabled(true);
        }
    }
}
