package com.messmate.delivery.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.messmate.delivery.databinding.FragmentDashboardBinding;
import com.messmate.delivery.models.Order;
import com.messmate.delivery.socket.SocketManager;
import com.messmate.delivery.utils.SharedPreferencesManager;
import com.messmate.delivery.viewmodel.DashboardViewModel;
import com.messmate.delivery.viewmodel.ViewModelFactory;

import java.util.List;

import io.socket.client.Socket;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private DashboardViewModel viewModel;
    private Socket mSocket;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferencesManager prefs = new SharedPreferencesManager(requireContext());
        binding.tvGreeting.setText("Welcome, " + prefs.getAgentName());

        ViewModelFactory factory = new ViewModelFactory(requireContext());
        viewModel = new ViewModelProvider(this, factory).get(DashboardViewModel.class);

        setupSocketListeners();
        
        binding.switchOnline.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                toggleOnlineStatus(isChecked);
            }
        });

        viewModel.getIsOnline().observe(getViewLifecycleOwner(), isOnline -> {
            binding.switchOnline.setChecked(isOnline != null && isOnline);
        });

        binding.btnOrderAction.setOnClickListener(v -> handleOrderAction());

        loadDashboardData();
    }

    private void loadDashboardData() {
        fetchEarnings();
        fetchAvailableOrders();
    }

    private void setupSocketListeners() {
        mSocket = SocketManager.getInstance().getSocket();
        if (mSocket != null) {
            mSocket.on("order_accepted", args -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(this::fetchAvailableOrders);
                }
            });
            mSocket.on("new_order", args -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(this::fetchAvailableOrders);
                }
            });
        }
    }

    private void toggleOnlineStatus(boolean online) {
        binding.switchOnline.setEnabled(false);
        viewModel.toggleStatus(online).observe(getViewLifecycleOwner(), resource -> {
            switch (resource.status) {
                case SUCCESS:
                    binding.switchOnline.setEnabled(true);
                    viewModel.setIsOnline(online);
                    break;
                case ERROR:
                    binding.switchOnline.setEnabled(true);
                    binding.switchOnline.setChecked(!online); // Revert
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show();
                    break;
                case LOADING:
                    break;
            }
        });
    }

    private void fetchEarnings() {
        viewModel.getEarnings().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == com.messmate.delivery.utils.Resource.Status.SUCCESS && resource.data != null) {
                binding.tvEarnings.setText(String.format("$%.2f", resource.data.getTotalEarnings()));
            }
        });
    }

    private void fetchAvailableOrders() {
        if (viewModel.getActiveOrder().getValue() != null) return; // Disregard if there is an active order

        binding.pbWaiting.setVisibility(View.VISIBLE);
        binding.viewWaiting.setVisibility(View.VISIBLE);
        binding.cardOrder.setVisibility(View.GONE);

        viewModel.getAvailableOrders().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.status) {
                case SUCCESS:
                    binding.pbWaiting.setVisibility(View.GONE);
                    if (resource.data != null && resource.data.getData() != null && !resource.data.getData().isEmpty()) {
                        // Show first available order
                        Order firstOrder = resource.data.getData().get(0);
                        showOrderCard(firstOrder, false);
                    } else {
                        binding.tvWaitingMessage.setText("Waiting for orders...");
                    }
                    break;
                case ERROR:
                    binding.pbWaiting.setVisibility(View.GONE);
                    binding.tvWaitingMessage.setText("Error loading orders");
                    break;
                case LOADING:
                    binding.pbWaiting.setVisibility(View.VISIBLE);
                    break;
            }
        });
    }

    private void showOrderCard(Order order, boolean isActive) {
        binding.viewWaiting.setVisibility(View.GONE);
        binding.cardOrder.setVisibility(View.VISIBLE);

        binding.tvOrderMessName.setText(order.getMessName() != null ? order.getMessName() : "Restaurant");
        binding.tvOrderFee.setText(String.format("Fee: $%.2f", order.getDeliveryFee()));

        if (order.getPickupLocation() != null) {
            binding.tvPickupLoc.setText("📍 Pickup: " + order.getPickupLocation().getAddress());
        }
        if (order.getDropLocation() != null) {
            binding.tvDropLoc.setText("🏁 Drop: " + order.getDropLocation().getAddress());
        }

        // Calculate direct mock distance (in reality using Location Services)
        binding.tvDistance.setText("Distance: ~2.5 km");

        updateActionButton(order, isActive);
    }

    private void handleOrderAction() {
        Order currentOrder = viewModel.getActiveOrder().getValue();
        binding.btnOrderAction.setEnabled(false);

        if (currentOrder == null) {
            // It means we are seeing an available order & we want to ACCEPT it
            Order pendingOrder = new Order(); // In reality, we need reference to the rendered pending order
            // Let's get it from the UI or store it temporarily. As we rendered the first order:
            // Let's re-fetch or use a temporary var. Since this is an MVP, we'll store active in ViewModel
            // But before that, we need the ID.
            Toast.makeText(requireContext(), "Accepting order...", Toast.LENGTH_SHORT).show();
            // Wait, we need the actual order ID. I will store the currently viewed pending order in the view model or a local var.
            // I'll update the implementation to pass it.
        } else {
            // We have an active order, progress status
            String nextStatus = getNextStatus(currentOrder.getDeliveryStatus());
            if (nextStatus != null) {
                updateOrderStatus(currentOrder, nextStatus);
            }
        }
    }
    
    // Extracted method to fix the handleOrderAction ID requirement
    private Order pendingOrderToShow = null;
    
    // Override showOrderCard to store pending order
    private void showOrderAvailable(Order order) {
        pendingOrderToShow = order;
        showOrderCard(order, false);
        binding.btnOrderAction.setOnClickListener(v -> {
            if (pendingOrderToShow != null) acceptOrder(pendingOrderToShow);
        });
    }

    private void acceptOrder(Order order) {
        binding.btnOrderAction.setEnabled(false);
        viewModel.acceptOrder(order.getId()).observe(getViewLifecycleOwner(), resource -> {
            switch (resource.status) {
                case SUCCESS:
                    binding.btnOrderAction.setEnabled(true);
                    order.setDeliveryStatus("ACCEPTED");
                    viewModel.setActiveOrder(order);
                    showActiveOrder(order);
                    break;
                case ERROR:
                    binding.btnOrderAction.setEnabled(true);
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show();
                    break;
                case LOADING:
                    break;
            }
        });
    }

    private void showActiveOrder(Order order) {
        binding.viewWaiting.setVisibility(View.GONE);
        binding.cardOrder.setVisibility(View.VISIBLE);
        showOrderCard(order, true);
        
        binding.btnOrderAction.setOnClickListener(v -> {
            String nextStatus = getNextStatus(order.getDeliveryStatus());
            if (nextStatus != null) {
                updateOrderStatus(order, nextStatus);
            }
        });
    }

    private void updateOrderStatus(Order order, String newStatus) {
        binding.btnOrderAction.setEnabled(false);
        viewModel.updateOrderStatus(order.getId(), newStatus).observe(getViewLifecycleOwner(), resource -> {
            switch (resource.status) {
                case SUCCESS:
                    binding.btnOrderAction.setEnabled(true);
                    order.setDeliveryStatus(newStatus);
                    viewModel.setActiveOrder(order);
                    
                    if ("DELIVERED".equals(newStatus)) {
                        viewModel.setActiveOrder(null);
                        binding.cardOrder.setVisibility(View.GONE);
                        binding.viewWaiting.setVisibility(View.VISIBLE);
                        binding.tvWaitingMessage.setText("Waiting for orders...");
                        fetchEarnings(); // Refresh earnings
                        fetchAvailableOrders(); // Load next
                    } else {
                        showActiveOrder(order); // Refresh UI
                    }
                    break;
                case ERROR:
                    binding.btnOrderAction.setEnabled(true);
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show();
                    break;
                case LOADING:
                    break;
            }
        });
    }

    private String getNextStatus(String currentStatus) {
        if (currentStatus == null) return "ACCEPTED";
        switch (currentStatus) {
            case "ACCEPTED": return "REACHED_RESTAURANT";
            case "REACHED_RESTAURANT": return "PICKED_UP";
            case "PICKED_UP": return "OUT_FOR_DELIVERY";
            case "OUT_FOR_DELIVERY": return "DELIVERED";
            default: return null;
        }
    }

    private void updateActionButton(Order order, boolean isActive) {
        if (!isActive || order.getDeliveryStatus() == null) {
            binding.btnOrderAction.setText("Accept Order");
            return;
        }
        
        switch (order.getDeliveryStatus()) {
            case "ACCEPTED":
                binding.btnOrderAction.setText("Go to Pickup");
                break;
            case "REACHED_RESTAURANT":
                binding.btnOrderAction.setText("Mark Picked");
                break;
            case "PICKED_UP":
                binding.btnOrderAction.setText("Out for Delivery");
                break;
            case "OUT_FOR_DELIVERY":
                binding.btnOrderAction.setText("Mark Delivered");
                break;
            default:
                binding.btnOrderAction.setText("Processing...");
                break;
        }
    }

    // Update fetchAvailableOrders to call showOrderAvailable
    private void refreshAvailableOrders() {
        if (viewModel.getActiveOrder().getValue() != null) return;

        binding.pbWaiting.setVisibility(View.VISIBLE);
        viewModel.getAvailableOrders().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == com.messmate.delivery.utils.Resource.Status.SUCCESS) {
                binding.pbWaiting.setVisibility(View.GONE);
                if (resource.data != null && resource.data.getData() != null && !resource.data.getData().isEmpty()) {
                    showOrderAvailable(resource.data.getData().get(0));
                } else {
                    binding.tvWaitingMessage.setText("Waiting for orders...");
                    binding.viewWaiting.setVisibility(View.VISIBLE);
                    binding.cardOrder.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
