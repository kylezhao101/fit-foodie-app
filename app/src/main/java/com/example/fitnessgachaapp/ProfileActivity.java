package com.example.fitnessgachaapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;
import java.util.Locale;

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


    }
}
