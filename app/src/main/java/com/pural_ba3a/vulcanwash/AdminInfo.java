package com.pural_ba3a.vulcanwash;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.pural_ba3a.vulcanwash.databinding.AdminInfopageBinding;

import java.util.HashMap;
import java.util.Map;

public class AdminInfo extends AppCompatActivity {

    AdminInfopageBinding binding;
    FirebaseUser currentUser;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = AdminInfopageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Video setup
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.vcw_loading);
        binding.loadingVideoView.setVideoURI(videoUri);
        binding.loadingVideoView.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            binding.loadingVideoView.start();
        });

        // Terms & Conditions checkbox logic
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

        // Attach TextWatchers for live validation
        setupTextWatcher(binding.shopname, binding.shopnameLayout, "Shop name is required");
        setupTextWatcher(binding.street, binding.streetLayout, "Street is required");
        setupTextWatcher(binding.brgy, binding.brgyLayout, "Barangay is required");
        setupTextWatcher(binding.city, binding.cityLayout, "City is required");
        setupTextWatcher(binding.prov, binding.provLayout, "Province is required");
        setupPhoneNumberValidation(); // Attach phone number validation

        // Submit button logic
        binding.submitBtn.setOnClickListener(view -> {
            if (!validateAllFields()) return;

            currentUser = mAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String uid = currentUser.getUid();
                Map<String, Object> data = new HashMap<>();
                data.put("shopName", binding.shopname.getText().toString());
                data.put("street", binding.street.getText().toString());
                data.put("barangay", binding.brgy.getText().toString());
                data.put("city", binding.city.getText().toString());
                data.put("province", binding.prov.getText().toString());
                data.put("contact", formatPhoneNumber(binding.contact.getText().toString()));

                firestore = FirebaseFirestore.getInstance();
                firestore.collection("users")
                        .document(uid)
                        .collection(uid)
                        .document("ShopInfo")
                        .set(data)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(this, "Data submitted successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(this, PendingPage.class);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to submit data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

    // Method to create and set a TextWatcher for live validation
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

    // Phone number validation setup
    private void setupPhoneNumberValidation() {
        binding.contact.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String phoneNumber = s.toString().trim();
                if (!isValidPhoneNumber(phoneNumber)) {
                    binding.contactLayout.setError("Invalid phone number. Use format +63xxxxxxxxxx");
                } else {
                    binding.contactLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // Validate all fields
    private boolean validateAllFields() {
        boolean isValid = true;

        if (binding.shopname.getText().toString().trim().isEmpty()) {
            binding.shopnameLayout.setError("Shop name is required");
            isValid = false;
        }
        if (binding.street.getText().toString().trim().isEmpty()) {
            binding.streetLayout.setError("Street is required");
            isValid = false;
        }
        if (binding.brgy.getText().toString().trim().isEmpty()) {
            binding.brgyLayout.setError("Barangay is required");
            isValid = false;
        }
        if (binding.city.getText().toString().trim().isEmpty()) {
            binding.cityLayout.setError("City is required");
            isValid = false;
        }
        if (binding.prov.getText().toString().trim().isEmpty()) {
            binding.provLayout.setError("Province is required");
            isValid = false;
        }
        if (!isValidPhoneNumber(binding.contact.getText().toString().trim())) {
            binding.contactLayout.setError("Invalid phone number");
            isValid = false;
        }

        return isValid;
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
                    Toast.makeText(this, "You have accepted the Terms and Conditions.", Toast.LENGTH_SHORT).show();
                    // Allow the checkbox to be ticked
                    checkBox.setChecked(true);
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Toast.makeText(this, "You have declined the Terms and Conditions.", Toast.LENGTH_SHORT).show();
                    // Prevent the checkbox from being ticked
                    checkBox.setChecked(false);
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }

    // Validate phone number with libphonenumber
    private boolean isValidPhoneNumber(String phoneNumber) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber parsedNumber = phoneUtil.parse(phoneNumber, "PH");
            return phoneUtil.isValidNumber(parsedNumber);
        } catch (NumberParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Format phone number to international format
    private String formatPhoneNumber(String phoneNumber) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber parsedNumber = phoneUtil.parse(phoneNumber, "PH");
            return phoneUtil.format(parsedNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
        } catch (NumberParseException e) {
            e.printStackTrace();
            return phoneNumber;
        }
    }
}
