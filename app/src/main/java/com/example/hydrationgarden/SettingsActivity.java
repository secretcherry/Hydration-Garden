package com.example.hydrationgarden;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.hydrationgarden.databinding.ActivitySettingsBinding;
import com.example.hydrationgarden.utils.FirebaseHelper;

public class SettingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding binding;
    private FirebaseHelper firebaseHelper;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseHelper = new FirebaseHelper();
        prefs = getSharedPreferences("app_settings", MODE_PRIVATE);

        setupUI();
        loadCurrentSettings();
    }

    private void setupUI() {
        // Back button
        binding.btnBack.setOnClickListener(v -> finish());

        // Dark theme switch
        binding.switchDarkTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("dark_theme", isChecked).apply();

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        // Daily goal buttons
        binding.btn1500ml.setOnClickListener(v -> setDailyGoal(1500));
        binding.btn2000ml.setOnClickListener(v -> setDailyGoal(2000));
        binding.btn2500ml.setOnClickListener(v -> setDailyGoal(2500));
        binding.btn3000ml.setOnClickListener(v -> setDailyGoal(3000));

        // Custom goal
        binding.btnSetCustomGoal.setOnClickListener(v -> {
            String customGoalStr = binding.etCustomGoal.getText().toString().trim();
            if (!customGoalStr.isEmpty()) {
                try {
                    int customGoal = Integer.parseInt(customGoalStr);
                    if (customGoal >= 500 && customGoal <= 5000) {
                        setDailyGoal(customGoal);
                    } else {
                        Toast.makeText(this, "Cilj mora biti između 500ml i 5000ml",
                                Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Unesite valjanu vrijednost",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadCurrentSettings() {
        // Load theme setting
        boolean isDarkTheme = prefs.getBoolean("dark_theme", false);
        binding.switchDarkTheme.setChecked(isDarkTheme);

        // Load current daily goal
        firebaseHelper.getUser()
                .thenAccept(user -> {
                    runOnUiThread(() -> {
                        binding.tvCurrentGoal.setText("Trenutni cilj: " + user.getDailyGoal() + "ml");
                    });
                });
    }

    private void setDailyGoal(int newGoal) {
        firebaseHelper.updateDailyGoal(newGoal)
                .thenRun(() -> {
                    runOnUiThread(() -> {
                        binding.tvCurrentGoal.setText("Trenutni cilj: " + newGoal + "ml");
                        Toast.makeText(this, "Dnevni cilj ažuriran na " + newGoal + "ml!",
                                Toast.LENGTH_SHORT).show();
                    });
                })
                .exceptionally(throwable -> {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Greška pri ažuriranju cilja",
                                Toast.LENGTH_SHORT).show();
                    });
                    return null;
                });
    }
}