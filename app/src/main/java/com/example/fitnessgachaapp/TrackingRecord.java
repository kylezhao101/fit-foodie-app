package com.example.fitnessgachaapp;

public class TrackingRecord {
    private String date; // Date of the activity
    private float distance; // Distance covered
    private float calories; // Calories burned
    private final long duration; // Duration of the activity

    // Constructor
    public TrackingRecord(String date, float distance, float calories, long duration) {
        this.date = date;
        this.distance = distance;
        this.calories = calories;
        this.duration = duration;
    }

    // Getters and Setters
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public float getCalories() {
        return calories;
    }

    public void setCalories(float calories) {
        this.calories = calories;
    }

    public long getDuration() {
        return duration;
    }
}
