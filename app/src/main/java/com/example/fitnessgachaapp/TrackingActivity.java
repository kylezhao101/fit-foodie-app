package com.example.fitnessgachaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.location.Location;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;


public class TrackingActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int REQUEST_LOCATION_PERMISSION = 1; // Location permission request code
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    // UI
    private GoogleMap googleMap;
    private ArrayList<LatLng> pathPoints; // to be used for distance and session paths

    private float totalDistance;
    private Location previousLocation; // to be used for distance and session paths
    private Marker currentUserLocationMarker;
    private float speedKilometersPerHour = 0;

    private float weight = 70; // also used for calorie calculation, will be asked in future implementation.

    // UI Elements
    private TextView sessionSpeedView, sessionDistanceView, sessionCalorieView;
    private Chronometer chronometer;
    private long pauseOffset;
    private boolean running; // flag for chronometer
    Button startButton;
    Button stopButton;
    float caloriesBurned = 0;

    // Activity lifecycle methods ------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracking_page);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        pathPoints = new ArrayList<>(); // to be used for distance and session paths

        // initialize location services
        createLocationRequest();
        createLocationCallback();

        if (!hasLocationPermission()) {
            requestLocationPermission();
        } else {
            startLocationUpdates();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialize UI elements
        sessionSpeedView = findViewById(R.id.sessionSpeed);
        sessionDistanceView = findViewById(R.id.sessionDistance);
        chronometer = findViewById(R.id.chronometer);
        sessionCalorieView = findViewById(R.id.sessionCalories);
        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTracking();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopTracking();
            }
        });
        stopButton.setEnabled(false);

        // Setup BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_tracker);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Intent intent = null;
            if (item.getItemId() == R.id.navigation_home) {
                intent = new Intent(TrackingActivity.this, MainActivity.class);
            } else if (item.getItemId() == R.id.navigation_tracker) {
                return false;
            } else if (item.getItemId() == R.id.navigation_profile) {
                intent = new Intent(TrackingActivity.this, ProfileActivity.class);
            } else if (item.getItemId() == R.id.navigation_gacha) {
                intent = new Intent(TrackingActivity.this, GachaActivity.class);
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

    @Override
    protected void onPause() {
        super.onPause();
        if (running) {
            chronometer.stop();
            pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
            running = false;
        }
    }
    // Calorie handling ----------------------------------------------------------------------------
    private void updateCaloriesBurned() {
        long elapsedRealtimeMillis = SystemClock.elapsedRealtime() - chronometer.getBase();
        float durationInHours = elapsedRealtimeMillis / 3600000.0f; // Convert milliseconds to hours
        float dynamicMET = getMETFromSpeed(speedKilometersPerHour);
        caloriesBurned = dynamicMET * weight * durationInHours;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sessionCalorieView.setText(String.format(Locale.US, "%.2f kcal", caloriesBurned));
            }
        });
    }
    private float getMETFromSpeed(float speedKph) {
        if (speedKph < 0.8) { // Assuming barely moving or stationary
            return 1.0f; // MET value for resting or very light activity
        } else if (speedKph < 3.2) {
            return 2.0f; // Slow walking
        } else if (speedKph <= 6.4) {
            return 3.0f; // Moderate walking
        } else if (speedKph <= 8.0) {
            return 4.3f; // Fast walking
        } else if (speedKph <= 11.3) {
            return 7.0f; // Jogging
        } else {
            return 9.0f; // Running
        }
    }

    // Location request and update methods ---------------------------------------------------------
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("MapReady", "Map is ready and adding marker.");
        this.googleMap = googleMap;
        LatLng markerPosition = new LatLng(10,10);
        googleMap.addMarker(new MarkerOptions().position(markerPosition).title("Marker Location"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerPosition,5));
    }

    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000); // Set the desired interval for active location updates
        locationRequest.setFastestInterval(250); // Set the fastest interval for location updates
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (googleMap != null) {
                        //If currentLocation is known, set the map market position and camera to it.
                        LatLng userPosition = new LatLng(location.getLatitude(), location.getLongitude());
                        pathPoints.add(userPosition);

                        // Check if the location has speed information
                        if (location.hasSpeed()) {
                            // Convert speed from meters per second to kilometers per hour
                            float speedMetersPerSecond = location.getSpeed();
                            speedKilometersPerHour = speedMetersPerSecond * 3.6f;
                            float thresholdKmH = 0.5f;
                            if (speedKilometersPerHour > thresholdKmH) {
                                sessionSpeedView.setText(String.format(Locale.US, "%.2f km/h", speedKilometersPerHour));
                            } else {
                                sessionSpeedView.setText("0.00 km/h");
                            }
                        } else {
                            sessionSpeedView.setText("Speed unavailable");
                        }

                        // Update or initialize the marker for the current user location
                        if (currentUserLocationMarker == null) {
                            currentUserLocationMarker = googleMap.addMarker(new MarkerOptions().position(userPosition).title("Your Location"));
                        } else {
                            currentUserLocationMarker.setPosition(userPosition);
                        }
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userPosition, 17));

                        if (previousLocation != null) {
                            totalDistance += previousLocation.distanceTo(location); // Calculate distance in meters
                        }
                        previousLocation = location;
                    } else {
                        Log.d("LocationUpdate", "GoogleMap not ready for updates.");
                    }

                    // Update UI with total distance
                    runOnUiThread(() -> sessionDistanceView.setText(String.format(Locale.US, "%.2f m", totalDistance)));
                    updateCaloriesBurned();
                }
            }
        };
    }
    private void startTracking() {
        pathPoints.clear(); // Clear previous path points
        totalDistance = 0; // Reset total distance
        previousLocation = null;
        caloriesBurned = 0;

        // Start the chronometer from 0
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
        running = true;

        stopButton.setEnabled(true);
        startButton.setEnabled(false);
        Toast.makeText(this, "Started Tracking", Toast.LENGTH_LONG).show();
        startLocationUpdates(); // Start location updates
    }
    private void stopTracking() {
        long sessionTime = SystemClock.elapsedRealtime() - chronometer.getBase();
        running = false;
        chronometer.stop();

        stopLocationUpdates(); // Stop location updates

        String summaryText = String.format(Locale.US, "Distance: %.2f m, Calories: %.2f kcal, Time: %s",
                totalDistance, caloriesBurned, sessionTime);
        Toast.makeText(this, summaryText, Toast.LENGTH_LONG).show();

        stopButton.setEnabled(false);
        startButton.setEnabled(true);
    }
    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        if (!hasLocationPermission()) return;
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    // Location Permission functions ---------------------------------------------------------------
    /** @noinspection BooleanMethodIsAlwaysInverted*/
    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check request code
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted
                startLocationUpdates();
            } else {
                // Permission denied
            }
        }
    }

}