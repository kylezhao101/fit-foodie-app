package com.example.fitnessgachaapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class Home extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        Button trackingButton = findViewById(R.id.trackingButton);
        Button gachaButton = findViewById(R.id.gachaButton);
        Button profileButton = findViewById(R.id.profileButton);

        trackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Tracker activity
                Intent intent = new Intent(Home.this, MainActivity.class);
                startActivity(intent);
            }
        });

        gachaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Tracker activity
                Intent intent = new Intent(Home.this, GachaActivity.class);
                startActivity(intent);
            }
        });

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Tracker activity
                Intent intent = new Intent(Home.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

    }


}
