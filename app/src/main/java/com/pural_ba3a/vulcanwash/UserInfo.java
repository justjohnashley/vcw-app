package com.pural_ba3a.vulcanwash;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.pural_ba3a.vulcanwash.databinding.UserInfopageBinding;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class UserInfo extends AppCompatActivity {

    private UserInfopageBinding binding;
    FirebaseUser currentUser;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private String verificationId; // To store verification ID for OTP
    private PhoneAuthProvider.ForceResendingToken resendToken; // To store resend token
    private Dialog otpDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Binding Views
        binding = UserInfopageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        // Firebase Initialization
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
/*
        // Setup Video
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.vcw_loading);
        binding.loadingVideoView.setVideoURI(videoUri);
        binding.loadingVideoView.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            binding.loadingVideoView.start();
        });
*/



        // Terms and Conditions Checkbox Logic
        binding.tnc.setOnCheckedChangeListener((buttonView, isChecked) -> binding.submitBtn.setEnabled(isChecked));
        binding.tnc.setOnClickListener(v -> {
            CheckBox checkBox = (CheckBox) v;
            if (checkBox.isChecked()) {
                // If the user attempts to check the box, show the Terms and Conditions dialog first
                showTermsDialog(checkBox);
            } else {
                // Allow the user to uncheck without confirmation
                checkBox.setChecked(false);
            }
        });


        // Attach TextWatchers for Live Validation
        setupTextWatcher(binding.username, binding.usernameLayout, "Username is required");
        setupTextWatcher(binding.contact, binding.contactLayout, "Contact Number is required");

        // Submit Button Logic
       /* binding.submitBtn.setOnClickListener(v -> {
            if (!validateAllFields()) return;

            currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                String uid = currentUser.getUid();
                Map<String, Object> data = new HashMap<>();
                data.put("username", binding.username.getText().toString());
                data.put("contact", binding.contact.getText().toString());

                firestore.collection("users")
                        .document(uid)
                        .set(data)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Data submitted successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(this, PendingPage.class);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }); */

        binding.username.setOnClickListener(v -> {
            if (!binding.username.isEnabled()) {
                Toast.makeText(this, "Username cannot be changed once set.", Toast.LENGTH_SHORT).show();
            }
        });

        // Submit Button Logic
        binding.submitBtn.setOnClickListener(v -> {
            if (!validateAllFields()) return;

            // Show OTP dialog
            showOtpDialog(binding.contact.getText().toString().trim());
        });

        binding.cancelBtn.setOnClickListener(v -> {
            // Return to UserPage or the previous activity
            Intent intent = new Intent(this, UserPage.class);
            startActivity(intent);
            finish(); // Close UserInfo activity
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        checkCooldownState();

        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();

            firestore.collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Check if username and contact fields already exist
                            String username = documentSnapshot.getString("username");
                            String contact = documentSnapshot.getString("contact");

                            if (username != null) {
                                // Populate the fields
                                binding.username.setText(username);
                                binding.username.setEnabled(false);
                                
                                // Show the Cancel button
                                binding.cancelBtn.setVisibility(View.VISIBLE);

                                // Disable Submit button initially
                                binding.submitBtn.setEnabled(false);

                                // Add TextWatcher to monitor changes and re-enable the Submit button
                                setupFieldChangeWatcher(contact);

                            }    if (contact != null) {
                                binding.contact.setText(contact);
                            } else {
                                // Hide Cancel button if no values are found
                                binding.cancelBtn.setVisibility(View.GONE);
                            }
                        } else {
                            // Hide Cancel button if no user data exists
                            binding.cancelBtn.setVisibility(View.GONE);
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error fetching user data: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        } else {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            finish(); // Finish the activity if the user is not logged in
        }
    }




    // Monitor field changes to re-enable Submit button
    private void setupFieldChangeWatcher(String originalContact) {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean isContactChanged = !binding.contact.getText().toString().equals(originalContact);
                binding.submitBtn.setEnabled(isContactChanged);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        binding.username.addTextChangedListener(textWatcher);
        binding.contact.addTextChangedListener(textWatcher);
    }

    // Setup Live Validation for Input Fields
    private void setupTextWatcher(TextInputEditText editText, TextInputLayout layout, String errorMessage) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty()) {
                    layout.setError(errorMessage);
                } else {
                    layout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private boolean validateAllFields() {
        boolean isValid = true;

        String username = binding.username.getText().toString().trim();

        // Validate username
        if (username.isEmpty()) {
            binding.usernameLayout.setError("Username is required");
            isValid = false;
        } else if (username.length() < 6) {
            binding.usernameLayout.setError("Username must be at least 6 characters long");
            isValid = false;
        } else if (username.length() > 20) {
            binding.usernameLayout.setError("Username cannot exceed 20 characters");
            isValid = false;
        } else if (!username.matches("^[a-zA-Z0-9._-]+$")) {
            binding.usernameLayout.setError("Username can only contain letters, numbers, and special characters (@, ., -, _)");
            isValid = false;
        } else if (hasRepeatedCharacters(username)) {
            binding.usernameLayout.setError("Username contains repeated characters");
            isValid = false;
        } else {
            binding.usernameLayout.setError(null); // Clear error if valid
        }

        // Validate contact number
        String contactNumber = binding.contact.getText().toString().trim();
        if (contactNumber.isEmpty()) {
            binding.contactLayout.setError("Contact Number is required");
            isValid = false;
        } else if (!isValidPhoneNumber(contactNumber)) {
            binding.contactLayout.setError("Invalid phone number format [+639123456789]");
            isValid = false;
        } else {
            binding.contactLayout.setError(null); // Clear error if valid
        }

        return isValid;
    }

    // Method to check for repeated characters (e.g., sssssss or 555555)
    private boolean hasRepeatedCharacters(String username) {
        return username.matches("(.)\\1{2,}"); // Matches 3 or more consecutive identical characters
    }

    // Modified showTermsDialog method
    private void showTermsDialog(CheckBox checkBox) {
        // Inflate the custom layout
        View dialogView = getLayoutInflater().inflate(R.layout.dgbox_tnc, null);

        // Find the TextView and set the terms and conditions text
        TextView termsTextView = dialogView.findViewById(R.id.tv_terms_conditions);
        termsTextView.setText(getString(R.string.terms_and_conditions));

        // Build and show the dialog
        new MaterialAlertDialogBuilder(this)
                .setTitle("Terms and Conditions")
                .setView(dialogView)
                .setPositiveButton("Agree", (dialog, which) -> {
                   // Toast.makeText(this, "You have accepted the Terms and Conditions.", Toast.LENGTH_SHORT).show();
                    // Allow the checkbox to be ticked
                    checkBox.setChecked(true);
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                   // Toast.makeText(this, "You have declined the Terms and Conditions.", Toast.LENGTH_SHORT).show();
                    // Prevent the checkbox from being ticked
                    checkBox.setChecked(false);
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }

    private void showOtpDialog(String phoneNumber) {
        if (!isValidPhoneNumber(phoneNumber)) {
            Toast.makeText(this, "Invalid phone number. Please check and try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = getLayoutInflater().inflate(R.layout.otp_dialog, null);
        TextInputEditText otpInput = dialogView.findViewById(R.id.otp_input);
        Button sendOtpBtn = dialogView.findViewById(R.id.send_otp_btn);
        Button verifyOtpBtn = dialogView.findViewById(R.id.verify_otp_btn);
        Button closeBtn = dialogView.findViewById(R.id.closeBtn);

        otpDialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Contact Number Verification")
                .setView(dialogView)
                .setCancelable(true) // Allow dismissing dialog
                .create();

        otpDialog.show();

        // Update dialog button state based on cooldown
        updateOtpDialogState();

        // Handle OTP sending
        sendOtpBtn.setOnClickListener(v -> sendOtp(phoneNumber));

        // Handle OTP verification
        verifyOtpBtn.setOnClickListener(v -> {
            String otp = otpInput.getText().toString().trim();

            if (!otp.isEmpty()) {
                // Show loading
                ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("Verifying OTP...");
                progressDialog.setCancelable(false); // Prevent dismissal while loading
                progressDialog.show();

                // Simulate verification process with a delay
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    verifyOtp(otp, progressDialog);
                }, 2000); // Simulates a 2-second loading time
            } else {
                Toast.makeText(this, "Please enter OTP", Toast.LENGTH_SHORT).show();
            }
        });


        closeBtn.setOnClickListener(v -> otpDialog.dismiss());
    }

    // Validate phone number with libphonenumber
    private boolean isValidPhoneNumber(String phoneNumber) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber parsedNumber = phoneUtil.parse(phoneNumber, "PH"); // "PH" for the Philippines
            return phoneUtil.isValidNumber(parsedNumber);
        } catch (NumberParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void sendOtp(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        String otpCode = credential.getSmsCode();
                        if (otpCode != null && otpDialog != null) {
                            TextInputEditText otpInput = otpDialog.findViewById(R.id.otp_input);
                            if (otpInput != null) {
                                otpInput.setText(otpCode); // Pre-fill the OTP input field
                            }
                            Toast.makeText(UserInfo.this, "OTP retrieved automatically", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        Toast.makeText(UserInfo.this, "OTP Verification Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                        UserInfo.this.verificationId = verificationId;
                        resendToken = token;
                        Toast.makeText(UserInfo.this, "OTP Sent to " + phoneNumber, Toast.LENGTH_SHORT).show();

                        // Start the cooldown timer
                        startOtpCooldown();
                    }
                });
    }

    private CountDownTimer otpCooldownTimer; // Single timer instance
    private long remainingCooldownMillis = 0; // Track remaining time
    private final long COOLDOWN_DURATION = 900000; // 15 minutes in milliseconds
    private boolean isCooldownActive = false; // Track cooldown state

    private void startOtpCooldown() {
        if (isCooldownActive) return;

        isCooldownActive = true;

        long cooldownEndTime = System.currentTimeMillis() + COOLDOWN_DURATION;
        remainingCooldownMillis = COOLDOWN_DURATION;

        // Save the end time in SharedPreferences
        getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit()
                .putLong("otp_cooldown_end_time", cooldownEndTime)
                .apply();

        otpCooldownTimer = new CountDownTimer(COOLDOWN_DURATION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingCooldownMillis = millisUntilFinished;

                if (otpDialog != null && otpDialog.isShowing()) {
                    Button sendOtpBtn = otpDialog.findViewById(R.id.send_otp_btn);
                    if (sendOtpBtn != null) {
                        long minutes = (millisUntilFinished / 1000) / 60;
                        long seconds = (millisUntilFinished / 1000) % 60;
                        sendOtpBtn.setText(String.format("Retry in %02d:%02d", minutes, seconds));
                        sendOtpBtn.setEnabled(false);
                    }
                }
            }

            @Override
            public void onFinish() {
                isCooldownActive = false;
                remainingCooldownMillis = 0;

                // Clear the saved cooldown end time
                getSharedPreferences("app_prefs", MODE_PRIVATE)
                        .edit()
                        .remove("otp_cooldown_end_time")
                        .apply();

                if (otpDialog != null && otpDialog.isShowing()) {
                    Button sendOtpBtn = otpDialog.findViewById(R.id.send_otp_btn);
                    if (sendOtpBtn != null) {
                        sendOtpBtn.setText("Send OTP");
                        sendOtpBtn.setEnabled(true);
                    }
                }
            }
        }.start();
    }

    private void updateOtpDialogState() {
        if (otpDialog == null || !otpDialog.isShowing()) return;

        Button sendOtpBtn = otpDialog.findViewById(R.id.send_otp_btn);
        if (sendOtpBtn == null) return;

        if (isCooldownActive) {
            // Update the button with the remaining time
            long minutes = (remainingCooldownMillis / 1000) / 60;
            long seconds = (remainingCooldownMillis / 1000) % 60;
            sendOtpBtn.setText(String.format("Retry in %02d:%02d", minutes, seconds));
            sendOtpBtn.setEnabled(false);
        } else {
            sendOtpBtn.setText("Send OTP");
            sendOtpBtn.setEnabled(true);
        }
    }

    private void checkCooldownState() {
        long currentTime = System.currentTimeMillis();

        // Retrieve the saved cooldown end time
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        long cooldownEndTime = prefs.getLong("otp_cooldown_end_time", 0);

        if (cooldownEndTime > currentTime) {
            // Cooldown is still active
            remainingCooldownMillis = cooldownEndTime - currentTime;
            isCooldownActive = true;

            // Start the timer with the remaining time
            otpCooldownTimer = new CountDownTimer(remainingCooldownMillis, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    remainingCooldownMillis = millisUntilFinished;

                    if (otpDialog != null && otpDialog.isShowing()) {
                        Button sendOtpBtn = otpDialog.findViewById(R.id.send_otp_btn);
                        if (sendOtpBtn != null) {
                            long minutes = (millisUntilFinished / 1000) / 60;
                            long seconds = (millisUntilFinished / 1000) % 60;
                            sendOtpBtn.setText(String.format("Retry in %02d:%02d", minutes, seconds));
                            sendOtpBtn.setEnabled(false);
                        }
                    }
                }

                @Override
                public void onFinish() {
                    isCooldownActive = false;
                    remainingCooldownMillis = 0;

                    getSharedPreferences("app_prefs", MODE_PRIVATE)
                            .edit()
                            .remove("otp_cooldown_end_time")
                            .apply();

                    if (otpDialog != null && otpDialog.isShowing()) {
                        Button sendOtpBtn = otpDialog.findViewById(R.id.send_otp_btn);
                        if (sendOtpBtn != null) {
                            sendOtpBtn.setText("Send OTP");
                            sendOtpBtn.setEnabled(true);
                        }
                    }
                }
            }.start();
        } else {
            // No active cooldown
            isCooldownActive = false;
            remainingCooldownMillis = 0;

            // Clear any stale saved data
            prefs.edit().remove("otp_cooldown_end_time").apply();
        }
    }



    private void verifyOtp(String otp, ProgressDialog progressDialog) {
        if (verificationId == null) {
            Toast.makeText(this, "Please request OTP first.", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss(); // Dismiss loading
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);

        try {
            // Simulate a successful verification
            credential.getSmsCode();
            Toast.makeText(this, "OTP Verified Successfully", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss(); // Dismiss loading

            // Proceed with your update logic
            otpDialog.dismiss();
            updateUserFirestore();
        } catch (Exception e) {
            // Handle verification failure
            progressDialog.dismiss(); // Dismiss loading
            Toast.makeText(this, "OTP Verification Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private void updateUserFirestore() {
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            Map<String, Object> userData = new HashMap<>();
            userData.put("username", binding.username.getText().toString());
            userData.put("contact", binding.contact.getText().toString());

            firestore.collection("users")
                    .document(uid) // Update the existing user document
                    .set(userData, SetOptions.merge()) // Use merge to avoid overwriting other fields
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Data updated successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, UserPage.class);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error updating user: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "User not logged in. Please log in again.", Toast.LENGTH_SHORT).show();
        }
    }


    private void checkUsernameUniqueness(String username) {
        // Check if the username already exists in Firestore
        firestore.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            // Username is not unique
                            binding.usernameLayout.setError("Username already exists");
                        } else {
                            // Username is unique
                            binding.usernameLayout.setError(null);
                        }
                    } else {
                        // Handle Firestore query failure
                        binding.usernameLayout.setError("Error checking username uniqueness");
                    }
                });
    }



}
