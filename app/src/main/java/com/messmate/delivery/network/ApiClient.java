package com.messmate.delivery.network;

import android.content.Context;

import com.messmate.delivery.utils.Constants;
import com.messmate.delivery.utils.SharedPreferencesManager;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static Retrofit retrofit = null;

    /* ============================================================
       🔥 GET CLIENT (SINGLETON)
    ============================================================ */

    public static Retrofit getClient(Context context) {

        // ✅ Reuse existing instance (performance optimization)
        if (retrofit != null) {
            return retrofit;
        }

        // 🔥 Use application context (avoid memory leak)
        Context appContext = context.getApplicationContext();

        SharedPreferencesManager prefsManager = new SharedPreferencesManager(appContext);
        AuthInterceptor authInterceptor = new AuthInterceptor(prefsManager);

        /* 🔥 LOGGING (ONLY FOR DEBUG) */
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)         // 🔐 Token attach
                .addInterceptor(loggingInterceptor)      // 🐞 Logs
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)          // 🔥 retry support
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit;
    }

    /* ============================================================
       🔥 CLEAR CLIENT (VERY IMPORTANT)
    ============================================================ */

    public static void clearClient() {
        retrofit = null;
    }
}