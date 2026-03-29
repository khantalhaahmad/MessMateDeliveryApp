package com.messmate.delivery.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.Toast;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.messmate.delivery.databinding.FragmentDashboardBinding;
import com.messmate.delivery.models.Order;
import com.messmate.delivery.socket.SocketManager;
import com.messmate.delivery.utils.SharedPreferencesManager;
import com.messmate.delivery.viewmodel.DashboardViewModel;
import com.messmate.delivery.viewmodel.ViewModelFactory;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private DashboardViewModel viewModel;

    private Order currentOrder = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d("DASHBOARD_DEBUG", "🚀 Dashboard Loaded");

        initUI();
        initViewModel();
        setupSocket();
        setupObservers();
        setupListeners();

        loadData();
    }

    // ================= INIT =================
    private void initUI() {
        SharedPreferencesManager prefs = new SharedPreferencesManager(requireContext());

        String name = prefs.getAgentName();
        Log.d("PREF_DEBUG", "👤 Agent Name: " + name);

        binding.tvGreeting.setText("👋 " + name);
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this,
                new ViewModelFactory(requireContext()))
                .get(DashboardViewModel.class);

        Log.d("DASHBOARD_DEBUG", "✅ ViewModel Initialized");
    }

    // ================= SOCKET =================
    private void setupSocket() {

        String agentId = new SharedPreferencesManager(requireContext()).getUserId();

        Log.d("SOCKET_DEBUG", "🔌 AgentId: " + agentId);

        SocketManager.connect();
        Log.d("SOCKET_DEBUG", "📡 Socket Connected");

        SocketManager.joinRoom("delivery_" + agentId);
        Log.d("SOCKET_DEBUG", "🏠 Joined Room: delivery_" + agentId);

        SocketManager.onNewOrder(order -> {
            requireActivity().runOnUiThread(() -> {

                Log.d("SOCKET_DEBUG", "🆕 New Order: " + order.getId());

                NewOrderDialog dialog = new NewOrderDialog(order, new NewOrderDialog.OnActionListener() {
                    @Override
                    public void onAccept(Order o) {
                        Log.d("ORDER_DEBUG", "✅ Accept Clicked: " + o.getId());
                        acceptOrder(o);
                    }

                    @Override
                    public void onReject(Order o) {
                        Log.d("ORDER_DEBUG", "❌ Rejected: " + o.getId());
                        Toast.makeText(requireContext(), "Order Rejected", Toast.LENGTH_SHORT).show();
                    }
                });

                dialog.show(getParentFragmentManager(), "NEW_ORDER");
            });
        });

        SocketManager.onOrderTaken(orderId -> {
            requireActivity().runOnUiThread(() -> {

                Log.d("SOCKET_DEBUG", "⚠️ Order taken by other: " + orderId);

                if (currentOrder != null && currentOrder.getId().equals(orderId)) {
                    resetToWaiting();
                }
            });
        });
    }

    // ================= OBSERVERS =================
    private void setupObservers() {

        viewModel.getEarnings().observe(getViewLifecycleOwner(), res -> {

            Log.d("EARNINGS_DEBUG", "📡 Earnings Status: " + res.status);

            if (res.status == com.messmate.delivery.utils.Resource.Status.SUCCESS && res.data != null) {
                Log.d("EARNINGS_DEBUG", "💰 Earnings: " + res.data.getTotalEarnings());
                binding.tvEarnings.setText("₹" + res.data.getTotalEarnings());
            }
        });

        viewModel.getAvailableOrders().observe(getViewLifecycleOwner(), res -> {

            Log.d("ORDER_DEBUG", "📡 Orders Status: " + res.status);

            switch (res.status) {

                case LOADING:
                    Log.d("ORDER_DEBUG", "⏳ Loading...");
                    binding.pbWaiting.setVisibility(View.VISIBLE);
                    break;

                case SUCCESS:
                    binding.pbWaiting.setVisibility(View.GONE);

                    if (res.data != null && res.data.getData() != null && !res.data.getData().isEmpty()) {

                        currentOrder = res.data.getData().get(0);

                        Log.d("ORDER_DEBUG", "✅ Order Found: " + currentOrder.getId());

                        showOrder(currentOrder);

                    } else {
                        Log.d("ORDER_DEBUG", "⚠️ No Orders");
                        resetToWaiting();
                    }
                    break;

                case ERROR:
                    binding.pbWaiting.setVisibility(View.GONE);

                    Log.e("ORDER_DEBUG", "❌ Load Failed: " + res.message);

                    binding.tvWaitingMessage.setText("⚠️ Failed to load orders");
                    break;
            }
        });
    }

    // ================= LISTENERS =================
    private void setupListeners() {

        binding.switchOnline.setOnCheckedChangeListener((btn, isChecked) -> {

            Log.d("TOGGLE_DEBUG", "🖱️ Switch: " + isChecked + " | Pressed: " + btn.isPressed());

            if (btn.isPressed()) toggleOnline(isChecked);
        });

        binding.btnOrderAction.setOnClickListener(v -> {

            if (currentOrder == null) return;

            Log.d("ORDER_DEBUG", "⚡ Button Click: " + currentOrder.getId());

            if (currentOrder.getDeliveryStatus() == null) {
                acceptOrder(currentOrder);
            } else {
                updateStatus();
            }
        });
    }

    // ================= LOAD =================
    private void loadData() {
        Log.d("DASHBOARD_DEBUG", "📥 Fetching data...");
        viewModel.fetchEarnings();
        viewModel.fetchAvailableOrders();
    }

    // ================= UI =================
    private void showOrder(Order order) {

        Log.d("UI_DEBUG", "📦 Show Order: " + (order != null ? order.getId() : "null"));

        if (order == null) return;

        binding.viewWaiting.setVisibility(View.GONE);
        binding.cardOrder.setVisibility(View.VISIBLE);

        binding.tvOrderMessName.setText(order.getMessName());
        binding.tvOrderFee.setText("₹" + order.getDeliveryFee());

        if (order.getPickupLocation() != null)
            binding.tvPickupLoc.setText("📍 " + order.getPickupLocation().getAddress());

        if (order.getDropLocation() != null)
            binding.tvDropLoc.setText("🏁 " + order.getDropLocation().getAddress());

        updateButton(order);
    }

    private void resetToWaiting() {
        Log.d("UI_DEBUG", "🔄 Reset to waiting");
        currentOrder = null;
        binding.cardOrder.setVisibility(View.GONE);
        binding.viewWaiting.setVisibility(View.VISIBLE);
    }

    private void updateButton(Order order) {

        Log.d("UI_DEBUG", "🔘 Update Button: " + order.getDeliveryStatus());

        if (order.getDeliveryStatus() == null) {
            binding.btnOrderAction.setText("Accept Order");
            return;
        }

        switch (order.getDeliveryStatus()) {
            case "ACCEPTED": binding.btnOrderAction.setText("Go to Pickup"); break;
            case "REACHED_RESTAURANT": binding.btnOrderAction.setText("Picked"); break;
            case "PICKED_UP": binding.btnOrderAction.setText("Out for Delivery"); break;
            case "OUT_FOR_DELIVERY": binding.btnOrderAction.setText("Delivered"); break;
        }
    }

    // ================= ACTIONS =================
    private void acceptOrder(Order order) {

        Log.d("ORDER_DEBUG", "📥 Accept Order: " + order.getId());

        viewModel.acceptOrder(order.getId()).observe(getViewLifecycleOwner(), res -> {

            Log.d("ORDER_DEBUG", "📡 Accept Status: " + res.status);

            if (res.status == com.messmate.delivery.utils.Resource.Status.SUCCESS) {

                currentOrder = order;
                currentOrder.setDeliveryStatus("ACCEPTED");

                showOrder(currentOrder);

            } else if (res.status == com.messmate.delivery.utils.Resource.Status.ERROR) {

                Log.e("ORDER_DEBUG", "❌ Accept Failed: " + res.message);

                Toast.makeText(requireContext(), res.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStatus() {

        String next = getNextStatus(currentOrder.getDeliveryStatus());

        Log.d("ORDER_DEBUG", "🔄 Next Status: " + next);

        viewModel.updateOrderStatus(currentOrder.getId(), next)
                .observe(getViewLifecycleOwner(), res -> {

                    Log.d("ORDER_DEBUG", "📡 Update Status: " + res.status);

                    if (res.status == com.messmate.delivery.utils.Resource.Status.SUCCESS) {

                        if ("DELIVERED".equals(next)) {
                            Log.d("ORDER_DEBUG", "🎉 Delivered");
                            resetToWaiting();
                            viewModel.fetchEarnings();
                            viewModel.fetchAvailableOrders();
                        } else {
                            currentOrder.setDeliveryStatus(next);
                            showOrder(currentOrder);
                        }

                    } else {

                        Log.e("ORDER_DEBUG", "❌ Update Failed: " + res.message);

                        Toast.makeText(requireContext(), res.message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getNextStatus(String s) {

        Log.d("ORDER_DEBUG", "📊 Current Status: " + s);

        if (s == null) return "ACCEPTED";

        switch (s) {
            case "ACCEPTED": return "REACHED_RESTAURANT";
            case "REACHED_RESTAURANT": return "PICKED_UP";
            case "PICKED_UP": return "OUT_FOR_DELIVERY";
            case "OUT_FOR_DELIVERY": return "DELIVERED";
        }
        return null;
    }

    private void toggleOnline(boolean online) {

        Log.d("TOGGLE_DEBUG", "🔘 Toggle Clicked: " + online);

        viewModel.toggleStatus(online).observe(getViewLifecycleOwner(), res -> {

            Log.d("TOGGLE_DEBUG", "📡 Toggle Status: " + res.status);

            if (res.status == com.messmate.delivery.utils.Resource.Status.ERROR) {

                Log.e("TOGGLE_DEBUG", "❌ Toggle Failed: " + res.message);

                binding.switchOnline.setChecked(!online);

                Toast.makeText(requireContext(), res.message, Toast.LENGTH_SHORT).show();
            } else {
                Log.d("TOGGLE_DEBUG", "✅ Toggle Success");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("SOCKET_DEBUG", "🧹 Remove Listeners");
        SocketManager.removeListeners();
        binding = null;
    }
}