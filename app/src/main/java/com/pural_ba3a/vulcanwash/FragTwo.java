package com.pural_ba3a.vulcanwash;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pural_ba3a.vulcanwash.databinding.FragTwoBinding;

import java.util.ArrayList;
import java.util.List;

public class FragTwo extends Fragment {
    private FragTwoBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore firestore;

    private OrderAdapter activeAdapter, historyAdapter;
    private List<Order> activeOrders = new ArrayList<>();
    private List<Order> historyOrders = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragTwoBinding.inflate(getLayoutInflater(), container, false);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        setupRecyclerViews();
        fetchOrders();

        return binding.getRoot();
    }

    private void setupRecyclerViews() {
        // Initialize adapters for active and history orders
        activeAdapter = new OrderAdapter(activeOrders);
        historyAdapter = new OrderAdapter(historyOrders);

        // Set up RecyclerView for active orders
        binding.activerecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.activerecycler.setAdapter(activeAdapter);

        // Set up RecyclerView for order history
        binding.historyrecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.historyrecycler.setAdapter(historyAdapter);
    }

    private void fetchOrders() {
        if (user == null) {
            Toast.makeText(getContext(), "User not authenticated. Please log in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();

        firestore.collection("orders")
                .whereEqualTo("userId", userId) // Retrieve only orders for the logged-in user
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    activeOrders.clear();
                    historyOrders.clear();

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Order order = document.toObject(Order.class);

                        if (order != null) {
                            String status = order.getStatus();

                            // Prioritize "finished" orders
                            if ("finished".equalsIgnoreCase(status)) {
                                historyOrders.add(order);
                            }
                            // Send rejected orders to history
                            else if (order.isRejected()) {
                                historyOrders.add(order);
                            }
                            // Pending and accepted orders go to active
                            else if ("pending".equalsIgnoreCase(status) || order.isAccepted()) {
                                activeOrders.add(order);
                            }
                        }
                    }

                    // Notify the adapters about data changes
                    activeAdapter.notifyDataSetChanged();
                    historyAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load orders", Toast.LENGTH_SHORT).show();
                });
    }

}
