package com.example.fitnessgachaapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.widget.Chronometer;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TrackingService extends Service {

    public static final String CHANNEL_ID = "LocationServiceChannel";
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private float speedKilometersPerHour = 0;
    private float weight = 70;

    private long startTimeMillis;
    private long endTimeMillis;
    float caloriesBurned = 0;
    private long sessionDuration;
    private float totalDistance;
    private Location previousLocation;

    private NotificationManager notificationManager;
    private int notificationId = 1;

    public static final String ACTION_START_TRACKING = "com.example.fitnessgachaapp.action.START_TRACKING";
    public static final String ACTION_STOP_TRACKING = "com.example.fitnessgachaapp.action.STOP_TRACKING";

    public TrackingService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        loadUserWeight();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable updateNotificationRunnable = new Runnable() {
        @Override
        public void run() {
            // Update the notification with the current elapsed time
            long elapsedTimeMillis = SystemClock.elapsedRealtime() - startTimeMillis;
            updateNotification("Tracking", totalDistance, speedKilometersPerHour, caloriesBurned, elapsedTimeMillis);
            // Schedule the next update in 1 second
            handler.postDelayed(this, 1000);
        }
    };


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Ensure notification channel is created and notification is set up immediately.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }
        // Default notification setup.
        updateNotification("Starting Tracking", 0, 0, 0, 0);

        // Now handle the intent action.
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_START_TRACKING.equals(action)) {
                startTracking();
            } else if (ACTION_STOP_TRACKING.equals(action)) {
                stopTracking();
            }
        }
        return START_NOT_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );

            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(serviceChannel);
        }
    }

    private void updateNotification(String contentText, float distance, float speed, float caloriesBurned, long elapsedTimeMillis) {
        // Builds the notification and issues the startForeground command immediately.
        String elapsedTimeFormatted = formatElapsedTime(elapsedTimeMillis);

        Intent notificationIntent = new Intent(this, TrackingActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        String notificationContent = String.format(Locale.getDefault(), "Time: %s, Dist: %.2f m, Speed: %.2f km/h, Calories: %.2f", elapsedTimeFormatted, distance, speed, caloriesBurned);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Tracking Your Activity")
                .setContentText(notificationContent)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .build();

        // This needs to be called within 5 seconds of the service being started.
        startForeground(notificationId, notification);
    }


    private String formatElapsedTime(long millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(hours);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis));
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void startTracking() {
        createLocationRequest();
        startTimeMillis = SystemClock.elapsedRealtime();
        handler.post(updateNotificationRunnable);
        totalDistance = 0; // Reset total distance
        previousLocation = null;
        caloriesBurned = 0;

    }

    private void stopTracking() {
        handler.removeCallbacks(updateNotificationRunnable);
        stopForeground(true);
        endTimeMillis = System.currentTimeMillis();
        sessionDuration = endTimeMillis - startTimeMillis;
    }

    private void createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(250);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationCallback = new LocationCallback() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                LatLng userPosition = null;
                for (Location location : locationResult.getLocations()) {
                    userPosition = new LatLng(location.getLatitude(), location.getLongitude());
                    if (location.hasSpeed()) {
                        // Convert speed from meters per second to kilometers per hour
                        float speedMetersPerSecond = location.getSpeed();
                        speedKilometersPerHour = speedMetersPerSecond * 3.6f;
                        if (speedKilometersPerHour < 0.5f) {
                            speedKilometersPerHour = 0;
                        }
                    } else {
                        speedKilometersPerHour = 0;
                    }
                    if (previousLocation != null && speedKilometersPerHour > 0) {
                        totalDistance += previousLocation.distanceTo(location); // Calculate distance in meters
                    }
                    previousLocation = location;
                }
                updateCaloriesBurned();
                Intent intent = new Intent("TrackingUpdate");
                intent.putExtra("Speed", speedKilometersPerHour);
                intent.putExtra("Distance", totalDistance);
                intent.putExtra("CaloriesBurned", caloriesBurned);
                intent.putExtra("Position", userPosition);
                sendBroadcast(intent);
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    };
    private void updateCaloriesBurned() {
        long elapsedRealtimeMillis = SystemClock.elapsedRealtime() - startTimeMillis;
        float durationInHours = elapsedRealtimeMillis / 3600000.0f;
        float dynamicMET = getMETFromSpeed(speedKilometersPerHour);
        caloriesBurned = dynamicMET * weight * durationInHours;
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

    private void loadUserWeight() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        // Assuming default weight is 70 if not found in SharedPreferences
        weight = sharedPreferences.getFloat("userWeight", 70.0f);
    }
}