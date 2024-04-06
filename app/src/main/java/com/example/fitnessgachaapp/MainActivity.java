package com.example.fitnessgachaapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        ImageView imageView = findViewById(R.id.pixel_art_view);
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.beer);
        Bitmap scaledBitmap = scalePixelArt(originalBitmap, 5);
        imageView.setImageBitmap(scaledBitmap);
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
