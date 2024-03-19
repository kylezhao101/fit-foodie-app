package com.example.fitnessgachaapp;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class GachaActivity extends AppCompatActivity implements SensorEventListener {
    //will use SQlite to pull from image database for gacha characters.
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private ProgressBar summonBar;
    private Button summonButton;
    private TextView textMove;
    private boolean sensorOn = false;
    private long startTime = 0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gacha_page);

        //Setup Sensor Manager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //layout setup
        summonButton = findViewById(R.id.summonButton);
        textMove = (TextView) findViewById(R.id.textMove);
        summonBar = findViewById(R.id.summonBar);


        // Setup BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_gacha);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Intent intent = null;
            if (item.getItemId() == R.id.navigation_home) {
                intent = new Intent(GachaActivity.this, MainActivity.class);
            } else if (item.getItemId() == R.id.navigation_tracker) {
                intent = new Intent(GachaActivity.this, TrackingActivity.class);
            } else if (item.getItemId() == R.id.navigation_profile) {
                intent = new Intent(GachaActivity.this, ProfileActivity.class);
            } else if (item.getItemId() == R.id.navigation_gacha) {
                return false;
            }
            if (intent != null) {
                startActivity(intent);
                return true;
            }
            return false;
        });






        //button to turn on the sensor
        summonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorOn = !sensorOn; // Toggle the state
                if (sensorOn) {
                    sensorManager.registerListener(GachaActivity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                } else {
                    sensorManager.unregisterListener(GachaActivity.this);
                    textMove.setText("N/A");
                }
            }
        });

    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if(sensorOn){
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

            // similar code from lab4 sensor test
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];


            // Calculate the magnitude of acceleration
            double acceleration = Math.sqrt(x * x + y * y + z * z);
            if (acceleration > 9.9) {
                // if phone is moving
                textMove.setText("Moving");
                //textMove.setText(String.valueOf(magnitude));

                long currentTime = System.currentTimeMillis();
                long elapsedTime = currentTime - startTime;
                if (elapsedTime >= 3000) { // 3 seconds
                    // Enough shaking, stop the progress bar or perform other actions
                    summonBar.setProgress(100);
                } else {
                    // Update progress bar based on shaking duration
                    int progress = (int) (100 * elapsedTime / 3000);
                    summonBar.setProgress(progress);
                }
            } else {
                // if phone is still
                startTime = System.currentTimeMillis();
                textMove.setText("Stationary");
            }
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
}
