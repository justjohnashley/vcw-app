package com.pural_ba3a.vulcanwash;

import android.content.ActivityNotFoundException;
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
import com.pural_ba3a.vulcanwash.databinding.FragThreeadminBinding;


public class FragThreeAdmin extends Fragment {

    NetworkMonitor networkMonitor;

    FragThreeadminBinding binding;
    FirebaseFirestore firestore;
    FirebaseAuth mAuth;
    FirebaseUser user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragThreeadminBinding.inflate(getLayoutInflater(), container, false);

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

            getActivity().finish();  // Finish the current activity
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
                    Intent intent = new Intent(getContext(), ManagerPage.class);
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

            getActivity().finish();  // Finish the current activity
        });


        binding.emailBtn.setOnClickListener(view -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:vcwash.app@gmail.com?subject=Client Inquiry"));

            try {
                startActivity(emailIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getContext(), "No email app found", Toast.LENGTH_SHORT).show();
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