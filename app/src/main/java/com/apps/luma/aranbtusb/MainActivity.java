package com.apps.luma.aranbtusb;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import Plugin.Serial;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void usbConnectionClicked(View view) {
        Intent i = new Intent(this, SerialActivity.class);
        i.putExtra("SerialComType", Serial.SerialComType.BLUETOOTH);
        startActivity(i);
    }

    public void btConnectionClicked(View view) {
        Intent i = new Intent(this, SerialActivity.class);
        i.putExtra("SerialComType", Serial.SerialComType.BLUETOOTH);
        startActivity(i);
    }
}
