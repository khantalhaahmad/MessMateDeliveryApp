package com.messmate.delivery.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.messmate.delivery.databinding.ActivityLoginBinding;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;

    // 🔥 ADD THIS (for resend OTP)
    private PhoneAuthProvider.ForceResendingToken resendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        binding.btnLogin.setText("Send OTP");

        binding.btnLogin.setOnClickListener(v -> {

            String phone = binding.etPhone.getText().toString().trim();

            /* -----------------------------
               VALIDATION (SWIGGY STYLE)
            ----------------------------- */

            if (TextUtils.isEmpty(phone)) {
                binding.etPhone.setError("Enter phone number");
                return;
            }

            phone = phone.replace(" ", "");

            if (!phone.matches("\\d+")) {
                binding.etPhone.setError("Only numbers allowed");
                return;
            }

            if (phone.length() < 10) {
                binding.etPhone.setError("Enter valid 10 digit number");
                return;
            }

            if (phone.length() > 10) {
                phone = phone.substring(phone.length() - 10);
            }

            String fullPhone = "+91" + phone;

            sendOtp(fullPhone);
        });
    }

    /* ============================================================
       🔥 SEND OTP
    ============================================================ */

    private void sendOtp(String phone) {

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnLogin.setEnabled(false);

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phone)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(callbacks)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    /* ============================================================
       📩 CALLBACKS
    ============================================================ */

    PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onCodeSent(String verificationId,
                                       PhoneAuthProvider.ForceResendingToken token) {

                    super.onCodeSent(verificationId, token);

                    // 🔥 SAVE TOKEN (important)
                    resendToken = token;

                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnLogin.setEnabled(true);

                    Intent intent = new Intent(LoginActivity.this, OtpActivity.class);
                    intent.putExtra("verificationId", verificationId);
                    intent.putExtra("phone", binding.etPhone.getText().toString());
                    startActivity(intent);
                }

                @Override
                public void onVerificationCompleted(PhoneAuthCredential credential) {
                    // 🔥 AUTO OTP (future use)
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnLogin.setEnabled(true);
                }

                @Override
                public void onVerificationFailed(FirebaseException e) {

                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnLogin.setEnabled(true);

                    Toast.makeText(LoginActivity.this,
                            "OTP Failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            };
}