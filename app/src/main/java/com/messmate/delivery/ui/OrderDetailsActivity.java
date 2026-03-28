package com.messmate.delivery.ui;

import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewModelFactory factory = new ViewModelFactory(this);
        viewModel = new ViewModelProvider(this, factory).get(OrderDetailsViewModel.class);

        orderId = getIntent().getStringExtra("orderId");
        String items = getIntent().getStringExtra("items");
        String locations = getIntent().getStringExtra("locations");

        binding.tvOrderIdTitle.setText("Order #" + orderId);
        binding.tvItems.setText("Items: " + (items != null ? items : "Standard Package"));
        binding.tvLocations.setText(locations != null ? locations : "Unknown Locations");

        binding.btnReached.setOnClickListener(v -> updateStatus("Reached Restaurant"));
        binding.btnPickedUp.setOnClickListener(v -> updateStatus("Picked Up"));
        binding.btnOut.setOnClickListener(v -> updateStatus("Out for Delivery"));
        binding.btnDelivered.setOnClickListener(v -> updateStatus("Delivered"));
    }

    private void updateStatus(String status) {
        viewModel.updateOrderStatus(orderId, status).observe(this, resource -> {
             switch (resource.status) {
                 case LOADING:
                     binding.progressBar.setVisibility(View.VISIBLE);
                     disableButtons();
                     break;
                 case SUCCESS:
                     binding.progressBar.setVisibility(View.GONE);
                     enableButtons();
                     Toast.makeText(this, "Status Updated: " + status, Toast.LENGTH_SHORT).show();
                     if ("Delivered".equals(status)) {
                         finish(); // Close activity once delivered
                     }
                     break;
                 case ERROR:
                     binding.progressBar.setVisibility(View.GONE);
                     enableButtons();
                     Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show();
                     break;
             }
        });
    }

    private void disableButtons() {
        binding.btnReached.setEnabled(false);
        binding.btnPickedUp.setEnabled(false);
        binding.btnOut.setEnabled(false);
        binding.btnDelivered.setEnabled(false);
    }

    private void enableButtons() {
        binding.btnReached.setEnabled(true);
        binding.btnPickedUp.setEnabled(true);
        binding.btnOut.setEnabled(true);
        binding.btnDelivered.setEnabled(true);
    }
}
