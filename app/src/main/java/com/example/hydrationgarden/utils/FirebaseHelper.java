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

    // Cache sistem
    private int cachedTodayIntake = -1;
    private String cachedDate = "";
    private long lastCacheUpdate = 0;
    private static final long CACHE_DURATION = 30000; // 30 sekundi

    public FirebaseHelper() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    }

    public String getCurrentUserId() {
        FirebaseUser user = auth.getCurrentUser();
        String userId = user != null ? user.getUid() : null;
        Log.d("FirebaseHelper", "Current user ID: " + userId);
        return userId;
    }

    public boolean isUserLoggedIn() {
        FirebaseUser currentUser = auth.getCurrentUser();
        return currentUser != null;
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
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("FirebaseHelper", "User document created successfully");
                                        future.complete(firebaseUser.getUid());
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("FirebaseHelper", "Error creating user document", e);
                                        future.completeExceptionally(e);
                                    });
                        }
                    } else {
                        Log.e("FirebaseHelper", "Registration failed", task.getException());
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
                            Log.d("FirebaseHelper", "Login successful for user: " + user.getUid());
                            // Update last login
                            db.collection("users").document(user.getUid())
                                    .update("lastLoginAt", Timestamp.now());

                            future.complete(user.getUid());
                        }
                    } else {
                        Log.e("FirebaseHelper", "Login failed", task.getException());
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

        Log.d("FirebaseHelper", "Adding water intake: " + amount + "ml for user: " + userId + " on date: " + today);

        db.collection("water_intake").document(intakeId)
                .set(intakeData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirebaseHelper", "Water intake added successfully: " + amount + "ml");
                    invalidateCache(); // Invalidate cache when new water is added
                    future.complete(intakeId);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseHelper", "Error adding water intake", e);
                    future.completeExceptionally(e);
                });

        return future;
    }

    public CompletableFuture<Integer> getTodayWaterIntake() {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        String userId = getCurrentUserId();

        if (userId == null) {
            Log.w("FirebaseHelper", "User not logged in for getTodayWaterIntake");
            future.complete(0);
            return future;
        }

        String today = dateFormat.format(new Date());
        long currentTime = System.currentTimeMillis();

        // Provjeri cache
        if (cachedTodayIntake != -1 &&
                today.equals(cachedDate) &&
                (currentTime - lastCacheUpdate) < CACHE_DURATION) {

            Log.d("FirebaseHelper", "Returning cached intake: " + cachedTodayIntake + "ml");
            future.complete(cachedTodayIntake);
            return future;
        }

        Log.d("FirebaseHelper", "=== GET TODAY WATER INTAKE (FRESH) ===");
        Log.d("FirebaseHelper", "User ID: " + userId);
        Log.d("FirebaseHelper", "Today date: " + today);

        db.collection("water_intake")
                .whereEqualTo("userId", userId)
                .whereEqualTo("date", today)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalAmount = 0;
                    int documentCount = queryDocumentSnapshots.size();

                    Log.d("FirebaseHelper", "Found " + documentCount + " water intake records for today");

                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Long amount = document.getLong("amount");
                        if (amount != null) {
                            totalAmount += amount.intValue();
                        }
                    }

                    // Ažuriraj cache
                    cachedTodayIntake = totalAmount;
                    cachedDate = today;
                    lastCacheUpdate = currentTime;

                    Log.d("FirebaseHelper", "=== FINAL TODAY TOTAL: " + totalAmount + "ml (CACHED) ===");
                    future.complete(totalAmount);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseHelper", "Error getting today water intake", e);
                    future.complete(0);
                });

        return future;
    }

    public void invalidateCache() {
        cachedTodayIntake = -1;
        cachedDate = "";
        lastCacheUpdate = 0;
        Log.d("FirebaseHelper", "Cache invalidated");
    }

    public CompletableFuture<User> getUser() {
        CompletableFuture<User> future = new CompletableFuture<>();
        String userId = getCurrentUserId();

        if (userId == null) {
            Log.e("FirebaseHelper", "Cannot get user - not logged in");
            future.completeExceptionally(new Exception("User not logged in"));
            return future;
        }

        Log.d("FirebaseHelper", "Getting user data for: " + userId);

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = new User();
                        user.setUid(documentSnapshot.getString("uid"));
                        user.setEmail(documentSnapshot.getString("email"));
                        user.setName(documentSnapshot.getString("name"));

                        Long dailyGoal = documentSnapshot.getLong("dailyGoal");
                        user.setDailyGoal(dailyGoal != null ? dailyGoal.intValue() : 2000);

                        Log.d("FirebaseHelper", "User loaded successfully: " + user.getName() + ", goal: " + user.getDailyGoal());
                        future.complete(user);
                    } else {
                        Log.e("FirebaseHelper", "User document not found for ID: " + userId);
                        future.completeExceptionally(new Exception("User document not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseHelper", "Error getting user", e);
                    future.completeExceptionally(e);
                });

        return future;
    }

    public CompletableFuture<Void> updateDailyGoal(int newGoal) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        String userId = getCurrentUserId();

        if (userId == null) {
            Log.e("FirebaseHelper", "Cannot update goal - user not logged in");
            future.completeExceptionally(new Exception("User not logged in"));
            return future;
        }

        Log.d("FirebaseHelper", "Updating daily goal to " + newGoal + " for user: " + userId);

        // Prvo provjeri postoji li dokument
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Dokument postoji, ažuriraj ga
                        db.collection("users").document(userId)
                                .update("dailyGoal", newGoal)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("FirebaseHelper", "Daily goal updated successfully to: " + newGoal);
                                    future.complete(null);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("FirebaseHelper", "Error updating daily goal", e);
                                    future.completeExceptionally(e);
                                });
                    } else {
                        Log.e("FirebaseHelper", "User document does not exist, cannot update goal");
                        future.completeExceptionally(new Exception("User document not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseHelper", "Error checking user document existence", e);
                    future.completeExceptionally(e);
                });

        return future;
    }

    public CompletableFuture<Integer> getWeeklyWaterIntake() {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        String userId = getCurrentUserId();

        if (userId == null) {
            Log.w("FirebaseHelper", "User not logged in for getWeeklyWaterIntake");
            future.complete(0);
            return future;
        }

        Log.d("FirebaseHelper", "=== GET WEEKLY WATER INTAKE (NO INDEX) ===");
        Log.d("FirebaseHelper", "User ID: " + userId);

        // Koristi samo filter po userId i obradi datume u kodu
        db.collection("water_intake")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalAmount = 0;
                    int documentCount = queryDocumentSnapshots.size();
                    int weeklyCount = 0;

                    // Izračunaj datum prije 7 dana
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.DAY_OF_YEAR, -6); // -6 da uključimo i današnji dan = 7 dana ukupno
                    String weekStartDate = dateFormat.format(calendar.getTime());
                    String today = dateFormat.format(new Date());

                    Log.d("FirebaseHelper", "Found " + documentCount + " total water records");
                    Log.d("FirebaseHelper", "Week start date: " + weekStartDate);
                    Log.d("FirebaseHelper", "Today date: " + today);

                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String date = document.getString("date");
                        Long amount = document.getLong("amount");

                        if (date != null && amount != null) {
                            // Provjeri je li datum u tjednom rasponu
                            if (date.compareTo(weekStartDate) >= 0 && date.compareTo(today) <= 0) {
                                totalAmount += amount.intValue();
                                weeklyCount++;
                                Log.d("FirebaseHelper", "Weekly record: " + date + " = " + amount + "ml, total now: " + totalAmount);
                            }
                        }
                    }

                    Log.d("FirebaseHelper", "=== FINAL WEEKLY TOTAL: " + totalAmount + "ml from " + weeklyCount + " records ===");
                    future.complete(totalAmount);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseHelper", "Error getting weekly intake", e);
                    future.complete(0);
                });

        return future;
    }

    public CompletableFuture<Integer> getMonthlyWaterIntake() {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        String userId = getCurrentUserId();

        if (userId == null) {
            Log.w("FirebaseHelper", "User not logged in for getMonthlyWaterIntake");
            future.complete(0);
            return future;
        }

        Log.d("FirebaseHelper", "=== GET MONTHLY WATER INTAKE (NO INDEX) ===");
        Log.d("FirebaseHelper", "User ID: " + userId);

        // Koristi samo filter po userId i obradi datume u kodu
        db.collection("water_intake")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalAmount = 0;
                    int documentCount = queryDocumentSnapshots.size();
                    int monthlyCount = 0;

                    // Izračunaj datum prije 30 dana
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.DAY_OF_YEAR, -29); // -29 da uključimo i današnji dan = 30 dana ukupno
                    String monthStartDate = dateFormat.format(calendar.getTime());
                    String today = dateFormat.format(new Date());

                    Log.d("FirebaseHelper", "Found " + documentCount + " total water records");
                    Log.d("FirebaseHelper", "Month start date: " + monthStartDate);
                    Log.d("FirebaseHelper", "Today date: " + today);

                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String date = document.getString("date");
                        Long amount = document.getLong("amount");

                        if (date != null && amount != null) {
                            // Provjeri je li datum u mjesečnom rasponu
                            if (date.compareTo(monthStartDate) >= 0 && date.compareTo(today) <= 0) {
                                totalAmount += amount.intValue();
                                monthlyCount++;
                                Log.d("FirebaseHelper", "Monthly record: " + date + " = " + amount + "ml, total now: " + totalAmount);
                            }
                        }
                    }

                    Log.d("FirebaseHelper", "=== FINAL MONTHLY TOTAL: " + totalAmount + "ml from " + monthlyCount + " records ===");
                    future.complete(totalAmount);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseHelper", "Error getting monthly intake", e);
                    future.complete(0);
                });

        return future;
    }

    public CompletableFuture<Integer> getBestDayWaterIntake() {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        String userId = getCurrentUserId();

        if (userId == null) {
            Log.w("FirebaseHelper", "User not logged in for getBestDayWaterIntake");
            future.complete(0);
            return future;
        }

        Log.d("FirebaseHelper", "=== GET BEST DAY WATER INTAKE (NO INDEX) ===");
        Log.d("FirebaseHelper", "User ID: " + userId);

        // Koristi samo filter po userId i obradi datume u kodu
        db.collection("water_intake")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, Integer> dailyTotals = new HashMap<>();
                    int documentCount = queryDocumentSnapshots.size();

                    // Izračunaj datum prije 30 dana za najbolji dan
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.DAY_OF_YEAR, -30);
                    String monthStartDate = dateFormat.format(calendar.getTime());
                    String today = dateFormat.format(new Date());

                    Log.d("FirebaseHelper", "Found " + documentCount + " total documents for best day calculation");
                    Log.d("FirebaseHelper", "Search from: " + monthStartDate + " to: " + today);

                    // Group by date and sum amounts
                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String date = document.getString("date");
                        Long amount = document.getLong("amount");

                        if (date != null && amount != null) {
                            // Provjeri je li datum u rasponu zadnjih 30 dana
                            if (date.compareTo(monthStartDate) >= 0 && date.compareTo(today) <= 0) {
                                int currentTotal = dailyTotals.getOrDefault(date, 0);
                                int newTotal = currentTotal + amount.intValue();
                                dailyTotals.put(date, newTotal);
                                Log.d("FirebaseHelper", "Date " + date + ": " + currentTotal + " + " + amount + " = " + newTotal);
                            }
                        }
                    }

                    // Find maximum daily total
                    int bestDay = 0;
                    String bestDate = "";
                    for (Map.Entry<String, Integer> entry : dailyTotals.entrySet()) {
                        if (entry.getValue() > bestDay) {
                            bestDay = entry.getValue();
                            bestDate = entry.getKey();
                        }
                        Log.d("FirebaseHelper", "Daily total for " + entry.getKey() + ": " + entry.getValue() + "ml");
                    }

                    Log.d("FirebaseHelper", "=== BEST DAY: " + bestDate + " with " + bestDay + "ml ===");
                    future.complete(bestDay);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseHelper", "Error getting best day intake", e);
                    future.complete(0);
                });

        return future;
    }

    public void signOut() {
        invalidateCache();
        auth.signOut();
        Log.d("FirebaseHelper", "User signed out");
    }
}
