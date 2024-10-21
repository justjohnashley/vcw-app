package com.pural_ba3a.vulcanwash;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pural_ba3a.vulcanwash.databinding.FragThreeBinding;
import com.pural_ba3a.vulcanwash.databinding.FragTwoBinding;


public class FragTwo extends Fragment {
    FragTwoBinding binding;
    FirebaseAuth mAuth;
    FirebaseUser user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragTwoBinding.inflate(getLayoutInflater(), container, false);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        return binding.getRoot();
    }
}