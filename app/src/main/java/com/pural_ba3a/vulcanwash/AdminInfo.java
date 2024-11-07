package com.pural_ba3a.vulcanwash;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pural_ba3a.vulcanwash.databinding.AdminInfopageBinding;

import java.util.HashMap;
import java.util.Map;

public class AdminInfo extends AppCompatActivity {

    AdminInfopageBinding binding;
    FirebaseUser currentUser;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    NetworkMonitor networkMonitor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = AdminInfopageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.tnc.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Enable the button only if the checkbox is checked
            binding.submitBtn.setEnabled(isChecked);
        });

        binding.tncBtn.setOnClickListener(view -> showTermsDialog());

        // Attach TextWatchers for live validation
        setupTextWatcher(binding.shopname, binding.shopnameLayout, "Shop name is required");
        setupTextWatcher(binding.street, binding.streetLayout, "Street is required");
        setupTextWatcher(binding.brgy, binding.brgyLayout, "Barangay is required");
        setupTextWatcher(binding.city, binding.cityLayout, "City is required");
        setupTextWatcher(binding.prov, binding.provLayout, "Province is required");

        binding.submitBtn.setOnClickListener(view -> {
            // Validate all fields before proceeding
            if (!validateAllFields()) return;

            currentUser = mAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String uid = currentUser.getUid();

                // Create a Map to hold your data
                Map<String, Object> data = new HashMap<>();
                data.put("shopName", binding.shopname.getText().toString());
                data.put("street", binding.street.getText().toString());
                data.put("barangay", binding.brgy.getText().toString());
                data.put("city", binding.city.getText().toString());
                data.put("province", binding.prov.getText().toString());

                // Get reference to Firestore
                firestore = FirebaseFirestore.getInstance();

                // Add to Firestore under the user's UID subcollection
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
                    layout.setBoxStrokeColor(ContextCompat.getColor(AdminInfo.this, R.color.gray));
                } else {
                    layout.setError(null);
                    layout.setBoxStrokeColor(ContextCompat.getColor(AdminInfo.this, R.color.green));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // Method to validate all fields at once
    private boolean validateAllFields() {
        boolean isValid = true;
        if (binding.shopname.getText().toString().trim().length() > 30) {
            binding.shopnameLayout.setError("Shop name can't exceed 30 characters");
            isValid = false;
        }
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
        return isValid;
    }

    private void showTermsDialog() {
        // Create a dialog
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dgbox_tnc);

        // Set up the terms text
        TextView termsTextView = dialog.findViewById(R.id.termsTextView);

        // Set up the close button
        Button closeButton = dialog.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> dialog.dismiss());

        // Show the dialog
        dialog.show();
    }
}