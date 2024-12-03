package com.pural_ba3a.vulcanwash;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pural_ba3a.vulcanwash.databinding.FragThreeBinding;

public class FragThree extends Fragment {

    NetworkMonitor networkMonitor;

    FragThreeBinding binding;
    FirebaseFirestore firestore;
    FirebaseAuth mAuth;
    FirebaseUser user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragThreeBinding.inflate(getLayoutInflater(), container, false);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        networkMonitor = new NetworkMonitor(getContext());

        if (user != null) {
            binding.userid.setText(user.getEmail());

        } else {
            Intent intent = new Intent(requireContext(), CustomerPage.class);
            startActivity(intent);
            getActivity().finish();

        }

        Uri videoUri = Uri.parse("android.resource://" + requireActivity().getPackageName() + "/" + R.raw.vcw_loading);
        binding.loadingVideoView.setVideoURI(videoUri);
        binding.loadingVideoView.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            binding.loadingVideoView.start();

        });

        binding.chpwBtn.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), ChangePw.class);
            startActivity(intent);

        });


        binding.logoutBtn.setOnClickListener(view -> {
            if (networkMonitor.isNetworkAvailable()) {
                // Network is available, proceed with sign-out
                binding.pgbarOverlay.setAlpha(1f);
                binding.pgbarOverlay.setClickable(true);
                binding.pgbarOverlay.setFocusable(true);

                new Handler().postDelayed(() -> {
                    binding.pgbarOverlay.setVisibility(View.GONE);
                    binding.pgbarOverlay.setClickable(false);
                    binding.pgbarOverlay.setFocusable(false);
                    mAuth.signOut();  // Sign out the user

                    Toast.makeText(getContext(), "Logged out Successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getContext(), CustomerPage.class);
                    startActivity(intent);
                    getActivity().finish();  // Finish the current activity
                }, 2000);
            } else {
                binding.pgbarOverlay.setVisibility(View.GONE);
                Toast.makeText(getContext(), "No internet connection. Please connect to the internet to log out.", Toast.LENGTH_LONG).show();
            }
        });

        binding.editinfoBtn.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), UserInfo.class); // Navigate to UserInfo activity
            startActivity(intent);
        });


        binding.delaccBtn.setOnClickListener(view -> {
            if (networkMonitor.isNetworkAvailable()) {
                binding.pgbarOverlay.setAlpha(1f);
                binding.pgbarOverlay.setClickable(true);
                binding.pgbarOverlay.setFocusable(true);

                new Handler().postDelayed(() -> {
                    if (user != null) {
                        // Step 1: Remove user's Firestore entry
                        String uid = user.getUid();
                        firestore = FirebaseFirestore.getInstance();
                        firestore.collection("users").document(uid)
                                .delete()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        // Step 2: Delete user's Firebase Authentication account
                                        user.delete()
                                                .addOnCompleteListener(task2 -> {
                                                    binding.pgbarOverlay.setVisibility(View.GONE);
                                                    binding.pgbarOverlay.setClickable(false);
                                                    binding.pgbarOverlay.setFocusable(false);

                                                    if (task2.isSuccessful()) {
                                                        Toast.makeText(getContext(), "Account deleted successfully.", Toast.LENGTH_SHORT).show();
                                                        // Step 3: Redirect to CustomerPage
                                                        Intent intent = new Intent(getContext(), CustomerPage.class);
                                                        startActivity(intent);
                                                        getActivity().finish();
                                                    } else {
                                                        Toast.makeText(getContext(), "Failed to delete account: " + task2.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                    } else {
                                        binding.pgbarOverlay.setVisibility(View.GONE);
                                        Toast.makeText(getContext(), "Failed to delete Firestore entry: " + task1.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        binding.pgbarOverlay.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "No user is currently logged in.", Toast.LENGTH_SHORT).show();
                    }
                }, 2000);
            } else {
                Toast.makeText(getContext(), "No internet connection. Please connect to the internet to delete your account.", Toast.LENGTH_LONG).show();
            }
        });


        return binding.getRoot();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister the network callback when the fragment or activity is destroyed
        binding = null;
        networkMonitor.unregisterCallback();
    }
}