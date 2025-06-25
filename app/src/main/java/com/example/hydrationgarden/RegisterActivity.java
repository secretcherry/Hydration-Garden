package com.example.hydrationgarden;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.hydrationgarden.databinding.ActivityRegisterBinding;
import com.example.hydrationgarden.utils.FirebaseHelper;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseHelper = new FirebaseHelper();
        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.btnRegister.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            String confirmPassword = binding.etConfirmPassword.getText().toString().trim();
            String name = binding.etName.getText().toString().trim();

            if (validateInput(email, password, confirmPassword, name)) {
                registerUser(email, password, name);
            }
        });

        binding.tvLogin.setOnClickListener(v -> {
            finish();
        });
    }

    private boolean validateInput(String email, String password, String confirmPassword, String name) {
        if (name.isEmpty()) {
            binding.etName.setError(getString(R.string.error_name_empty));
            return false;
        }

        if (email.isEmpty()) {
            binding.etEmail.setError(getString(R.string.error_email_empty));
            return false;
        }

        if (password.isEmpty()) {
            binding.etPassword.setError(getString(R.string.error_password_empty));
            return false;
        }

        if (!password.equals(confirmPassword)) {
            binding.etConfirmPassword.setError(getString(R.string.error_passwords_dont_match));
            return false;
        }

        return true;
    }

    private void registerUser(String email, String password, String name) {
        binding.btnRegister.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        firebaseHelper.registerUser(email, password, name)
                .thenAccept(userId -> {
                    runOnUiThread(() -> {
                        binding.btnRegister.setEnabled(true);
                        binding.progressBar.setVisibility(View.GONE);

                        Toast.makeText(this, "Uspješna registracija!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, DashboardActivity.class));
                        finish();
                    });
                })
                .exceptionally(throwable -> {
                    runOnUiThread(() -> {
                        binding.btnRegister.setEnabled(true);
                        binding.progressBar.setVisibility(View.GONE);

                        Toast.makeText(RegisterActivity.this,
                                getString(R.string.error_register_failed) + ": " + throwable.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
                    return null;
                });
    }
}