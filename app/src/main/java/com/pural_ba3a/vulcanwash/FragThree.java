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
import com.pural_ba3a.vulcanwash.databinding.FragThreeBinding;

public class FragThree extends Fragment {



    FragThreeBinding binding;
    FirebaseAuth mAuth;
    FirebaseUser user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragThreeBinding.inflate(getLayoutInflater(), container, false);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

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


        binding.logoutBtn.setOnClickListener(view -> {
            binding.pgbarOverlay.setAlpha(1f);

            new Handler().postDelayed(() -> {
                binding.pgbarOverlay.setVisibility(view.GONE);
                mAuth.signOut();

                Toast.makeText(requireContext(), "Logged out Successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(requireActivity(), CustomerPage.class);
                startActivity(intent);
                getActivity().finish();
            }, 2000);
        });

        return binding.getRoot();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}