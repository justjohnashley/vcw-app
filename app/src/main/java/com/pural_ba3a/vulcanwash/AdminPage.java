package com.pural_ba3a.vulcanwash;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pural_ba3a.vulcanwash.databinding.AdminHomepageBinding;

public class AdminPage extends AppCompatActivity {

    AdminHomepageBinding binding;
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore firestore;
    DocumentReference shopInfoDocRef, userDocRef;
    DocumentSnapshot userSnapshot, shopInfoSnapshot;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            firestore = FirebaseFirestore.getInstance();

            // Retrieve usertype and check ShopInfo in a transaction
            userDocRef = firestore.collection("users").document(uid);
            shopInfoDocRef = firestore.collection("users").document(uid)
                    .collection(uid).document("ShopInfo");

            firestore.runTransaction(transaction -> {
                userSnapshot = transaction.get(userDocRef);

                // Check if user is a manager
                if (userSnapshot.exists()) {
                    String userType = userSnapshot.getString("usertype");
                    String status = userSnapshot.getString("status");

                    if ("manager".equals(userType)) {
                        // If manager, check if status is pending
                        if ("pending".equals(status)) {
                            shopInfoSnapshot = transaction.get(shopInfoDocRef);
                            if (shopInfoSnapshot.exists()) {
                                // Redirect to pendingPage if status is pending and ShopInfo exists
                                return "pendingPage";
                            } else {
                                // Redirect to AdminInfo if ShopInfo does not exist
                                return "adminInfoPage";
                            }
                        }
                    }
                }
                return null;  // No redirection needed
            }).addOnSuccessListener(pageToRedirect -> {
                if ("pendingPage".equals(pageToRedirect)) {
                    Intent intent = new Intent(AdminPage.this, PendingPage.class);
                    startActivity(intent);
                    finish();
                } else if ("adminInfoPage".equals(pageToRedirect)) {
                    Intent intent = new Intent(AdminPage.this, AdminInfo.class);
                    startActivity(intent);
                    finish();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(AdminPage.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = AdminHomepageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.vcw_loading);
        binding.loadingVideoView.setVideoURI(videoUri);
        binding.loadingVideoView.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            binding.loadingVideoView.start();
        });

        // Show initial fragment
        showFragment(new FragOneAdmin(), "FRAG_ONE");

        // Set up navigation bar listener for fragment switching
        binding.btnvbar.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.fragOneAdmin) {
                showFragment(new FragOneAdmin(), "FRAG_ONE");
            } else if (item.getItemId() == R.id.fragTwoAdmin) {
                showFragment(new FragTwoAdmin(), "FRAG_TWO");
            } else if (item.getItemId() == R.id.fragThreeAdmin) {
                showFragment(new FragThreeAdmin(), "FRAG_THREE");
            }
            return true;
        });
    }

    // Optimized method to show fragments without re-creating them
    private void showFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // Hide all other fragments
        for (Fragment existingFragment : fragmentManager.getFragments()) {
            transaction.hide(existingFragment);
        }

        Fragment existingFragment = fragmentManager.findFragmentByTag(tag);

        if (existingFragment == null) {
            // Add the fragment if it doesn't already exist in the back stack
            transaction.add(R.id.fragLayout, fragment, tag);
        } else {
            // Show the fragment if it already exists
            transaction.show(existingFragment);
        }

        transaction.commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
