package com.messmate.delivery.network;

import android.util.Log;

import com.messmate.delivery.utils.SharedPreferencesManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final SharedPreferencesManager prefsManager;

    public AuthInterceptor(SharedPreferencesManager prefsManager) {
        this.prefsManager = prefsManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        Request originalRequest = chain.request();
        String path = originalRequest.url().encodedPath();

        String token = prefsManager.getToken();

        // 🔍 DEBUG: request info
        Log.d("AUTH_DEBUG", "➡️ URL: " + originalRequest.url());
        Log.d("AUTH_DEBUG", "➡️ Path: " + path);
        Log.d("AUTH_DEBUG", "➡️ Token from prefs: " + token);

        /* ============================================================
           🔥 1. SKIP FIREBASE LOGIN (VERY IMPORTANT)
        ============================================================ */
        if (path.contains("firebase-login")) {
            Log.d("AUTH_DEBUG", "⏭️ Skipping interceptor (Firebase login)");
            return chain.proceed(originalRequest);
        }

        /* ============================================================
           🔐 2. ATTACH JWT TOKEN
        ============================================================ */
        if (token != null && !token.isEmpty()) {

            String finalToken = "Bearer " + token;

            Log.d("AUTH_DEBUG", "✅ Adding Authorization Header");

            Request newRequest = originalRequest.newBuilder()
                    .header("Authorization", finalToken) // 🔥 use header (replace)
                    .build();

            Response response = chain.proceed(newRequest);

            Log.d("AUTH_DEBUG", "⬅️ Response Code: " + response.code());

            return response;
        }

        /* ============================================================
           ❌ 3. NO TOKEN CASE
        ============================================================ */
        Log.e("AUTH_DEBUG", "❌ No token found → sending without auth");

        Response response = chain.proceed(originalRequest);

        Log.d("AUTH_DEBUG", "⬅️ Response Code (No Token): " + response.code());

        return response;
    }
}