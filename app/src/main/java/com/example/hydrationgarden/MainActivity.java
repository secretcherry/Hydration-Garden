package com.example.hydrationgarden;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.hydrationgarden.utils.FirebaseHelper;

public class MainActivity extends AppCompatActivity {
    private FirebaseHelper firebaseHelper;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before calling super.onCreate()
        applyTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseHelper = new FirebaseHelper();
        prefs = getSharedPreferences("app_settings", MODE_PRIVATE);

        // Splash screen delay
        new Handler().postDelayed(() -> {
            checkUserAuthentication();
        }, 2000);
    }

    private void applyTheme() {
        prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        boolean isDarkTheme = prefs.getBoolean("dark_theme", false);

        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void checkUserAuthentication() {
        if (firebaseHelper.isUserLoggedIn()) {
            startActivity(new Intent(this, DashboardActivity.class));
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
        finish();
    }
}
