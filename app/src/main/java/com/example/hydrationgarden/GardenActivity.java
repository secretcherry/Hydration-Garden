package com.example.hydrationgarden;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.hydrationgarden.adapters.PlantAdapter;
import com.example.hydrationgarden.databinding.ActivityGardenBinding;
import com.example.hydrationgarden.fragments.PlantDialogFragment;
import com.example.hydrationgarden.models.Plant;
import com.example.hydrationgarden.models.PlantType;
import com.example.hydrationgarden.utils.FirebaseHelper;
import java.util.ArrayList;
import java.util.List;

public class GardenActivity extends AppCompatActivity implements PlantAdapter.OnPlantClickListener {
    private ActivityGardenBinding binding;
    private FirebaseHelper firebaseHelper;
    private PlantAdapter plantAdapter;
    private List<Plant> plantList;
    private boolean isGoalAchieved = false;

    // DODANE VARIJABLE:
    private int currentIntake = 0;
    private int dailyGoal = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        binding = ActivityGardenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Log.d("GardenActivity", "GardenActivity created");

        firebaseHelper = new FirebaseHelper();
        initializePlants();
        setupRecyclerView();
        setupBottomNavigation();
        checkTodayGoal();
    }

    private void initializePlants() {
        plantList = new ArrayList<>();

        // Create plants without sunflower
        plantList.add(new Plant("1", "Alojka", PlantType.ALOE));
        plantList.add(new Plant("2", "Spike", PlantType.CACTUS));
        plantList.add(new Plant("3", "Rosa", PlantType.ROSE));
        plantList.add(new Plant("4", "Orchie", PlantType.ORCHID));
        plantList.add(new Plant("5", "Cvjetko", PlantType.SIMPLE_FLOWER));
        plantList.add(new Plant("6", "Listko", PlantType.LEAFY_PLANT));

        Log.d("GardenActivity", "Initialized " + plantList.size() + " plants");
    }

    private void setupRecyclerView() {
        plantAdapter = new PlantAdapter(plantList, this);
        binding.recyclerViewPlants.setLayoutManager(new GridLayoutManager(this, 2));
        binding.recyclerViewPlants.setAdapter(plantAdapter);
    }

    private void setupBottomNavigation() {
        Log.d("Navigation", "Setting up bottom navigation in GardenActivity");

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                Log.d("Navigation", "Going to Dashboard from Garden");
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_garden) {
                return true; // Already on garden
            } else if (itemId == R.id.nav_stats) {
                Log.d("Navigation", "Going to Stats from Garden");
                startActivity(new Intent(this, StatsActivity.class));
                finish();
                return true;
            }
            return false;
        });
        binding.bottomNavigation.setSelectedItemId(R.id.nav_garden);
    }

    private void checkTodayGoal() {
        Log.d("GardenActivity", "=== CHECKING TODAY GOAL ===");

        firebaseHelper.getTodayWaterIntake()
                .thenAccept(intake -> {
                    Log.d("GardenActivity", "✅ Firebase returned INTAKE: " + intake + "ml");

                    firebaseHelper.getUser()
                            .thenAccept(user -> {
                                Log.d("GardenActivity", "✅ Firebase returned GOAL: " + user.getDailyGoal() + "ml");

                                runOnUiThread(() -> {
                                    currentIntake = intake;
                                    dailyGoal = user.getDailyGoal();
                                    isGoalAchieved = currentIntake >= dailyGoal;

                                    Log.d("GardenActivity", " FINAL CALCULATION:");
                                    Log.d("GardenActivity", "   Current intake: " + currentIntake + "ml");
                                    Log.d("GardenActivity", "   Daily goal: " + dailyGoal + "ml");
                                    Log.d("GardenActivity", "   Goal achieved: " + isGoalAchieved);

                                    updatePlantsHappiness();
                                    updateHeaderMessage();
                                });
                            })
                            .exceptionally(throwable -> {
                                Log.e("GardenActivity", "ERROR loading user: " + throwable.getMessage());
                                throwable.printStackTrace();
                                return null;
                            });
                })
                .exceptionally(throwable -> {
                    Log.e("GardenActivity", "ERROR loading water intake: " + throwable.getMessage());
                    throwable.printStackTrace();
                    return null;
                });
    }

    private void updatePlantsHappiness() {
        Log.d("GardenActivity", "Current intake: " + currentIntake + ", Goal: " + dailyGoal);
        Log.d("GardenActivity", "Goal achieved: " + isGoalAchieved);

        for (Plant plant : plantList) {
            plant.setHappy(isGoalAchieved);
            Log.d("GardenActivity", "Plant " + plant.getName() + " set to happy: " + plant.isHappy());
        }

        if (plantAdapter != null) {
            plantAdapter.notifyDataSetChanged();
        }
    }

    private void updateHeaderMessage() {
        String title, subtitle;

        if (isGoalAchieved) {
            title = "Tvoj vrt cvjeta! 🌸";
            subtitle = String.format("Sve biljke su sretne! Postigao si %d/%d ml (%d%%)",
                    currentIntake, dailyGoal, (currentIntake * 100) / dailyGoal);
        } else {
            title = "Tvoj virtualni vrt";
            subtitle = String.format("Potrebno još %d ml da usrećiš biljke! (%d/%d ml)",
                    dailyGoal - currentIntake, currentIntake, dailyGoal);
        }

        binding.tvGardenTitle.setText(title);
        binding.tvGardenSubtitle.setText(subtitle);

        Log.d("GardenActivity", "Updated header: " + title + " | " + subtitle);
    }

    @Override
    public void onPlantClick(Plant plant) {
        Log.d("GardenActivity", "Plant clicked: " + plant.getName());
        PlantDialogFragment dialog = PlantDialogFragment.newInstance(plant);
        dialog.show(getSupportFragmentManager(), "PlantDialog");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("GardenActivity", "onResume - NOT refreshing data to prevent ANR");
        // UKLONJEN checkTodayGoal(); - poziva se samo u onCreate()
    }

    public void refreshGardenData() {
        Log.d("GardenActivity", "Manually refreshing garden data");
        checkTodayGoal();
    }
}
