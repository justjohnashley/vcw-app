package com.pural_ba3a.vulcanwash;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pural_ba3a.vulcanwash.databinding.PendingPageBinding;

public class PendingPage extends AppCompatActivity {

    PendingPageBinding binding;

    @Override
    protected void onStart() {
        super.onStart();
        checkStatus();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = PendingPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.refreshpg.setOnClickListener(view -> {
            refreshPage();
        });

    }
    private void refreshPage() {
        onStart();  // This calls onStart again to refresh the page logic
    }

    private void checkStatus() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            // Reference to the user's document in Firestore
            DocumentReference userDocRef = firestore.collection("users").document(uid);

            userDocRef.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String status = documentSnapshot.getString("status");
                            if ("approved".equals(status)) {
                                // Redirect if status is approved
                                Intent intent = new Intent(PendingPage.this, AdminPage.class);
                                startActivity(intent);
                                finish();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(PendingPage.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}