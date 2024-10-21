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




        replaceFragment(new FragOne());

        binding.btnvbar.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.fragOne) {
                replaceFragment(new FragOne());
            }
            else if (item.getItemId() == R.id.fragTwo) {
                replaceFragment(new FragTwo());
            }
            else if (item.getItemId() == R.id.fragThree) {
                replaceFragment(new FragThree());
            }
            return true;
        });
    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragManager = getSupportFragmentManager();
        FragmentTransaction fragTrans = fragManager.beginTransaction();
        fragTrans.replace(R.id.fragLayout, fragment);
        fragTrans.commit();
    }
}

