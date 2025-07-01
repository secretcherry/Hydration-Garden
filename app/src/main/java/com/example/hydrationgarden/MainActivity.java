package com.example.hydrationgarden;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        checkUserLoginStatus();
    }

    private void checkUserLoginStatus() {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            // Korisnik je već ulogiran, idi na Dashboard
            Log.d("MainActivity", "User already logged in: " + currentUser.getUid());
            startActivity(new Intent(this, DashboardActivity.class));
        } else {
            // Korisnik nije ulogiran, idi na Login
            Log.d("MainActivity", "No user logged in, going to login");
            startActivity(new Intent(this, LoginActivity.class));
        }

        finish();
    }
}