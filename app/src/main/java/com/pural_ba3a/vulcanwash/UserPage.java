package com.pural_ba3a.vulcanwash;

import static androidx.navigation.ui.NavigationUI.setupActionBarWithNavController;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pural_ba3a.vulcanwash.databinding.UserHomepageBinding;

public class UserPage extends AppCompatActivity {

    UserHomepageBinding binding;
    FirebaseAuth mAuth;
    FirebaseUser user;

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

