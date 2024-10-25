package com.pural_ba3a.vulcanwash;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pural_ba3a.vulcanwash.databinding.ChangePwBinding;

public class ChangePw extends AppCompatActivity {

    ChangePwBinding binding;
    FirebaseAuth mAuth;
    FirebaseUser user;
    NetworkMonitor networkMonitor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ChangePwBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        networkMonitor = new NetworkMonitor(getApplicationContext());

        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.vcw_loading);
        binding.loadingVideoView.setVideoURI(videoUri);
        binding.loadingVideoView.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            binding.loadingVideoView.start();

        });

        binding.oldpw.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword2();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        binding.newpw.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });


        binding.checkBtn.setOnClickListener(v -> {
            if (networkMonitor.isNetworkAvailable()) {
                binding.pgbarOverlay.setVisibility(View.VISIBLE);
            binding.pgbarOverlay.setAlpha(1f);

                if (validatePassword()) {
                    String currentPassword = binding.oldpw.getText().toString().trim();
                    String newPassword = binding.newpw.getText().toString().trim();

            new Handler().postDelayed(() -> {
            if (!currentPassword.isEmpty() && !newPassword.isEmpty()) {
                changeUserPassword(currentPassword, newPassword);
            } else {
                binding.pgbarOverlay.setAlpha(0f);
                binding.pgbarOverlay.setVisibility(View.VISIBLE);
                Toast.makeText(ChangePw.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            }
            }, 2000);
                } else {
                    binding.pgbarOverlay.setVisibility(View.GONE);
                }
            } else {
                binding.pgbarOverlay.setVisibility(View.GONE);
                Toast.makeText(this, "No internet connection. Please connect to the internet to proceed.", Toast.LENGTH_LONG).show();
            }
        });


    }

    @Override
    protected void onPause() {
        super.onPause();

        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister the network callback when the fragment or activity is destroyed
        binding = null;
        networkMonitor.unregisterCallback();
    }

    public void changeUserPassword(String currentPassword, String newPassword) {
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        if (user != null) {
            String email = user.getEmail();

            // Re-authenticate the user
            AuthCredential credential = EmailAuthProvider.getCredential(email, currentPassword);
            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Re-authentication successful, proceed with password update
                    new Handler().postDelayed(() -> {
                    user.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                        if (updateTask.isSuccessful()) {
                            Toast.makeText(ChangePw.this, "Password updated successfully.", Toast.LENGTH_SHORT).show();
                            mAuth.signOut();

                            Intent intent = new Intent(ChangePw.this, CustomerPage.class);
                            startActivity(intent);
                            Toast.makeText(ChangePw.this, "Session Expired. Please login again.", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            // Password update failed
                            binding.pgbarOverlay.setAlpha(0f);
                            binding.pgbarOverlay.setVisibility(View.VISIBLE);
                            Toast.makeText(ChangePw.this, "Failed to update password.", Toast.LENGTH_SHORT).show();
                        }

                    });
                    }, 1000);
                } else {
                    binding.pgbarOverlay.setVisibility(View.GONE);
                    Toast.makeText(ChangePw.this, "Re-authentication failed. Please check your current password.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Real-time password validation
    private boolean validatePassword() {
        String password = binding.newpw.getText().toString().trim();
        if (password.isEmpty()) {
            binding.newpw.setBackgroundResource(R.drawable.border_error);
            binding.newpw.setError("Password cannot be empty.");
            return false;
        } else if (!isValidPassword(password)) {
            binding.newpw.setBackgroundResource(R.drawable.border_error);
            binding.newpw.setError("Password must be at least 8 characters and contain letters and numbers.");
            return false;
        } else {
            binding.newpw.setError(null);
            binding.newpw.setBackgroundResource(R.drawable.border_success);
            // Clear the error
            return true;
        }
    }

    private boolean validatePassword2() {
        String password = binding.oldpw.getText().toString().trim();
        if (password.isEmpty()) {
            binding.oldpw.setBackgroundResource(R.drawable.border_error);
            binding.oldpw.setError("Password cannot be empty.");
            return false;
        } else {
            binding.oldpw.setError(null);
            binding.oldpw.setBackgroundResource(R.drawable.border_success);
            // Clear the error
            return true;
        }
    }

    private boolean isValidPassword(String password) {
        // Password must be at least 8 characters and contain both letters and numbers
        if (password.length() < 8) {
            return false;
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            }
            if (Character.isDigit(c)) {
                hasDigit = true;
            }
            if (hasLetter && hasDigit) {
                return true;
            }
        }
        return false;
    }

}