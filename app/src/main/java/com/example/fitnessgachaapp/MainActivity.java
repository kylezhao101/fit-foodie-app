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

    // Speed calculation
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private boolean isAccelerometerSensorAvailable, itIsNotFirstTime = false;
    private float currentSpeed = 0f;
    private float lastX = 0, lastY = 0, lastZ = 0;

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

        // initialize sensor services
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) { // check if device has accelerometer
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            isAccelerometerSensorAvailable = true;
        } else {
            isAccelerometerSensorAvailable = false;
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
        if (isAccelerometerSensorAvailable) {
            sensorManager.registerListener(accelerometerListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (!running) {
            chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
            chronometer.start();
            running = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isAccelerometerSensorAvailable) {
            sensorManager.unregisterListener(accelerometerListener);
        }
        if (running) {
            chronometer.stop();
            pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
            running = false;
        }
    }

    // Device speed tracking -----------------------------------------------------------------------
    private final SensorEventListener accelerometerListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {

            if (itIsNotFirstTime) { // first time flag for edge case
                float xDifference = Math.abs(lastX - event.values[0]);
                float yDifference = Math.abs(lastY - event.values[1]);
                float zDifference = Math.abs(lastZ - event.values[2]);

                // filter minor movements if needed
                float NOISE = (float) 0.0;
                if (xDifference > NOISE || yDifference > NOISE || zDifference > NOISE) {
                    // Calculate speed using the change in acceleration
                    currentSpeed = (xDifference + yDifference + zDifference) / 3;

                    // Update UI
                    runOnUiThread(() -> {
                        sessionSpeedView.setText(String.format(Locale.US, "%.2f km/h", currentSpeed));
                        sessionDistanceView.setText(String.format(Locale.US, "%.2f m", totalDistance));
                    });

                }
            }
            lastX = event.values[0];
            lastY = event.values[1];
            lastZ = event.values[2];
            itIsNotFirstTime = true;
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

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
        locationRequest = new LocationRequest.Builder(5000)
                .build();
    }
    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    //If currentLocation is known, set the map market position and camera to it.
                    LatLng userPosition = new LatLng(location.getLatitude(), location.getLongitude());
                    pathPoints.add(userPosition);

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