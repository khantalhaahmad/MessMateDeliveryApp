package com.messmate.delivery.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.OvershootInterpolator;

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

        // 🔥 Start animation
        startLogoAnimation();

        // 🔥 Delay for splash screen (1.5 sec)
        new Handler(Looper.getMainLooper()).postDelayed(this::checkLogin, 1500);
    }

    /* ============================================================
       🎬 LOGO ANIMATION
    ============================================================ */

    private void startLogoAnimation() {

        // Start small
        binding.logo.setScaleX(0f);
        binding.logo.setScaleY(0f);
        binding.logo.setAlpha(0f);

        // Animate
        binding.logo.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(800)
                .setInterpolator(new OvershootInterpolator())
                .start();

        // Optional: fade in text
        binding.appName.setAlpha(0f);
        binding.appName.animate()
                .alpha(1f)
                .setDuration(600)
                .setStartDelay(300)
                .start();

        binding.tvDelivery.setAlpha(0f);
        binding.tvDelivery.animate()
                .alpha(1f)
                .setDuration(600)
                .setStartDelay(500)
                .start();
    }

    /* ============================================================
       🔐 CHECK LOGIN STATE
    ============================================================ */

    private void checkLogin() {

        SharedPreferencesManager prefs = new SharedPreferencesManager(this);

        if (prefs.getToken() != null && !prefs.getToken().trim().isEmpty()) {

            // ✅ Already logged in
            startActivity(new Intent(this, MainActivity.class));

        } else {

            // ❌ Not logged in
            startActivity(new Intent(this, LoginActivity.class));
        }

        finish();
    }
}