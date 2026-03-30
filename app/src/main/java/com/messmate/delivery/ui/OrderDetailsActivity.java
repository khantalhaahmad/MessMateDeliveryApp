package com.messmate.delivery.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.messmate.delivery.databinding.ActivityOrderDetailsBinding;
import com.messmate.delivery.viewmodel.OrderDetailsViewModel;
import com.messmate.delivery.viewmodel.ViewModelFactory;

public class OrderDetailsActivity extends AppCompatActivity {

    private ActivityOrderDetailsBinding binding;
    private OrderDetailsViewModel viewModel;
    private String orderId;

    private String currentStatus = "ACCEPTED";

    private double restaurantLat = 28.6139;
    private double restaurantLng = 77.2090;

    private double customerLat = 28.5355;
    private double customerLng = 77.3910;

    private static final String TAG = "ORDER_DEBUG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this, new ViewModelFactory(this))
                .get(OrderDetailsViewModel.class);

        orderId = getIntent().getStringExtra("orderId");

        Log.d(TAG, "Order ID: " + orderId);

        String items = getIntent().getStringExtra("items");
        String locations = getIntent().getStringExtra("locations");

        binding.tvOrderIdTitle.setText("Order #" + orderId);
        binding.tvItems.setText("Items: " + (items != null ? items : "Standard Package"));
        binding.tvLocations.setText(locations != null ? locations : "Restaurant → Customer");

        setupClicks();
        updateUI(currentStatus);
    }

    private void setupClicks() {

        binding.btnNavigateRestaurant.setOnClickListener(v -> {
            Log.d(TAG, "Navigate Restaurant");
            openMap(restaurantLat, restaurantLng);
        });

        binding.btnReached.setOnClickListener(v -> {
            Log.d(TAG, "Clicked REACHED");
            updateStatus("REACHED_RESTAURANT");
        });

        binding.btnPickedUp.setOnClickListener(v -> {
            Log.d(TAG, "Clicked PICKED_UP");
            updateStatus("PICKED_UP");
        });

        binding.btnNavigateCustomer.setOnClickListener(v -> {
            Log.d(TAG, "Navigate Customer");
            openMap(customerLat, customerLng);
        });

        binding.btnOut.setOnClickListener(v -> {
            Log.d(TAG, "Clicked OUT_FOR_DELIVERY");
            updateStatus("OUT_FOR_DELIVERY");
        });

        binding.btnDelivered.setOnClickListener(v -> {
            Log.d(TAG, "Clicked DELIVERED");
            updateStatus("DELIVERED");
        });
    }

    // 🔥 MAIN CONTROL FUNCTION
    private void updateUI(String status) {

        Log.d(TAG, "UI State → " + status);

        // reset everything
        hideAllButtons();
        enableAllButtons();

        switch (status) {

            case "ACCEPTED":
                binding.btnNavigateRestaurant.setVisibility(View.VISIBLE);
                binding.btnReached.setVisibility(View.VISIBLE);
                binding.tvStatus.setText("Go to Restaurant");
                break;

            case "REACHED_RESTAURANT":
                binding.btnPickedUp.setVisibility(View.VISIBLE);
                binding.tvStatus.setText("Reached Restaurant");
                break;

            case "PICKED_UP":
                binding.btnNavigateCustomer.setVisibility(View.VISIBLE);
                binding.btnOut.setVisibility(View.VISIBLE);
                binding.tvStatus.setText("Heading to Customer");
                break;

            case "OUT_FOR_DELIVERY":
                binding.btnDelivered.setVisibility(View.VISIBLE);
                binding.tvStatus.setText("Delivering...");
                break;

            case "DELIVERED":
                binding.tvStatus.setText("Delivered ✅");
                break;
        }
    }

    private void updateStatus(String status) {

        Log.d(TAG, "API CALL → " + status);

        viewModel.updateOrderStatus(orderId, status).observe(this, res -> {

            switch (res.status) {

                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    disableAllButtons();
                    break;

                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);

                    Log.d(TAG, "SUCCESS → " + status);

                    currentStatus = status;
                    updateUI(currentStatus);

                    Toast.makeText(this, status, Toast.LENGTH_SHORT).show();

                    if (status.equals("DELIVERED")) finish();
                    break;

                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);

                    Log.e(TAG, "ERROR → " + res.message);

                    updateUI(currentStatus);

                    Toast.makeText(this, res.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    // 🔥 HELPERS

    private void hideAllButtons() {
        binding.btnNavigateRestaurant.setVisibility(View.GONE);
        binding.btnReached.setVisibility(View.GONE);
        binding.btnPickedUp.setVisibility(View.GONE);
        binding.btnNavigateCustomer.setVisibility(View.GONE);
        binding.btnOut.setVisibility(View.GONE);
        binding.btnDelivered.setVisibility(View.GONE);
    }

    private void enableAllButtons() {
        binding.btnReached.setEnabled(true);
        binding.btnPickedUp.setEnabled(true);
        binding.btnOut.setEnabled(true);
        binding.btnDelivered.setEnabled(true);
    }

    private void disableAllButtons() {
        binding.btnReached.setEnabled(false);
        binding.btnPickedUp.setEnabled(false);
        binding.btnOut.setEnabled(false);
        binding.btnDelivered.setEnabled(false);
    }

    private void openMap(double lat, double lng) {
        Uri uri = Uri.parse("google.navigation:q=" + lat + "," + lng);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.google.android.apps.maps");
        startActivity(intent);
    }
}