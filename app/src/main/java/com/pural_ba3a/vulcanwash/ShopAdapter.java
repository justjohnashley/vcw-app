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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
                                // Fetch the shop's address
                                fetchShopInfo(shopUid, (address, contactNumber) -> {
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
                                    TextView contactNumberTV = bottomSheetView.findViewById(R.id.contactNum); // Add TextView for contact number
                                    Button confirmBookingButton = bottomSheetView.findViewById(R.id.confirmBookingButton);
                                    Button cancelBookingButton = bottomSheetView.findViewById(R.id.cancelBookingButton);
                                    Button selectTimeButton = bottomSheetView.findViewById(R.id.selectTimeButton);
                                    Button selectPaymentMethodButton = bottomSheetView.findViewById(R.id.selectPaymentMethodButton);
                                    Button selectServicesButton = bottomSheetView.findViewById(R.id.selectServicesButton);

                                    // Set shop name and fetched address
                                    bookingTitle.setText(shop.getShopName());
                                    bookingMessage.setText(address);
                                    contactNumberTV.setText(contactNumber != null ? contactNumber : "Contact number not available");


                                    selectTimeButton.setOnClickListener(v -> {
                                        showTimePicker((FragmentActivity) context, selectTimeButton);
                                    });

                                    selectPaymentMethodButton.setOnClickListener(v -> {
                                        showPaymentMethodPicker(context, selectPaymentMethodButton);
                                    });

                                    selectServicesButton.setOnClickListener(v -> {
                                        fetchServices(shopUid, servicesData -> {
                                            if (servicesData != null) {
                                                showServicePicker(context, selectServicesButton, servicesData);
                                            } else {
                                                Toast.makeText(context, "Failed to load services", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    });

                                    confirmBookingButton.setOnClickListener(confirmView -> {
                                        // Collect the selected time, payment method, and services
                                        String selectedTime = selectTimeButton.getText().toString(); // Assume the time is set on the button
                                        String selectedPaymentMethod = selectPaymentMethodButton.getText().toString(); // Assume payment method is selected
                                        String selectedServices = selectServicesButton.getText().toString(); // Assume services are selected

                                        // Validate that all required fields are selected
                                        if (selectedTime.equals("Select Time") || selectedPaymentMethod.equals("Select Payment Method") || selectedServices.equals("Pick a Service")) {
                                            Toast.makeText(context, "Please complete all fields before confirming the booking.", Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        // Generate a random orderId (12 alphanumeric characters)
                                        String orderId = generateRandomOrderId();

                                        // Get the current user's UID
                                        FirebaseAuth auth = FirebaseAuth.getInstance();
                                        String currentUserUid = auth.getCurrentUser().getUid();

                                        if (currentUserUid == null || currentUserUid.isEmpty()) {
                                            Toast.makeText(context, "User not authenticated. Please log in.", Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        // Fetch the current user's details
                                        firestore.collection("users")
                                                .document(currentUserUid)
                                                .get()
                                                .addOnSuccessListener(userSnapshot -> { // Renamed variable here
                                                    if (userSnapshot.exists()) {
                                                        String username = userSnapshot.getString("username"); // User's name
                                                        String contact = userSnapshot.getString("contact");  // User's contact details

                                                        if (username == null || contact == null) {
                                                            Toast.makeText(context, "User details are incomplete. Please update your profile.", Toast.LENGTH_SHORT).show();
                                                            return;
                                                        }

                                                        // Create a map for the order details
                                                        Map<String, Object> orderData = new HashMap<>();
                                                        orderData.put("shopId", shop.getUid());      // Shop UID
                                                        orderData.put("shopName", shop.getShopName()); // Shop Name
                                                        orderData.put("username", username);         // Customer's username
                                                        orderData.put("contact", contact);           // Customer's contact
                                                        orderData.put("userId", currentUserUid);      // Customer's UID
                                                        orderData.put("time", selectedTime);
                                                        orderData.put("paymethod", selectedPaymentMethod);
                                                        orderData.put("service", selectedServices);
                                                        orderData.put("orderId", orderId);
                                                        orderData.put("accepted", false);  // Default value for accepted
                                                        orderData.put("rejected", false);  // Default value for rejected
                                                        orderData.put("status", "pending"); // Default value for status

                                                        // Store the order in the "orders" collection
                                                        firestore.collection("orders")
                                                                .document(orderId)
                                                                .set(orderData)
                                                                .addOnSuccessListener(aVoid -> {
                                                                    Toast.makeText(context, "Booking confirmed for " + shop.getShopName(), Toast.LENGTH_SHORT).show();
                                                                    bottomSheetDialog.dismiss();
                                                                })
                                                                .addOnFailureListener(e -> {
                                                                    Toast.makeText(context, "Failed to confirm booking", Toast.LENGTH_SHORT).show();
                                                                });
                                                    } else {
                                                        Toast.makeText(context, "Failed to fetch user details.", Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(context, "Error fetching user details.", Toast.LENGTH_SHORT).show();
                                                });
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
                                });
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
            int selectedHour = timePicker.getHour();
            int selectedMinute = timePicker.getMinute();

            // Get current time
            Calendar currentTime = Calendar.getInstance();
            int currentHour = currentTime.get(Calendar.HOUR_OF_DAY);
            int currentMinute = currentTime.get(Calendar.MINUTE);

            // Calculate the time difference in minutes
            int selectedTotalMinutes = selectedHour * 60 + selectedMinute;
            int currentTotalMinutes = currentHour * 60 + currentMinute;
            int timeDifference = selectedTotalMinutes - currentTotalMinutes;

            if (timeDifference < 60) {
                // Show error if the selected time is less than one hour from the current time
                Toast.makeText(activity, "Please select a time at least one hour from now.", Toast.LENGTH_SHORT).show();
            } else {
                // Convert 24-hour to 12-hour format
                boolean isAM = selectedHour < 12;
                String period = isAM ? "AM" : "PM";

                if (selectedHour > 12) {
                    selectedHour -= 12;
                } else if (selectedHour == 0) {
                    selectedHour = 12;
                }

                // Format the time as a string
                String formattedTime = String.format("%02d:%02d %s", selectedHour, selectedMinute, period);

                // Update the button text with the selected time
                timeButton.setText(formattedTime);
            }
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

        // Map database keys to friendly display names
        Map<String, String> serviceNameMap = new LinkedHashMap<>();
        serviceNameMap.put("Service1", "Check-up");
        serviceNameMap.put("Service2", "Maintenance");
        serviceNameMap.put("Service3", "Repair");
        serviceNameMap.put("Service4", "Car Wash");

        // Ordered lists for services
        List<String> serviceKeys = new ArrayList<>();
        List<String> serviceDisplayNames = new ArrayList<>();

        for (Map.Entry<String, String> entry : serviceNameMap.entrySet()) {
            // Check if the service exists in the data and is enabled
            Boolean isEnabled = (Boolean) servicesData.get(entry.getKey());
            if (isEnabled != null && isEnabled) {
                serviceKeys.add(entry.getKey()); // Keep track of the database keys in order
                serviceDisplayNames.add(entry.getValue()); // Use friendly names in order
            }
        }

        // If no services are enabled
        if (serviceDisplayNames.isEmpty()) {
            Toast.makeText(context, "No services available", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean[] checkedItems = new boolean[serviceDisplayNames.size()]; // Default all unchecked

        // Create a list of services with checkboxes
        CharSequence[] servicesArray = serviceDisplayNames.toArray(new CharSequence[0]);

        // Build the dialog
        new MaterialAlertDialogBuilder(context)
                .setTitle("Available Services")
                .setMultiChoiceItems(servicesArray, checkedItems, (dialog, which, isChecked) -> {
                    checkedItems[which] = isChecked; // Update selected state
                })
                .setPositiveButton("OK", (dialog, which) -> {
                    // Update button text with selected services
                    StringBuilder selectedServices = new StringBuilder();
                    for (int i = 0; i < serviceKeys.size(); i++) {
                        if (checkedItems[i]) {
                            if (selectedServices.length() > 0) {
                                selectedServices.append(", ");
                            }
                            selectedServices.append(serviceDisplayNames.get(i));
                        }
                    }
                    if (selectedServices.length() > 0) {
                        servicesButton.setText(selectedServices.toString());
                    } else {
                        servicesButton.setText("Pick a Service");
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void fetchShopInfo(String uid, OnShopInfoFetchedListener listener) {
        firestore.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String street = documentSnapshot.getString("street");
                        String barangay = documentSnapshot.getString("barangay");
                        String city = documentSnapshot.getString("city");
                        String province = documentSnapshot.getString("province");
                        String contactNumber = documentSnapshot.getString("contact"); // Retrieve the contact number

                        // Format the address
                        StringBuilder addressBuilder = new StringBuilder();
                        if (street != null && !street.isEmpty()) addressBuilder.append(street).append(", ");
                        if (barangay != null && !barangay.isEmpty()) addressBuilder.append(barangay).append(", ");
                        if (city != null && !city.isEmpty()) addressBuilder.append(city).append(", ");
                        if (province != null && !province.isEmpty()) addressBuilder.append(province);

                        String formattedAddress = addressBuilder.toString().replaceAll(", $", "");

                        // Pass both address and contact number to the listener
                        listener.onShopInfoFetched(formattedAddress, contactNumber);
                    } else {
                        listener.onShopInfoFetched("Address not available", null);
                    }
                })
                .addOnFailureListener(e -> listener.onShopInfoFetched("Error fetching address", null));
    }

    interface OnShopInfoFetchedListener {
        void onShopInfoFetched(String address, String contactNumber);
    }

    private String generateRandomOrderId() {
        // Generate a random 12-character alphanumeric string for orderId
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder orderId = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            int index = (int) (Math.random() * characters.length());
            orderId.append(characters.charAt(index));
        }
        return orderId.toString();
    }



}
