package com.example.hydrationgarden;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.example.hydrationgarden.databinding.ActivityStatsBinding;
import com.example.hydrationgarden.utils.FirebaseHelper;

public class StatsActivity extends AppCompatActivity {
    private ActivityStatsBinding binding;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStatsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Log.d("StatsActivity", "StatsActivity created");

        firebaseHelper = new FirebaseHelper();
        setupBottomNavigation();
        loadStats();
    }

    private void setupBottomNavigation() {
        Log.d("Navigation", "Setting up bottom navigation in StatsActivity");

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                Log.d("Navigation", "Going to Dashboard from Stats");
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_garden) {
                Log.d("Navigation", "Going to Garden from Stats");
                startActivity(new Intent(this, GardenActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_stats) {
                return true; // Already on stats
            }
            return false;
        });
        binding.bottomNavigation.setSelectedItemId(R.id.nav_stats);
    }

    private void loadStats() {
        Log.d("StatsActivity", "Starting loadStats");

        firebaseHelper.getTodayWaterIntake()
                .thenAccept(todayIntake -> {
                    Log.d("StatsActivity", "Today intake: " + todayIntake + "ml");

                    firebaseHelper.getUser()
                            .thenAccept(user -> {
                                Log.d("StatsActivity", "User loaded for stats");

                                // Učitaj PRAVE tjedne/mjesečne podatke
                                firebaseHelper.getWeeklyWaterIntake()
                                        .thenAccept(weeklyTotal -> {
                                            firebaseHelper.getMonthlyWaterIntake()
                                                    .thenAccept(monthlyTotal -> {
                                                        runOnUiThread(() -> {
                                                            // Update UI s PRAVIM podacima
                                                            binding.tvTodayIntake.setText(todayIntake + " ml");

                                                            int weeklyAverage = weeklyTotal / 7;
                                                            binding.tvWeeklyAverage.setText(weeklyAverage + " ml");

                                                            // Najbolji dan - možeš implementirati kasnije
                                                            binding.tvBestDay.setText("0 ml"); // Za sada 0

                                                            binding.tvDailyGoal.setText(user.getDailyGoal() + " ml");

                                                            int goalPercentage = (todayIntake * 100) / user.getDailyGoal();
                                                            binding.progressGoal.setProgress(goalPercentage);
                                                            binding.tvGoalPercentage.setText(goalPercentage + "%");

                                                            // Update text data s PRAVIM podacima
                                                            updateWeeklyStats(weeklyTotal);
                                                            updateMonthlyStats(monthlyTotal);

                                                            Log.d("StatsActivity", "Stats updated with real data");
                                                        });
                                                    });
                                        });
                            })
                            .exceptionally(throwable -> {
                                Log.e("StatsActivity", "Error loading user: " + throwable.getMessage());
                                runOnUiThread(() -> {
                                    // Pokaži 0 za novog korisnika
                                    binding.tvTodayIntake.setText("0 ml");
                                    binding.tvWeeklyAverage.setText("0 ml");
                                    binding.tvBestDay.setText("0 ml");
                                    binding.tvDailyGoal.setText("2000 ml");
                                });
                                return null;
                            });
                });
    }

    private void updateWeeklyStats(int weeklyTotal) {
        // PRAVI tjedni podaci
        int weeklyAverage = weeklyTotal / 7;

        String weeklyData = String.format(
                "Tjedni podaci:\n" +
                        "• Ukupno: %d ml\n" +
                        "• Dnevni prosjek: %d ml\n" +
                        "• Dana s ciljem: %s\n" +
                        "• Napredak: %s",
                weeklyTotal,
                weeklyAverage,
                weeklyAverage >= 2000 ? "Većina" : "Treba poboljšanje",
                weeklyTotal > 0 ? "U tijeku" : "Početak putovanja"
        );

        binding.tvWeeklyData.setText(weeklyData);
    }

    private void updateMonthlyStats(int monthlyTotal) {
        // PRAVI mjesečni podaci
        int monthlyAverage = monthlyTotal / 30;

        String monthlyData = String.format(
                "Mjesečni sažetak:\n" +
                        "Ukupno: %d ml\n" +
                        "Dnevni prosjek: %d ml\n" +
                        "Status: %s\n" +
                        "Motivacija: %s",
                monthlyTotal,
                monthlyAverage,
                monthlyAverage >= 2000 ? "Odličan!" : monthlyAverage >= 1500 ? "Dobar" : "Početnik",
                monthlyTotal > 0 ? "Nastavi tako!" : "Svaki gutljaj je korak naprijed!"
        );

        binding.tvMonthlyData.setText(monthlyData);
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d("StatsActivity", "onResume - refreshing stats");
        loadStats();
    }
}