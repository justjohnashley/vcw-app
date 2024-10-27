package com.pural_ba3a.vulcanwash;

import android.net.Uri;
import android.os.Bundle;

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
import com.pural_ba3a.vulcanwash.databinding.AdminHomepageBinding;

public class AdminPage extends AppCompatActivity {

    AdminHomepageBinding binding;
    FirebaseAuth mAuth;
    FirebaseUser user;

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

        replaceFragment(new FragOneAdmin());

        binding.btnvbar.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.fragOne) {
                replaceFragment(new FragOneAdmin());
            }
            else if (item.getItemId() == R.id.fragTwo) {
                replaceFragment(new FragTwoAdmin());
            }
            else if (item.getItemId() == R.id.fragThree) {
                replaceFragment(new FragThreeAdmin());
            }
            return true;
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragManager = getSupportFragmentManager();
        FragmentTransaction fragTrans = fragManager.beginTransaction();

        // Find the existing fragment in the container (if any)
        Fragment currentFragment = fragManager.findFragmentById(R.id.fragLayout);

        if (currentFragment != null) {
            // Option 1: Remove the current fragment
            // fragTrans.remove(currentFragment);

            // Option 2: Detach the current fragment
            fragTrans.detach(currentFragment);
        }

        // Replace the fragment in the container
        fragTrans.replace(R.id.fragLayout, fragment);

        // Commit the transaction
        fragTrans.commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}