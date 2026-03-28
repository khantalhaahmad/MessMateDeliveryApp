package com.messmate.delivery.repository;

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

    private final ApiService apiService;

    public DeliveryRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    /* ===============================
       🟢 ONLINE / OFFLINE
    =============================== */
    public LiveData<Resource<GenericResponse>> toggleStatus(boolean online) {
        MutableLiveData<Resource<GenericResponse>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));

        Call<GenericResponse> call = online
                ? apiService.goOnline()
                : apiService.goOffline();

        call.enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.setValue(Resource.success(response.body()));
                } else {
                    data.setValue(Resource.error("Failed to update status", null));
                }
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });

        return data;
    }

    /* ===============================
       📦 AVAILABLE ORDERS
    =============================== */
    public LiveData<Resource<AvailableOrdersResponse>> getAvailableOrders() {
        MutableLiveData<Resource<AvailableOrdersResponse>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));

        apiService.getAvailableOrders().enqueue(new Callback<AvailableOrdersResponse>() {
            @Override
            public void onResponse(Call<AvailableOrdersResponse> call, Response<AvailableOrdersResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.setValue(Resource.success(response.body()));
                } else {
                    data.setValue(Resource.error("Failed to fetch orders", null));
                }
            }

            @Override
            public void onFailure(Call<AvailableOrdersResponse> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });

        return data;
    }

    /* ===============================
       ✅ ACCEPT ORDER
    =============================== */
    public LiveData<Resource<GenericResponse>> acceptOrder(String orderId) {
        MutableLiveData<Resource<GenericResponse>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));

        Map<String, String> body = new HashMap<>();
        body.put("orderId", orderId);

        apiService.acceptOrder(body).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.setValue(Resource.success(response.body()));
                } else {
                    data.setValue(Resource.error("Failed to accept order", null));
                }
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });

        return data;
    }

    /* ===============================
       🔄 UPDATE ORDER STATUS
    =============================== */
    public LiveData<Resource<GenericResponse>> updateOrderStatus(String orderId, String status) {
        MutableLiveData<Resource<GenericResponse>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));

        apiService.updateOrderStatus(
                new OrderStatusRequest(orderId, status)
        ).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.setValue(Resource.success(response.body()));
                } else {
                    data.setValue(Resource.error("Failed to update status", null));
                }
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });

        return data;
    }

    /* ===============================
       💰 EARNINGS
    =============================== */
    public LiveData<Resource<EarningsResponse>> getEarnings() {
        MutableLiveData<Resource<EarningsResponse>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));

        apiService.getEarnings().enqueue(new Callback<EarningsResponse>() {
            @Override
            public void onResponse(Call<EarningsResponse> call, Response<EarningsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.setValue(Resource.success(response.body()));
                } else {
                    data.setValue(Resource.error("Failed to fetch earnings", null));
                }
            }

            @Override
            public void onFailure(Call<EarningsResponse> call, Throwable t) {
                data.setValue(Resource.error(t.getMessage(), null));
            }
        });

        return data;
    }
}