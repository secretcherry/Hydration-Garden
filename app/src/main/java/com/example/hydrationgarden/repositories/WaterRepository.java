package com.example.hydrationgarden.repositories;

import com.example.hydrationgarden.models.User;
import com.example.hydrationgarden.models.WaterIntake;
import com.example.hydrationgarden.utils.FirebaseHelper;
import com.example.hydrationgarden.utils.DateUtils;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WaterRepository {
    private FirebaseHelper firebaseHelper;

    public WaterRepository() {
        firebaseHelper = new FirebaseHelper();
    }

    public CompletableFuture<String> addWaterIntake(int amount) {
        return firebaseHelper.addWaterIntake(amount);
    }

    public CompletableFuture<Integer> getTodayWaterIntake() {
        return firebaseHelper.getTodayWaterIntake();
    }

    public CompletableFuture<Integer> getWeeklyAverage() {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        // Demo data - in real app would calculate from last 7 days
        future.complete(1750);
        return future;
    }

    public CompletableFuture<Boolean> hasAchievedGoalToday() {
        return getTodayWaterIntake()
                .thenCompose(todayIntake ->
                        firebaseHelper.getUser()
                                .thenApply(user -> todayIntake >= user.getDailyGoal())
                );
    }

    public CompletableFuture<User> getCurrentUser() {
        return firebaseHelper.getUser();
    }
}