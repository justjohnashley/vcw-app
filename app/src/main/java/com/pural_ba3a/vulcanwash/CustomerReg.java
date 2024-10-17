package com.pural_ba3a.vulcanwash;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pural_ba3a.vulcanwash.databinding.CustomerRegBinding;

public class CustomerReg extends AppCompatActivity {

    CustomerRegBinding binding;
    FirebaseAuth mAuth;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), UserPage.class);
            startActivity(intent);

            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        mAuth = FirebaseAuth.getInstance();

        binding = CustomerRegBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.backBtn.setOnClickListener(view -> {

            Intent intent = new Intent(CustomerReg.this, HomePage.class);
            CustomerReg.this.startActivity(intent);

            finish();

        });

        binding.clogin.setOnClickListener(view -> {

            Intent intent = new Intent(CustomerReg.this, CustomerPage.class);
            CustomerReg.this.startActivity(intent);

            finish();


        });

        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.vcw_loading);
        binding.loadingVideoView.setVideoURI(videoUri);
        binding.loadingVideoView.setOnPreparedListener(mp -> {
            mp.setLooping(true);  // Loop the video
            binding.loadingVideoView.start();

        });

        binding.signupBtn.setOnClickListener(view -> {
            binding.pgbarOverlay.setVisibility(View.VISIBLE);
            binding.pgbarOverlay.setAlpha(1f);

            String email, password;

            email = String.valueOf(binding.email.getText());
            password = String.valueOf(binding.password.getText());

            if (TextUtils.isEmpty(email)){
                binding.pgbarOverlay.setVisibility(View.GONE);
                Toast.makeText(CustomerReg.this, "Email cannot be empty.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)){
                binding.pgbarOverlay.setVisibility(View.GONE);
                Toast.makeText(CustomerReg.this, "Password cannot be empty.", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            new Handler().postDelayed(() -> {
                                binding.pgbarOverlay.setVisibility(View.GONE);

                                if (task.isSuccessful()) {
                                    Toast.makeText(CustomerReg.this, "Account Successfully Created.",
                                            Toast.LENGTH_SHORT).show();



                                } else {
                                    binding.pgbarOverlay.setAlpha(0);
                                    binding.pgbarOverlay.setVisibility(View.VISIBLE);

                                    Toast.makeText(CustomerReg.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();

                                }
                            }, 1000);
                        }
                    });

        });

    }
}