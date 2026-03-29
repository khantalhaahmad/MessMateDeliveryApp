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

    // ================= STATE =================
    private final MutableLiveData<Boolean> isOnline = new MutableLiveData<>(false);
    private final MutableLiveData<Order> activeOrder = new MutableLiveData<>(null);

    private final MutableLiveData<Resource<AvailableOrdersResponse>> availableOrders = new MutableLiveData<>();
    private final MutableLiveData<Resource<EarningsResponse>> earnings = new MutableLiveData<>();

    public DashboardViewModel(DeliveryRepository repository) {
        this.repository = repository;
    }

    // ================= ONLINE =================
    public LiveData<Boolean> getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(boolean online) {
        isOnline.setValue(online);
    }

    public LiveData<Resource<GenericResponse>> toggleStatus(boolean online) {
        return repository.toggleStatus(online);
    }

    // ================= ACTIVE ORDER =================
    public LiveData<Order> getActiveOrder() {
        return activeOrder;
    }

    public void setActiveOrder(Order order) {
        activeOrder.setValue(order);
    }

    public void clearActiveOrder() {
        activeOrder.setValue(null);
    }

    // ================= EARNINGS =================
    public LiveData<Resource<EarningsResponse>> getEarnings() {
        return earnings;
    }

    public void fetchEarnings() {
        earnings.setValue(Resource.loading(null));

        repository.getEarnings().observeForever(response -> {
            earnings.postValue(response);
        });
    }

    // ================= AVAILABLE ORDERS =================
    public LiveData<Resource<AvailableOrdersResponse>> getAvailableOrders() {
        return availableOrders;
    }

    public void fetchAvailableOrders() {
        // 🔥 IMPORTANT: if already on active order → skip
        if (activeOrder.getValue() != null) return;

        availableOrders.setValue(Resource.loading(null));

        repository.getAvailableOrders().observeForever(response -> {
            availableOrders.postValue(response);
        });
    }

    // ================= ACCEPT ORDER =================
    public LiveData<Resource<GenericResponse>> acceptOrder(String orderId) {

        MutableLiveData<Resource<GenericResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        repository.acceptOrder(orderId).observeForever(response -> {

            if (response.status == Resource.Status.SUCCESS) {
                // 🔥 set active order after accept
                if (availableOrders.getValue() != null &&
                        availableOrders.getValue().data != null &&
                        availableOrders.getValue().data.getData() != null) {

                    for (Order o : availableOrders.getValue().data.getData()) {
                        if (o.getId().equals(orderId)) {
                            o.setDeliveryStatus("ACCEPTED");
                            activeOrder.postValue(o);
                            break;
                        }
                    }
                }
            }

            result.postValue(response);
        });

        return result;
    }

    // ================= UPDATE STATUS =================
    public LiveData<Resource<GenericResponse>> updateOrderStatus(String orderId, String status) {

        MutableLiveData<Resource<GenericResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        repository.updateOrderStatus(orderId, status).observeForever(response -> {

            if (response.status == Resource.Status.SUCCESS) {

                Order current = activeOrder.getValue();

                if (current != null && current.getId().equals(orderId)) {
                    current.setDeliveryStatus(status);

                    if ("DELIVERED".equals(status)) {
                        activeOrder.postValue(null);
                    } else {
                        activeOrder.postValue(current);
                    }
                }
            }

            result.postValue(response);
        });

        return result;
    }
}