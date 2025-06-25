package com.example.hydrationgarden.models;

public class WaterIntake {
    private String id;
    private String userId;
    private int amount; // ml
    private long timestamp;
    private String date;

    public WaterIntake() {
    }

    public WaterIntake(String userId, int amount, String date) {
        this.userId = userId;
        this.amount = amount;
        this.date = date;
        this.timestamp = System.currentTimeMillis();
    }

    public WaterIntake(String id, String userId, int amount, String date) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.date = date;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}