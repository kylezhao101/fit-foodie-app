package com.example.fitnessgachaapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize DatabaseHelper
        databaseHelper = new DatabaseHelper(this);
        List<GachaItem> gachaCollection = databaseHelper.getAllGachaItems();

        // Setup the RecyclerView with a GridLayoutManager
        RecyclerView recyclerView = findViewById(R.id.gachaView);
        int numberOfColumns = 3;
        recyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        recyclerView.setAdapter(new GachaAdapter(gachaCollection, this));

        TextView userCaloriesView = findViewById(R.id.userCalories);
        float totalCalories = databaseHelper.getUserTotalCalories();
        userCaloriesView.setText(String.valueOf((int) totalCalories));

        TextView gachaCollectedView = findViewById(R.id.gachaCollected);
        // Count total available gacha items from JSON
        int totalAvailableItems = countItemsFromJson(this, "drawable_items.json");

        int uniqueCollectedItems = databaseHelper.getUniqueGachaItemCount();
        gachaCollectedView.setText(uniqueCollectedItems + "/" + totalAvailableItems);

        // Setup BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Intent intent = null;
            if (item.getItemId() == R.id.navigation_home) {
                return false;
            } else if (item.getItemId() == R.id.navigation_tracker) {
                intent = new Intent(MainActivity.this, TrackingActivity.class);
            } else if (item.getItemId() == R.id.navigation_profile) {
                intent = new Intent(MainActivity.this, ProfileActivity.class);
            } else if (item.getItemId() == R.id.navigation_gacha) {
                intent = new Intent(MainActivity.this, GachaActivity.class);
            }

            if (intent != null) {
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
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
}
