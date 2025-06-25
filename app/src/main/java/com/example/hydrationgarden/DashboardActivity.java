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

        firebaseHelper = new FirebaseHelper();

        firebaseHelper.ensureUserStatsExist()
                .thenRun(() -> Log.d("Dashboard", "User stats ensured"))
                .exceptionally(throwable -> {
                    Log.e("Dashboard", "Error ensuring user stats", throwable);
                    return null;
                });

        setupBottomNavigation();
        setupClickListeners();
        loadUserData();
        loadTodayIntake();
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {

                return true;
            } else if (itemId == R.id.nav_garden) {
                startActivity(new Intent(this, GardenActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
            } else if (itemId == R.id.nav_stats) {
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
        binding.btnAddWater.setOnClickListener(v -> showWaterInputDialog());

        // DODAJ OVO:
        binding.btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });

        binding.btnSignOut.setOnClickListener(v -> {
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
        firebaseHelper.addWaterIntake(amount)
                .thenAccept(intakeId -> {
                    runOnUiThread(() -> {
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
                    });
                })
                .exceptionally(throwable -> {
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
        Log.d("Dashboard", "Current user ID: " + userId);

        if (userId == null) {
            Log.e("Dashboard", "User ID is null - user not logged in!");
            return;
        }

        firebaseHelper.getUser()
                .thenAccept(user -> {
                    Log.d("Dashboard", "User loaded successfully: " + user.getName());
                    runOnUiThread(() -> {
                        dailyGoal = user.getDailyGoal();
                        binding.tvWelcome.setText("Dobrodošao, " + user.getName() + "!");
                        updateUI();
                    });
                })
                .exceptionally(throwable -> {
                    Log.e("Dashboard", "Error loading user: " + throwable.getMessage());
                    runOnUiThread(() -> {
                        // NE PRIKAZUJ Toast grešku - samo logiraj
                        Log.w("Dashboard", "Using default user data");
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
                    Log.d("Dashboard", "Dashboard got: " + intake + "ml from Firebase");
                    runOnUiThread(() -> {
                        currentIntake = intake;
                        updateUI();
                    });
                })
                .exceptionally(throwable -> {
                    Log.e("Dashboard", "Dashboard error: " + throwable.getMessage());
                    return null;
                });
    }

    private void updateUI() {
        int percentage = (currentIntake * 100) / dailyGoal;

        binding.tvCurrentIntake.setText(currentIntake + getString(R.string.ml));
        binding.tvDailyGoal.setText(getString(R.string.of) + " " + dailyGoal + getString(R.string.ml));
        binding.progressWater.setProgress(percentage);
        binding.progressBarLinear.setProgress(percentage);
        binding.tvPercentage.setText(percentage + "%");

        boolean isGoalAchieved = currentIntake >= dailyGoal;
        updatePlantPreview(isGoalAchieved);
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
        binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
        loadTodayIntake();
    }
}