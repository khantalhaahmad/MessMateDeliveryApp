package com.messmate.delivery.network;

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

        String token = prefsManager.getToken();

        // ✅ Skip adding token for Firebase login API
        if (originalRequest.url().encodedPath().contains("auth/firebase-login")) {
            return chain.proceed(originalRequest);
        }

        // ✅ Attach JWT automatically
        if (token != null && !token.isEmpty()) {
            Request newRequest = originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer " + token)
                    .build();
            return chain.proceed(newRequest);
        }

        return chain.proceed(originalRequest);
    }
}