package com.example.hydrationgarden.models;

public class User {
    private String uid;
    private String email;
    private String name;
    private int dailyGoal; // ml
    private long createdAt;

    public User() {
    }

    public User(String uid, String email, String name) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.dailyGoal = 2000; // default 2L
        this.createdAt = System.currentTimeMillis();
    }

    public User(String uid, String email, String name, int dailyGoal) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.dailyGoal = dailyGoal;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getDailyGoal() { return dailyGoal; }
    public void setDailyGoal(int dailyGoal) { this.dailyGoal = dailyGoal; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}