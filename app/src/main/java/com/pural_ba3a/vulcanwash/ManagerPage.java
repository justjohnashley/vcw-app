package com.pural_ba3a.vulcanwash;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.pural_ba3a.vulcanwash.databinding.ManagerPgBinding;

import java.util.concurrent.TimeUnit;

public class ManagerPage extends AppCompatActivity {

    ManagerPgBinding binding;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    FirebaseUser user;
    DocumentReference docRef;
    NetworkMonitor networkMonitor;
    private Dialog otpDialog;
    private String verificationId;


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
        networkMonitor = new NetworkMonitor(getApplicationContext());

        binding = ManagerPgBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.backBtn.setOnClickListener(view -> {

            Intent intent = new Intent(ManagerPage.this, HomePage.class);
            ManagerPage.this.startActivity(intent);

            finish();

        });

        binding.signup.setOnClickListener(view -> {

            Intent intent = new Intent(ManagerPage.this, ManagerReg.class);
            ManagerPage.this.startActivity(intent);

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


        binding.loginBtn.setOnClickListener(view -> {
            binding.pgbarOverlay.setVisibility(View.VISIBLE);
            binding.pgbarOverlay.setAlpha(1f);
            binding.pgbarOverlay.setClickable(true);
            binding.pgbarOverlay.setFocusable(true);

            if (networkMonitor.isNetworkAvailable()) {
                if (validateEmail() && validatePassword()) {
                    String email = binding.email.getText().toString().trim();
                    String password = binding.password.getText().toString().trim();

                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    new Handler().postDelayed(() -> {
                                        binding.pgbarOverlay.setVisibility(View.GONE);
                                        binding.pgbarOverlay.setClickable(false);
                                        binding.pgbarOverlay.setFocusable(false);

                                        if (task.isSuccessful()) {
                                            user = mAuth.getCurrentUser();

                                            if (user != null) {
                                                String uid = user.getUid();
                                                firestore = FirebaseFirestore.getInstance();
                                                docRef = firestore.collection("users").document(uid);
                                                docRef.get().addOnCompleteListener(task1 -> {
                                                    if (task1.isSuccessful() && task1.getResult() != null) {
                                                        String userType = task1.getResult().getString("usertype");

                                                        // Check if usertype is "customer"
                                                        if ("manager".equals(userType)) {
                                                            // Proceed to user page
                                                            Toast.makeText(ManagerPage.this, "Logged In Successfully.", Toast.LENGTH_SHORT).show();
                                                            Intent intent = new Intent(ManagerPage.this, AdminPage.class);
                                                            ManagerPage.this.startActivity(intent);
                                                            finish();
                                                        } else {
                                                            // Incorrect usertype; prevent login
                                                            Toast.makeText(ManagerPage.this, "Access denied.", Toast.LENGTH_LONG).show();
                                                            mAuth.signOut();
                                                        }
                                                    } else {
                                                        // Error retrieving document or usertype field missing
                                                        Toast.makeText(ManagerPage.this, "Error checking user type. Please try again.", Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                            }
                                        } else {
                                            binding.pgbarOverlay.setAlpha(0);
                                            binding.pgbarOverlay.setVisibility(View.VISIBLE);
                                            Toast.makeText(ManagerPage.this, "Authentication failed. Double check your credentials.", Toast.LENGTH_LONG).show();
                                        }
                                    }, 1000);
                                }
                            });
                } else {
                    binding.pgbarOverlay.setVisibility(View.GONE);
                }
            } else {
                binding.pgbarOverlay.setVisibility(View.GONE);
                Toast.makeText(this, "No internet connection. Please connect to internet to proceed.", Toast.LENGTH_LONG).show();
            }

        });

        // Handle Forgot Password Click (Prompt the user to enter their phone number)
        binding.forgotPw.setOnClickListener(view -> {
            // Show dialog to input phone number
            showPhoneNumberDialog();
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

    private void showPhoneNumberDialog() {
        // Create a new dialog to enter phone number
        View dialogView = getLayoutInflater().inflate(R.layout.contact_dialog, null);
        TextInputEditText phoneNumberInput = dialogView.findViewById(R.id.phone_number_input);
        Button submitButton = dialogView.findViewById(R.id.submit_button);
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);

        // Create the dialog
        AlertDialog phoneNumberDialog = new AlertDialog.Builder(this)
                .setTitle("Enter Phone Number")
                .setView(dialogView)
                .setCancelable(true)
                .create();

        submitButton.setOnClickListener(v -> {
            String phoneNumber = phoneNumberInput.getText().toString().trim();

            if (TextUtils.isEmpty(phoneNumber) || !isValidPhoneNumber(phoneNumber)) {
                Toast.makeText(ManagerPage.this, "Enter a valid phone number. +63 123 456 789", Toast.LENGTH_SHORT).show();
                return;
            }

            // After phone number validation, send OTP
            phoneNumberDialog.dismiss();
            showOtpDialog(phoneNumber);
        });

        // Show the phone number dialog
        phoneNumberDialog.show();

        cancelButton.setOnClickListener(v -> phoneNumberDialog.dismiss());
    }


    private void showOtpDialog(String phoneNumber) {
        View dialogView = getLayoutInflater().inflate(R.layout.otp_dialog, null);
        TextInputEditText otpInput = dialogView.findViewById(R.id.otp_input);
        Button sendOtpBtn = dialogView.findViewById(R.id.send_otp_btn);
        Button verifyOtpBtn = dialogView.findViewById(R.id.verify_otp_btn);
        Button closeBtn = dialogView.findViewById(R.id.closeBtn);

        otpDialog = new MaterialAlertDialogBuilder(this)
                .setTitle("OTP Verification")
                .setView(dialogView)
                .setCancelable(true)
                .create();

        otpDialog.show();

        // Update dialog button state based on cooldown
        updateOtpDialogState();

        sendOtpBtn.setOnClickListener(v -> sendOtp(phoneNumber));

        verifyOtpBtn.setOnClickListener(v -> {
            String otp = otpInput.getText().toString().trim();
            if (!otp.isEmpty()) {
                verifyOtp(otp, phoneNumber); // Pass phone number here
            } else {
                Toast.makeText(this, "Please enter OTP.", Toast.LENGTH_SHORT).show();
            }
        });

        closeBtn.setOnClickListener(v -> otpDialog.dismiss());
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
                        Toast.makeText(ManagerPage.this, "OTP Verification Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                        // Dismiss the progress dialog if verification failed
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    }

                    @Override
                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                        // Save verificationId for later OTP verification
                        ManagerPage.this.verificationId = verificationId;
                        Toast.makeText(ManagerPage.this, "OTP Sent to " + phoneNumber, Toast.LENGTH_SHORT).show();

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

    // Verify the OTP (do not sign the user in)
    private void verifyOtp(String otp, String phoneNumber) {
        if (verificationId == null) {
            Toast.makeText(this, "Request OTP first.", Toast.LENGTH_SHORT).show();
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);

        // Check if the OTP is correct (do not sign in the user)
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,  // The phone number to be verified
                60,           // Timeout duration
                TimeUnit.SECONDS,  // Timeout unit
                this,            // Context
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        // This would be called if the OTP verification is automatic
                        // No need to sign in, just proceed with your flow
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        Toast.makeText(ManagerPage.this, "OTP verification failed.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                        super.onCodeSent(verificationId, token);
                        ManagerPage.this.verificationId = verificationId;  // Save verificationId for later verification
                    }
                });

        // If OTP matches, you can proceed to reset the password
        // Here, do not sign in, just reset password
        searchUserByPhoneAndSendResetEmail(phoneNumber);
    }

    private void searchUserByPhoneAndSendResetEmail(String phoneNumber) {
        firestore = FirebaseFirestore.getInstance();
        firestore.collection("users")
                .whereEqualTo("contact", phoneNumber)  // Searching by contact field
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // User found with matching phone number
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);  // Assuming only one match
                        String email = document.getString("email");  // Fetch the email field
                        String uid = document.getId();  // Fetch the uid field (document ID)

                        if (email != null) {
                            // Once email is retrieved, send the password reset email
                            sendPasswordResetEmail(email, otpDialog);
                        } else {
                            Toast.makeText(ManagerPage.this, "Email not found for this user.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ManagerPage.this, "No user found with this phone number.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendPasswordResetEmail(String email, Dialog otpDialog) {
        // Show a progress dialog while waiting for the task to complete
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending password reset email...");
        progressDialog.setCancelable(false); // Prevent canceling the dialog by touching outside
        progressDialog.show();

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    // Dismiss the progress dialog
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }

                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Password reset email sent.", Toast.LENGTH_SHORT).show();

                        // Automatically close the OTP dialog
                        if (otpDialog != null && otpDialog.isShowing()) {
                            otpDialog.dismiss();  // Close the dialog
                        }
                    } else {
                        Toast.makeText(this, "Failed to send password reset email.", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister the network callback when the fragment or activity is destroyed
        binding = null;
        networkMonitor.unregisterCallback();
    }
}