package com.pural_ba3a.vulcanwash;

import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pural_ba3a.vulcanwash.databinding.FragOneBinding;

import java.util.ArrayList;
import java.util.List;


public class FragOne extends Fragment {

    FragOneBinding binding;
    FirebaseFirestore firestore;
    ShopAdapter shopAdapter;
    List<Shop> shopList;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragOneBinding.inflate(inflater, container, false);
        firestore = FirebaseFirestore.getInstance();
        shopList = new ArrayList<>();
        shopAdapter = new ShopAdapter(shopList, getContext());

        binding.recyclerView.setAdapter(shopAdapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        Uri videoUri = Uri.parse("android.resource://" + requireActivity().getPackageName() + "/" + R.raw.vcw_loading);
        binding.loadingVideoView.setVideoURI(videoUri);
        binding.loadingVideoView.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            binding.loadingVideoView.start();
        });
        binding.pgbarOverlay.setAlpha(1f);
        loadShops();

        return binding.getRoot();
    }

    private void loadShops() {

        new Handler().postDelayed(() -> {
        firestore.collection("users")
                .whereEqualTo("usertype", "manager")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    shopList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String uid = document.getId();

                        // Retrieve ShopInfo document
                        firestore.collection("users").document(uid).collection(uid)
                                .document("ShopInfo")
                                .get()
                                .addOnSuccessListener(storeInfoDoc -> {
                                    if (storeInfoDoc.exists()) {
                                        String shopName = storeInfoDoc.getString("shopName");
                                        if (shopName != null) {
                                            shopList.add(new Shop(uid, shopName));
                                            shopAdapter.notifyDataSetChanged();
                                        }
                                    }
                                });
                    }

                    binding.pgbarOverlay.setVisibility(View.GONE); // Hide loading overlay
                })
                .addOnFailureListener(e -> {
                    binding.pgbarOverlay.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Failed to load shops: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        }, 1000);
    }

}

