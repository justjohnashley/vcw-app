package com.pural_ba3a.vulcanwash;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.pural_ba3a.vulcanwash.databinding.HomePageBinding;

public class HomePage extends AppCompatActivity {

    HomePageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = HomePageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.mBtn.setOnClickListener(view -> {

            Intent intent = new Intent(HomePage.this, ManagerPage.class);
            HomePage.this.startActivity(intent);

            finish();
        });

        binding.cBtn.setOnClickListener(view -> {

            Intent intent = new Intent(HomePage.this, CustomerPage.class);
            HomePage.this.startActivity(intent);

            finish();
        });
    }
}