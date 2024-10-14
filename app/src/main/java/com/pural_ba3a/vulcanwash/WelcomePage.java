package com.pural_ba3a.vulcanwash;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.pural_ba3a.vulcanwash.databinding.WelcomePageBinding;

public class WelcomePage extends AppCompatActivity {

    WelcomePageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = WelcomePageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.getstarted.setOnClickListener(view -> {

            Intent intent = new Intent(WelcomePage.this, HomePage.class);
            WelcomePage.this.startActivity(intent);

            finish();
        });

    }
}