package com.pural_ba3a.vulcanwash;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private List<Order> orderList;
    private Context context;

    public OrderAdapter(List<Order> orderList) {
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext(); // Save the context for color resources
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.serviceTextView.setText(order.getService());
        holder.timeTextView.setText(order.getTime());
        holder.shopNameTextView.setText(order.getShopName());

        // Priority: "finished" overrides all other statuses
        if ("finished".equalsIgnoreCase(order.getStatus())) {
            holder.statusTextView.setText("Finished");
            holder.statusTextView.setTextColor(ContextCompat.getColor(context, R.color.white));
            holder.statuscv.setCardBackgroundColor(ContextCompat.getColor(context, R.color.darkgray));
        }
        // Check "rejected" next
        else if (order.isRejected()) {
            holder.statusTextView.setText("Rejected");
            holder.statusTextView.setTextColor(ContextCompat.getColor(context, R.color.white));
            holder.statuscv.setCardBackgroundColor(ContextCompat.getColor(context, R.color.red));
        }
        // Check "ongoing"
        else if ("ongoing".equalsIgnoreCase(order.getStatus())) {
            holder.statusTextView.setText("Ongoing");
            holder.statusTextView.setTextColor(ContextCompat.getColor(context, R.color.black));
            holder.statuscv.setCardBackgroundColor(ContextCompat.getColor(context, R.color.lighterblue));
        }
        // Check "accepted"
        else if (order.isAccepted()) {
            holder.statusTextView.setText("Active");
            holder.statusTextView.setTextColor(ContextCompat.getColor(context, R.color.black));
            holder.statuscv.setCardBackgroundColor(ContextCompat.getColor(context, R.color.green));
        }
        // Check "pending"
        else if ("pending".equalsIgnoreCase(order.getStatus())) {
            holder.statusTextView.setText("Pending");
            holder.statusTextView.setTextColor(ContextCompat.getColor(context, R.color.black));
            holder.statuscv.setCardBackgroundColor(ContextCompat.getColor(context, R.color.yellow));
        }
        // Default case for unknown status
        else {
            holder.statusTextView.setText("Unknown");
            holder.statusTextView.setTextColor(ContextCompat.getColor(context, R.color.black));
            holder.statuscv.setCardBackgroundColor(Color.WHITE);
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView serviceTextView, statusTextView, timeTextView, shopNameTextView;
        MaterialCardView statuscv;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceTextView = itemView.findViewById(R.id.serviceTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            shopNameTextView = itemView.findViewById(R.id.shopNameTextView);
            statuscv = itemView.findViewById(R.id.statuscv);
        }
    }
}
