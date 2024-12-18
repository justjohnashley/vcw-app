package com.pural_ba3a.vulcanwash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pural_ba3a.vulcanwash.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    NetworkMonitor networkMonitor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        networkMonitor = new NetworkMonitor(this);


        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // Get the current user
        currentUser = mAuth.getCurrentUser();
        if (networkMonitor.isNetworkAvailable()) {
        if (currentUser != null) {
            // If user is logged in, check user type
            checkUserTypeAndNavigate();
        } else {
            // No user is logged in, navigate to WelcomePage after a delay
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(MainActivity.this, WelcomePage.class);
                startActivity(intent);
                finish();
            }, 1000);
        }
        } else {
            Snackbar netSnackbar = Snackbar.make(binding.getRoot(), "You do not have internet connection. Please connect to the internet to proceed.", Snackbar.LENGTH_INDEFINITE);
            netSnackbar.show();
        }
    }

    private void checkUserTypeAndNavigate() {
        String uid = currentUser.getUid();

        FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnCompleteListener(task -> {
                    new Handler().postDelayed(() -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        String usertype = task.getResult().getString("usertype");

                        Intent intent;
                        if ("manager".equals(usertype)) {
                            intent = new Intent(MainActivity.this, AdminPage.class);
                        } else {
                            intent = new Intent(MainActivity.this, UserPage.class);
                        }
                        startActivity(intent);
                        finish();
                    } else {
                        // Navigate to WelcomePage if there's an error retrieving user type
                        Intent intent = new Intent(MainActivity.this, WelcomePage.class);
                        startActivity(intent);
                        finish();
                    }
                    }, 1000);
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
        binding = null;
    }
}
