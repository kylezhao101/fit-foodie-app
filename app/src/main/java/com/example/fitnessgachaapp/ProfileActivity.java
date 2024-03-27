package com.example.fitnessgachaapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class ProfileActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TrackingRecordAdapter adapter;
    private List<TrackingRecord> trackingRecords;
    private DatabaseHelper databaseHelper;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);

        databaseHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.TrackingHistory);
        trackingRecords = databaseHelper.getAllTrackingRecords();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TrackingRecordAdapter(this, trackingRecords);
        recyclerView.setAdapter(adapter);

        // Setup BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_profile);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Intent intent = null;
            if (item.getItemId() == R.id.navigation_home) {
                intent = new Intent(ProfileActivity.this, MainActivity.class);
            } else if (item.getItemId() == R.id.navigation_tracker) {
                intent = new Intent(ProfileActivity.this, TrackingActivity.class);
            } else if (item.getItemId() == R.id.navigation_profile) {
                return false;
            } else if (item.getItemId() == R.id.navigation_gacha) {
                intent = new Intent(ProfileActivity.this, GachaActivity.class);
            }

            if (intent != null) {
                startActivity(intent);
                return true;
            }
            return false;
        });

        EditText editTextWeight = findViewById(R.id.editTextWeight);
        Button saveWeightButton = findViewById(R.id.saveWeightButton);
        Button clearHistoryButton = findViewById(R.id.clearButton);
        clearHistoryButton.setOnClickListener(view -> {
            clearTrackingHistory();
        });

        loadAndDisplayUserWeight();
        saveWeightButton.setOnClickListener(view -> {
            String weight = editTextWeight.getText().toString();
            if (!weight.isEmpty()) {
                saveWeightToSharedPreferences(weight);
                Toast.makeText(ProfileActivity.this, "Saved: " + weight + " kg", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveWeightToSharedPreferences(String weightString) {
        float weight;
        try {
            weight = Float.parseFloat(weightString);
        } catch (NumberFormatException e) {
            weight = 70.0f;
            Log.e("saveWeight", "Error parsing weight string to float", e);
        }

        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("userWeight", weight);
        editor.apply();
    }
    private void loadAndDisplayUserWeight() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        // Retrieve the weight as a float. The second parameter is the default value if the key is not found.
        float weight = sharedPreferences.getFloat("userWeight", 70.0f);
        EditText editTextWeight = findViewById(R.id.editTextWeight);

        // Set the weight as the EditText content
        editTextWeight.setText(String.valueOf(weight));
    }

    @SuppressLint("NotifyDataSetChanged")
    private void clearTrackingHistory() {
        databaseHelper.clearTrackingHistory();

        trackingRecords.clear();
        adapter.notifyDataSetChanged();

        Toast.makeText(ProfileActivity.this, "Tracking history cleared", Toast.LENGTH_SHORT).show();
    }
}
