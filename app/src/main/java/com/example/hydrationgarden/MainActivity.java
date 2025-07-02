package com.example.hydrationgarden;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.example.hydrationgarden.utils.FirebaseHelper;
import com.example.hydrationgarden.utils.ThemeHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before calling super.onCreate()
        ThemeHelper.applyTheme(this);

        super.onCreate(savedInstanceState);

        // Hide action bar if it exists
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Log.d("MainActivity", "MainActivity started");

        FirebaseHelper firebaseHelper = new FirebaseHelper();

        if (firebaseHelper.isUserLoggedIn()) {
            Log.d("MainActivity", "User is logged in, going to Dashboard");
            startActivity(new Intent(this, DashboardActivity.class));
        } else {
            Log.d("MainActivity", "User not logged in, going to Login");
            startActivity(new Intent(this, LoginActivity.class));
        }

        finish();
    }
}
