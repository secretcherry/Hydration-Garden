package com.example.hydrationgarden;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.hydrationgarden.databinding.ActivityLoginBinding;
import com.example.hydrationgarden.utils.FirebaseHelper;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseHelper = new FirebaseHelper();
        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            if (validateInput(email, password)) {
                loginUser(email, password);
            }
        });

        binding.tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private boolean validateInput(String email, String password) {
        if (email.isEmpty()) {
            binding.etEmail.setError(getString(R.string.error_email_empty));
            return false;
        }

        if (password.isEmpty()) {
            binding.etPassword.setError(getString(R.string.error_password_empty));
            return false;
        }

        return true;
    }

    private void loginUser(String email, String password) {
        binding.btnLogin.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        firebaseHelper.loginUser(email, password)
                .thenAccept(userId -> {
                    runOnUiThread(() -> {
                        binding.btnLogin.setEnabled(true);
                        binding.progressBar.setVisibility(View.GONE);

                        startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                        finish();
                    });
                })
                .exceptionally(throwable -> {
                    runOnUiThread(() -> {
                        binding.btnLogin.setEnabled(true);
                        binding.progressBar.setVisibility(View.GONE);

                        Toast.makeText(LoginActivity.this,
                                getString(R.string.error_login_failed) + ": " + throwable.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
                    return null;
                });
    }
}