package com.messmate.delivery.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.messmate.delivery.models.AvailableOrdersResponse;
import com.messmate.delivery.models.EarningsResponse;
import com.messmate.delivery.models.GenericResponse;
import com.messmate.delivery.models.Order;
import com.messmate.delivery.repository.DeliveryRepository;
import com.messmate.delivery.utils.Resource;

public class DashboardViewModel extends ViewModel {

    private final DeliveryRepository repository;
    
    private final MutableLiveData<Boolean> isOnline = new MutableLiveData<>(false);
    private final MutableLiveData<Order> activeOrder = new MutableLiveData<>(null);

    public DashboardViewModel(DeliveryRepository repository) {
        this.repository = repository;
    }

    public LiveData<Boolean> getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(boolean online) {
        isOnline.setValue(online);
    }
    
    public LiveData<Order> getActiveOrder() {
        return activeOrder;
    }
    
    public void setActiveOrder(Order order) {
        activeOrder.setValue(order);
    }

    public LiveData<Resource<GenericResponse>> toggleStatus(boolean online) {
        return repository.toggleStatus(online);
    }

    public LiveData<Resource<EarningsResponse>> getEarnings() {
        return repository.getEarnings();
    }
    
    public LiveData<Resource<AvailableOrdersResponse>> getAvailableOrders() {
        return repository.getAvailableOrders();
    }
    
    public LiveData<Resource<GenericResponse>> acceptOrder(String orderId) {
        return repository.acceptOrder(orderId);
    }
    
    public LiveData<Resource<GenericResponse>> updateOrderStatus(String orderId, String status) {
        return repository.updateOrderStatus(orderId, status);
    }
}
