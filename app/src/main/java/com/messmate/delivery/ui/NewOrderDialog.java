package com.messmate.delivery.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.*;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.messmate.delivery.databinding.DialogNewOrderBinding;
import com.messmate.delivery.models.Order;

public class NewOrderDialog extends DialogFragment {

    private DialogNewOrderBinding binding;
    private Order order;
    private OnActionListener listener;

    public interface OnActionListener {
        void onAccept(Order order);
        void onReject(Order order);
    }

    public NewOrderDialog(Order order, OnActionListener listener) {
        this.order = order;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = DialogNewOrderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        if (order == null) return;

        // ================= DATA =================
        binding.tvMessName.setText(order.getMessName());
        binding.tvAmount.setText("₹" + order.getDeliveryFee());

        if (order.getPickupLocation() != null) {
            binding.tvPickup.setText("📍 " + order.getPickupLocation().getAddress());
        }

        if (order.getDropLocation() != null) {
            binding.tvDrop.setText("🏁 " + order.getDropLocation().getAddress());
        }

        binding.tvDistance.setText("~2.5 km"); // later GPS

        // ================= BUTTONS =================

        binding.btnAccept.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAccept(order);
            }
            dismiss();
        });

        binding.btnReject.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReject(order);
            }
            dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}