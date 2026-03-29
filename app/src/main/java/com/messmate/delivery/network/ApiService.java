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
       🔥 AUTH (FIREBASE LOGIN)
    ============================================================ */

    /**
     * 🔐 Firebase Login
     * Header: Authorization: Bearer <firebase_token>
8102195503     *
     * ⚠️ NOTE:
     * - Interceptor is skipped for this API
     * - Bearer manually add karna hai (OtpActivity me)
     */
    @POST("api/auth/firebase-login")
    Call<LoginResponse> firebaseLogin(
            @Header("Authorization") String firebaseToken
    );


    /* ============================================================
       🚚 DELIVERY APIs (AUTO JWT VIA INTERCEPTOR)
    ============================================================ */

    /**
     * 🟢 GO ONLINE
     * Auth: JWT (Interceptor)
     */
    @POST("api/delivery/go-online")
    Call<GenericResponse> goOnline();


    /**
     * 🔴 GO OFFLINE
     * Auth: JWT (Interceptor)
     */
    @POST("api/delivery/go-offline")
    Call<GenericResponse> goOffline();


    /**
     * 📦 GET AVAILABLE ORDERS
     * Auth: JWT (Interceptor)
     */
    @GET("api/delivery/available-orders")
    Call<AvailableOrdersResponse> getAvailableOrders();


    /**
     * ✅ ACCEPT ORDER
     * Body: { orderId }
     * Auth: JWT (Interceptor)
     */
    @POST("api/delivery/accept-order")
    Call<GenericResponse> acceptOrder(
            @Body Map<String, String> body
    );


    /**
     * 🔄 UPDATE ORDER STATUS
     * Body: OrderStatusRequest
     * Auth: JWT (Interceptor)
     */
    @POST("api/delivery/update-status")
    Call<GenericResponse> updateOrderStatus(
            @Body OrderStatusRequest request
    );


    /**
     * 💰 GET EARNINGS
     * Auth: JWT (Interceptor)
     */
    @GET("api/delivery/earnings")
    Call<EarningsResponse> getEarnings();
}