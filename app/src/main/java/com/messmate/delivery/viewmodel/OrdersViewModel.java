package com.messmate.delivery.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.messmate.delivery.models.GenericResponse;
import com.messmate.delivery.models.Order;
import com.messmate.delivery.models.AvailableOrdersResponse;
import com.messmate.delivery.repository.DeliveryRepository;
import com.messmate.delivery.utils.Resource;

import java.util.List;

public class OrdersViewModel extends ViewModel {

    private final DeliveryRepository repository;

    public OrdersViewModel(DeliveryRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<AvailableOrdersResponse>> getAvailableOrders() {
        return repository.getAvailableOrders();
    }

    public LiveData<Resource<GenericResponse>> acceptOrder(String orderId) {
        return repository.acceptOrder(orderId);
    }
}
