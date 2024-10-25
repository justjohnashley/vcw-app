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
import com.google.firebase.firestore.FirebaseFirestore;
import com.pural_ba3a.vulcanwash.databinding.WelcomePageBinding;

public class WelcomePage extends AppCompatActivity {

    WelcomePageBinding binding;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    @Override
    public void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            FirebaseFirestore.getInstance().collection("users").document(uid).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            String usertype = task.getResult().getString("usertype");
                            if ("manager".equals(usertype)) {
                                Intent intent = new Intent(WelcomePage.this, AdminPage.class);
                                startActivity(intent);
                                finish();

                            } else if ("customer".equals(usertype)) {
                                Intent intent = new Intent(WelcomePage.this, UserPage.class);
                                startActivity(intent);
                                finish();

                            }
                            finish();
                        }
                    });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        binding = WelcomePageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.getstarted.setOnClickListener(view -> {

            Intent intent = new Intent(WelcomePage.this, HomePage.class);
            WelcomePage.this.startActivity(intent);

            finish();
        });

    }

}