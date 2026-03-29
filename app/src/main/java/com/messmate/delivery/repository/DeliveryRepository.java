package com.messmate.delivery.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.messmate.delivery.models.AvailableOrdersResponse;
import com.messmate.delivery.models.EarningsResponse;
import com.messmate.delivery.models.GenericResponse;
import com.messmate.delivery.models.OrderStatusRequest;
import com.messmate.delivery.network.ApiService;
import com.messmate.delivery.utils.Resource;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeliveryRepository {

    private static final String TAG = "DeliveryRepository";

    private final ApiService apiService;

    public DeliveryRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    // ================= GENERIC HANDLER =================
    private <T> LiveData<Resource<T>> execute(Call<T> call) {

        MutableLiveData<Resource<T>> liveData = new MutableLiveData<>();
        liveData.setValue(Resource.loading(null));

        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {

                if (response.isSuccessful() && response.body() != null) {
                    liveData.setValue(Resource.success(response.body()));
                } else {
                    String errorMsg = "API Error: " + response.code();

                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Log.e(TAG, errorMsg);
                    liveData.setValue(Resource.error(errorMsg, null));
                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                Log.e(TAG, "Network Error: " + t.getMessage());
                liveData.setValue(Resource.error(t.getMessage(), null));
            }
        });

        return liveData;
    }

    /* ================= ONLINE / OFFLINE ================= */
    public LiveData<Resource<GenericResponse>> toggleStatus(boolean online) {
        Call<GenericResponse> call = online
                ? apiService.goOnline()
                : apiService.goOffline();

        return execute(call);
    }

    /* ================= AVAILABLE ORDERS ================= */
    public LiveData<Resource<AvailableOrdersResponse>> getAvailableOrders() {
        return execute(apiService.getAvailableOrders());
    }

    /* ================= ACCEPT ORDER ================= */
    public LiveData<Resource<GenericResponse>> acceptOrder(String orderId) {

        Map<String, String> body = new HashMap<>();
        body.put("orderId", orderId);

        return execute(apiService.acceptOrder(body));
    }

    /* ================= UPDATE STATUS ================= */
    public LiveData<Resource<GenericResponse>> updateOrderStatus(String orderId, String status) {

        OrderStatusRequest request = new OrderStatusRequest(orderId, status);

        return execute(apiService.updateOrderStatus(request));
    }

    /* ================= EARNINGS ================= */
    public LiveData<Resource<EarningsResponse>> getEarnings() {
        return execute(apiService.getEarnings());
    }
}