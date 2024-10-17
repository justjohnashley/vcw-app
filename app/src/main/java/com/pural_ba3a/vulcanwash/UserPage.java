package com.pural_ba3a.vulcanwash;

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

        if (user != null) {
            binding.userid.setText(user.getEmail());

        }
        else {
            Intent intent = new Intent(getApplicationContext(), CustomerPage.class);
            startActivity(intent);
            finish();

        }

        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.vcw_loading);
        binding.loadingVideoView.setVideoURI(videoUri);
        binding.loadingVideoView.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            binding.loadingVideoView.start();

        });



        binding.logoutBtn.setOnClickListener(view -> {
            binding.pgbarOverlay.setAlpha(1f);

            new Handler().postDelayed(() -> {
                binding.pgbarOverlay.setVisibility(view.GONE);
                mAuth.signOut();

                Toast.makeText(UserPage.this, "Logged out Successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), CustomerPage.class);
                startActivity(intent);
                finish();
            }, 2000);
        });
    }

    }

