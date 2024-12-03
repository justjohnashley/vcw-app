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
import com.google.firebase.firestore.FirebaseFirestore;
import com.pural_ba3a.vulcanwash.databinding.UserHomepageBinding;

public class UserPage extends AppCompatActivity {

    private UserHomepageBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = UserHomepageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.vcw_loading);
        binding.loadingVideoView.setVideoURI(videoUri);
        binding.loadingVideoView.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            binding.loadingVideoView.start();
        });

        // Show initial fragment
        showFragment(new FragOne(), "FRAG_ONE");

        // Set up navigation bar listener for fragment switching
        binding.btnvbar.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.fragOne) {
                showFragment(new FragOne(), "FRAG_ONE");
            } else if (item.getItemId() == R.id.fragTwo) {
                showFragment(new FragTwo(), "FRAG_TWO");
            } else if (item.getItemId() == R.id.fragThree) {
                showFragment(new FragThree(), "FRAG_THREE");
            }
            return true;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (user != null) {
            String uid = user.getUid();
            DocumentReference userDocRef = firestore.collection("users").document(uid);

            userDocRef.get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    String username = snapshot.getString("username");
                    String contact = snapshot.getString("contact");

                    if (username == null || contact == null) {
                        // Redirect to the UserInfo page if either field is missing
                        Toast.makeText(this, "Please complete your profile information.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, UserInfo.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }).addOnFailureListener(e ->
                    Toast.makeText(this, "Error checking fields: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        }
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
}
