package com.messmate.delivery.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.messmate.delivery.databinding.DialogNewOrderBinding;
import com.messmate.delivery.models.Order;

public class NewOrderDialog extends DialogFragment {

    private DialogNewOrderBinding binding;
    private Order order;
    private OnActionListener listener;
    private CountDownTimer timer;

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

        // ================= BASIC =================
        binding.tvMessName.setText(order.getMessName());
        binding.tvAmount.setText("₹" + order.getTotalPrice() + " | Fee ₹" + order.getDeliveryFee());

        // ================= CUSTOMER =================
        binding.tvCustomerName.setText(order.getCustomerName());
        binding.tvCustomerPhone.setText(order.getCustomerPhone());

        // ================= PAYMENT =================
        binding.tvPayment.setText(order.getPaymentMethod());

        // ================= ADDRESS (SAFE) =================
        if (order.getPickupLocation() != null && order.getPickupLocation().getAddress() != null) {
            binding.tvPickup.setText("📍 " + order.getPickupLocation().getAddress());
        } else {
            binding.tvPickup.setText("📍 " + order.getPickupAddress());
        }

        if (order.getDropLocation() != null && order.getDropLocation().getAddress() != null) {
            binding.tvDrop.setText("🏁 " + order.getDropLocation().getAddress());
        } else {
            binding.tvDrop.setText("🏁 " + order.getDropAddress());
        }

        // ================= DISTANCE (dummy for now) =================
        binding.tvDistance.setText("~2.5 km");

        // ================= TIMER =================
        long timeLeft = order.getExpiresAt() - System.currentTimeMillis();

        if (timeLeft > 0) {
            timer = new CountDownTimer(timeLeft, 1000) {

                public void onTick(long millisUntilFinished) {
                    binding.tvTimer.setText((millisUntilFinished / 1000) + "s");
                }

                public void onFinish() {
                    dismiss(); // auto close
                }

            }.start();
        }

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

        if (timer != null) {
            timer.cancel();
        }

        binding = null;
    }
}