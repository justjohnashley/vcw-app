package com.pural_ba3a.vulcanwash;

import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pural_ba3a.vulcanwash.databinding.FragOneadminBinding;

import java.util.HashMap;
import java.util.Map;

public class FragOneAdmin extends Fragment {

    NetworkMonitor networkMonitor;
    FragOneadminBinding binding;
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore firestore;
    boolean initialSwitch1State, initialSwitch2State, initialSwitch3State, initialSwitch4State;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragOneadminBinding.inflate(getLayoutInflater(), container, false);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        networkMonitor = new NetworkMonitor(getContext());

        // Disable button initially
        binding.updateBtn.setEnabled(false);

        // Load switch states from Firestore
        loadShopName();
        loadSwitchStatesFromDatabase();

        // Load initial shop status to set the button appearance
        loadShopStatus();

        // Set listeners for switches
        CompoundButton.OnCheckedChangeListener switchChangeListener = (buttonView, isChecked) -> {
            boolean hasChanged = (binding.switch1.isChecked() != initialSwitch1State) ||
                    (binding.switch2.isChecked() != initialSwitch2State) ||
                    (binding.switch3.isChecked() != initialSwitch3State) ||
                    (binding.switch4.isChecked() != initialSwitch4State);

            binding.updateBtn.setEnabled(hasChanged);
        };

        // Attach the listener to each switch
        binding.switch1.setOnCheckedChangeListener(switchChangeListener);
        binding.switch2.setOnCheckedChangeListener(switchChangeListener);
        binding.switch3.setOnCheckedChangeListener(switchChangeListener);
        binding.switch4.setOnCheckedChangeListener(switchChangeListener);

        // Set up OnClickListener for shopBtn
        binding.shopBtn.setOnClickListener(view -> {
                    if (networkMonitor.isNetworkAvailable()) {
                        binding.pgbarOverlay.setVisibility(View.VISIBLE);
                        binding.pgbarOverlay.setAlpha(1f);
                        binding.pgbarOverlay.setClickable(true);
                        binding.pgbarOverlay.setFocusable(true);
                        new Handler().postDelayed(() -> {
                            binding.pgbarOverlay.setVisibility(View.GONE);
                            binding.pgbarOverlay.setClickable(false);
                            binding.pgbarOverlay.setFocusable(false);

                            toggleShopStatus();
                        }, 2000);
            } else {
                Snackbar.make(binding.getRoot(), "No internet connection. Please connect to the internet to proceed with this request.", Snackbar.LENGTH_INDEFINITE).show();

            }

        });

        // Update Firestore on button click
        binding.updateBtn.setOnClickListener(view -> {
            if (networkMonitor.isNetworkAvailable()) {
                binding.pgbarOverlay.setVisibility(View.VISIBLE);
                binding.pgbarOverlay.setAlpha(1f);
                binding.pgbarOverlay.setClickable(true);
                binding.pgbarOverlay.setFocusable(true);
                new Handler().postDelayed(() -> {
                    binding.pgbarOverlay.setVisibility(View.GONE);
                    binding.pgbarOverlay.setClickable(false);
                    binding.pgbarOverlay.setFocusable(false);

                    updateServicesInDatabase();
                    loadSwitchStatesFromDatabase();
                }, 2000);
            } else {
                binding.pgbarOverlay.setAlpha(0);
                Snackbar.make(binding.getRoot(), "No internet connection. Please connect to the internet to proceed with this request.", Snackbar.LENGTH_INDEFINITE).show();

            }
        });

        Uri videoUri = Uri.parse("android.resource://" + requireActivity().getPackageName() + "/" + R.raw.vcw_loading);
        binding.loadingVideoView.setVideoURI(videoUri);
        binding.loadingVideoView.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            binding.loadingVideoView.start();
        });

        return binding.getRoot();
    }

    // Method to load the initial shop status and set button appearance
    private void loadShopStatus() {
        if (user != null) {
            String uid = user.getUid();

            firestore.collection("users").document(uid)
                    .collection(uid)
                    .document("ShopStatus")  // Assuming "status" is the document name
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Boolean isOpen = task.getResult().getBoolean("open");
                            updateShopButtonAppearance(isOpen != null && isOpen); // Update button based on "open" field
                        } else {
                            Snackbar.make(binding.getRoot(), "Failed to load shop status", Snackbar.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    // Method to toggle shop status in Firestore
    private void toggleShopStatus() {
        if (user != null) {
            String uid = user.getUid();

            firestore.collection("users").document(uid)
                    .collection(uid)
                    .document("ShopStatus")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Boolean isOpen = task.getResult().getBoolean("open");

                            // Toggle `open` field and set `closed` field accordingly
                            boolean newOpenStatus = isOpen == null || !isOpen;

                            // Update Firestore with new values
                            Map<String, Object> statusUpdate = new HashMap<>();
                            statusUpdate.put("open", newOpenStatus);
                            statusUpdate.put("closed", !newOpenStatus);

                            firestore.collection("users").document(uid)
                                    .collection(uid)
                                    .document("ShopStatus")
                                    .set(statusUpdate)
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            // Reflect changes in button appearance
                                            updateShopButtonAppearance(newOpenStatus);
                                        } else {
                                            Snackbar.make(binding.getRoot(), "Failed to update shop status", Snackbar.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Snackbar.make(binding.getRoot(), "Failed to toggle shop status", Snackbar.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    // Method to update button appearance based on shop status
    private void updateShopButtonAppearance(boolean isOpen) {
        if (isOpen) {
            binding.shopBtn.setText("OPEN");
            binding.shopBtn.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green));
            Snackbar.make(binding.getRoot(), "Shop is now OPEN.", Snackbar.LENGTH_SHORT).show();
        } else {
            binding.shopBtn.setText("CLOSED");
            binding.shopBtn.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.red));
            Snackbar.make(binding.getRoot(), "Shop is now CLOSED.", Snackbar.LENGTH_SHORT).show();
        }
    }


    private void loadShopName() {
        if (user != null) {
            String uid = user.getUid();
            firestore.collection("users").document(uid)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            String shopName = task.getResult().getString("shopName");
                            binding.shoptv.setText(shopName != null ? shopName : "No Shop Name");
                        } else {
                            Snackbar.make(binding.getRoot(), "Failed to load shop name", Snackbar.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    // Fetch and set switch states based on Firestore data
    private void loadSwitchStatesFromDatabase() {
        if (user != null) {
            String uid = user.getUid();

            firestore.collection("users")
                    .document(uid)
                    .collection(uid)
                    .document("Services")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                // Set switch states based on the retrieved data
                                initialSwitch1State = document.getBoolean("Service1") != null && document.getBoolean("Service1");
                                initialSwitch2State = document.getBoolean("Service2") != null && document.getBoolean("Service2");
                                initialSwitch3State = document.getBoolean("Service3") != null && document.getBoolean("Service3");
                                initialSwitch4State = document.getBoolean("Service4") != null && document.getBoolean("Service4");

                                // Update switch UI
                                binding.switch1.setChecked(initialSwitch1State);
                                binding.switch2.setChecked(initialSwitch2State);
                                binding.switch3.setChecked(initialSwitch3State);
                                binding.switch4.setChecked(initialSwitch4State);

                                binding.updateBtn.setEnabled(false);
                            }
                        } else {
                            Snackbar.make(binding.getRoot(), "Failed to load services", Snackbar.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    // Update Firestore with current switch states
    private void updateServicesInDatabase() {
        if (user != null) {
            String uid = user.getUid();
            Map<String, Object> servicesData = new HashMap<>();
            servicesData.put("Service1", binding.switch1.isChecked());
            servicesData.put("Service2", binding.switch2.isChecked());
            servicesData.put("Service3", binding.switch3.isChecked());
            servicesData.put("Service4", binding.switch4.isChecked());

            firestore.collection("users")
                    .document(uid)
                    .collection(uid)
                    .document("Services")
                    .set(servicesData)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Snackbar.make(binding.getRoot(), "Services updated successfully", Snackbar.LENGTH_SHORT).show();
                        } else {
                            Snackbar.make(binding.getRoot(), "Failed to update services", Snackbar.LENGTH_SHORT).show();
                        }
                    });
        }
    }


}
