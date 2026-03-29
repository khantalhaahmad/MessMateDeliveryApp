package com.messmate.delivery.ui;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.messmate.delivery.databinding.ItemOrderBinding;
import com.messmate.delivery.models.Order;

import java.util.ArrayList;
import java.util.List;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {

    private final List<Order> orders = new ArrayList<>();
    private final OnOrderAcceptClickListener listener;

    public interface OnOrderAcceptClickListener {
        void onAcceptClicked(Order order);
    }

    public OrdersAdapter(OnOrderAcceptClickListener listener) {
        this.listener = listener;
    }

    // ================= SET LIST =================
    @SuppressLint("NotifyDataSetChanged")
    public void setOrders(List<Order> newOrders) {
        orders.clear();

        if (newOrders != null) {
            orders.addAll(newOrders);
        }

        notifyDataSetChanged();
    }

    // ================= ADD ORDER =================
    public void addOrder(Order order) {
        if (order == null) return;

        orders.add(0, order); // top pe show
        notifyItemInserted(0);
    }

    // ================= REMOVE ORDER =================
    public void removeOrder(String orderId) {
        if (orderId == null) return;

        for (int i = 0; i < orders.size(); i++) {
            if (orderId.equals(orders.get(i).getId())) {
                orders.remove(i);
                notifyItemRemoved(i);
                return;
            }
        }
    }

    // ================= UPDATE ORDER =================
    public void updateOrder(Order updatedOrder) {
        if (updatedOrder == null) return;

        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i).getId().equals(updatedOrder.getId())) {
                orders.set(i, updatedOrder);
                notifyItemChanged(i);
                return;
            }
        }
    }

    // ================= GET ORDER =================
    public Order getOrderAt(int position) {
        if (position < 0 || position >= orders.size()) return null;
        return orders.get(position);
    }

    // ================= CLEAR =================
    public void clear() {
        orders.clear();
        notifyDataSetChanged();
    }

    // ================= ADAPTER =================
    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderBinding binding = ItemOrderBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new OrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        holder.bind(orders.get(position));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    // ================= VIEW HOLDER =================
    class OrderViewHolder extends RecyclerView.ViewHolder {

        private final ItemOrderBinding binding;

        public OrderViewHolder(ItemOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Order order) {

            if (order == null) return;

            // 🔥 Order ID
            binding.tvOrderId.setText("Order #" + safe(order.getId()));

            // 💰 Amount
            binding.tvAmount.setText("₹" + order.getDeliveryFee());

            // 📍 Pickup
            if (order.getPickupLocation() != null) {
                binding.tvPickup.setText("📍 " + safe(order.getPickupLocation().getAddress()));
            } else {
                binding.tvPickup.setText("📍 Pickup unavailable");
            }

            // 🏁 Drop
            if (order.getDropLocation() != null) {
                binding.tvDrop.setText("🏁 " + safe(order.getDropLocation().getAddress()));
            } else {
                binding.tvDrop.setText("🏁 Drop unavailable");
            }

            // 🚀 Accept Button (status based)
            if ("NOT_ASSIGNED".equals(order.getDeliveryStatus())) {

                binding.btnAccept.setVisibility(View.VISIBLE);

                binding.btnAccept.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onAcceptClicked(order);
                    }
                });

            } else {
                binding.btnAccept.setVisibility(View.GONE);
            }
        }

        private String safe(String text) {
            return text == null ? "" : text;
        }
    }
}