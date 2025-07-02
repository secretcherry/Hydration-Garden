package com.example.hydrationgarden;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import com.example.hydrationgarden.databinding.ActivityDashboardBinding;
import com.example.hydrationgarden.fragments.WaterInputFragment;
import com.example.hydrationgarden.utils.FirebaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DashboardActivity extends AppCompatActivity {
    private ActivityDashboardBinding binding;
    private FirebaseHelper firebaseHelper;
    private int dailyGoal = 2000;
    private int currentIntake = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Log.d("Dashboard", "DashboardActivity created");

        firebaseHelper = new FirebaseHelper();

        // Provjeri je li korisnik ulogiran
        if (firebaseHelper.getCurrentUserId() == null) {
            Log.w("Dashboard", "No user logged in, redirecting to login");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Debug trenutnog korisnika
        debugCurrentUser();

        setupBottomNavigation();
        setupClickListeners();
        loadUserData();
        loadTodayIntake();
    }

    private void debugCurrentUser() {
        String userId = firebaseHelper.getCurrentUserId();
        Log.d("Dashboard", "=== USER DEBUG INFO ===");
        Log.d("Dashboard", "Current User ID: " + userId);

        if (userId != null) {
            // Provjeri postoji li user dokument
            firebaseHelper.getUser()
                    .thenAccept(user -> {
                        Log.d("Dashboard", "User document found: " + user.getName() + ", goal: " + user.getDailyGoal());
                    })
                    .exceptionally(throwable -> {
                        Log.e("Dashboard", "User document NOT found: " + throwable.getMessage());
                        return null;
                    });
        }
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true; // Already on home
            } else if (itemId == R.id.nav_garden) {
                Log.d("Dashboard", "Navigating to Garden");
                startActivity(new Intent(this, GardenActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
            } else if (itemId == R.id.nav_stats) {
                Log.d("Dashboard", "Navigating to Stats");
                startActivity(new Intent(this, StatsActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
            }
            return false;
        });

        // Set selected item
        binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
    }

    private void setupClickListeners() {
        binding.btnAddWater.setOnClickListener(v -> {
            Log.d("Dashboard", "Add water button clicked");
            showWaterInputDialog();
        });

        binding.btnSettings.setOnClickListener(v -> {
            Log.d("Dashboard", "Settings button clicked");
            startActivity(new Intent(this, SettingsActivity.class));
        });

        binding.btnSignOut.setOnClickListener(v -> {
            Log.d("Dashboard", "Sign out button clicked");
            firebaseHelper.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void showWaterInputDialog() {
        WaterInputFragment fragment = new WaterInputFragment();
        fragment.setOnWaterAddedListener(amount -> addWaterIntake(amount));
        fragment.show(getSupportFragmentManager(), "WaterInputFragment");
    }

    private void addWaterIntake(int amount) {
        Log.d("Dashboard", "Adding water intake: " + amount + "ml");

        firebaseHelper.addWaterIntake(amount)
                .thenAccept(intakeId -> {
                    Log.d("Dashboard", "Water intake added successfully with ID: " + intakeId);
                    runOnUiThread(() -> {
                        // Odmah ažuriraj UI
                        currentIntake += amount;
                        updateUI();

                        Toast.makeText(this,
                                getString(R.string.water_added_success, amount),
                                Toast.LENGTH_SHORT).show();

                        if (currentIntake >= dailyGoal) {
                            Toast.makeText(this,
                                    getString(R.string.goal_achieved),
                                    Toast.LENGTH_LONG).show();
                        }

                        // Refresh podatke iz Firebase-a (cache je invalidiran)
                        loadTodayIntake();
                    });
                })
                .exceptionally(throwable -> {
                    Log.e("Dashboard", "Error adding water intake", throwable);
                    runOnUiThread(() -> {
                        Toast.makeText(this,
                                getString(R.string.error_add_water_failed),
                                Toast.LENGTH_SHORT).show();
                    });
                    return null;
                });
    }

    private void loadUserData() {
        // Postavi default welcome dok se Firebase ne učita
        binding.tvWelcome.setText("Dobrodošao!");

        String userId = firebaseHelper.getCurrentUserId();
        Log.d("Dashboard", "Loading user data for ID: " + userId);

        if (userId == null) {
            Log.e("Dashboard", "User ID is null - user not logged in!");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        firebaseHelper.getUser()
                .thenAccept(user -> {
                    Log.d("Dashboard", "User loaded successfully: " + user.getName() + ", goal: " + user.getDailyGoal());
                    runOnUiThread(() -> {
                        dailyGoal = user.getDailyGoal();
                        binding.tvWelcome.setText("Dobrodošao, " + user.getName() + "!");
                        updateUI();
                    });
                })
                .exceptionally(throwable -> {
                    Log.e("Dashboard", "Error loading user: " + throwable.getMessage());
                    runOnUiThread(() -> {
                        // Koristi default vrijednosti i nastavi
                        Log.w("Dashboard", "Using default user data due to error");
                        binding.tvWelcome.setText("Dobrodošao!");
                        dailyGoal = 2000; // Default goal
                        updateUI();
                    });
                    return null;
                });
    }

    private void loadTodayIntake() {
        Log.d("Dashboard", "=== LOADING TODAY INTAKE ===");

        firebaseHelper.getTodayWaterIntake()
                .thenAccept(intake -> {
                    Log.d("Dashboard", "Today intake loaded: " + intake + "ml");
                    runOnUiThread(() -> {
                        currentIntake = intake;
                        updateUI();
                        Log.d("Dashboard", "UI updated with intake: " + currentIntake + "ml");
                    });
                })
                .exceptionally(throwable -> {
                    Log.e("Dashboard", "Error loading today intake: " + throwable.getMessage());
                    runOnUiThread(() -> {
                        // Postavi na 0 i nastavi
                        currentIntake = 0;
                        updateUI();
                    });
                    return null;
                });
    }

    private void updateUI() {
        Log.d("Dashboard", "Updating UI - intake: " + currentIntake + "ml, goal: " + dailyGoal + "ml");

        int percentage = dailyGoal > 0 ? (currentIntake * 100) / dailyGoal : 0;

        binding.tvCurrentIntake.setText(currentIntake + getString(R.string.ml));
        binding.tvDailyGoal.setText(getString(R.string.of) + " " + dailyGoal + getString(R.string.ml));
        binding.progressWater.setProgress(Math.min(percentage, 100)); // Max 100%
        binding.progressBarLinear.setProgress(Math.min(percentage, 100));
        binding.tvPercentage.setText(percentage + "%");

        boolean isGoalAchieved = currentIntake >= dailyGoal;
        updatePlantPreview(isGoalAchieved);

        Log.d("Dashboard", "UI updated - percentage: " + percentage + "%, goal achieved: " + isGoalAchieved);
    }

    private void updatePlantPreview(boolean isHappy) {
        if (isHappy) {
            binding.ivPlantPreview.setImageResource(R.drawable.plant_happy_aloe);
            binding.tvPlantMessage.setText("Moje biljke su sretne!");
        } else {
            binding.ivPlantPreview.setImageResource(R.drawable.plant_sad_aloe);
            binding.tvPlantMessage.setText("Biljke trebaju više vode...");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Dashboard", "onResume called - NOT refreshing data to prevent ANR");
        binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
        // UKLONJEN loadTodayIntake(); - poziva se samo u onCreate() i nakon dodavanja vode
    }
}
