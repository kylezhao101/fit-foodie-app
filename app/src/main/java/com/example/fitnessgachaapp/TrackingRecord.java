package com.example.fitnessgachaapp;

public class TrackingRecord {
    private String date; // Date of the activity
    private float distance; // Distance covered
    private float calories; // Calories burned

    // Constructor
    public TrackingRecord(String date, float distance, float calories) {
        this.date = date;
        this.distance = distance;
        this.calories = calories;
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
}
