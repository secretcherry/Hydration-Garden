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

        // Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        binding = ActivityStatsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Log.d("StatsActivity", "StatsActivity created");

        firebaseHelper = new FirebaseHelper();
        setupBottomNavigation();

        // Prikaži loading tekst na početku
        showLoadingState();

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

    private void showLoadingState() {
        binding.tvTodayIntake.setText("Učitavanje...");
        binding.tvWeeklyAverage.setText("Učitavanje...");
        binding.tvBestDay.setText("Učitavanje...");
        binding.tvDailyGoal.setText("Učitavanje...");
        binding.tvGoalPercentage.setText("0%");
        binding.progressGoal.setProgress(0);
        binding.tvWeeklyData.setText("Učitavanje tjednih podataka...");
        binding.tvMonthlyData.setText("Učitavanje mjesečnih podataka...");
    }

    private void loadStats() {
        Log.d("StatsActivity", "Starting loadStats");

        // Paralelno učitaj sve podatke
        firebaseHelper.getTodayWaterIntake()
                .thenAccept(todayIntake -> {
                    Log.d("StatsActivity", "Today intake loaded: " + todayIntake + "ml");

                    firebaseHelper.getUser()
                            .thenAccept(user -> {
                                Log.d("StatsActivity", "User loaded: " + user.getName());

                                // Paralelno učitaj sve ostale podatke
                                firebaseHelper.getWeeklyWaterIntake()
                                        .thenAccept(weeklyTotal -> {
                                            Log.d("StatsActivity", "Weekly total: " + weeklyTotal + "ml");

                                            firebaseHelper.getMonthlyWaterIntake()
                                                    .thenAccept(monthlyTotal -> {
                                                        Log.d("StatsActivity", "Monthly total: " + monthlyTotal + "ml");

                                                        firebaseHelper.getBestDayWaterIntake()
                                                                .thenAccept(bestDay -> {
                                                                    Log.d("StatsActivity", "Best day: " + bestDay + "ml");

                                                                    // Sada kad imamo sve podatke, ažuriraj UI
                                                                    runOnUiThread(() -> updateAllStats(todayIntake, user.getDailyGoal(), weeklyTotal, monthlyTotal, bestDay));
                                                                })
                                                                .exceptionally(throwable -> {
                                                                    Log.e("StatsActivity", "Error loading best day: " + throwable.getMessage());
                                                                    runOnUiThread(() -> updateAllStats(todayIntake, user.getDailyGoal(), weeklyTotal, monthlyTotal, 0));
                                                                    return null;
                                                                });
                                                    })
                                                    .exceptionally(throwable -> {
                                                        Log.e("StatsActivity", "Error loading monthly data: " + throwable.getMessage());
                                                        runOnUiThread(() -> updateAllStats(todayIntake, user.getDailyGoal(), weeklyTotal, 0, 0));
                                                        return null;
                                                    });
                                        })
                                        .exceptionally(throwable -> {
                                            Log.e("StatsActivity", "Error loading weekly data: " + throwable.getMessage());

                                            // Pokušaj učitati mjesečne podatke i bez tjednih
                                            firebaseHelper.getMonthlyWaterIntake()
                                                    .thenAccept(monthlyTotal -> {
                                                        firebaseHelper.getBestDayWaterIntake()
                                                                .thenAccept(bestDay -> {
                                                                    runOnUiThread(() -> updateAllStats(todayIntake, user.getDailyGoal(), 0, monthlyTotal, bestDay));
                                                                })
                                                                .exceptionally(bestDayError -> {
                                                                    runOnUiThread(() -> updateAllStats(todayIntake, user.getDailyGoal(), 0, monthlyTotal, 0));
                                                                    return null;
                                                                });
                                                    })
                                                    .exceptionally(monthlyError -> {
                                                        Log.e("StatsActivity", "Error loading monthly data: " + monthlyError.getMessage());
                                                        runOnUiThread(() -> updateAllStats(todayIntake, user.getDailyGoal(), 0, 0, 0));
                                                        return null;
                                                    });
                                            return null;
                                        });
                            })
                            .exceptionally(throwable -> {
                                Log.e("StatsActivity", "Error loading user: " + throwable.getMessage());
                                runOnUiThread(() -> updateAllStats(todayIntake, 2000, 0, 0, 0)); // Default goal 2000ml
                                return null;
                            });
                })
                .exceptionally(throwable -> {
                    Log.e("StatsActivity", "Error loading today intake: " + throwable.getMessage());
                    runOnUiThread(() -> updateAllStats(0, 2000, 0, 0, 0)); // Sve na 0 ako ne možemo učitati osnovne podatke
                    return null;
                });
    }

    private void updateAllStats(int todayIntake, int dailyGoal, int weeklyTotal, int monthlyTotal, int bestDay) {
        Log.d("StatsActivity", String.format("Updating UI: today=%d, goal=%d, weekly=%d, monthly=%d, best=%d",
                todayIntake, dailyGoal, weeklyTotal, monthlyTotal, bestDay));

        // Osnovni podaci
        binding.tvTodayIntake.setText(todayIntake + " ml");
        binding.tvDailyGoal.setText(dailyGoal + " ml");

        // Tjedni prosjek
        int weeklyAverage = weeklyTotal > 0 ? weeklyTotal / 7 : 0;
        binding.tvWeeklyAverage.setText(weeklyAverage + " ml");

        // Najbolji dan
        binding.tvBestDay.setText(bestDay + " ml");

        // Postotak cilja
        int goalPercentage = dailyGoal > 0 ? (todayIntake * 100) / dailyGoal : 0;
        binding.progressGoal.setProgress(Math.min(goalPercentage, 100)); // Maksimalno 100%
        binding.tvGoalPercentage.setText(goalPercentage + "%");

        // Tjedni i mjesečni sažetak
        updateWeeklyStats(weeklyTotal, weeklyAverage, dailyGoal);
        updateMonthlyStats(monthlyTotal, dailyGoal);

        Log.d("StatsActivity", "UI updated successfully");
    }

    private void updateWeeklyStats(int weeklyTotal, int weeklyAverage, int dailyGoal) {
        String statusText;
        String progressText;

        if (weeklyTotal == 0) {
            statusText = "Početak putovanja";
            progressText = "Započni dodavanjem prve količine vode!";
        } else if (weeklyAverage >= dailyGoal) {
            statusText = "Odličan tjedan!";
            progressText = "Postigao si cilj većinu dana";
        } else if (weeklyAverage >= dailyGoal * 0.7) {
            statusText = "Dobar napredak";
            progressText = "Blizu si svakodnevnog cilja";
        } else {
            statusText = "Treba poboljšanje";
            progressText = "Pokušaj piti više vode svaki dan";
        }

        String weeklyData = String.format(
                "Tjedni podaci:\n" +
                        "• Ukupno: %d ml\n" +
                        "• Dnevni prosjek: %d ml\n" +
                        "• Status: %s\n" +
                        "• Savjet: %s",
                weeklyTotal,
                weeklyAverage,
                statusText,
                progressText
        );

        binding.tvWeeklyData.setText(weeklyData);
    }

    private void updateMonthlyStats(int monthlyTotal, int dailyGoal) {
        int monthlyAverage = monthlyTotal > 0 ? monthlyTotal / 30 : 0;

        String statusText;
        String motivationText;

        if (monthlyTotal == 0) {
            statusText = "Početnik";
            motivationText = "Svaki gutljaj je korak naprijed!";
        } else if (monthlyAverage >= dailyGoal) {
            statusText = "Hidracijski heroj!";
            motivationText = "Nastavi fantastično!";
        } else if (monthlyAverage >= dailyGoal * 0.8) {
            statusText = "Odličan napredak";
            motivationText = "Blizu si savršenstva!";
        } else if (monthlyAverage >= dailyGoal * 0.6) {
            statusText = "Dobro napredovanje";
            motivationText = "Polako ali sigurno!";
        } else {
            statusText = "Početne stepenice";
            motivationText = "Svaki dan je nova prilika!";
        }

        String monthlyData = String.format(
                "Mjesečni sažetak:\n" +
                        "• Ukupno: %d ml\n" +
                        "• Dnevni prosjek: %d ml\n" +
                        "• Status: %s\n" +
                        "• Motivacija: %s",
                monthlyTotal,
                monthlyAverage,
                statusText,
                motivationText
        );

        binding.tvMonthlyData.setText(monthlyData);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("StatsActivity", "onResume - NOT refreshing stats to prevent ANR");
        // UKLONJEN showLoadingState() i loadStats() - poziva se samo u onCreate()
    }

    public void refreshStats() {
        Log.d("StatsActivity", "Manually refreshing stats");
        showLoadingState();
        loadStats();
    }
}
