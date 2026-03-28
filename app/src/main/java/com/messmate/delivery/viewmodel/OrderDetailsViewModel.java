package com.messmate.delivery.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.messmate.delivery.models.GenericResponse;
import com.messmate.delivery.repository.DeliveryRepository;
import com.messmate.delivery.utils.Resource;

public class OrderDetailsViewModel extends ViewModel {

    private final DeliveryRepository repository;

    public OrderDetailsViewModel(DeliveryRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<GenericResponse>> updateOrderStatus(String orderId, String status) {
        return repository.updateOrderStatus(orderId, status);
    }
}
