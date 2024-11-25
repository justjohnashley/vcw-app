package com.pural_ba3a.vulcanwash;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.widget.TextView;

// Other imports...

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ShopViewHolder> {
    private List<Shop> shopList;
    private Context context;
    private FirebaseFirestore firestore;
    private List<ListenerRegistration> listenerRegistrations = new ArrayList<>();

    public ShopAdapter(List<Shop> shopList, Context context) {
        this.shopList = shopList;
        this.context = context;
        this.firestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ShopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.shop_card, parent, false);
        return new ShopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopViewHolder holder, int position) {
        Shop shop = shopList.get(position);
        String shopUid = shop.getUid();

        // Set the shop name
        holder.shopNameTextView.setText(shop.getShopName());

        // Real-time listener for ShopStatus
        ListenerRegistration registration = firestore.collection("users")
                .document(shopUid)
                .collection(shopUid)
                .document("ShopStatus")
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(context, "Failed to load shop status", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        Boolean isOpen = documentSnapshot.getBoolean("open");
                        Boolean isClosed = documentSnapshot.getBoolean("closed");

                        // Customize the text based on the shop's status
                        if (isOpen != null && isOpen) {
                            holder.shopAvail.setText("Shop is currently open");
                            holder.bookButton.setEnabled(true);
                        } else if (isClosed != null && isClosed) {
                            holder.shopAvail.setText("Shop is currently closed");
                            holder.bookButton.setEnabled(false);
                        } else {
                            holder.shopAvail.setText("Shop status unknown");
                            holder.bookButton.setEnabled(false);
                        }

                        holder.bookButton.setOnClickListener(view -> {
                            if (isOpen != null && isOpen) {
                                // Create the BottomSheetDialog
                                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);

                                // Inflate the bottom sheet layout
                                View bottomSheetView = LayoutInflater.from(context).inflate(
                                        R.layout.dialog_booking,
                                        (ViewGroup) ((FragmentActivity) context).findViewById(R.id.bottomSheetContainer)
                                );



                                // Initialize UI elements from the bottom sheet layout
                                TextView bookingTitle = bottomSheetView.findViewById(R.id.bookingTitle);
                                TextView bookingMessage = bottomSheetView.findViewById(R.id.bookingMessage);
                                Button confirmBookingButton = bottomSheetView.findViewById(R.id.confirmBookingButton);
                                Button cancelBookingButton = bottomSheetView.findViewById(R.id.cancelBookingButton);
                                Button selectTimeButton = bottomSheetView.findViewById(R.id.selectTimeButton); // Add this button to your layout
                                Button selectPaymentMethodButton = bottomSheetView.findViewById(R.id.selectPaymentMethodButton);
                                Button selectServicesButton = bottomSheetView.findViewById(R.id.selectServicesButton);

                                // Move fetchServices logic to button click
                                selectServicesButton.setOnClickListener(v -> {
                                    // Fetch services for the selected shop only when button is clicked
                                    fetchServices(shopUid, servicesData -> {
                                        if (servicesData != null) {
                                            // Create and show the checkbox picker
                                            showServicePicker(context, selectServicesButton, servicesData);
                                        } else {
                                            Toast.makeText(context, "Failed to load services", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                });


                                bookingTitle.setText(shop.getShopName());
                                bookingMessage.setText("[shop address here]");

                                selectTimeButton.setOnClickListener(v -> {
                                    showTimePicker((FragmentActivity) context, selectTimeButton);
                                });


                                selectPaymentMethodButton.setOnClickListener(v -> {
                                    showPaymentMethodPicker(context, selectPaymentMethodButton);
                                });


                                confirmBookingButton.setOnClickListener(confirmView -> {
                                    Toast.makeText(context, "Booking confirmed for " + shop.getShopName(), Toast.LENGTH_SHORT).show();
                                    bottomSheetDialog.dismiss();
                                    // Add code to save booking to Firestore
                                });

                                cancelBookingButton.setOnClickListener(cancelView -> bottomSheetDialog.dismiss());

                                bottomSheetDialog.setContentView(bottomSheetView);
                                bottomSheetDialog.setOnShowListener(dialog -> {
                                    BottomSheetDialog d = (BottomSheetDialog) dialog;
                                    FrameLayout bottomSheet = d.findViewById(R.id.bottomSheetContainer);
                                    if (bottomSheet != null) {
                                        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                                        behavior.setPeekHeight(Resources.getSystem().getDisplayMetrics().heightPixels, true);
                                        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                                        behavior.setSkipCollapsed(true);
                                    }
                                });

                                bottomSheetDialog.show();

                            } else {
                                Toast.makeText(context, "Shop is currently closed", Toast.LENGTH_SHORT).show();
                            }
                        });


                    }
                });

        listenerRegistrations.add(registration);
    }

    @Override
    public int getItemCount() {
        return shopList.size();
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        for (ListenerRegistration registration : listenerRegistrations) {
            registration.remove();
        }
    }

    public static class ShopViewHolder extends RecyclerView.ViewHolder {
        TextView shopNameTextView, shopAvail;
        Button bookButton;

        public ShopViewHolder(@NonNull View itemView) {
            super(itemView);
            shopNameTextView = itemView.findViewById(R.id.shopNameTextView);
            shopAvail = itemView.findViewById(R.id.shopAvail);
            bookButton = itemView.findViewById(R.id.bookButton);
        }
    }

    private void showTimePicker(FragmentActivity activity, Button timeButton) {
        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H) // Use 12-hour format for AM/PM
                .setHour(12) // Default hour
                .setMinute(0) // Default minute
                .setTitleText("Select Time")
                .build();

        timePicker.show(activity.getSupportFragmentManager(), "MaterialTimePicker");

        timePicker.addOnPositiveButtonClickListener(v -> {
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();
            boolean isAM = hour < 12;
            String period = isAM ? "AM" : "PM";

            // Convert 24-hour to 12-hour format
            if (hour > 12) {
                hour -= 12;
            } else if (hour == 0) {
                hour = 12;
            }

            // Format the time as a string
            String formattedTime = String.format("%02d:%02d %s", hour, minute, period);

            // Update the button text with the selected time
            timeButton.setText(formattedTime);
        });

        timePicker.addOnNegativeButtonClickListener(v -> {
            Toast.makeText(activity, "Time selection canceled", Toast.LENGTH_SHORT).show();
        });
    }

    private void showPaymentMethodPicker(Context context, Button paymentMethodButton) {
        // Payment options
        String[] paymentMethods = {"Cash"};
        int[] selectedItem = {0}; // Default selection index (0 for "Cash")

        // Create a Material Alert Dialog
        new MaterialAlertDialogBuilder(context)
                .setTitle("Select Payment Method")
                .setSingleChoiceItems(paymentMethods, selectedItem[0], (dialog, which) -> {
                    selectedItem[0] = which; // Update selected item index
                })
                .setPositiveButton("OK", (dialog, which) -> {
                    // Update button text with selected payment method
                    String selectedPaymentMethod = paymentMethods[selectedItem[0]];
                    paymentMethodButton.setText(selectedPaymentMethod);

                    // Dismiss dialog
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Dismiss dialog
                    dialog.dismiss();
                })
                .show();
    }

    private void fetchServices(String shopUid, OnServicesFetchedListener listener) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("users")
                .document(shopUid)
                .collection(shopUid)
                .document("Services")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        listener.onServicesFetched(documentSnapshot.getData());
                    } else {
                        listener.onServicesFetched(null);
                    }
                })
                .addOnFailureListener(e -> {
                    listener.onServicesFetched(null);
                });
    }

    // Define a callback interface
    interface OnServicesFetchedListener {
        void onServicesFetched(Map<String, Object> services);
    }

    private void showServicePicker(Context context, Button servicesButton, Map<String, Object> servicesData) {
        if (servicesData == null || servicesData.isEmpty()) {
            Toast.makeText(context, "No services available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Extract services and their availability
        List<String> servicesList = new ArrayList<>(servicesData.keySet());
        boolean[] checkedItems = new boolean[servicesList.size()];
        boolean[] enabledItems = new boolean[servicesList.size()];

        for (int i = 0; i < servicesList.size(); i++) {
            Boolean isEnabled = (Boolean) servicesData.get(servicesList.get(i));
            enabledItems[i] = isEnabled != null && isEnabled; // Enabled if true in Firestore
            checkedItems[i] = false; // Default all unchecked
        }

        // Create a list of services with checkboxes
        CharSequence[] servicesArray = servicesList.toArray(new CharSequence[0]);

        // Build the dialog
        new MaterialAlertDialogBuilder(context)
                .setTitle("Select Services")
                .setMultiChoiceItems(servicesArray, checkedItems, (dialog, which, isChecked) -> {
                    if (!enabledItems[which]) {
                        Toast.makeText(context, "Service unavailable", Toast.LENGTH_SHORT).show();
                        ((androidx.appcompat.app.AlertDialog) dialog).getListView().setItemChecked(which, false);
                    } else {
                        checkedItems[which] = isChecked;
                    }
                })
                .setPositiveButton("OK", (dialog, which) -> {
                    // Update button text with selected services
                    StringBuilder selectedServices = new StringBuilder();
                    for (int i = 0; i < servicesList.size(); i++) {
                        if (checkedItems[i]) {
                            if (selectedServices.length() > 0) {
                                selectedServices.append(", ");
                            }
                            selectedServices.append(servicesList.get(i));
                        }
                    }
                    if (selectedServices.length() > 0) {
                        servicesButton.setText(selectedServices.toString());
                    } else {
                        servicesButton.setText("Select Services");
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }


}
