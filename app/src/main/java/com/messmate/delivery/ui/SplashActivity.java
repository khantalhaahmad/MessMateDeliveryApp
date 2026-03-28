package com.messmate.delivery.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.messmate.delivery.databinding.ActivitySplashBinding;
import com.messmate.delivery.utils.SharedPreferencesManager;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 🔥 Delay for splash screen (1.5 sec)
        new Handler(Looper.getMainLooper()).postDelayed(this::checkLogin, 1500);
    }

    /* ============================================================
       🔐 CHECK LOGIN STATE
    ============================================================ */

    private void checkLogin() {

        SharedPreferencesManager prefs = new SharedPreferencesManager(this);

        // ✅ Clean check (production safe)
        if (prefs.getToken() != null && !prefs.getToken().trim().isEmpty()) {

            // 🔥 Already logged in → go to dashboard
            startActivity(new Intent(this, MainActivity.class));

        } else {

            // ❌ Not logged in → go to login
            startActivity(new Intent(this, LoginActivity.class));
        }

        finish();
    }
}