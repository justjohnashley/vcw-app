package com.pural_ba3a.vulcanwash;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pural_ba3a.vulcanwash.databinding.CustomerRegBinding;
import com.pural_ba3a.vulcanwash.databinding.ManagerRegBinding;

import java.util.HashMap;
import java.util.Map;

public class ManagerReg extends AppCompatActivity {

    ManagerRegBinding binding;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    NetworkMonitor networkMonitor;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), AdminPage.class);
            startActivity(intent);

            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        networkMonitor = new NetworkMonitor(getApplicationContext());

        binding = ManagerRegBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.backBtn.setOnClickListener(view -> {

            Intent intent = new Intent(ManagerReg.this, HomePage.class);
            startActivity(intent);

            finish();

        });

        binding.login.setOnClickListener(view -> {

            Intent intent = new Intent(ManagerReg.this, ManagerPage.class);
            startActivity(intent);

            finish();


        });

        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.vcw_loading);
        binding.loadingVideoView.setVideoURI(videoUri);
        binding.loadingVideoView.setOnPreparedListener(mp -> {
            mp.setLooping(true);  // Loop the video
            binding.loadingVideoView.start();

        });

        binding.email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateEmail();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        binding.password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        // show/hide password function

        binding.password.setOnFocusChangeListener((v, hasFocus) -> {
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
            if ((binding.password.getInputType() & InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                // Set input type to hidden password
                binding.password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                binding.eyeIcon.setImageResource(R.drawable.baseline_visibility_off_24); // Closed eye icon
            } else {
                // Set input type to visible password
                binding.password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                binding.eyeIcon.setImageResource(R.drawable.baseline_visibility_24); // Open eye icon
            }

            // Move the cursor to the end of the text
            binding.password.setSelection(binding.password.getText().length());
        });

        binding.signupBtn.setOnClickListener(view -> {
            binding.pgbarOverlay.setVisibility(View.VISIBLE);
            binding.pgbarOverlay.setAlpha(1f);
            binding.pgbarOverlay.setClickable(true);
            binding.pgbarOverlay.setFocusable(true);
            if (networkMonitor.isNetworkAvailable()) {
                if (validateEmail() && validatePassword()) {
                    String email = binding.email.getText().toString().trim();
                    String password = binding.password.getText().toString().trim();


                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    new Handler().postDelayed(() -> {
                                        binding.pgbarOverlay.setVisibility(View.GONE);
                                        binding.pgbarOverlay.setClickable(false);
                                        binding.pgbarOverlay.setFocusable(false);

                                        if (task.isSuccessful()) {
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            if (user != null) {
                                                String uid = user.getUid();
                                                String email = user.getEmail();

                                                // Create the customer data to save
                                                Map<String, Object> managerData = new HashMap<>();
                                                managerData.put("uid", uid);
                                                managerData.put("email", email);
                                                managerData.put("usertype", "manager");
                                                managerData.put("status","pending");

                                                // Save the data in Firestore
                                                firestore.collection("users").document(uid)
                                                        .set(managerData)
                                                        .addOnCompleteListener(task1 -> {
                                                            if (task1.isSuccessful()) {
                                                                Toast.makeText(ManagerReg.this, "Account created successfully.", Toast.LENGTH_SHORT).show();
                                                                Intent intent = new Intent(ManagerReg.this, AdminInfo.class);
                                                                startActivity(intent);
                                                                finish();
                                                            } else {
                                                                Toast.makeText(ManagerReg.this, "Failed to create Firestore entry.", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            }
                                        } else {
                                            binding.pgbarOverlay.setAlpha(0);
                                            binding.pgbarOverlay.setVisibility(View.VISIBLE);
                                            Toast.makeText(ManagerReg.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                        }
                                    }, 1000);
                                }
                            });
                } else {
                    binding.pgbarOverlay.setVisibility(View.GONE);
                }
            } else {
                Toast.makeText(this, "No internet connection. Please connect to the internet to proceed with this request.", Toast.LENGTH_LONG).show();
            }
        });

        binding.privacypolicy.setOnClickListener(view -> {
            // The URL to the privacy policy
            String url = "https://sites.google.com/view/vcwashapp-privacypolicy/home";

            // Create an Intent to open the URL
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

            // Start the activity to open the website
            startActivity(intent);
        });


    }

    // Real-time email validation
    private boolean validateEmail() {
        String email = binding.email.getText().toString().trim();
        if (email.isEmpty()) {
            binding.email.setBackgroundResource(R.drawable.border_error);
            binding.email.setError("Email cannot be empty.");
            return false;
        } else if (!isValidEmail(email)) {
            binding.email.setBackgroundResource(R.drawable.border_error);
            binding.email.setError("Please enter a valid email.");
            return false;
        } else {
            binding.email.setError(null);
            binding.email.setBackgroundResource(R.drawable.border_success);
            return true;
        }
    }

    // Real-time password validation
    private boolean validatePassword() {
        String password = binding.password.getText().toString().trim();
        if (password.isEmpty()) {
            binding.password.setBackgroundResource(R.drawable.border_error);
            binding.password.setError("Password cannot be empty.");
            return false;
        } else if (!isValidPassword(password)) {
            binding.password.setBackgroundResource(R.drawable.border_error);
            binding.password.setError("Password must be 8 characters long and contain at least 2 uppercase letters, 2 lowercase letters, 2 digits, and 1 special character.");
            return false;
        } else {
            binding.password.setError(null);
            binding.password.setBackgroundResource(R.drawable.border_success);
            // Clear the error
            return true;
        }
    }


    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
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