package com.messmate.delivery.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.messmate.delivery.databinding.FragmentOrdersBinding;
import com.messmate.delivery.models.Order;
import com.messmate.delivery.models.AvailableOrdersResponse;
import com.messmate.delivery.viewmodel.OrdersViewModel;
import com.messmate.delivery.viewmodel.ViewModelFactory;

public class OrdersFragment extends Fragment implements OrdersAdapter.OnOrderAcceptClickListener {

    private FragmentOrdersBinding binding;
    private OrdersViewModel viewModel;
    private OrdersAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOrdersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewModelFactory factory = new ViewModelFactory(requireContext());
        viewModel = new ViewModelProvider(this, factory).get(OrdersViewModel.class);

        adapter = new OrdersAdapter(this);
        binding.rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvOrders.setAdapter(adapter);

        fetchOrders();
    }

    private void fetchOrders() {
        viewModel.getAvailableOrders().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.status) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.tvNoOrders.setVisibility(View.GONE);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    if (resource.data != null && resource.data.getData() != null && !resource.data.getData().isEmpty()) {
                        adapter.setOrders(resource.data.getData());
                        binding.tvNoOrders.setVisibility(View.GONE);
                        binding.rvOrders.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvNoOrders.setVisibility(View.VISIBLE);
                        binding.rvOrders.setVisibility(View.GONE);
                    }
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    @Override
    public void onAcceptClicked(Order order) {
        viewModel.acceptOrder(order.getId()).observe(getViewLifecycleOwner(), resource -> {
             switch (resource.status) {
                 case LOADING:
                     binding.progressBar.setVisibility(View.VISIBLE);
                     break;
                 case SUCCESS:
                     binding.progressBar.setVisibility(View.GONE);
                     Toast.makeText(requireContext(), "Order Accepted!", Toast.LENGTH_SHORT).show();
                     Intent intent = new Intent(requireActivity(), OrderDetailsActivity.class);
                     intent.putExtra("orderId", order.getId());
                     intent.putExtra("items", "Standard Delivery Package");
                     
                     String pickup = order.getPickupLocation() != null ? order.getPickupLocation().getAddress() : "Unknown";
                     String drop = order.getDropLocation() != null ? order.getDropLocation().getAddress() : "Unknown";
                     
                     intent.putExtra("locations", "Pickup: " + pickup + "\nDrop: " + drop);
                     startActivity(intent);
                     fetchOrders(); // Refresh list
                     break;
                 case ERROR:
                     binding.progressBar.setVisibility(View.GONE);
                     Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show();
                     break;
             }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
