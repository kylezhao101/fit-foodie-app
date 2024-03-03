package com.example.fitnessgachaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
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

import android.location.Location;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int REQUEST_LOCATION_PERMISSION = 1; // Location permission request code
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private GoogleMap googleMap;
    private ArrayList<Object> pathPoints;
    private long startTime;
    private float totalDistance;
    private Location previousLocation;
    private boolean isTrackingStarted = false;
    private Marker currentUserLocationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        pathPoints = new ArrayList<>();

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
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("MapReady", "Map is ready and adding marker.");
            this.googleMap = googleMap;
            LatLng markerPosition = new LatLng(10,10);
            googleMap.addMarker(new MarkerOptions().position(markerPosition).title("Marker Location"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerPosition,5));
    }

    // Location request and update methods ---------------------------------------------------------

    private void createLocationRequest() {
        locationRequest = new LocationRequest.Builder(5000)
                .build();
    }
    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Log.e("location result","failed");
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (!isTrackingStarted) {
                        // This is the first location update
                        isTrackingStarted = true;
                        previousLocation = location; // Initialize previousLocation on first update
                    } else {
                        // Calculate distance and speed using previousLocation
                        float distanceBetween = previousLocation.distanceTo(location); // Distance in meters
                        totalDistance += distanceBetween;
                        float speed = (location.getSpeed() * 3600) / 1000; // Convert m/s to km/h

                        //update UI
                    }

                    previousLocation = location;

                    //If currentLocation is known, set the map market postition and camera to it.
                    LatLng userPosition = new LatLng(location.getLatitude(), location.getLongitude());
                    pathPoints.add(userPosition);

                    // Update or initialize the marker
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
        isTrackingStarted = false; // Reset tracking flag
        startTime = System.currentTimeMillis(); // Reset start time
        startLocationUpdates(); // Start location updates
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