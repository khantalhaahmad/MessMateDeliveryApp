package com.messmate.delivery.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.messmate.delivery.databinding.ItemOrderBinding;
import com.messmate.delivery.models.Order;

import java.util.ArrayList;
import java.util.List;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {

    private List<Order> orders = new ArrayList<>();
    private final OnOrderAcceptClickListener listener;

    public interface OnOrderAcceptClickListener {
        void onAcceptClicked(Order order);
    }

    public OrdersAdapter(OnOrderAcceptClickListener listener) {
        this.listener = listener;
    }

    public void setOrders(List<Order> newOrders) {
        this.orders = newOrders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderBinding binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new OrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orders == null ? 0 : orders.size();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {

        private final ItemOrderBinding binding;

        public OrderViewHolder(ItemOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Order order) {
            binding.tvOrderId.setText("Order #" + order.getId());
            binding.tvAmount.setText(String.format("$%.2f", order.getDeliveryFee()));
            
            if (order.getPickupLocation() != null) {
                binding.tvPickup.setText("Pickup: " + order.getPickupLocation().getAddress());
            }
            if (order.getDropLocation() != null) {
                binding.tvDrop.setText("Drop: " + order.getDropLocation().getAddress());
            }
            
            binding.btnAccept.setOnClickListener(v -> {
                if(listener != null) listener.onAcceptClicked(order);
            });
        }
    }
}
