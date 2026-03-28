package com.messmate.delivery.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.messmate.delivery.models.EarningsResponse;
import com.messmate.delivery.repository.DeliveryRepository;
import com.messmate.delivery.utils.Resource;

public class EarningsViewModel extends ViewModel {

    private final DeliveryRepository repository;

    public EarningsViewModel(DeliveryRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<EarningsResponse>> getEarnings() {
        return repository.getEarnings();
    }
}
