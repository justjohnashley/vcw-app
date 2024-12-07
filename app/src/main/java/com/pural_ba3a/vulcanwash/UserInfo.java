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
import com.google.firebase.auth.PhoneAuthOptions;
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

    private String userType; // Class-level variable to store user type


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
            if ("manager".equalsIgnoreCase(userType)) {
                // Redirect to AdminPage
                Intent intent = new Intent(this, AdminPage.class);
                startActivity(intent);
            } else if ("customer".equalsIgnoreCase(userType)) {
                // Redirect to UserPage
                Intent intent = new Intent(this, UserPage.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Unknown user type. Returning to default page.", Toast.LENGTH_SHORT).show();
                // Optionally, send to a default page if usertype is invalid
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
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
                            // Fetch and store usertype
                            userType = documentSnapshot.getString("usertype");
                            String shopName = documentSnapshot.getString("shopName");
                            String username = documentSnapshot.getString("username");
                            String contact = documentSnapshot.getString("contact");

                            if ("manager".equalsIgnoreCase(userType)) {
                                // Show shopName layout only
                                binding.shopnameLayout.setVisibility(View.VISIBLE);
                                binding.usernameLayout.setVisibility(View.GONE);

                                if (shopName != null && !shopName.isEmpty()) {
                                    binding.shopname.setText(shopName);
                                    binding.shopname.setEnabled(false);
                                }
                            } else if ("customer".equalsIgnoreCase(userType)) {
                                // Show username layout only
                                binding.usernameLayout.setVisibility(View.VISIBLE);
                                binding.shopnameLayout.setVisibility(View.GONE);

                                if (username != null && !username.isEmpty()) {
                                    binding.username.setText(username);
                                    binding.username.setEnabled(false);
                                }
                            } else {
                                // Fallback: Show both layouts
                                binding.shopnameLayout.setVisibility(View.VISIBLE);
                                binding.usernameLayout.setVisibility(View.VISIBLE);
                            }

                            if (contact != null && !contact.isEmpty()) {
                                binding.contact.setText(contact);
                            }

                            if ((shopName != null || username != null) && contact != null) {
                                binding.cancelBtn.setVisibility(View.VISIBLE);
                                binding.submitBtn.setEnabled(false);
                            } else {
                                binding.cancelBtn.setVisibility(View.GONE);
                            }
                        } else {
                            // Show all layouts if user data is missing
                            binding.usernameLayout.setVisibility(View.VISIBLE);
                            binding.shopnameLayout.setVisibility(View.VISIBLE);
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

        // Validate username only if its layout is not GONE
        if (binding.usernameLayout.getVisibility() != View.GONE) {
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
        }

        // Validate shopname only if its layout is not GONE (for manager users)
        if (binding.shopnameLayout.getVisibility() != View.GONE) {
            String shopName = binding.shopname.getText().toString().trim();

            // Validate shop name
            if (shopName.isEmpty()) {
                binding.shopnameLayout.setError("Shop Name is required");
                isValid = false;
            } else {
                binding.shopnameLayout.setError(null); // Clear error if valid
            }
        }

        // Validate contact number (always validate if visible)
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

    // Send OTP to the provided phone number for verification (not for login)
    private void sendOtp(String phoneNumber) {
        // Show progress dialog while waiting for OTP to be sent
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending OTP...");
        progressDialog.setCancelable(false); // Prevent dismissing by tapping outside
        progressDialog.show();

        // PhoneAuthOptions to send OTP
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber) // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout duration
                .setActivity(this) // Activity (for callback binding)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        // If OTP is automatically verified (e.g., via auto-retrieval)
                        String otp = credential.getSmsCode();
                        if (otp != null) {
                            TextInputEditText otpInput = otpDialog.findViewById(R.id.otp_input);
                            if (otpInput != null) {
                                otpInput.setText(otp);
                            }
                        }

                        // Dismiss the progress dialog if verification is completed
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        Toast.makeText(UserInfo.this, "OTP Verification Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                        // Dismiss the progress dialog if verification failed
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    }

                    @Override
                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                        // Save verificationId for later OTP verification
                        UserInfo.this.verificationId = verificationId;
                        Toast.makeText(UserInfo.this, "OTP Sent to " + phoneNumber, Toast.LENGTH_SHORT).show();

                        // Start the cooldown timer
                        startOtpCooldown();

                        // Dismiss the progress dialog once OTP is sent
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    }
                })
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
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

            // Get the usertype from Firestore
            firestore.collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Get the usertype to determine which fields to update
                            String userType = documentSnapshot.getString("usertype");

                            // Prepare a map to store updated user data
                            Map<String, Object> userData = new HashMap<>();

                            // Check the usertype and decide which fields to update
                            if ("manager".equalsIgnoreCase(userType)) {
                                // For managers, only update shopName and contact
                                if (!binding.shopname.getText().toString().isEmpty()) {
                                    userData.put("shopName", binding.shopname.getText().toString());
                                }
                            } else if ("customer".equalsIgnoreCase(userType)) {
                                // For customers, only update username and contact
                                if (!binding.username.getText().toString().isEmpty()) {
                                    userData.put("username", binding.username.getText().toString());
                                }
                            }

                            // Update the contact field if it's not empty
                            String contact = binding.contact.getText().toString();
                            if (!contact.isEmpty()) {
                                userData.put("contact", contact);
                            }

                            // Only update if there's something to update
                            if (!userData.isEmpty()) {
                                firestore.collection("users")
                                        .document(uid) // Update the existing user document
                                        .set(userData, SetOptions.merge()) // Use merge to avoid overwriting other fields
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, "Data updated successfully", Toast.LENGTH_SHORT).show();

                                            // Redirect based on usertype
                                            Intent intent;
                                            if ("manager".equalsIgnoreCase(userType)) {
                                                intent = new Intent(this, AdminPage.class); // Redirect to AdminPage for manager
                                            } else {
                                                intent = new Intent(this, UserPage.class); // Redirect to UserPage for customer
                                            }
                                            startActivity(intent);
                                            finish(); // Close the activity
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(this, "Error updating user: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                            } else {
                                Toast.makeText(this, "No data to update.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "User not found.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error fetching user data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
