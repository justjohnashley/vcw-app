package com.pural_ba3a.vulcanwash;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
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
import com.google.firebase.firestore.FirebaseFirestore;
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

        // show/hide password function

        binding.newpw.setOnFocusChangeListener((v, hasFocus) -> {
            // Show or hide the eye icon based on focus
            if (hasFocus) {
                binding.eyeIcon.setVisibility(View.VISIBLE);
                binding.bgeye.setVisibility(View.VISIBLE);
            } else {
                binding.eyeIcon.setVisibility(View.GONE);
                binding.bgeye.setVisibility(View.GONE);
            }
        });

        binding.eyeIcon.setOnClickListener(v -> {
            // Toggle password visibility
            if ((binding.newpw.getInputType() & InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                // Set input type to hidden password
                binding.newpw.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                binding.eyeIcon.setImageResource(R.drawable.baseline_visibility_off_24); // Closed eye icon
            } else {
                // Set input type to visible password
                binding.newpw.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                binding.eyeIcon.setImageResource(R.drawable.baseline_visibility_24); // Open eye icon
            }

            // Move the cursor to the end of the text
            binding.newpw.setSelection(binding.newpw.getText().length());
        });

        binding.backBtn.setOnClickListener(view -> {
            if (user != null) {
                // Retrieve the current user's UID
                String uid = user.getUid();

                // Access Firestore to determine user type
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("users").document(uid)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String userType = documentSnapshot.getString("usertype");

                                // Redirect based on user type
                                if ("manager".equals(userType)) {
                                    Intent intent = new Intent(ChangePw.this, ManagerPage.class);
                                    startActivity(intent);
                                } else if ("customer".equals(userType)) {
                                    Intent intent = new Intent(ChangePw.this, CustomerPage.class);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(ChangePw.this, "Unknown user type.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(ChangePw.this, "User data not found.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(ChangePw.this, "Failed to fetch user data.", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(ChangePw.this, "User not logged in.", Toast.LENGTH_SHORT).show();
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
            binding.newpw.setError("Password must be 8 characters long and contain at least 2 uppercase letters, 2 lowercase letters, 2 digits, and 1 special character.");
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
        // Password must be at least 8 characters
        if (password.length() < 8) {
            return false;
        }

        int upperCaseCount = 0;
        int lowerCaseCount = 0;
        int digitCount = 0;
        int specialCharCount = 0;

        // Check each character in the password
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                upperCaseCount++;
            } else if (Character.isLowerCase(c)) {
                lowerCaseCount++;
            } else if (Character.isDigit(c)) {
                digitCount++;
            } else if (!Character.isLetterOrDigit(c)) {
                specialCharCount++;
            }
        }

        // Validate that all criteria are met
        return upperCaseCount >= 2 && lowerCaseCount >= 2 && digitCount >= 2 && specialCharCount >= 1;
    }

}