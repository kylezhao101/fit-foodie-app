package com.example.fitnessgachaapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class GachaActivity extends AppCompatActivity implements SensorEventListener {
    //will use SQlite to pull from image database for gacha characters.
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private ProgressBar summonBar;
    private Button summonButton;
    private Button calorieButton;
    private TextView textMove;
    private TextView gachaTitle;
    private TextView caloriesText;
    private boolean sensorOn = false;
    private long startTime = 0;
    private DatabaseHelper databaseHelper;
    private static final int CALORIE_COST_PER_PULL = 200;
    private MediaPlayer done;
    private MediaPlayer cooking;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gacha_page);

        //Setup Sensor Manager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        databaseHelper = new DatabaseHelper(this);

        //layout setup
        summonButton = findViewById(R.id.summonButton);
        calorieButton = findViewById(R.id.calorieButton);
        textMove =  findViewById(R.id.textMove);
        gachaTitle =  findViewById(R.id.gachaTextView);
        caloriesText =  findViewById(R.id.calorieTextView);
        Typeface Minecraft = ResourcesCompat.getFont(this, R.font.minecraft_font);
        gachaTitle.setTypeface(Minecraft);
        textMove.setTypeface(Minecraft);
        caloriesText.setTypeface(Minecraft);
        summonBar = findViewById(R.id.summonBar);
        done = MediaPlayer.create(this, R.raw.cook);
        cooking = MediaPlayer.create(this, R.raw.cooking);


        databaseHelper = new DatabaseHelper(this);
        List<GachaItem> gachaCollection = databaseHelper.getAllGachaItems();



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

        //display the available calories for pulling from the database
        float totalCalories = databaseHelper.getUserTotalCalories();
        String caloriesString = String.valueOf((int) totalCalories);
        updateCaloriesText();

        //button to turn on the sensor
        summonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if the user has enough calories to summon
                if (databaseHelper.getUserTotalCalories() >= Math.abs(CALORIE_COST_PER_PULL)) {
                    sensorOn = !sensorOn; // Toggle the sensor state only if enough calories
                    if (sensorOn) {
                        // Register the sensor listener to start detecting shakes
                        sensorManager.registerListener(GachaActivity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                        textMove.setText("Shake to Cook!");
                    } else {
                        // Unregister the sensor listener to stop detecting shakes
                        sensorManager.unregisterListener(GachaActivity.this);
                        textMove.setText("N/A");
                    }
                } else {
                    // If not enough calories, show a toast and do not toggle sensor state
                    notifyUser("Not enough calories to summon. Burn more calories!");
                }
            }
        });

        gachaTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseHelper.updateUserTotalCalories(+100);
                updateCaloriesText();
            }
        });

    }

    private void updateCaloriesText() {
        float totalCalories = databaseHelper.getUserTotalCalories();
        String caloriesString = String.valueOf((int) totalCalories);
        caloriesText.setText("Calories per pull: " +CALORIE_COST_PER_PULL +"\nAvailable Calories: " + caloriesString);
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

                long currentTime = System.currentTimeMillis();
                long elapsedTime = currentTime - startTime;
                    // Update progress bar based on shaking duration
                    if (summonBar.getProgress() == 100 && databaseHelper.getUserTotalCalories() >0) {
                        summonBar.setProgress(100);
                        done.start();
                        Intent intent = new Intent(GachaActivity.this, PullActivity.class);
                        startActivity(intent);
                    }
                    else{
                        int progress = (int) (100 * elapsedTime / 600);
                        summonBar.setProgress(progress);
                        cooking.start();
                    }
            } else {
                // if phone is still
                startTime = System.currentTimeMillis();
            }
        }
    }

    private int countItemsFromJson(Context context, String filename) {
        try {
            // Open the drawable_items.json file from assets folder
            InputStream is = context.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            // Convert the buffer into a string
            String json = new String(buffer, "UTF-8");

            // Parse the JSON string to get the count of items
            JSONArray jsonArray = new JSONArray(json);
            return jsonArray.length(); // Return the number of items
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return 0; // In case of an error, return 0
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

    private void notifyUser(String message) {
        runOnUiThread(() -> Toast.makeText(GachaActivity.this, message, Toast.LENGTH_SHORT).show());
    }

    public static Bitmap scalePixelArt(Bitmap bitmap, int scale) {
        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap.getWidth() * scale, bitmap.getHeight() * scale, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(scaledBitmap);
        Paint paint = new Paint();
        paint.setFilterBitmap(false); // Disables bilinear filtering
        canvas.scale(scale, scale);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return scaledBitmap;
    }
}
