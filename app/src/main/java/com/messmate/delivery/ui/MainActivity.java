package com.messmate.delivery.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.messmate.delivery.R;
import com.messmate.delivery.databinding.ActivityMainBinding;
import com.messmate.delivery.socket.SocketManager;
import com.messmate.delivery.utils.SharedPreferencesManager;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SocketManager socketManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
        }

        setupSocket();
    }

    private void setupSocket() {
        socketManager = SocketManager.getInstance();
        socketManager.connect();
        Socket mSocket = socketManager.getSocket();

        SharedPreferencesManager prefs = new SharedPreferencesManager(this);
        String agentId = prefs.getAgentId();

        mSocket.on(Socket.EVENT_CONNECT, args -> {
            try {
                JSONObject obj = new JSONObject();
                obj.put("agentId", agentId);
                mSocket.emit("join_delivery", agentId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        mSocket.on("new_order", args -> {
            runOnUiThread(() -> {
                Toast.makeText(this, "New Order Received! Check Dashboard.", Toast.LENGTH_LONG).show();
            });
        });

        mSocket.on("order_accepted", args -> {
            runOnUiThread(() -> {
                Toast.makeText(this, "Order list updated", Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (socketManager != null) {
            socketManager.disconnect();
        }
    }
}
