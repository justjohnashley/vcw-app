package com.pural_ba3a.vulcanwash;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

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
                            holder.shopAvail.setText("Open from 9am to 10pm");
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
                                new MaterialAlertDialogBuilder(context)
                                        .setTitle(context.getString(R.string.booking_title)) // Customize title
                                        .setMessage(context.getString(R.string.booking_message, shop.getShopName())) // Display shop name in message
                                        .setNeutralButton(context.getString(R.string.cancel), (dialog, which) -> {
                                            // Handle cancel button click
                                            dialog.dismiss();
                                        })
                                        .setNegativeButton(context.getString(R.string.decline), (dialog, which) -> {
                                            // Handle decline button click
                                            Toast.makeText(context, "Booking declined", Toast.LENGTH_SHORT).show();
                                        })
                                        .setPositiveButton(context.getString(R.string.accept), (dialog, which) -> {
                                            // Handle accept button click
                                            Toast.makeText(context, "Booking confirmed for " + shop.getShopName(), Toast.LENGTH_SHORT).show();
                                            // Additional code to proceed with booking can go here
                                        })
                                        .show();
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
}
