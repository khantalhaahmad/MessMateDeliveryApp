package com.messmate.delivery.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.Toast;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import java.util.List;
import java.util.ArrayList;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.location.Location;
import android.os.Looper;
import com.google.android.gms.location.Priority;

import android.location.Address;
import android.location.Geocoder;

import java.util.Locale;
import android.graphics.Color;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;

import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.animation.Easing;

import com.messmate.delivery.databinding.FragmentDashboardBinding;
import com.messmate.delivery.models.Order;
import com.messmate.delivery.socket.SocketManager;
import com.messmate.delivery.utils.SharedPreferencesManager;
import com.messmate.delivery.viewmodel.DashboardViewModel;
import com.messmate.delivery.viewmodel.ViewModelFactory;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private DashboardViewModel viewModel;
    private FusedLocationProviderClient fusedLocationClient;
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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        getLiveLocation();
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

        SocketManager.joinRoom("agent_" + agentId);
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
                double today = res.data.getData().getToday();

                Log.d("EARNINGS_DEBUG", "💰 Today Earnings: " + today);

                binding.tvEarnings.setText("₹" + today);

// 🔥 graph call
                setupWeeklyGraph(res.data.getData().getWeekly());
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
    private void setupWeeklyGraph(java.util.List<Double> data) {

        if (data == null || data.isEmpty()) return;

        List<Entry> entries = new ArrayList<>();

        for (int i = 0; i < data.size(); i++) {
            entries.add(new Entry(i, data.get(i).floatValue()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Earnings (₹)");

        // 🎨 Styling
        dataSet.setColor(Color.parseColor("#6C63FF")); // purple
        dataSet.setLineWidth(3f);

        dataSet.setCircleColor(Color.parseColor("#6C63FF"));
        dataSet.setCircleRadius(4f);

        dataSet.setDrawValues(false);

        // Smooth curve
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        // Fill gradient
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#D1C4E9"));

        LineData lineData = new LineData(dataSet);
        binding.chart.setData(lineData);

        // ❌ Remove junk UI
        binding.chart.getDescription().setEnabled(false);
        binding.chart.getLegend().setEnabled(true);
        binding.chart.getLegend().setTextSize(12f);
        binding.chart.setTouchEnabled(true);
        binding.chart.setPinchZoom(true);
        binding.chart.setHighlightPerTapEnabled(true);
        binding.chart.setTouchEnabled(true);
        binding.chart.setPinchZoom(true);
        binding.chart.setHighlightPerTapEnabled(true);
        binding.chart.getAxisRight().setEnabled(false);

        // ✅ X AXIS (IMPORTANT)
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

        XAxis xAxis = binding.chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(days));
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.GRAY);

        // ✅ Y AXIS (₹)
        YAxis yAxis = binding.chart.getAxisLeft();
        yAxis.setTextColor(Color.GRAY);
        yAxis.setDrawGridLines(true);

        // 🎯 Animation
        binding.chart.animateX(1200, Easing.EaseInOutQuad);

        binding.chart.invalidate();
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

        // 🔥 BASIC INFO
        binding.tvOrderMessName.setText(order.getMessName());
        binding.tvOrderFee.setText("₹" + order.getDeliveryFee());

        // 🔥 CUSTOMER INFO
        binding.tvCustomerName.setText(order.getCustomerName());
        binding.tvCustomerPhone.setText(order.getCustomerPhone());

        // 🔥 PAYMENT
        binding.tvPayment.setText(order.getPaymentMethod());

        // 🔥 ADDRESS (SAFE FALLBACK)
        if (order.getPickupLocation() != null && order.getPickupLocation().getAddress() != null) {
            binding.tvPickupLoc.setText("📍 " + order.getPickupLocation().getAddress());
        } else {
            binding.tvPickupLoc.setText("📍 " + order.getPickupAddress());
        }

        if (order.getDropLocation() != null && order.getDropLocation().getAddress() != null) {
            binding.tvDropLoc.setText("🏁 " + order.getDropLocation().getAddress());
        } else {
            binding.tvDropLoc.setText("🏁 " + order.getDropAddress());
        }

        // 🔥 TIMER (FIXED)
        long timeLeft = order.getExpiresAt() - System.currentTimeMillis();

        if (timeLeft > 0) {
            new android.os.CountDownTimer(timeLeft, 1000) {

                public void onTick(long millisUntilFinished) {
                    binding.tvTimer.setText((millisUntilFinished / 1000) + "s");
                }

                public void onFinish() {
                    resetToWaiting();
                }

            }.start();
        }

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

    private void getLiveLocation() {

        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 5000
        ).build();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);

        fusedLocationClient.requestLocationUpdates(locationRequest,
                new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {

                        if (locationResult == null) return;

                        Location location = locationResult.getLastLocation();

                        if (location != null) {

                            double lat = location.getLatitude();
                            double lng = location.getLongitude();

                            Log.d("LOCATION_DEBUG", "📍 Lat: " + lat + ", Lng: " + lng);

                            try {
                                Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);

                                if (addresses != null && !addresses.isEmpty()) {

                                    Address address = addresses.get(0);

                                    String area = address.getSubLocality();
                                    String city = address.getLocality();

                                    if (area == null) area = "";
                                    if (city == null) city = "";

                                    String locationText = "📍 " + area + ", " + city;

                                    binding.tvLocation.setText(locationText);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            // 🔥 IMPORTANT: stop updates after getting location
                            fusedLocationClient.removeLocationUpdates(this);
                        }
                    }
                },
                Looper.getMainLooper());
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("SOCKET_DEBUG", "🧹 Remove Listeners");
        SocketManager.removeListeners();
        binding = null;
    }
}