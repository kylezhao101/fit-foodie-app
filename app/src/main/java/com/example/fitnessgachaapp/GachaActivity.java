package com.example.fitnessgachaapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class GachaActivity extends AppCompatActivity implements SensorEventListener {
    //will use SQlite to pull from image database for gacha characters.
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gacha_page);

    }
    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
