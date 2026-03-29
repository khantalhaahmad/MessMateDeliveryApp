package com.messmate.delivery.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.messmate.delivery.databinding.ActivityOtpBinding;
import com.messmate.delivery.models.LoginResponse;
import com.messmate.delivery.network.ApiClient;
import com.messmate.delivery.network.ApiService;
import com.messmate.delivery.utils.SharedPreferencesManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtpActivity extends AppCompatActivity {

    private ActivityOtpBinding binding;

    private String verificationId;
    private String phone;

    private FirebaseAuth mAuth;
    private SharedPreferencesManager prefsManager;

    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        prefsManager = new SharedPreferencesManager(this);

        verificationId = getIntent().getStringExtra("verificationId");
        phone = getIntent().getStringExtra("phone");

        binding.tvPhone.setText("+91 " + phone);

        startResendTimer();

        binding.btnVerify.setOnClickListener(v -> {

            String otp = binding.etOtp.getText().toString().trim();

            if (otp.length() < 6) {
                binding.etOtp.setError("Enter valid OTP");
                return;
            }

            verifyOtp(otp);
        });

        binding.tvResend.setOnClickListener(v -> {
            Toast.makeText(this, "Resending OTP...", Toast.LENGTH_SHORT).show();
            // 👉 future: resend OTP API call
        });
    }

    /* ================= OTP VERIFY ================= */

    private void verifyOtp(String otp) {

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnVerify.setEnabled(false);

        PhoneAuthCredential credential =
                PhoneAuthProvider.getCredential(verificationId, otp);

        signInWithCredential(credential);
    }

    /* ================= FIREBASE LOGIN ================= */

    private void signInWithCredential(PhoneAuthCredential credential) {

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        getFirebaseToken();
                    } else {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnVerify.setEnabled(true);
                        Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /* ================= GET FIREBASE TOKEN ================= */

    private void getFirebaseToken() {

        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnVerify.setEnabled(true);
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            return;
        }

        user.getIdToken(true)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        String idToken = task.getResult().getToken();
                        sendToBackend(idToken);
                    } else {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnVerify.setEnabled(true);
                        Toast.makeText(this, "Token failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /* ================= BACKEND LOGIN ================= */

    private void sendToBackend(String firebaseToken) {

        Log.d("LOGIN_DEBUG", "🔥 Firebase Token: " + firebaseToken);

        ApiService api = ApiClient.getClient(this).create(ApiService.class);

        // ✅ FIX: NO Bearer here
        Log.d("LOGIN_DEBUG", "🚀 Sending Bearer Token");
        Call<LoginResponse> call =
                api.firebaseLogin("Bearer " + firebaseToken);

        Log.d("LOGIN_DEBUG", "📡 Sending login request to backend...");

        call.enqueue(new Callback<LoginResponse>() {

            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {

                binding.progressBar.setVisibility(View.GONE);
                binding.btnVerify.setEnabled(true);

                Log.d("LOGIN_DEBUG", "⬅️ Response Code: " + response.code());

                if (response.errorBody() != null) {
                    try {
                        Log.e("LOGIN_DEBUG", "❌ Error Body: " + response.errorBody().string());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (response.isSuccessful() && response.body() != null) {

                    LoginResponse data = response.body();

                    Log.d("LOGIN_DEBUG", "✅ API Success: " + data.isSuccess());
                    Log.d("LOGIN_DEBUG", "📩 Message: " + data.getMessage());
                    Log.d("LOGIN_DEBUG", "🔐 Token from backend: " + data.getToken());

                    if (!data.isSuccess()) {
                        Toast.makeText(OtpActivity.this,
                                data.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    /* ✅ SAVE TOKEN */
                    prefsManager.saveToken(data.getToken());

                    Log.d("LOGIN_DEBUG", "💾 Token Saved Successfully");

                    // 🔥 IMPORTANT: refresh API client
                    ApiClient.clearClient();

                    /* ✅ SAVE AGENT */
                    if (data.getAgent() != null) {
                        Log.d("LOGIN_DEBUG", "👤 Agent ID: " + data.getAgent().getId());
                        Log.d("LOGIN_DEBUG", "👤 Agent Name: " + data.getAgent().getName());

                        prefsManager.saveAgentId(data.getAgent().getId());
                        prefsManager.saveAgentName(data.getAgent().getName());
                    } else {
                        Log.e("LOGIN_DEBUG", "❌ Agent is NULL");
                    }

                    /* ✅ SAVE ROLE */
                    prefsManager.saveRole("delivery");
                    Log.d("LOGIN_DEBUG", "🎭 Role Saved: delivery");

                    /* 🚀 NAVIGATE */
                    startActivity(new Intent(OtpActivity.this, MainActivity.class));
                    finish();

                } else {
                    Log.e("LOGIN_DEBUG", "❌ Login failed (response not successful)");
                    Toast.makeText(OtpActivity.this,
                            "Login failed",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {

                binding.progressBar.setVisibility(View.GONE);
                binding.btnVerify.setEnabled(true);

                Log.e("LOGIN_DEBUG", "💥 API FAILURE: " + t.getMessage());

                Toast.makeText(OtpActivity.this,
                        "Network error",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /* ================= RESEND TIMER ================= */

    private void startResendTimer() {

        binding.tvResend.setEnabled(false);

        timer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                binding.tvResend.setText("Resend in " + millisUntilFinished / 1000 + "s");
            }

            @Override
            public void onFinish() {
                binding.tvResend.setEnabled(true);
                binding.tvResend.setText("Resend OTP");
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
    }
}