package com.messmate.delivery.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.messmate.delivery.models.LoginResponse;
import com.messmate.delivery.network.ApiService;
import com.messmate.delivery.utils.Resource;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    private final ApiService apiService;

    public AuthRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    /* ============================================================
       🔥 NEW LOGIN (FIREBASE OTP BASED)
    ============================================================ */

    public LiveData<Resource<LoginResponse>> firebaseLogin(String firebaseToken) {

        MutableLiveData<Resource<LoginResponse>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));

        apiService.firebaseLogin("Bearer " + firebaseToken)
                .enqueue(new Callback<LoginResponse>() {

                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {

                        if (response.isSuccessful() && response.body() != null) {

                            if (response.body().isSuccess()) {
                                data.setValue(Resource.success(response.body()));
                            } else {
                                data.setValue(Resource.error(response.body().getMessage(), null));
                            }

                        } else {
                            data.setValue(Resource.error("Login Failed: " + response.message(), null));
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        data.setValue(Resource.error(t.getMessage(), null));
                    }
                });

        return data;
    }
}