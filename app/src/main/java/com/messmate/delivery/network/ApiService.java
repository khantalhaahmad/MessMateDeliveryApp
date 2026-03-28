package com.messmate.delivery.network;

import com.messmate.delivery.models.AvailableOrdersResponse;
import com.messmate.delivery.models.EarningsResponse;
import com.messmate.delivery.models.GenericResponse;
import com.messmate.delivery.models.LoginResponse;
import com.messmate.delivery.models.OrderStatusRequest;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {

    /* ============================================================
       🔥 FIREBASE LOGIN (TOKEN FROM FIREBASE)
    ============================================================ */

    @POST("auth/firebase-login")
    Call<LoginResponse> firebaseLogin(
            @Header("Authorization") String firebaseToken
    );

    /* ============================================================
       🚚 DELIVERY APIs (JWT VIA INTERCEPTOR)
    ============================================================ */

    @POST("delivery/go-online")
    Call<GenericResponse> goOnline();

    @POST("delivery/go-offline")
    Call<GenericResponse> goOffline();

    @GET("delivery/available-orders")
    Call<AvailableOrdersResponse> getAvailableOrders();

    @POST("delivery/accept-order")
    Call<GenericResponse> acceptOrder(@Body Map<String, String> body);

    @POST("delivery/update-status")
    Call<GenericResponse> updateOrderStatus(@Body OrderStatusRequest request);

    @GET("delivery/earnings")
    Call<EarningsResponse> getEarnings();
}