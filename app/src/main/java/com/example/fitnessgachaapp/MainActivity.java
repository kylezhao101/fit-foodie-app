package com.example.fitnessgachaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.Manifest;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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

import android.location.Location;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Chronometer;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int REQUEST_LOCATION_PERMISSION = 1; // Location permission request code
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    // UI
    private GoogleMap googleMap;
    private ArrayList<Object> pathPoints; // to be used for distance and session paths

    private float totalDistance;
    private Location previousLocation; // to be used for distance and session paths
    private Marker currentUserLocationMarker;

    // UI Elements
    private TextView sessionSpeedView, sessionDistanceView;
    private Chronometer chronometer;
    private long pauseOffset;
    private boolean running; // flag for chronometer

    // Activity lifecycle methods ------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        // Start tracking session
        startTracking();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!running) {
            chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
            chronometer.start();
            running = true;
        }
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
                    //If currentLocation is known, set the map market position and camera to it.
                    LatLng userPosition = new LatLng(location.getLatitude(), location.getLongitude());
                    pathPoints.add(userPosition);

                    // Check if the location has speed information
                    if (location.hasSpeed()) {
                        // Convert speed from meters per second to kilometers per hour
                        float speedMetersPerSecond = location.getSpeed();
                        float speedKilometersPerHour = speedMetersPerSecond * 3.6f;
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
                }
            }
        };
    }
    private void startTracking() {
        pathPoints.clear(); // Clear previous path points
        totalDistance = 0; // Reset total distance
        startLocationUpdates(); // Start location updates
        if (!running) {
            chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
            chronometer.start();
            running = true;
        }
    }
    private void stopTracking() {
        if (running) {
            chronometer.stop();
            pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
            running = false;
        }
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