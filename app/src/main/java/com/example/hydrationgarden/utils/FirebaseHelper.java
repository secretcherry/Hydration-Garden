package com.example.hydrationgarden.utils;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.Timestamp;
import com.example.hydrationgarden.models.User;
import com.example.hydrationgarden.models.WaterIntake;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class FirebaseHelper {
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private SimpleDateFormat dateFormat;

    public FirebaseHelper() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    }

    public String getCurrentUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public CompletableFuture<String> registerUser(String email, String password, String name) {
        CompletableFuture<String> future = new CompletableFuture<>();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Create user document
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("uid", firebaseUser.getUid());
                            userData.put("email", email);
                            userData.put("name", name);
                            userData.put("dailyGoal", 2000);
                            userData.put("createdAt", Timestamp.now());
                            userData.put("lastLoginAt", Timestamp.now());

                            db.collection("users").document(firebaseUser.getUid())
                                    .set(userData)
                                    .addOnSuccessListener(aVoid -> future.complete(firebaseUser.getUid()))
                                    .addOnFailureListener(future::completeExceptionally);
                        }
                    } else {
                        future.completeExceptionally(task.getException());
                    }
                });

        return future;
    }

    public CompletableFuture<String> loginUser(String email, String password) {
        CompletableFuture<String> future = new CompletableFuture<>();

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            // Update last login
                            db.collection("users").document(user.getUid())
                                    .update("lastLoginAt", Timestamp.now());
                            future.complete(user.getUid());
                        }
                    } else {
                        future.completeExceptionally(task.getException());
                    }
                });

        return future;
    }

    public CompletableFuture<String> addWaterIntake(int amount) {
        CompletableFuture<String> future = new CompletableFuture<>();
        String userId = getCurrentUserId();

        if (userId == null) {
            future.completeExceptionally(new Exception("User not logged in"));
            return future;
        }

        String today = dateFormat.format(new Date());
        String intakeId = UUID.randomUUID().toString();

        Map<String, Object> intakeData = new HashMap<>();
        intakeData.put("id", intakeId);
        intakeData.put("userId", userId);
        intakeData.put("amount", amount);
        intakeData.put("date", today);
        intakeData.put("timestamp", Timestamp.now());
        intakeData.put("createdAt", Timestamp.now());

        db.collection("water_intake").document(intakeId)
                .set(intakeData)
                .addOnSuccessListener(aVoid -> future.complete(intakeId))
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    public CompletableFuture<Integer> getTodayWaterIntake() {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        String userId = getCurrentUserId();

        if (userId == null) {
            future.completeExceptionally(new Exception("User not logged in"));
            return future;
        }

        String today = dateFormat.format(new Date());

        db.collection("water_intake")
                .whereEqualTo("userId", userId)
                .whereEqualTo("date", today)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalAmount = 0;
                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Long amount = document.getLong("amount");
                        if (amount != null) {
                            totalAmount += amount.intValue();
                        }
                    }
                    future.complete(totalAmount);
                })
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    public CompletableFuture<User> getUser() {
        CompletableFuture<User> future = new CompletableFuture<>();
        String userId = getCurrentUserId();

        if (userId == null) {
            future.completeExceptionally(new Exception("User not logged in"));
            return future;
        }

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = new User();
                        user.setUid(documentSnapshot.getString("uid"));
                        user.setEmail(documentSnapshot.getString("email"));
                        user.setName(documentSnapshot.getString("name"));

                        Long dailyGoal = documentSnapshot.getLong("dailyGoal");
                        user.setDailyGoal(dailyGoal != null ? dailyGoal.intValue() : 2000);

                        future.complete(user);
                    } else {
                        future.completeExceptionally(new Exception("User document not found"));
                    }
                })
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }
    public CompletableFuture<Void> createUserStats(String userId) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        Map<String, Object> statsData = new HashMap<>();
        statsData.put("userId", userId);
        statsData.put("totalWaterThisWeek", 0);
        statsData.put("totalWaterThisMonth", 0);
        statsData.put("longestStreak", 0);
        statsData.put("currentStreak", 0);
        statsData.put("daysGoalAchieved", 0);
        statsData.put("lastUpdated", Timestamp.now());

        db.collection("user_stats").document(userId)
                .set(statsData)
                .addOnSuccessListener(aVoid -> future.complete(null))
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    public CompletableFuture<Map<String, Object>> getUserStats() {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        String userId = getCurrentUserId();

        if (userId == null) {
            future.completeExceptionally(new Exception("User not logged in"));
            return future;
        }

        db.collection("user_stats").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        future.complete(documentSnapshot.getData());
                    } else {
                        // Create stats if they don't exist
                        createUserStats(userId).thenRun(() -> {
                            Map<String, Object> defaultStats = new HashMap<>();
                            defaultStats.put("totalWaterThisWeek", 0);
                            defaultStats.put("totalWaterThisMonth", 0);
                            defaultStats.put("longestStreak", 0);
                            defaultStats.put("currentStreak", 0);
                            defaultStats.put("daysGoalAchieved", 0);
                            future.complete(defaultStats);
                        });
                    }
                })
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }


    public CompletableFuture<Void> ensureUserStatsExist() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        String userId = getCurrentUserId();

        if (userId == null) {
            future.completeExceptionally(new Exception("User not logged in"));
            return future;
        }

        // Provjeri postoji li user_stats dokument
        db.collection("user_stats").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        // Kreiraj user_stats ako ne postoji
                        Map<String, Object> statsData = new HashMap<>();
                        statsData.put("userId", userId);
                        statsData.put("totalWaterThisWeek", 0);
                        statsData.put("totalWaterThisMonth", 0);
                        statsData.put("longestStreak", 0);
                        statsData.put("currentStreak", 0);
                        statsData.put("daysGoalAchieved", 0);
                        statsData.put("lastUpdated", Timestamp.now());

                        db.collection("user_stats").document(userId)
                                .set(statsData)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("FirebaseHelper", "User stats created successfully");
                                    future.complete(null);
                                })
                                .addOnFailureListener(future::completeExceptionally);
                    } else {
                        Log.d("FirebaseHelper", "User stats already exist");
                        future.complete(null);
                    }
                })
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    public CompletableFuture<Void> updateDailyGoal(int newGoal) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        String userId = getCurrentUserId();

        if (userId == null) {
            future.completeExceptionally(new Exception("User not logged in"));
            return future;
        }

        db.collection("users").document(userId)
                .update("dailyGoal", newGoal)
                .addOnSuccessListener(aVoid -> future.complete(null))
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }
    public void signOut() {
        auth.signOut();
    }
    public CompletableFuture<Integer> getWeeklyWaterIntake() {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        String userId = getCurrentUserId();

        if (userId == null) {
            future.complete(0);
            return future;
        }

        // Get date 7 days ago
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        String weekAgo = dateFormat.format(calendar.getTime());

        db.collection("water_intake")
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("date", weekAgo)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalAmount = 0;
                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Long amount = document.getLong("amount");
                        if (amount != null) {
                            totalAmount += amount.intValue();
                        }
                    }
                    future.complete(totalAmount);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseHelper", "Error getting weekly intake", e);
                    future.complete(0); // Return 0 on error
                });

        return future;
    }

    public CompletableFuture<Integer> getMonthlyWaterIntake() {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        String userId = getCurrentUserId();

        if (userId == null) {
            future.complete(0);
            return future;
        }

        // Get date 30 days ago
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -30);
        String monthAgo = dateFormat.format(calendar.getTime());

        db.collection("water_intake")
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("date", monthAgo)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalAmount = 0;
                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Long amount = document.getLong("amount");
                        if (amount != null) {
                            totalAmount += amount.intValue();
                        }
                    }
                    future.complete(totalAmount);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseHelper", "Error getting monthly intake", e);
                    future.complete(0); // Return 0 on error
                });

        return future;
    }

}