package com.messmate.delivery.viewmodel;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.messmate.delivery.network.ApiClient;
import com.messmate.delivery.network.ApiService;
import com.messmate.delivery.repository.AuthRepository;
import com.messmate.delivery.repository.DeliveryRepository;

public class ViewModelFactory implements ViewModelProvider.Factory {

    private final AuthRepository authRepository;
    private final DeliveryRepository deliveryRepository;

    public ViewModelFactory(Context context) {
        ApiService apiService = ApiClient.getClient(context).create(ApiService.class);
        this.authRepository = new AuthRepository(apiService);
        this.deliveryRepository = new DeliveryRepository(apiService);
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AuthViewModel.class)) {
            return (T) new AuthViewModel(authRepository);
        } else if (modelClass.isAssignableFrom(DashboardViewModel.class)) {
            return (T) new DashboardViewModel(deliveryRepository);
        } else if (modelClass.isAssignableFrom(OrdersViewModel.class)) {
            return (T) new OrdersViewModel(deliveryRepository);
        } else if (modelClass.isAssignableFrom(OrderDetailsViewModel.class)) {
            return (T) new OrderDetailsViewModel(deliveryRepository);
        } else if (modelClass.isAssignableFrom(EarningsViewModel.class)) {
            return (T) new EarningsViewModel(deliveryRepository);
        } else if (modelClass.isAssignableFrom(ProfileViewModel.class)) {
            return (T) new ProfileViewModel();
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
