package com.pural_ba3a.vulcanwash;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class BookingDialogFragment extends DialogFragment {

    private String shopName;

    // Constructor to initialize shop name
    public BookingDialogFragment(String shopName) {
        this.shopName = shopName;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Build the dialog, you could also inflate a custom layout here if needed
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("Booking Confirmation")
                .setMessage("Do you want to book an appointment at " + shopName + "?")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    // Add booking logic here
                    Toast.makeText(requireContext(), "Booking confirmed for " + shopName, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        return builder.create();
    }
}

